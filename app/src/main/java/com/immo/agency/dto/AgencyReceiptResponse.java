package com.immo.agency.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgencyReceiptResponse {
    private UUID paymentId;
    private UUID contractId;
    private String tenantName;
    private String propertyTitle;
    private String period;
    private BigDecimal amount;
    private LocalDate issuedAt;
    private String reference;
}
