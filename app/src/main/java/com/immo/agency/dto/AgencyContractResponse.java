package com.immo.agency.dto;

import com.immo.property.entity.enums.ContractStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgencyContractResponse {
    private UUID id;
    private String tenantId;
    private UUID propertyId;
    private String propertyTitle;
    private String locataireNom;
    private String locataireEmail;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private BigDecimal loyerMensuel;
    private BigDecimal depot;
    private ContractStatus statut;
}
