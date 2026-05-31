package com.immo.agency.controller;

import com.immo.agency.dto.*;
import com.immo.agency.service.AgencyWorkspaceService;
import com.immo.common.dto.ApiResponse;
import com.immo.common.dto.GeneratedDocument;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agency")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN_AGENCE', 'AGENT', 'SECRETAIRE')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Agence", description = "Espace agence: biens, locataires, loyers, contrats et litiges")
public class AgencyWorkspaceController {
    private final AgencyWorkspaceService service;

    @Operation(summary = "Donnees du dashboard agence", description = "Retourne les indicateurs et listes synthetiques de l'agence courante.")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AgencyDashboardData>> dashboard(@TenantHeaderDoc @RequestHeader("X-Tenant-ID") String tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(service.dashboard(tenantId)));
    }

    @Operation(summary = "Lister les biens de l'agence")
    @GetMapping("/properties")
    public ResponseEntity<ApiResponse<List<AgencyPropertyResponse>>> listProperties(@TenantHeaderDoc @RequestHeader("X-Tenant-ID") String tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(service.listProperties(tenantId)));
    }

    @Operation(summary = "Consulter un bien")
    @GetMapping("/properties/{id}")
    public ResponseEntity<ApiResponse<AgencyPropertyResponse>> getProperty(@TenantHeaderDoc @RequestHeader("X-Tenant-ID") String tenantId, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(service.getProperty(tenantId, id)));
    }

    @Operation(summary = "Creer un bien")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Bien cree"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Donnees invalides")
    })
    @PostMapping("/properties")
    public ResponseEntity<ApiResponse<AgencyPropertyResponse>> createProperty(
            @TenantHeaderDoc @RequestHeader("X-Tenant-ID") String tenantId,
            @Valid @RequestBody AgencyPropertyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.createProperty(tenantId, request)));
    }

    @Operation(summary = "Modifier un bien")
    @PutMapping("/properties/{id}")
    public ResponseEntity<ApiResponse<AgencyPropertyResponse>> updateProperty(
            @TenantHeaderDoc @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody AgencyPropertyRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(service.updateProperty(tenantId, id, request)));
    }

    @Operation(summary = "Supprimer un bien")
    @DeleteMapping("/properties/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProperty(@TenantHeaderDoc @RequestHeader("X-Tenant-ID") String tenantId, @PathVariable UUID id) {
        service.deleteProperty(tenantId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "Lister les contrats")
    @GetMapping("/contracts")
    public ResponseEntity<ApiResponse<List<AgencyContractResponse>>> listContracts(@TenantHeaderDoc @RequestHeader("X-Tenant-ID") String tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(service.listContracts(tenantId)));
    }

    @Operation(summary = "Lister les locataires", description = "Retourne les contrats enrichis avec les informations locataire.")
    @GetMapping("/tenants")
    public ResponseEntity<ApiResponse<List<AgencyContractResponse>>> listTenants(@TenantHeaderDoc @RequestHeader("X-Tenant-ID") String tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(service.listTenants(tenantId)));
    }

    @Operation(summary = "Consulter un contrat")
    @GetMapping("/contracts/{id}")
    public ResponseEntity<ApiResponse<AgencyContractResponse>> getContract(@TenantHeaderDoc @RequestHeader("X-Tenant-ID") String tenantId, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(service.getContract(tenantId, id)));
    }

    @Operation(summary = "Creer un contrat")
    @PostMapping("/contracts")
    public ResponseEntity<ApiResponse<AgencyContractResponse>> createContract(
            @TenantHeaderDoc @RequestHeader("X-Tenant-ID") String tenantId,
            @Valid @RequestBody AgencyContractRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.createContract(tenantId, request)));
    }

    @Operation(summary = "Modifier un contrat")
    @PutMapping("/contracts/{id}")
    public ResponseEntity<ApiResponse<AgencyContractResponse>> updateContract(
            @TenantHeaderDoc @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody AgencyContractRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(service.updateContract(tenantId, id, request)));
    }

    @Operation(summary = "Supprimer un contrat")
    @DeleteMapping("/contracts/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteContract(@TenantHeaderDoc @RequestHeader("X-Tenant-ID") String tenantId, @PathVariable UUID id) {
        service.deleteContract(tenantId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "Telecharger le PDF d'un contrat")
    @GetMapping("/contracts/{id}/pdf")
    public ResponseEntity<byte[]> contractPdf(@TenantHeaderDoc @RequestHeader("X-Tenant-ID") String tenantId, @PathVariable UUID id) {
        return pdfResponse(service.contractPdf(tenantId, id));
    }

    @Operation(summary = "Lister les paiements")
    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<List<AgencyPaymentResponse>>> listPayments(@TenantHeaderDoc @RequestHeader("X-Tenant-ID") String tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(service.listPayments(tenantId)));
    }

    @Operation(summary = "Consulter un paiement")
    @GetMapping("/payments/{id}")
    public ResponseEntity<ApiResponse<AgencyPaymentResponse>> getPayment(@TenantHeaderDoc @RequestHeader("X-Tenant-ID") String tenantId, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(service.getPayment(tenantId, id)));
    }

    @Operation(summary = "Creer un paiement")
    @PostMapping("/payments")
    public ResponseEntity<ApiResponse<AgencyPaymentResponse>> createPayment(
            @TenantHeaderDoc @RequestHeader("X-Tenant-ID") String tenantId,
            @Valid @RequestBody AgencyPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.createPayment(tenantId, request)));
    }

    @Operation(summary = "Modifier un paiement")
    @PutMapping("/payments/{id}")
    public ResponseEntity<ApiResponse<AgencyPaymentResponse>> updatePayment(
            @TenantHeaderDoc @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody AgencyPaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(service.updatePayment(tenantId, id, request)));
    }

    @Operation(summary = "Marquer un paiement comme paye")
    @PatchMapping("/payments/{id}/paid")
    public ResponseEntity<ApiResponse<AgencyPaymentResponse>> markPaymentPaid(@TenantHeaderDoc @RequestHeader("X-Tenant-ID") String tenantId, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(service.markPaymentPaid(tenantId, id)));
    }

    @Operation(summary = "Supprimer un paiement")
    @DeleteMapping("/payments/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePayment(@TenantHeaderDoc @RequestHeader("X-Tenant-ID") String tenantId, @PathVariable UUID id) {
        service.deletePayment(tenantId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "Lister les quittances", description = "Genere la liste des quittances a partir des paiements payes.")
    @GetMapping("/receipts")
    public ResponseEntity<ApiResponse<List<AgencyReceiptResponse>>> listReceipts(@TenantHeaderDoc @RequestHeader("X-Tenant-ID") String tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(service.listReceipts(tenantId)));
    }

    @Operation(summary = "Telecharger le PDF d'une quittance")
    @GetMapping("/receipts/{paymentId}/pdf")
    public ResponseEntity<byte[]> receiptPdf(@TenantHeaderDoc @RequestHeader("X-Tenant-ID") String tenantId, @PathVariable UUID paymentId) {
        return pdfResponse(service.receiptPdf(tenantId, paymentId));
    }

    @Operation(summary = "Lister les litiges")
    @GetMapping("/disputes")
    public ResponseEntity<ApiResponse<List<AgencyDisputeResponse>>> listDisputes(@TenantHeaderDoc @RequestHeader("X-Tenant-ID") String tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(service.listDisputes(tenantId)));
    }

    @Operation(summary = "Consulter un litige")
    @GetMapping("/disputes/{id}")
    public ResponseEntity<ApiResponse<AgencyDisputeResponse>> getDispute(@TenantHeaderDoc @RequestHeader("X-Tenant-ID") String tenantId, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(service.getDispute(tenantId, id)));
    }

    @Operation(summary = "Creer un litige")
    @PostMapping("/disputes")
    public ResponseEntity<ApiResponse<AgencyDisputeResponse>> createDispute(
            @TenantHeaderDoc @RequestHeader("X-Tenant-ID") String tenantId,
            @Valid @RequestBody AgencyDisputeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.createDispute(tenantId, request)));
    }

    @Operation(summary = "Modifier un litige")
    @PutMapping("/disputes/{id}")
    public ResponseEntity<ApiResponse<AgencyDisputeResponse>> updateDispute(
            @TenantHeaderDoc @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable UUID id,
            @Valid @RequestBody AgencyDisputeRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(service.updateDispute(tenantId, id, request)));
    }

    @Operation(summary = "Resoudre un litige")
    @PatchMapping("/disputes/{id}/resolve")
    public ResponseEntity<ApiResponse<AgencyDisputeResponse>> resolveDispute(
            @TenantHeaderDoc @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable UUID id,
            @RequestBody(required = false) AgencyDisputeResolutionRequest request) {
        String resolution = request == null ? null : request.getResolution();
        return ResponseEntity.ok(ApiResponse.ok(service.resolveDispute(tenantId, id, resolution)));
    }

    @Operation(summary = "Supprimer un litige")
    @DeleteMapping("/disputes/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDispute(@TenantHeaderDoc @RequestHeader("X-Tenant-ID") String tenantId, @PathVariable UUID id) {
        service.deleteDispute(tenantId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Parameter(name = "X-Tenant-ID", description = "Identifiant tenant de l'agence courante", required = true)
    private @interface TenantHeaderDoc {
    }

    private ResponseEntity<byte[]> pdfResponse(GeneratedDocument document) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.filename() + "\"")
                .body(document.content());
    }
}
