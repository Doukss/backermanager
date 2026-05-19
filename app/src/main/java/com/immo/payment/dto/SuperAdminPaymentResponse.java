package com.immo.payment.dto;

import com.immo.payment.entity.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuperAdminPaymentResponse {
    private UUID id;
    private String tenantId;
    private UUID contractId;
    private BigDecimal montant;
    private LocalDate dateEcheance;
    private LocalDate datePaiement;
    private PaymentStatus statut;
    private String reference;
}
