package com.brewforce.auth_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginJwtResponseDTO {
    private String token;
    private String username;
    private String name;
    private String role;
}
