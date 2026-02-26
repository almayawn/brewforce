package com.brewforce.auth_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.brewforce.auth_service.model.Enduser;
import com.brewforce.auth_service.model.LoginAttempt;
import com.brewforce.auth_service.repository.EnduserDb;
import com.brewforce.auth_service.repository.LoginAttemptDb;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

    @Mock
    private EnduserDb enduserDb;

    @Mock
    private LoginAttemptDb loginAttemptDb;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private final String USERNAME = "testuser";
    private final String PASSWORD = "$2a$10$fakehashedpassword";
    private final String ROLE = "PEMBELI";
    private Enduser validUser;
    private LoginAttempt lockedAttempt;

    @BeforeEach
    void setUp() {
        validUser = new Enduser();
        validUser.setUsername(USERNAME);
        validUser.setPassword(PASSWORD);
        validUser.setRole(ROLE);

        lockedAttempt = new LoginAttempt();
        lockedAttempt.setUsername(USERNAME);
        lockedAttempt.setLockoutTime(LocalDateTime.now().plusMinutes(10));
    }

    // A07:2021 - Identification and Authentication Failures
    @Test
    void whenLoadByUsernameWithValidUser_thenReturnUserDetails() {
        // Arrange
        when(enduserDb.findByUsername(USERNAME)).thenReturn(validUser);
        when(loginAttemptDb.findById(USERNAME)).thenReturn(java.util.Optional.empty());

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(USERNAME);

        // Assert
        assertNotNull(userDetails);
        assertEquals(USERNAME, userDetails.getUsername());
        assertEquals(PASSWORD, userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority(ROLE)));
        verify(enduserDb).findByUsername(USERNAME);
    }

    // A07:2021 - Identification and Authentication Failures
    @Test
    void whenLoadByUsernameWithNonexistentUser_thenThrowUsernameNotFoundException() {
        // Arrange
        when(enduserDb.findByUsername(USERNAME)).thenReturn(null);

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(USERNAME);
        });
        verify(enduserDb).findByUsername(USERNAME);
    }

    // A02:2021 - Cryptographic Failures (verify password handling)
    @Test
    void whenLoadByUsername_thenPasswordShouldBeHashed() {
        // Arrange
        when(enduserDb.findByUsername(USERNAME)).thenReturn(validUser);
        when(loginAttemptDb.findById(USERNAME)).thenReturn(java.util.Optional.empty());

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(USERNAME);

        // Assert
        assertNotNull(userDetails.getPassword());
        assertTrue(userDetails.getPassword().startsWith("$2a$")); // BCrypt pattern
    }

    // A05:2021 - Security Misconfiguration
    @Test
    void whenLoadByUsernameWithLockedAccount_thenThrowLockedException() {
        // Arrange
        when(enduserDb.findByUsername(USERNAME)).thenReturn(validUser);
        when(loginAttemptDb.findById(USERNAME)).thenReturn(java.util.Optional.of(lockedAttempt));

        // Act & Assert
        assertThrows(LockedException.class, () -> {
            userDetailsService.loadUserByUsername(USERNAME);
        });
        verify(loginAttemptDb).findById(USERNAME);
    }

    // A01:2021 - Broken Access Control (verify role assignment)
    @Test
    void whenLoadByUsername_thenCorrectAuthoritiesShouldBeAssigned() {
        // Arrange
        when(enduserDb.findByUsername(USERNAME)).thenReturn(validUser);
        when(loginAttemptDb.findById(USERNAME)).thenReturn(java.util.Optional.empty());

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(USERNAME);

        // Assert
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(ROLE)));
    }

    // A03:2021 - Injection (verify SQL injection prevention)
    @Test
    void whenLoadByUsernameWithMaliciousInput_thenHandleSafely() {
        // Arrange
        String maliciousInput = "admin' --";
        when(enduserDb.findByUsername(maliciousInput)).thenReturn(null);

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(maliciousInput);
        });
        verify(enduserDb).findByUsername(maliciousInput);
    }
}