package com.example.project.repository;

import com.example.project.model.UserProjectMonthly;
import com.example.project.model.UserProjectMonthlyId;
import com.example.project.repository.projections.ProjectLaborCostAgg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for the UserProjectMonthly entity.
 * Extends JpaRepository to provide CRUD operations and query methods
 * for interacting with the "user_project_monthly" table in the database.
 */

@Repository
public interface UserProjectMonthlyRepository extends JpaRepository<UserProjectMonthly, UserProjectMonthlyId> {

    @Query("""
        select upm.id.idProject as idProject,
               coalesce(sum(upm.hoursWorked * upm.hourlyCost), 0) as laborCost
        from UserProjectMonthly upm
        where (:asOf is null or upm.id.date <= :asOf)
        group by upm.id.idProject
    """)
    List<ProjectLaborCostAgg> aggregateLaborCostByProject(LocalDate asOf);

}