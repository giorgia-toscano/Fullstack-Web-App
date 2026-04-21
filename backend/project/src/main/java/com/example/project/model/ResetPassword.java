package com.example.project.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity class representing a Password Reset.
 * This class maps to the "password_reset" table in the database and
 * contains fields for storing details about password reset requests.
 **/

@Getter @Setter
@Entity
@Table(name = "password_reset")
public class ResetPassword {

    @Id
    @Column(name = "id_password_reset", nullable = false)
    private String idPasswordReset; // UUID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @Column(name = "otp", nullable = false)
    private String otp;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "attempts", nullable = false)
    private Integer attempts = 0;

    @Column(name = "used", nullable = false)
    private Boolean used = false;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name="reset_token", nullable = false)
    private String resetToken;

    @Column(name="reset_token_expires_at", nullable = false)
    private LocalDateTime resetTokenExpiresAt;
}