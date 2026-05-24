package com.immo.agency.dto;

import com.immo.payment.entity.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
public class AgencyPaymentRequest {
    @NotNull
    private UUID contractId;
    @NotNull
    @Positive
    private BigDecimal montant;
    @NotNull
    private LocalDate dateEcheance;
    private LocalDate datePaiement;
    private PaymentStatus statut = PaymentStatus.EN_ATTENTE;
    private String reference;
}
