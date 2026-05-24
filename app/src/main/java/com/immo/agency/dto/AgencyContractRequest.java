package com.immo.agency.dto;

import com.immo.property.entity.enums.ContractStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
public class AgencyContractRequest {
    @NotNull
    private UUID propertyId;
    @NotBlank
    private String locataireNom;
    @Email
    private String locataireEmail;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    @PositiveOrZero
    private BigDecimal loyerMensuel;
    @PositiveOrZero
    private BigDecimal depot;
    private ContractStatus statut = ContractStatus.BROUILLON;
}
