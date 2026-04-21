package com.example.project.controller;

import com.example.project.DTO.project.*;
import com.example.project.service.ProjectService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for managing project-related operations.
 * Provides endpoints for listing, summarizing, and managing projects.
 */

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @GetMapping
    public ProjectPageResponse<ProjectTable> listProjects(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String buId,
            @RequestParam(required = false) String status,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startTo,

            @RequestParam(required = false) Integer marginMin,
            @RequestParam(required = false) Integer marginMax,

            @RequestParam(defaultValue = "name") String sortField,
            @RequestParam(defaultValue = "asc") String sortDir,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.pageProjectsForRole(
                q, buId, status, startFrom, startTo,
                marginMin, marginMax, sortField, sortDir, page, size
        );
    }

    @GetMapping("/summary")
    public ProjectSummaryResponse summary(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String buId,
            @RequestParam(required = false) String status,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startTo,

            @RequestParam(required = false) Integer marginMin,
            @RequestParam(required = false) Integer marginMax
    ) {
        return service.summaryForRole(q, buId, status, startFrom, startTo, marginMin, marginMax);
    }

    @GetMapping("/totals")
    public ProjectSummaryResponse getProjectTotals(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String buId,
            @RequestParam(required = false) String status,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startTo,

            @RequestParam(required = false) Integer marginMin,
            @RequestParam(required = false) Integer marginMax
    ) {
        return service.summaryForRole(q, buId, status, startFrom, startTo, marginMin, marginMax);
    }

    @PostMapping("/create")
    public ResponseEntity<Void> addProject(@RequestBody AddProjectRequest request) {
        service.addProject(request);
        return ResponseEntity.status(201).build();
    }

    @GetMapping("/assignable-users")
    public List<ProjectUser> getAssignableUsers() {
        return service.getAssignableUsers();
    }
}