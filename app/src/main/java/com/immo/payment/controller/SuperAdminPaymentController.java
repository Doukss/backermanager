package com.immo.payment.controller;

import com.immo.common.dto.ApiResponse;
import com.immo.payment.dto.PaymentStatsResponse;
import com.immo.payment.dto.SuperAdminPaymentResponse;
import com.immo.payment.service.SuperAdminPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/super-admin/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Super Admin - Paiements", description = "Suivi global des paiements")
public class SuperAdminPaymentController {
    private final SuperAdminPaymentService service;

    @Operation(summary = "Lister les paiements")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SuperAdminPaymentResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(service.listPayments()));
    }

    @Operation(summary = "Statistiques des paiements")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<PaymentStatsResponse>> stats() {
        return ResponseEntity.ok(ApiResponse.ok(service.stats()));
    }
}
