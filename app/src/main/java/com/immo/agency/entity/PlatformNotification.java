package com.immo.agency.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "platform_notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String message;
    private String type;
    private String priority;
    private boolean read;
    private String target;
    private OffsetDateTime createdAt;
}
