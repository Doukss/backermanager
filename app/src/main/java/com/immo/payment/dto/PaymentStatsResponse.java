package com.immo.payment.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentStatsResponse {
    private long totalPayments;
    private long paidPayments;
    private long pendingPayments;
    private BigDecimal totalPaidAmount;
}
