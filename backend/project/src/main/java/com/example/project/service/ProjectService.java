package com.example.project.service;

import com.example.project.DTO.project.*;
import com.example.project.model.Project;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for managing projects.
 * Provides methods for retrieving, adding, and managing project data.
 */

public interface ProjectService {

    ProjectPageResponse<ProjectTable> pageProjectsForRole(
            String q, String buId, String status,
            LocalDate startFrom, LocalDate startTo,
            Integer marginMin, Integer marginMax,
            String sortField, String sortDir,
            int page, int size
    );

    ProjectSummaryResponse summaryForRole(
            String q, String buId, String status,
            LocalDate startFrom, LocalDate startTo,
            Integer marginMin, Integer marginMax
    );

    BigDecimal calculateAverageMargin(BigDecimal totalRevenue, BigDecimal totalCost);

    void addProject(AddProjectRequest request);

    void assignUserToProjectInternal(Project project, ProjectUserAssignmentRequest a, String role);

    List<ProjectUser> getAssignableUsers();
}