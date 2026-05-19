package com.immo.agency.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardResponse {
    private long agencies;
    private long activeAgencies;
    private long suspendedAgencies;
    private long activeSubscriptions;
    private BigDecimal monthlyRevenue;
}
