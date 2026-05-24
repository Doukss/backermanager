package com.immo.agency.dto;

import com.immo.payment.entity.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgencyPaymentResponse {
    private UUID id;
    private String tenantId;
    private UUID contractId;
    private String tenantName;
    private String propertyTitle;
    private BigDecimal montant;
    private LocalDate dateEcheance;
    private LocalDate datePaiement;
    private PaymentStatus statut;
    private String reference;
}
