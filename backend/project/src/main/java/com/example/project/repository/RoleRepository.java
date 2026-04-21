package com.example.project.repository;

import com.example.project.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for the Role entity.
 * Extends JpaRepository to provide CRUD operations and query methods
 * for interacting with the "role" table in the database.
 */

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

    Optional<Role> findById(Long id);

}