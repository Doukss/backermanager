package com.immo.agency.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;
@Entity @Table(name = "agencies")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Agency {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, unique = true) private String tenantId;
    @Column(nullable = false) private String nom;
    private String adresse;
    private String telephone;
    private String email;
    private String plan; // STARTER, PRO, ENTERPRISE
    private boolean active = true;
    private OffsetDateTime createdAt;
}
