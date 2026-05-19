package com.immo.auth.dto;

import com.immo.auth.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SuperAdminUserRequest {
    @NotBlank
    private String tenantId;
    @NotBlank
    private String fullName;
    @Email
    @NotBlank
    private String email;
    private String phone;
    @NotNull
    private Role role;
    @NotBlank
    private String password;
}
