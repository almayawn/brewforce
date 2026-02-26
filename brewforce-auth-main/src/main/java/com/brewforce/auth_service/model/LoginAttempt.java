package com.brewforce.auth_service.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "login_attempt")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginAttempt {
    @Id
    private String username;
    
    private int attempts;

    private LocalDateTime lastAttempt;

    private LocalDateTime lockoutTime;
    
}
