package com.example.project.service;

import com.example.project.DTO.project.*;
import com.example.project.exception.BusinessException;
import com.example.project.model.*;
import com.example.project.repository.*;
import com.example.project.repository.projections.ProjectLaborCostAgg;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for managing projects.
 * Provides methods for retrieving, adding, and managing project data.
 */

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserProjectMonthlyRepository userProjectMonthlyRepository;
    private final ExtraCostRepository extraCostRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final StatusRepository statusRepository;

    private final SimpMessagingTemplate messagingTemplate;

    public ProjectServiceImpl(ProjectRepository projectRepository, UserRepository userRepository, UserProjectMonthlyRepository userProjectMonthlyRepository, ExtraCostRepository extraCostRepository, BusinessUnitRepository businessUnitRepository, StatusRepository statusRepository, SimpMessagingTemplate messagingTemplate) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.userProjectMonthlyRepository = userProjectMonthlyRepository;
        this.extraCostRepository = extraCostRepository;
        this.businessUnitRepository = businessUnitRepository;
        this.statusRepository = statusRepository;
        this.messagingTemplate = messagingTemplate;
    }


    private String getCurrentUserRole() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email);
        if (user == null) throw new BusinessException("USER_NOT_FOUND");
        return user.getRole().getName();
    }

    private String getCurrentUserBusinessUnitId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email);
        if (user == null) throw new BusinessException("USER_NOT_FOUND");
        return user.getBusinessUnit().getIdBusinessUnit();
    }

    private BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private BigDecimal marginPct(BigDecimal rev, BigDecimal cost) {
        if (rev == null || rev.compareTo(BigDecimal.ZERO) == 0) return null;
        BigDecimal c = (cost == null) ? BigDecimal.ZERO : cost;
        return rev.subtract(c)
                .divide(rev, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isActiveOn(ExtraCost e, LocalDate day) {
        if (e == null) return false;
        LocalDate start = e.getStartDate();
        LocalDate end = e.getEndDate();
        if (start != null && start.isAfter(day)) return false;
        return end == null || !end.isBefore(day);
    }

    private Sort buildDbSort(String sortField, boolean desc) {
        String prop = switch (sortField) {
            case "name" -> "name";
            case "bu" -> "businessUnit.name";
            case "status" -> "status.name";
            case "startDate" -> "startDate";
            case "endDate" -> "endDate";
            case "currentRevenue" -> "estimatedRevenue";
            default -> null;
        };

        if (prop == null) return Sort.unsorted();

        return Sort.by(desc ? Sort.Direction.DESC : Sort.Direction.ASC, prop)
                .and(Sort.by(Sort.Direction.ASC, "name"));
    }

    private boolean isComputedSort(String sortField) {
        return "currentCost".equals(sortField) || "currentMargin".equals(sortField);
    }

    private Comparator<ProjectTable> buildComputedComparator(String sortField) {
        return switch (sortField) {
            case "currentCost" ->
                    Comparator.comparing(ProjectTable::getCurrentCost, Comparator.nullsLast(BigDecimal::compareTo));
            case "currentMargin" ->
                    Comparator.comparing(ProjectTable::getCurrentMargin, Comparator.nullsLast(BigDecimal::compareTo));
            default -> null;
        };
    }

    private List<ProjectTable> buildRowsForRole(
            String q, String buId, String status,
            LocalDate startFrom, LocalDate startTo,
            Integer marginMin, Integer marginMax,
            String sortField, String sortDir
    ) {
        String role = getCurrentUserRole();
        if ("MANAGER".equalsIgnoreCase(role)) {
            buId = getCurrentUserBusinessUnitId();
        }

        boolean desc = "desc".equalsIgnoreCase(sortDir);
        Sort dbSort = isComputedSort(sortField) ? Sort.unsorted() : buildDbSort(sortField, desc);

        List<Project> projects = projectRepository.search(
                q, buId, status, startFrom, startTo, dbSort
        );

        LocalDate today = LocalDate.now();

        Map<String, BigDecimal> laborCostByProject = userProjectMonthlyRepository
                .aggregateLaborCostByProject(today)
                .stream()
                .collect(Collectors.toMap(
                        ProjectLaborCostAgg::getIdProject,
                        a -> nvl(a.getLaborCost())
                ));

        Map<String, BigDecimal> extraCostByProject = extraCostRepository.findAll().stream()
                .filter(e -> isActiveOn(e, today))
                .collect(Collectors.groupingBy(
                        ExtraCost::getIdProject,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                e -> nvl(e.getCost()),
                                BigDecimal::add
                        )
                ));

        List<ProjectTable> rows = projects.stream()
                .map(p -> {
                    BigDecimal currentRevenue = p.getEstimatedRevenue();
                    BigDecimal labor = laborCostByProject.getOrDefault(p.getIdProject(), BigDecimal.ZERO);
                    BigDecimal extra = extraCostByProject.getOrDefault(p.getIdProject(), BigDecimal.ZERO);
                    BigDecimal currentCost = labor.add(extra);
                    BigDecimal currentMargin = marginPct(currentRevenue, currentCost);

                    return new ProjectTable(
                            p.getIdProject(),
                            p.getName(),
                            p.getStatus().getName(),
                            "MANAGER".equalsIgnoreCase(role) ? null : p.getBusinessUnit().getName(),
                            p.getStartDate(),
                            p.getEndDate(),
                            currentRevenue,
                            currentCost,
                            currentMargin
                    );
                })
                .toList();

        if (marginMin != null || marginMax != null) {
            BigDecimal min = (marginMin == null) ? null : BigDecimal.valueOf(marginMin);
            BigDecimal max = (marginMax == null) ? null : BigDecimal.valueOf(marginMax);

            rows = rows.stream()
                    .filter(r -> {
                        BigDecimal m = r.getCurrentMargin();
                        if (m == null) return false;
                        if (min != null && m.compareTo(min) < 0) return false;
                        return max == null || m.compareTo(max) <= 0;
                    })
                    .toList();
        }

        if (isComputedSort(sortField)) {
            Comparator<ProjectTable> cmp = buildComputedComparator(sortField);
            if (cmp != null) {
                rows = rows.stream()
                        .sorted(desc ? cmp.reversed() : cmp)
                        .toList();
            }
        }

        return rows;
    }

    @Override
    public ProjectPageResponse<ProjectTable> pageProjectsForRole(
            String q, String buId, String status,
            LocalDate startFrom, LocalDate startTo,
            Integer marginMin, Integer marginMax,
            String sortField, String sortDir,
            int page, int size
    ) {
        List<ProjectTable> all = buildRowsForRole(
                q, buId, status, startFrom, startTo, marginMin, marginMax, sortField, sortDir
        );

        ProjectSummaryResponse summary = summaryForRole(q, buId, status, startFrom, startTo, marginMin, marginMax);

        long total = all.size();

        int from = Math.max(0, page * size);
        int to = Math.min(all.size(), from + size);

        List<ProjectTable> items = (from >= to) ? List.of() : all.subList(from, to);

        return new ProjectPageResponse<>(
                items, total, page, size,
                summary.getTotalRevenue(),
                summary.getTotalCost(),
                summary.getMarginPct()
        );
    }

    @Override
    public ProjectSummaryResponse summaryForRole(
            String q, String buId, String status,
            LocalDate startFrom, LocalDate startTo,
            Integer marginMin, Integer marginMax
    ) {
        List<ProjectTable> all = buildRowsForRole(
                q, buId, status, startFrom, startTo, marginMin, marginMax, "name", "asc"
        );

        BigDecimal totalRevenue = all.stream()
                .map(ProjectTable::getCurrentRevenue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCost = all.stream()
                .map(ProjectTable::getCurrentCost)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal marginPct = marginPct(totalRevenue, totalCost);

        long atRiskCount = all.stream()
                .filter(p -> p.getCurrentMargin() != null && p.getCurrentMargin().compareTo(new BigDecimal("15")) < 0)
                .count();

        long borderlineCount = all.stream()
                .filter(p -> p.getCurrentMargin() != null &&
                        p.getCurrentMargin().compareTo(new BigDecimal("15")) >= 0 &&
                        p.getCurrentMargin().compareTo(new BigDecimal("25")) < 0)
                .count();

        return new ProjectSummaryResponse(all.size(), totalRevenue, totalCost, marginPct, atRiskCount, borderlineCount);
    }

    @Override
    public BigDecimal calculateAverageMargin(BigDecimal totalRevenue, BigDecimal totalCost) {
        if (totalRevenue == null || totalRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalRevenue.subtract(totalCost)
                .divide(totalRevenue, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }


    @Transactional
    @Override
    public void addProject(AddProjectRequest request) {
        Project project = new Project();
        project.setIdProject(UUID.randomUUID().toString());
        project.setName(request.getName());
        String role = getCurrentUserRole();
        if ("MANAGER".equalsIgnoreCase(role)) {
            String businessUnitId = getCurrentUserBusinessUnitId();
            project.setBusinessUnit(businessUnitRepository.findById(businessUnitId)
                    .orElseThrow(() -> new BusinessException("BUSINESS_UNIT_NOT_FOUND")));
        } else {
            // Use the BU provided in the request for non-managers
            project.setBusinessUnit(businessUnitRepository.findById(request.getBusinessUnitId())
                    .orElseThrow(() -> new BusinessException("BUSINESS_UNIT_NOT_FOUND")));
        }
        project.setStartDate(request.getStartDate());
        project.setPlannedEndDate(request.getPlannedEndDate());
        project.setEstimatedRevenue(request.getEstimatedRevenue());
        project.setEstimatedCost(request.getEstimatedCost());

        String status = calculateStatus(project);
        project.setStatus(statusRepository.findByName(status)
                .orElseThrow(() -> new BusinessException("STATUS_NOT_FOUND")));

        Project saved = projectRepository.save(project);

        if (request.getUsers() != null && !request.getUsers().isEmpty()) {
            for (ProjectUserAssignmentRequest a : request.getUsers()) {
                assignUserToProjectInternal(saved, a, role);
            }
        }

        final String destination = "/topic/bu/" + saved.getBusinessUnit().getIdBusinessUnit() + "/projects.created";
        final String globalDestination = "/topic/projects.created";
        final ProjectCreatedEvent event = new ProjectCreatedEvent(
                saved.getIdProject(),
                saved.getName(),
                saved.getBusinessUnit().getIdBusinessUnit(),
                saved.getStartDate(),
                saved.getPlannedEndDate()
        );

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    messagingTemplate.convertAndSend(destination, event);
                    messagingTemplate.convertAndSend(globalDestination, event);
                }
            });
        } else {
            messagingTemplate.convertAndSend(destination, event);
            messagingTemplate.convertAndSend(globalDestination, event);
        }

    }

    private String calculateStatus(Project project) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = project.getStartDate();

        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }

        if (startDate.isAfter(today)) {
            return "PLANNING";
        } else {
            return "IN_PROGRESS";
        }
    }

    public void assignUserToProjectInternal(Project project, ProjectUserAssignmentRequest a, String role) {
       if(a.getUserId() == null || a.getUserId().isBlank()) throw new BusinessException("USER_REQUIRED");

       User user = userRepository.findById(a.getUserId())
               .orElseThrow(() -> new BusinessException("USER_NOT_FOUND"));

       String userBu = user.getBusinessUnit().getIdBusinessUnit();
       String projectBu = project.getBusinessUnit().getIdBusinessUnit();

       if("MANAGER".equalsIgnoreCase(role) && !userBu.equals(projectBu)) {
           throw new BusinessException("USER_BUSINESS_UNIT_MISMATCH");
       }

        UserProjectMonthlyId id = new UserProjectMonthlyId();
        id.setIdProject(project.getIdProject());
        id.setIdUser(user.getIdUser());
        id.setDate(project.getStartDate());

        UserProjectMonthly userProjectMonthly = new UserProjectMonthly();
        userProjectMonthly.setId(id);
        userProjectMonthly.setHourlyCost(a.getHourlyCost());
        userProjectMonthly.setAllocationRate(null);

        userProjectMonthlyRepository.save(userProjectMonthly);
    }

    public List<ProjectUser> getAssignableUsers() {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail);
        if (currentUser == null) {
            throw new BusinessException("USER_NOT_FOUND");
        }

        String role = currentUser.getRole().getName();
        List<User> users;

        if ("ADMIN".equalsIgnoreCase(role)) {
            // Admin can see all users except themselves
            users = userRepository.findAll().stream()
                    .filter(user -> !user.getEmail().equals(currentUserEmail))
                    .toList();
        } else if ("MANAGER".equalsIgnoreCase(role)) {
            // Manager can see users in their BU except themselves
            String currentUserBuId = currentUser.getBusinessUnit().getIdBusinessUnit();
            users = userRepository.findByBusinessUnitId(currentUserBuId).stream()
                    .filter(user -> !user.getEmail().equals(currentUserEmail))
                    .toList();
        } else {
            throw new BusinessException("ROLE_NOT_ALLOWED");
        }

        // Map users to ProjectUser DTO
        return users.stream()
                .map(user -> {
                    ProjectUser projectUser = new ProjectUser();
                    projectUser.setUserId(user.getIdUser());
                    projectUser.setName(user.getFirstName() + " " + user.getLastName());
                    projectUser.setBusinessUnitId(user.getBusinessUnit().getIdBusinessUnit());
                    return projectUser;
                })
                .toList();
    }
}