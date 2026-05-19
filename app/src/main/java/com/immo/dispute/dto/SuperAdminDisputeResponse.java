package com.immo.dispute.dto;

import com.immo.dispute.entity.enums.DisputeStatus;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuperAdminDisputeResponse {
    private UUID id;
    private String tenantId;
    private UUID contractId;
    private String titre;
    private String description;
    private DisputeStatus statut;
    private String priorite;
    private String resolution;
}
