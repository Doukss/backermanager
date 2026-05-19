package com.immo.property.entity;
import com.immo.property.entity.enums.PropertyType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;
@Entity @Table(name = "properties")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Property {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false) private String tenantId;
    @Column(nullable = false) private String titre;
    private String adresse;
    private String ville;
    @Enumerated(EnumType.STRING) private PropertyType type;
    private BigDecimal loyerMensuel;
    private BigDecimal surface;
    private int nombrePieces;
    private boolean disponible = true;
}
