package com.immo.auth.dto;

import com.immo.auth.entity.Role;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuperAdminUserResponse {
    private UUID id;
    private String tenantId;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private boolean active;
    private OffsetDateTime createdAt;
}
