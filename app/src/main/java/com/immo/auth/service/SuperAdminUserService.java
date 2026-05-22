package com.immo.auth.service;

import com.immo.auth.dto.SuperAdminUserRequest;
import com.immo.auth.dto.SuperAdminUserResponse;
import com.immo.auth.entity.Role;
import com.immo.auth.entity.User;
import com.immo.auth.repository.UserRepository;
import com.immo.common.exception.ResourceNotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SuperAdminUserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<SuperAdminUserResponse> listPlatformUsers() {
        return userRepository.findByRoleIn(List.of(Role.ADMIN_AGENCE, Role.AGENT, Role.SECRETAIRE, Role.LOCATAIRE))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SuperAdminUserResponse create(SuperAdminUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("Cet email existe deja");
        }
        OffsetDateTime now = OffsetDateTime.now();
        User user = User.builder()
                .tenantId(request.getTenantId())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(request.getRole())
                .password(passwordEncoder.encode(request.getPassword()))
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return toResponse(userRepository.save(user));
    }

    public SuperAdminUserResponse setActive(UUID id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        user.setActive(active);
        user.setUpdatedAt(OffsetDateTime.now());
        return toResponse(userRepository.save(user));
    }

    private SuperAdminUserResponse toResponse(User user) {
        return SuperAdminUserResponse.builder()
                .id(user.getId())
                .tenantId(user.getTenantId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
