package com.example.project.service;

import com.example.project.DTO.user.*;
import com.example.project.exception.BusinessException;
import com.example.project.model.User;
import com.example.project.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service implementation for managing user-related operations.
 * Provides methods for user authentication, profile management, and updating user details.
 */

@Service
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        String roleName = (user.getRole() != null)
                ? "ROLE_" + user.getRole().getName()
                : "ROLE_USER";

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(roleName)
                .disabled(!user.getEnabled())
                .build();
    }

    @Override
    @Transactional
    public void setEnabled(String idUser, boolean enabled) {
        userRepository.updateEnabledStatus(idUser, enabled);
    }

    @Override
    @Transactional
    public UserProfile getMyProfile(String email) {
        User user = userRepository.findByEmail(email);
        if(user == null){
            throw new BusinessException("USER_NOT_FOUND");
        }
        return new UserProfile(
                user.getEmail(),
                user.getRole() != null ? user.getRole().getName() : null,
                user.getBusinessUnit() != null ? user.getBusinessUnit().getIdBusinessUnit() : null,
                user.getFirstName(),
                user.getLastName(),
                user.getFiscalCode(),
                user.getIdCardNumber(),
                user.getBirthDay(),
                user.getBirthPlace(),
                user.getAddress(),
                user.getCity(),
                user.getPhoneNumber(),
                user.getIban(),
                user.getIbanHolder()
        );
    }

    private User getUserOrThrow(String email) {
        User u = userRepository.findByEmail(email);
        if (u == null) throw new BusinessException("USER_NOT_FOUND");
        return u;
    }

    @Override
    @Transactional
    public void updatePersonal(String email, UpdatePersonalRequest dto) {
        User u = getUserOrThrow(email);

        if (dto.getFirstName() != null) u.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) u.setLastName(dto.getLastName());
        if (dto.getFiscalCode() != null) u.setFiscalCode(dto.getFiscalCode());
        if (dto.getIdCardNumber() != null) u.setIdCardNumber(dto.getIdCardNumber());
        if (dto.getBirthPlace() != null) u.setBirthPlace(dto.getBirthPlace());
        if (dto.getBirthDay() != null) u.setBirthDay(dto.getBirthDay());

        userRepository.save(u);
    }

    @Override
    @Transactional
    public void updateResidence(String email, UpdateResidenceRequest dto) {
        User u = getUserOrThrow(email);

        if (dto.getAddress() != null) u.setAddress(dto.getAddress());
        if (dto.getCity() != null) u.setCity(dto.getCity());

        userRepository.save(u);
    }

    @Override
    @Transactional
    public void updateContacts(String email, UpdateContactsRequest dto) {
        User u = getUserOrThrow(email);

        if (dto.getPhoneNumber() != null) u.setPhoneNumber(dto.getPhoneNumber());

        userRepository.save(u);
    }

    @Override
    @Transactional
    public void updateBank(String email, UpdateBankRequest dto) {
        User u = getUserOrThrow(email);

        if (dto.getIban() != null) u.setIban(dto.getIban());
        if (dto.getIbanHolder() != null) u.setIbanHolder(dto.getIbanHolder());

        userRepository.save(u);
    }

    @Override
    @Transactional
    public void changePassword(String email, UpdatePasswordRequest request) {
        if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank())
            throw new BusinessException("PASSWORD_CURRENT_REQUIRED");

        if (request.getNewPassword() == null || request.getNewPassword().isBlank())
            throw new BusinessException("PASSWORD_NEW_REQUIRED");

        if (request.getConfirmNewPassword() == null || request.getConfirmNewPassword().isBlank())
            throw new BusinessException("PASSWORD_CONFIRM_REQUIRED");

        if (!request.getNewPassword().equals(request.getConfirmNewPassword()))
            throw new BusinessException("PASSWORD_MISMATCH");

        User user = userRepository.findByEmail(email);
        if (user == null) throw new BusinessException("USER_NOT_FOUND");

        boolean matches = passwordEncoder.matches(request.getCurrentPassword(), user.getPassword());
        if (!matches) throw new BusinessException("PASSWORD_CURRENT_INVALID");

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword()))
            throw new BusinessException("PASSWORD_SAME_AS_OLD");

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}