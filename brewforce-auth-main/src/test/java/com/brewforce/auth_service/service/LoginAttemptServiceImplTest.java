package com.brewforce.auth_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.brewforce.auth_service.model.LoginAttempt;
import com.brewforce.auth_service.repository.LoginAttemptDb;

@ExtendWith(MockitoExtension.class)
public class LoginAttemptServiceImplTest {
    
    @Mock
    private LoginAttemptDb loginAttemptDb;

    @InjectMocks
    private LoginAttemptServiceImpl loginAttemptService;

    private LoginAttempt existingAttempt;
    private final String TEST_USERNAME = "testuser";
    private final LocalDateTime NOW = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        existingAttempt = new LoginAttempt();
        existingAttempt.setUsername(TEST_USERNAME);
        existingAttempt.setAttempts(2);
        existingAttempt.setLastAttempt(NOW.minusMinutes(5));
    }

    // A07:2021 - Identification and Authentication Failures
    @Test
    void whenLoginFailedFirstTime_thenCreateNewAttempt() {
        when(loginAttemptDb.findById(TEST_USERNAME)).thenReturn(Optional.empty());
        when(loginAttemptDb.save(any(LoginAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        loginAttemptService.loginFailed(TEST_USERNAME);

        verify(loginAttemptDb).findById(TEST_USERNAME);
        verify(loginAttemptDb).save(argThat(attempt -> 
            attempt.getUsername().equals(TEST_USERNAME) && 
            attempt.getAttempts() == 1 &&
            attempt.getLockoutTime() == null
        ));
    }

    // A07:2021 - Identification and Authentication Failures
    @Test
    void whenLoginFailedUnderMaxAttempts_thenIncrementAttempts() {
        existingAttempt.setAttempts(1);
        when(loginAttemptDb.findById(TEST_USERNAME)).thenReturn(Optional.of(existingAttempt));
        when(loginAttemptDb.save(any(LoginAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        loginAttemptService.loginFailed(TEST_USERNAME);

        verify(loginAttemptDb).save(argThat(attempt -> 
            attempt.getAttempts() == 2 &&
            attempt.getLockoutTime() == null
        ));
    }

    // A05:2021 - Security Misconfiguration
    @Test
    void whenLoginFailedReachesMaxAttempts_thenLockAccount() {
        existingAttempt.setAttempts(2);
        when(loginAttemptDb.findById(TEST_USERNAME)).thenReturn(Optional.of(existingAttempt));
        when(loginAttemptDb.save(any(LoginAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        loginAttemptService.loginFailed(TEST_USERNAME);

        verify(loginAttemptDb).save(argThat(attempt -> 
            attempt.getAttempts() == 3 &&
            attempt.getLockoutTime() != null &&
            attempt.getLockoutTime().isAfter(NOW)
        ));
    }

    // A07:2021 - Identification and Authentication Failures
    @Test
    void whenLoginFailedAfterLockoutExpired_thenResetAttempts() {
        existingAttempt.setAttempts(3);
        existingAttempt.setLockoutTime(NOW.minusMinutes(11));
        existingAttempt.setLastAttempt(NOW.minusMinutes(11));
        when(loginAttemptDb.findById(TEST_USERNAME)).thenReturn(Optional.of(existingAttempt));
        when(loginAttemptDb.save(any(LoginAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        loginAttemptService.loginFailed(TEST_USERNAME);

        verify(loginAttemptDb).save(argThat(attempt -> 
            attempt.getAttempts() == 1 &&
            attempt.getLockoutTime() == null
        ));
    }

    // A07:2021 - Identification and Authentication Failures
    @Test
    void whenLoginSuccess_thenDeleteAttempt() {
        loginAttemptService.loginSuccess(TEST_USERNAME);

        verify(loginAttemptDb).deleteByUsername(TEST_USERNAME);
    }

    // A05:2021 - Security Misconfiguration
    @Test
    void whenIsLockedWithActiveLockout_thenReturnTrue() {
        existingAttempt.setLockoutTime(NOW.plusMinutes(5));
        when(loginAttemptDb.findById(TEST_USERNAME)).thenReturn(Optional.of(existingAttempt));

        assertTrue(loginAttemptService.isLocked(TEST_USERNAME));
    }

    // A05:2021 - Security Misconfiguration
    @Test
    void whenIsLockedWithExpiredLockout_thenReturnFalse() {
        existingAttempt.setLockoutTime(NOW.minusMinutes(5));
        when(loginAttemptDb.findById(TEST_USERNAME)).thenReturn(Optional.of(existingAttempt));

        assertFalse(loginAttemptService.isLocked(TEST_USERNAME));
    }

    // A07:2021 - Identification and Authentication Failures
    @Test
    void whenGetRemainingAttempts_thenReturnCorrectValue() {
        when(loginAttemptDb.findById(TEST_USERNAME)).thenReturn(Optional.of(existingAttempt));

        assertEquals(1, loginAttemptService.getRemainingAttempts(TEST_USERNAME));
    }

    // A07:2021 - Identification and Authentication Failures
    @Test
    void whenGetRemainingAttemptsForNewUser_thenReturnMaxAttempts() {
        when(loginAttemptDb.findById(TEST_USERNAME)).thenReturn(Optional.empty());

        assertEquals(3, loginAttemptService.getRemainingAttempts(TEST_USERNAME));
    }

    // A05:2021 - Security Misconfiguration
    @Test
    void whenGetLockoutMinutesWithActiveLockout_thenReturnPositive() {
        existingAttempt.setLockoutTime(NOW.plusMinutes(10));
        when(loginAttemptDb.findById(TEST_USERNAME)).thenReturn(Optional.of(existingAttempt));

        long minutes = loginAttemptService.getLockoutMinutes(TEST_USERNAME);

        assertTrue(minutes > 0 && minutes <= 10);
    }

    // A05:2021 - Security Misconfiguration
    @Test
    void whenGetLockoutMinutesWithoutLockout_thenReturnZero() {
        when(loginAttemptDb.findById(TEST_USERNAME)).thenReturn(Optional.of(existingAttempt));

        assertEquals(0, loginAttemptService.getLockoutMinutes(TEST_USERNAME));
    }
    
}