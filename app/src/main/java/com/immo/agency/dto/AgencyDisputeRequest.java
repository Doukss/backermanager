package com.immo.agency.dto;

import com.immo.dispute.entity.enums.DisputeStatus;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Data;

@Data
public class AgencyDisputeRequest {
    private UUID contractId;
    @NotBlank
    private String titre;
    private String description;
    private DisputeStatus statut = DisputeStatus.OUVERT;
    private String priorite = "NORMALE";
    private String resolution;
}
