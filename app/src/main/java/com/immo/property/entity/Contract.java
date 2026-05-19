package com.immo.property.entity;
import com.immo.property.entity.enums.ContractStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
@Entity @Table(name = "contracts")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Contract {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false) private String tenantId;
    private UUID propertyId;
    private String locataireNom;
    private String locataireEmail;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private BigDecimal loyerMensuel;
    private BigDecimal depot;
    @Enumerated(EnumType.STRING) private ContractStatus statut;
}
