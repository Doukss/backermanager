package com.immo.dispute.entity;
import com.immo.dispute.entity.enums.DisputeStatus;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
@Entity @Table(name = "disputes")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Dispute {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false) private String tenantId;
    private UUID contractId;
    private String titre;
    private String description;
    @Enumerated(EnumType.STRING) private DisputeStatus statut;
    private String priorite;
    private String resolution;
}
