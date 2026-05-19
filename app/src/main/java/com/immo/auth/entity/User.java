package com.immo.auth.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;
@Entity @Table(name = "users")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String tenantId;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    private String fullName;
    private String phone;
    @Enumerated(EnumType.STRING)
    private Role role;
    private boolean active = true;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
