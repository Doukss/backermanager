package com.immo.payment.entity;
import com.immo.payment.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
@Entity @Table(name = "payments")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false) private String tenantId;
    private UUID contractId;
    private BigDecimal montant;
    private LocalDate dateEcheance;
    private LocalDate datePaiement;
    @Enumerated(EnumType.STRING) private PaymentStatus statut;
    private String reference;
}
