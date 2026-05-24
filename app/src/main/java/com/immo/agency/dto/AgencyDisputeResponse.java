package com.immo.agency.dto;

import com.immo.dispute.entity.enums.DisputeStatus;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgencyDisputeResponse {
    private UUID id;
    private String tenantId;
    private UUID contractId;
    private String tenantName;
    private String titre;
    private String description;
    private DisputeStatus statut;
    private String priorite;
    private String resolution;
}
