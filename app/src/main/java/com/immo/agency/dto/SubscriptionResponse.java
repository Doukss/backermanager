package com.immo.agency.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriptionResponse {
    private UUID id;
    private String tenantId;
    private String agencyName;
    private String plan;
    private String status;
    private BigDecimal monthlyPrice;
    private LocalDate startedAt;
    private LocalDate nextBillingAt;
}
