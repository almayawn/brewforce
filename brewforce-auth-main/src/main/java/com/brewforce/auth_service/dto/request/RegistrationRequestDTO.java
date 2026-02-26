package com.brewforce.auth_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class RegistrationRequestDTO {
    @Size(max = 50)
    @NotBlank(message = "Username cannot be blank")
    @Pattern(regexp = "^[a-zA-Z0-9.]+$", message = "Username can only contain alphanumeric characters and dots")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
        message = "Password must contain at least 8 characters, including uppercase, lowercase, number, and special character, and no whitespace"
    )
    private String password;

    @Size(max = 50)
    @NotBlank(message = "Name cannot be blank")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Name can only contain letters, spaces, apostrophes, and hyphens")
    private String name;

    @Size(max = 50)
    @Pattern(
        regexp = "^(PEMBELI|KASIR|ADMIN)$", 
        message = "Role must be one of: PEMBELI, KASIR, or ADMIN"
    )
    @NotBlank(message = "Role cannot be blank")

    private String role;
}