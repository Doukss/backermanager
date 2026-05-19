package com.immo.dispute.controller;

import com.immo.common.dto.ApiResponse;
import com.immo.dispute.dto.DisputeStatsResponse;
import com.immo.dispute.dto.SuperAdminDisputeResponse;
import com.immo.dispute.service.SuperAdminDisputeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/super-admin/disputes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Super Admin - Litiges", description = "Suivi global des litiges")
public class SuperAdminDisputeController {
    private final SuperAdminDisputeService service;

    @Operation(summary = "Lister les litiges")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SuperAdminDisputeResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(service.listDisputes()));
    }

    @Operation(summary = "Statistiques des litiges")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DisputeStatsResponse>> stats() {
        return ResponseEntity.ok(ApiResponse.ok(service.stats()));
    }
}
