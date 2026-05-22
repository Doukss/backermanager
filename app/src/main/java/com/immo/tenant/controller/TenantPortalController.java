package com.immo.tenant.controller;

import com.immo.agency.dto.AgencyDashboardData;
import com.immo.common.dto.ApiResponse;
import com.immo.tenant.dto.TenantContactRequest;
import com.immo.tenant.dto.TenantPasswordRequest;
import com.immo.tenant.dto.TenantProfileRequest;
import com.immo.tenant.service.TenantPortalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LOCATAIRE')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Locataire", description = "Espace locataire: dashboard, logement, paiements, documents et demandes agence")
public class TenantPortalController {
    private final TenantPortalService service;

    @Operation(summary = "Donnees du dashboard locataire")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AgencyDashboardData>> dashboard(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(ApiResponse.ok(service.dashboard(userId)));
    }

    @Operation(summary = "Modifier le profil locataire")
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<AgencyDashboardData.AgencyTenantItem>> updateProfile(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody TenantProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(service.updateProfile(userId, request)));
    }

    @Operation(summary = "Changer le mot de passe locataire")
    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody TenantPasswordRequest request) {
        service.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "Contacter l'agence", description = "Cree un litige/demande rattache au contrat du locataire.")
    @PostMapping("/contact")
    public ResponseEntity<ApiResponse<AgencyDashboardData.AgencyDisputeItem>> contactAgency(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody TenantContactRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(service.contactAgency(userId, request)));
    }

    @Operation(summary = "Payer un loyer", description = "Marque le paiement du locataire comme paye.")
    @PatchMapping("/payments/{id}/pay")
    public ResponseEntity<ApiResponse<AgencyDashboardData.AgencyPaymentItem>> pay(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(service.pay(userId, id)));
    }
}
