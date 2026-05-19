package com.immo.agency.controller;

import com.immo.agency.dto.*;
import com.immo.agency.entity.PlatformNotification;
import com.immo.agency.service.SuperAdminAgencyService;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/super-admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Super Admin - Agences", description = "Pilotage global des agences, abonnements et notifications plateforme")
public class SuperAdminAgencyController {
    private final SuperAdminAgencyService service;

    @Operation(summary = "Tableau de bord super-admin")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> dashboard() {
        return ResponseEntity.ok(ApiResponse.ok(service.dashboard()));
    }

    @Operation(summary = "Lister les agences")
    @GetMapping("/agencies")
    public ResponseEntity<ApiResponse<List<AgencyResponse>>> agencies() {
        return ResponseEntity.ok(ApiResponse.ok(service.listAgencies()));
    }

    @Operation(summary = "Creer une agence")
    @PostMapping("/agencies")
    public ResponseEntity<ApiResponse<AgencyResponse>> createAgency(
            @Valid @RequestBody AgencyRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(service.createAgency(request)));
    }

    @Operation(summary = "Activer une agence")
    @PatchMapping("/agencies/{id}/activate")
    public ResponseEntity<ApiResponse<AgencyResponse>> activateAgency(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(service.setAgencyActive(id, true)));
    }

    @Operation(summary = "Suspendre une agence")
    @PatchMapping("/agencies/{id}/suspend")
    public ResponseEntity<ApiResponse<AgencyResponse>> suspendAgency(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(service.setAgencyActive(id, false)));
    }

    @Operation(summary = "Lister les abonnements")
    @GetMapping("/subscriptions")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> subscriptions() {
        return ResponseEntity.ok(ApiResponse.ok(service.listSubscriptions()));
    }

    @Operation(summary = "Lister les notifications plateforme")
    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<PlatformNotification>>> notifications() {
        return ResponseEntity.ok(ApiResponse.ok(service.listNotifications()));
    }

    @Operation(summary = "Marquer une notification comme lue")
    @PatchMapping("/notifications/{id}/read")
    public ResponseEntity<ApiResponse<PlatformNotification>> readNotification(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(service.markNotificationRead(id)));
    }
}
