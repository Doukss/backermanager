package com.immo.agency.controller;

import com.immo.agency.dto.AgencyDashboardData;
import com.immo.agency.service.AgencyWorkspaceService;
import com.immo.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agency")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN_AGENCE', 'AGENT', 'SECRETAIRE')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Agence", description = "Espace agence: biens, locataires, loyers, contrats et litiges")
public class AgencyWorkspaceController {
    private final AgencyWorkspaceService service;

    @Operation(summary = "Donnees du dashboard agence")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AgencyDashboardData>> dashboard(@RequestHeader("X-Tenant-ID") String tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(service.dashboard(tenantId)));
    }
}
