package com.immo.agency.dto;

import com.immo.property.entity.enums.PropertyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class AgencyPropertyRequest {
    @NotBlank
    private String titre;
    private String adresse;
    private String ville;
    private PropertyType type;
    @PositiveOrZero
    private BigDecimal loyerMensuel;
    @PositiveOrZero
    private BigDecimal surface;
    @PositiveOrZero
    private int nombrePieces;
    private boolean disponible = true;
}
