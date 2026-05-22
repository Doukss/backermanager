package com.immo.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterAgencyRequest {
    @NotBlank
    private String agencyName;
    @NotBlank
    @Email
    private String email;
    private String phone;
    @NotBlank
    @Size(min = 8)
    private String password;
}
