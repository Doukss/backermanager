package com.immo.auth.service;

import com.immo.agency.entity.Agency;
import com.immo.agency.entity.Subscription;
import com.immo.agency.repository.AgencyRepository;
import com.immo.agency.repository.SubscriptionRepository;
import com.immo.auth.dto.LoginRequest;
import com.immo.auth.dto.LoginResponse;
import com.immo.auth.dto.RegisterAgencyRequest;
import com.immo.auth.entity.Role;
import com.immo.auth.entity.User;
import com.immo.auth.repository.UserRepository;
import com.immo.common.exception.UnauthorizedException;
import com.immo.common.util.JwtUtil;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final AgencyRepository agencyRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    @Transactional
    public LoginResponse registerAgency(RegisterAgencyRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("Cet email existe deja");
        }

        OffsetDateTime now = OffsetDateTime.now();
        String tenantId = uniqueTenantId(request.getAgencyName());

        Agency agency = Agency.builder()
                .tenantId(tenantId)
                .nom(request.getAgencyName())
                .telephone(request.getPhone())
                .email(request.getEmail())
                .plan("PRO")
                .active(true)
                .createdAt(now)
                .build();
        agencyRepository.save(agency);

        subscriptionRepository.save(Subscription.builder()
                .tenantId(tenantId)
                .plan("PRO")
                .status("ACTIVE")
                .monthlyPrice(BigDecimal.valueOf(15000))
                .startedAt(LocalDate.now())
                .nextBillingAt(LocalDate.now().plusMonths(1))
                .build());

        User user = User.builder()
                .tenantId(tenantId)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getAgencyName())
                .phone(request.getPhone())
                .role(Role.ADMIN_AGENCE)
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
        User saved = userRepository.save(user);

        return LoginResponse.builder()
                .accessToken(jwtUtil.generate(saved.getId().toString(), saved.getTenantId(), saved.getRole().name()))
                .tokenType("Bearer")
                .expiresIn(expirationMs / 1000)
                .build();
    }

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

    private String uniqueTenantId(String agencyName) {
        String normalized = Normalizer.normalize(agencyName == null ? "agence" : agencyName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        String base = normalized.isBlank() ? "agence" : normalized;
        String candidate = base;
        int suffix = 2;
        while (agencyRepository.existsByTenantId(candidate)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }
}
