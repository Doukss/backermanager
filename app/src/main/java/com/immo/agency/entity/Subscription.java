package com.immo.agency.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "subscriptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String tenantId;
    @Column(nullable = false)
    private String plan;
    @Column(nullable = false)
    private String status;
    @Column(nullable = false)
    private BigDecimal monthlyPrice;
    private LocalDate startedAt;
    private LocalDate nextBillingAt;
}
