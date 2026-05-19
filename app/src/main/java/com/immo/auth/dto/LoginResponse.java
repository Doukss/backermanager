package com.immo.auth.dto;
import lombok.*;
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private long expiresIn;
}
