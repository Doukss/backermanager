package com.immo.dispute.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DisputeStatsResponse {
    private long totalDisputes;
    private long openDisputes;
    private long inProgressDisputes;
    private long resolvedDisputes;
}
