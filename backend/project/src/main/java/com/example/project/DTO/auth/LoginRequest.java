package com.example.project.DTO.auth;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * Data Transfer Object (DTO) for handling login requests.
 * This class contains the necessary fields for user authentication.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 5926468583005150707L;
    private String email;
    private String password;
    private Boolean rememberMe;
}