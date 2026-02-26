package com.brewforce.auth_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import java.util.Date;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.brewforce.auth_service.model.Enduser;
import com.brewforce.auth_service.security.jwt.JwtUtils;
import com.brewforce.auth_service.service.UserService;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private String validToken;
    private Enduser testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        validToken = "valid.jwt.token";
        testUserId = UUID.randomUUID();
        
        testUser = new Enduser();
        testUser.setUserID(testUserId);
        testUser.setUsername("testuser");
        testUser.setName("Test User");
        testUser.setRole("KASIR");
    }

    @Test
    void whenGetAllCashiers_thenSuccess() throws Exception {
        Map<String, Object> cashier = new HashMap<>();
        cashier.put("userId", testUserId);
        cashier.put("username", "testuser");
        cashier.put("name", "Test User");

        List<Map<String, Object>> cashiers = Arrays.asList(cashier);
        when(userService.getUsersWithRole("KASIR")).thenReturn(cashiers);

        mockMvc.perform(get("/api/users/cashiers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Data KASIR berhasil diambil"))
                .andExpect(jsonPath("$.data[0].username").value("testuser"));
    }

    @Test
    void whenGetAllCashiers_thenThrowsException() throws Exception {
        when(userService.getUsersWithRole("KASIR"))
                .thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(get("/api/users/cashiers"))
                .andDo(print()) // Add this to see the actual response
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Terjadi kesalahan: Database connection failed"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void whenGetCurrentUser_withValidToken_thenSuccess() throws Exception {
        when(jwtUtils.validateJwtToken("valid.jwt.token")).thenReturn(true);
        when(jwtUtils.getUsernameJwtToken("valid.jwt.token")).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);

        mockMvc.perform(get("/api/users/current")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.role").value("KASIR"));
    }

    @Test
    void whenGetCurrentUser_withInvalidToken_thenUnauthorized() throws Exception {
        when(jwtUtils.validateJwtToken("invalid.token")).thenReturn(false);

        mockMvc.perform(get("/api/users/current")
                .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token is not valid or expired."));
    }

    @Test
    void whenGetUserById_withValidTokenAndId_thenSuccess() throws Exception {
        when(jwtUtils.validateJwtToken(validToken)).thenReturn(true);
        when(userService.getUserById(testUserId)).thenReturn(testUser);

        mockMvc.perform(get("/api/users/" + testUserId)
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.role").value("KASIR"));
    }

    @Test
    void whenGetUserById_withValidTokenButNonexistentId_thenNotFound() throws Exception {
        when(jwtUtils.validateJwtToken(validToken)).thenReturn(true);
        when(userService.getUserById(any(UUID.class))).thenReturn(null);

        mockMvc.perform(get("/api/users/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found."));
    }

    @Test
    void whenGetUserById_withInvalidToken_thenUnauthorized() throws Exception {
        when(jwtUtils.validateJwtToken("invalid.token")).thenReturn(false);

        mockMvc.perform(get("/api/users/" + testUserId)
                .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token is not valid or expired."));
    }

    @Test
    void whenGetUserById_throwsUnexpectedException_thenBadRequest() throws Exception {
        when(jwtUtils.validateJwtToken(validToken)).thenReturn(true);
        
        // Simulasikan exception saat ambil user
        when(userService.getUserById(testUserId)).thenThrow(new RuntimeException("Database down"));

        mockMvc.perform(get("/api/users/" + testUserId)
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("An error occurred:")));
    }

}