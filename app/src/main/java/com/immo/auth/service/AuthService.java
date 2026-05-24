package com.immo.auth.service;

import com.immo.auth.dto.LoginRequest;
import com.immo.auth.dto.LoginResponse;
import com.immo.auth.entity.User;
import com.immo.auth.repository.UserRepository;
import com.immo.common.exception.UnauthorizedException;
import com.immo.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Identifiants invalides"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Identifiants invalides");
        }
        if (!user.isActive()) {
            throw new UnauthorizedException("Votre compte est suspendu. Contactez le super-admin.");
        }

        return LoginResponse.builder()
                .accessToken(jwtUtil.generate(user.getId().toString(), user.getTenantId(), user.getRole().name()))
                .tokenType("Bearer")
                .expiresIn(expirationMs / 1000)
                .build();
    }

    public LoginResponse refresh(String authorization) {
        String token = authorization.replace("Bearer ", "");
        var claims = jwtUtil.parse(token);
        User user = userRepository.findById(java.util.UUID.fromString(claims.getSubject()))
                .orElseThrow(() -> new UnauthorizedException("Utilisateur introuvable"));
        if (!user.isActive()) {
            throw new UnauthorizedException("Votre compte est suspendu. Contactez le super-admin.");
        }
        return LoginResponse.builder()
                .accessToken(jwtUtil.generate(claims.getSubject(), claims.get("tenantId", String.class), claims.get("role", String.class)))
                .tokenType("Bearer")
                .expiresIn(expirationMs / 1000)
                .build();
    }
}
