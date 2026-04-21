package com.example.project.controller;

import com.example.project.DTO.user.*;
import com.example.project.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing user-related operations.
 * Provides endpoints for retrieving and updating user profile information.
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfile> getProfile(Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        String email = authentication.getName();
        UserProfile profile = userService.getMyProfile(email);

        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile/personal")
    public ResponseEntity<Void> updatePersonal(@Valid @RequestBody UpdatePersonalRequest dto,
                                                 Authentication auth) {
        String email = auth.getName();
        userService.updatePersonal(email, dto);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/profile/residence")
    public ResponseEntity<Void> updateResidence(@Valid @RequestBody UpdateResidenceRequest dto,
                                               Authentication auth) {
        String email = auth.getName();
        userService.updateResidence(email, dto);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/profile/contacts")
    public ResponseEntity<Void> updateContacts(@Valid @RequestBody UpdateContactsRequest dto,
                                                 Authentication auth) {
        String email = auth.getName();
        userService.updateContacts(email, dto);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/profile/bank")
    public ResponseEntity<Void> updateBank(@Valid @RequestBody UpdateBankRequest dto,
                                             Authentication auth) {
        String email = auth.getName();
        userService.updateBank(email, dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/profile/change-password")
    public ResponseEntity<Void> changePassword(Authentication authentication,
                                               @RequestBody UpdatePasswordRequest request) {
        String email = authentication.getName();
        userService.changePassword(email, request);
        return ResponseEntity.ok().build();
    }

}
