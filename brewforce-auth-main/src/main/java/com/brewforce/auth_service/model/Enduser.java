package com.brewforce.auth_service.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@Entity
@Table(name = "enduser")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Enduser {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonProperty("id")
    private UUID userID;

    @Size(max = 50)
    @NotNull(message = "Username cannot be null")
    @Pattern(regexp = "^[a-zA-Z0-9.]+$", message = "Username can only contain alphanumeric characters and dots")
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @NotNull(message = "Password cannot be null")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9])(?=\\S+$).{8,}$",
        message = "Password must contain at least 8 characters, including uppercase, lowercase, number, and special character, and no whitespace"
    )
    @Column(name = "password", nullable = false)
    private String password;

    @Size(max = 50)
    @NotNull(message = "Name cannot be null")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Name can only contain letters, spaces, apostrophes, and hyphens")
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 50)
    @NotNull(message = "Role cannot be null")
    @Pattern(
        regexp = "^(PEMBELI|KASIR|ADMIN)$", 
        message = "Role must be one of: PEMBELI, KASIR, or ADMIN"
    )
    @Column(name = "role", nullable = false)
    private String role;

}

