package com.immo.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TenantContactRequest {
    @NotBlank
    private String subject;
    @NotBlank
    private String message;
    private String priority = "normal";
}
