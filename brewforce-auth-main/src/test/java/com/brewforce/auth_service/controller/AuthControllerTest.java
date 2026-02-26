package com.brewforce.auth_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.hamcrest.Matchers.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.brewforce.auth_service.model.Enduser;
import com.brewforce.auth_service.service.UserServiceImpl;
import com.brewforce.auth_service.service.AuditLogService;
import com.brewforce.auth_service.service.AuditLogServiceImpl;
import com.brewforce.auth_service.service.LoginAttemptServiceImpl;
import com.brewforce.auth_service.dto.request.LoginJwtRequestDTO;
import com.brewforce.auth_service.security.jwt.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {
    
    private MockMvc mockMvc;
    private Enduser testUser;
    private Enduser testUserKasir;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private LoginAttemptServiceImpl loginAttemptService;

    @Mock
    private AuditLogServiceImpl auditLogService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    
        // Regular test user with PEMBELI role
        testUser = new Enduser();
        testUser.setUsername("testuser");
        testUser.setPassword("Test123!@#");
        testUser.setName("Test User");
        testUser.setRole("PEMBELI");
        
        // Test user with KASIR role
        testUserKasir = new Enduser();
        testUserKasir.setUsername("testuserkasir");
        testUserKasir.setPassword("Test123!@#");
        testUserKasir.setName("Test Kasir User");
        testUserKasir.setRole("KASIR");
    }

    @Test
    public void testRegisterUser() throws Exception {
        when(userService.register(any(Enduser.class))).thenReturn(testUser);

        ObjectMapper objectMapper = new ObjectMapper();
        String userJson = objectMapper.writeValueAsString(testUser);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Register berhasil!"));
    }

    // A04:2021 - Insecure Design
    @Test
    public void testRegisterUserWithInvalidData() throws Exception {
        testUser.setUsername("");  // Invalid username

        ObjectMapper objectMapper = new ObjectMapper();
        String userJson = objectMapper.writeValueAsString(testUser);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isBadRequest());
    }

    // A01:2021 - Broken Access Control
    // A10:2021 - Server-Side Request Forgery (SSRF)
    @Test
    public void testRegisterKasirWithoutToken() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String userJson = objectMapper.writeValueAsString(testUserKasir);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Hanya ADMIN yang dapat membuat akun kasir."));
    }

    // A07:2021 - Identification and Authentication Failures
    // A10:2021 - Server-Side Request Forgery (SSRF)
    @Test
    public void testRegisterKasirWithAdminToken() throws Exception {
        when(jwtUtils.getRoleFromJwtToken(any())).thenReturn("ADMIN");
        when(userService.register(any(Enduser.class))).thenReturn(testUserKasir);

        ObjectMapper objectMapper = new ObjectMapper();
        String userJson = objectMapper.writeValueAsString(testUserKasir);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer fake-token")
                .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Register berhasil!"));
    }

    // A01:2021 - Broken Access Control
    // A07:2021 - Identification and Authentication Failures
    // A10:2021 - Server-Side Request Forgery (SSRF)
    @Test
    public void testRegisterKasirWithNonAdminToken() throws Exception {
        when(jwtUtils.getRoleFromJwtToken(any())).thenReturn("PEMBELI");

        ObjectMapper objectMapper = new ObjectMapper();
        String userJson = objectMapper.writeValueAsString(testUserKasir);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer fake-token")
                .content(userJson))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Hanya ADMIN yang dapat membuat akun kasir."));
    }

    // A05:2021 - Security Misconfiguration
    @Test
    public void testRegisterUserWithException() throws Exception {
        when(userService.register(any(Enduser.class))).thenThrow(new RuntimeException("Username already exists"));

        ObjectMapper objectMapper = new ObjectMapper();
        String userJson = objectMapper.writeValueAsString(testUser);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Register gagal: Username already exists"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    // A03:2021 - Injection
    @Test
    public void testRegisterUserWithSQLInjectionInputs_ShouldReturnBadRequest() throws Exception {
        Enduser sqlInjectionUser = new Enduser();
        sqlInjectionUser.setUsername("admin'; DROP TABLE users; --");
        sqlInjectionUser.setPassword("password'; DELETE FROM users; --");
        sqlInjectionUser.setName("Robert'); DROP TABLE users; --");
        sqlInjectionUser.setRole("HACKER'); DELETE FROM users; --");
    
        ObjectMapper objectMapper = new ObjectMapper();
        String userJson = objectMapper.writeValueAsString(sqlInjectionUser);
    
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message", allOf(
                        containsString("Role must be one of: PEMBELI, KASIR, or ADMIN"),
                        containsString("Password must contain at least 8 characters"),
                        containsString("Username can only contain alphanumeric characters"),
                        containsString("Name can only contain letters")
                )));
    }

    // A08:2021 - Software and Data Integrity Failures
    @Test
    public void testJWTSignatureValidationInRegister() throws Exception {
        testUserKasir.setRole("KASIR");
        String tamperedToken = "eyJhbGciOiJIUzI1NiJ9.tampered.signature";

        when(jwtUtils.getRoleFromJwtToken(tamperedToken)).thenReturn("NOT_ADMIN");
        
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + tamperedToken)
                .content(new ObjectMapper().writeValueAsString(testUserKasir)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Hanya ADMIN yang dapat membuat akun kasir."));

        verify(jwtUtils).getRoleFromJwtToken(tamperedToken);
    }

    // A07:2021 - Identification and Authentication Failures
    @Test
    public void testLoginSuccessful() throws Exception {
        // Tests proper authentication flow with valid credentials
        LoginJwtRequestDTO loginRequest = new LoginJwtRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("Test123!@#");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testuser");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(loginAttemptService.isLocked("testuser")).thenReturn(false);
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        when(userService.getUserRole("testuser")).thenReturn("PEMBELI");
        when(jwtUtils.generateJwtToken("testuser")).thenReturn("test.jwt.token");

        ObjectMapper objectMapper = new ObjectMapper();
        String loginJson = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("test.jwt.token"))
                .andExpect(jsonPath("$.data.role").value("PEMBELI"));

        verify(loginAttemptService).loginSuccess("testuser"); // A07: Verify attempt reset
    }
     
    // A07:2021 - Identification and Authentication Failures
    @Test
    public void testLoginWithInvalidCredentials() throws Exception {
        // Tests brute force protection mechanism
        LoginJwtRequestDTO loginRequest = new LoginJwtRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        when(loginAttemptService.isLocked("testuser")).thenReturn(false);
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        when(loginAttemptService.getRemainingAttempts("testuser")).thenReturn(2);

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Username atau password salah!"));

        verify(loginAttemptService).loginFailed("testuser"); // A07: Verify attempt tracking
    }

    // A05:2021 - Security Misconfiguration
    @Test
    public void testLoginWithLockedUser() throws Exception {
        // Tests account lockout enforcement
        LoginJwtRequestDTO loginRequest = new LoginJwtRequestDTO();
        loginRequest.setUsername("lockeduser");
        loginRequest.setPassword("anyPassword");

        when(loginAttemptService.isLocked("lockeduser")).thenReturn(true);
        when(loginAttemptService.getLockoutMinutes("lockeduser")).thenReturn(10L);

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Akun terkunci karena 3 kali percobaan login gagal. Silakan coba lagi setelah 10 menit."));
    }

    // A07:2021 - Identification and Authentication Failures
    // A10:2021 - Server-Side Request Forgery (SSRF)
    @Test
    public void testLoginWithUsernameNotFound() throws Exception {
        // Tests handling of non-existent users (prevent user enumeration)
        LoginJwtRequestDTO loginRequest = new LoginJwtRequestDTO();
        loginRequest.setUsername("notfounduser");
        loginRequest.setPassword("anyPassword");

        when(loginAttemptService.isLocked("notfounduser")).thenReturn(false);
        when(authenticationManager.authenticate(any()))
                .thenThrow(new UsernameNotFoundException("User not found"));
        when(loginAttemptService.getRemainingAttempts("notfounduser")).thenReturn(0);
        when(loginAttemptService.getLockoutMinutes("notfounduser")).thenReturn(15L);

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Akun terkunci karena 3 kali percobaan login gagal. Silakan coba lagi setelah 15 menit."));
    }

    // A08:2021 - Software and Data Integrity Failures
    @Test
    public void testLoginThrowsUnexpectedException() throws Exception {
        LoginJwtRequestDTO loginRequest = new LoginJwtRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("Test123!@#");

        when(loginAttemptService.isLocked("testuser")).thenReturn(false);
        when(authenticationManager.authenticate(any()))
                .thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(containsString("Terjadi kesalahan pada sistem")))
                .andExpect(jsonPath("$.data").doesNotExist()); // A08: No sensitive data exposed
    }

    // A02:2021 - Cryptographic Failures
    @Test
    public void testJWTTokenGenerationAfterLogin() throws Exception {
        // Tests proper JWT token generation
        LoginJwtRequestDTO loginRequest = new LoginJwtRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("Test123!@#");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testuser");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(loginAttemptService.isLocked("testuser")).thenReturn(false);
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        when(userService.getUserRole("testuser")).thenReturn("PEMBELI");
        when(jwtUtils.generateJwtToken("testuser")).thenReturn("valid.jwt.token");

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.token").isString());
    }


    // A03:2021 - Injection
    @Test
    public void testLoginRejectsInjectionAttempts() throws Exception {
    // SQL Injection
    LoginJwtRequestDTO sqlInjectionRequest = new LoginJwtRequestDTO();
    sqlInjectionRequest.setUsername("admin'; DROP TABLE users; --");
    sqlInjectionRequest.setPassword("password'; DELETE FROM users; --");

    when(loginAttemptService.isLocked(sqlInjectionRequest.getUsername())).thenReturn(false);
    when(authenticationManager.authenticate(argThat(auth ->
            auth instanceof UsernamePasswordAuthenticationToken &&
            ((UsernamePasswordAuthenticationToken) auth).getPrincipal().equals(sqlInjectionRequest.getUsername())
    ))).thenThrow(new BadCredentialsException("Bad credentials"));

    mockMvc.perform(post("/api/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(sqlInjectionRequest)))
            .andExpect(status().isUnauthorized());

    // XSS
    LoginJwtRequestDTO xssRequest = new LoginJwtRequestDTO();
    xssRequest.setUsername("<script>alert('XSS')</script>");
    xssRequest.setPassword("anything");

    when(loginAttemptService.isLocked(xssRequest.getUsername())).thenReturn(false);
    when(authenticationManager.authenticate(argThat(auth ->
            auth instanceof UsernamePasswordAuthenticationToken &&
            ((UsernamePasswordAuthenticationToken) auth).getPrincipal().equals(xssRequest.getUsername())
    ))).thenThrow(new BadCredentialsException("Bad credentials"));

    mockMvc.perform(post("/api/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(xssRequest)))
            .andExpect(status().isUnauthorized());
    }
}