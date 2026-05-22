package com.immo.auth.controller;
import com.immo.auth.dto.LoginRequest;
import com.immo.auth.dto.LoginResponse;
import com.immo.auth.dto.RegisterAgencyRequest;
import com.immo.auth.service.AuthService;
import com.immo.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Connexion et renouvellement du JWT")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Connexion")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(req)));
    }

    @Operation(summary = "Inscription agence", description = "Cree une agence active, un compte ADMIN_AGENCE et un abonnement PRO par defaut.")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody RegisterAgencyRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(authService.registerAgency(req)));
    }

    @Operation(summary = "Renouveler le token")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(ApiResponse.ok(authService.refresh(token)));
    }

    @Operation(summary = "Utilisateur courant")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<String>> me(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(ApiResponse.ok(userId));
    }
}
