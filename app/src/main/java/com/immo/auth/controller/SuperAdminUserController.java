package com.immo.auth.controller;

import com.immo.auth.dto.SuperAdminUserRequest;
import com.immo.auth.dto.SuperAdminUserResponse;
import com.immo.auth.service.SuperAdminUserService;
import com.immo.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/super-admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Super Admin - Utilisateurs", description = "Gestion des utilisateurs des agences")
public class SuperAdminUserController {
    private final SuperAdminUserService service;

    @Operation(summary = "Lister les utilisateurs agences")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SuperAdminUserResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(service.listPlatformUsers()));
    }

    @Operation(summary = "Creer un utilisateur agence")
    @PostMapping
    public ResponseEntity<ApiResponse<SuperAdminUserResponse>> create(
            @Valid @RequestBody SuperAdminUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(service.create(request)));
    }

    @Operation(summary = "Activer un utilisateur")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<SuperAdminUserResponse>> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(service.setActive(id, true)));
    }

    @Operation(summary = "Suspendre un utilisateur")
    @PatchMapping("/{id}/suspend")
    public ResponseEntity<ApiResponse<SuperAdminUserResponse>> suspend(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(service.setActive(id, false)));
    }
}
