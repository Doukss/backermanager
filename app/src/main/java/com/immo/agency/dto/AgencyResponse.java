package com.immo.agency.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgencyResponse {
    private UUID id;
    private String tenantId;
    private String nom;
    private String adresse;
    private String telephone;
    private String email;
    private String plan;
    private boolean active;
    private OffsetDateTime createdAt;
}
