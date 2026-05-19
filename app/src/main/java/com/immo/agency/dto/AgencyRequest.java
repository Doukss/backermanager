package com.immo.agency.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AgencyRequest {
    @NotBlank
    private String tenantId;
    @NotBlank
    private String nom;
    private String adresse;
    private String telephone;
    @Email
    private String email;
    private String plan = "STARTER";
}
