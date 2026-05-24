package com.immo.agency.dto;

import com.immo.property.entity.enums.PropertyType;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgencyPropertyResponse {
    private UUID id;
    private String tenantId;
    private String titre;
    private String adresse;
    private String ville;
    private PropertyType type;
    private BigDecimal loyerMensuel;
    private BigDecimal surface;
    private int nombrePieces;
    private boolean disponible;
}
