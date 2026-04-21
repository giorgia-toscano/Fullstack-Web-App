package com.example.project.service;

import com.example.project.DTO.user.*;
import com.example.project.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Service interface for managing user-related operations.
 * Extends the UserDetailsService to provide user authentication and additional user management functionalities.
 */

public interface UserService extends UserDetailsService {
    UserDetails loadUserByUsername(String username);

    User saveUser(User user);
    boolean existsByEmail(String email);

    void setEnabled(String idUser, boolean enabled);

    UserProfile getMyProfile(String email);

    void updatePersonal(String email, UpdatePersonalRequest dto);
    void updateResidence(String email, UpdateResidenceRequest dto);
    void updateContacts(String email, UpdateContactsRequest dto);
    void updateBank(String email, UpdateBankRequest dto);

    void changePassword(String email, UpdatePasswordRequest request);

}