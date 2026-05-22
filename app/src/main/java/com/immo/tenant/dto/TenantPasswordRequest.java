package com.immo.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TenantPasswordRequest {
    @NotBlank
    private String currentPassword;
    @NotBlank
    @Size(min = 8)
    private String newPassword;
}
