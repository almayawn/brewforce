package com.brewforce.auth_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.brewforce.auth_service.model.Enduser;
import com.brewforce.auth_service.repository.EnduserDb;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    
    @Mock
    private EnduserDb enduserDb;

    @InjectMocks
    private UserServiceImpl userService;

    private Enduser testUser;
    private Enduser testAdmin;

    @BeforeEach
    void setUp() {
        testUser = new Enduser();
        testUser.setUsername("testuser");
        testUser.setPassword("Test123!@#");
        testUser.setName("Test User");
        testUser.setRole("PEMBELI");

        testAdmin = new Enduser();
        testAdmin.setUsername("admin");
        testAdmin.setPassword("Admin123!@#");
        testAdmin.setName("Admin User");
        testAdmin.setRole("ADMIN");
    }

    @Test
    void whenRegisterNewUser_thenSuccess() {
        // Arrange
        when(enduserDb.findByUsername(testUser.getUsername())).thenReturn(null);
        when(enduserDb.save(any(Enduser.class))).thenReturn(testUser);

        // Act
        Enduser result = userService.register(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getName(), result.getName());
        assertEquals(testUser.getRole(), result.getRole());
        verify(enduserDb).findByUsername(testUser.getUsername());
        verify(enduserDb).save(any(Enduser.class));
    }

    @Test
    void whenRegisterExistingUsername_thenThrowException() {
        // Arrange
        when(enduserDb.findByUsername(testUser.getUsername())).thenReturn(testUser);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.register(testUser);
        });
        assertEquals("Username already exists!", exception.getMessage());
        verify(enduserDb).findByUsername(testUser.getUsername());
        verify(enduserDb, never()).save(any(Enduser.class));
    }

    @Test
    void whenRegisterSecondAdmin_thenThrowException() {
        // Arrange
        when(enduserDb.findByUsername(testAdmin.getUsername())).thenReturn(null);
        when(enduserDb.findAll()).thenReturn(Arrays.asList(testAdmin));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.register(testAdmin);
        });
        assertEquals("Admin already exists. Cannot create another admin!", exception.getMessage());
        verify(enduserDb).findByUsername(testAdmin.getUsername());
        verify(enduserDb, never()).save(any(Enduser.class));
    }

    // A02:2021 - Cryptographic Failures
    @Test
    void whenHashPassword_thenReturnHashedPassword() {
        // Arrange
        String password = "Test123!@#";

        // Act
        String hashedPassword = userService.hashPassword(password);

        // Assert
        assertNotNull(hashedPassword);
        assertNotEquals(password, hashedPassword);
        assertTrue(new BCryptPasswordEncoder().matches(password, hashedPassword));
    }
}