package com.brewforce.auth_service.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.brewforce.auth_service.controller.AuthController;
import com.brewforce.auth_service.dto.request.LoginJwtRequestDTO;
import com.brewforce.auth_service.model.Enduser;
import com.brewforce.auth_service.security.jwt.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class AuditLogServiceImplTest {

    private MockMvc mockMvc;
    
    @Mock
    private UserServiceImpl userService;
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private LoginAttemptServiceImpl loginAttemptService;
    
    @Mock
    private JwtUtils jwtUtils;
    
    @Mock
    private AuditLogServiceImpl auditLogService;
    
    @InjectMocks
    private AuthController authController;
    
    private Enduser testUser;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        
        testUser = new Enduser();
        testUser.setUsername("testuser");
        testUser.setPassword("Test123!@#");
        testUser.setName("Test User");
        testUser.setRole("PEMBELI");
    }

    // A09:2021 - Security Logging and Monitoring Failures
    @Test
    public void whenInvalidCredentials_thenLogFailure() throws Exception {
        LoginJwtRequestDTO request = new LoginJwtRequestDTO();
        request.setUsername("testuser");
        request.setPassword("wrongpass");
        
        when(loginAttemptService.isLocked(anyString())).thenReturn(false);
        when(authenticationManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("Bad credentials"));
        when(loginAttemptService.getRemainingAttempts(anyString())).thenReturn(2);

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
        
        // Verify logging
        verify(auditLogService).logFailedLogin("testuser");
        verify(loginAttemptService).loginFailed("testuser");
    }

}