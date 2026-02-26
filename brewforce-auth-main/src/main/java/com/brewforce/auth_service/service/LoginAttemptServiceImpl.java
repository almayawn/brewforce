package com.brewforce.auth_service.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.brewforce.auth_service.model.LoginAttempt;
import com.brewforce.auth_service.repository.LoginAttemptDb;

import jakarta.transaction.Transactional;

@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {
    @Autowired
    private LoginAttemptDb loginAttemptDb;

    private static final int MAX_ATTEMPTS = 3;
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(10);

    @Override
    @Transactional
    public void loginFailed(String username) {
        LoginAttempt attempt = loginAttemptDb.findById(username).orElse(null);
        LocalDateTime now = LocalDateTime.now();

        // Create new attempt if none found
        if (attempt == null) {
            attempt = new LoginAttempt();
            attempt.setUsername(username);
            attempt.setAttempts(0);
            attempt.setLockoutTime(null);
        }

        // Reset lockout if 10 minutes passed or lockout time passed
        if ((attempt.getLastAttempt() != null && attempt.getLastAttempt().isBefore(now.minusMinutes(10))) ||
                (attempt.getLockoutTime() != null && attempt.getLockoutTime().isBefore(LocalDateTime.now()))) {
            attempt.setAttempts(1); // Reset but count this attempt
            attempt.setLockoutTime(null);

        } else {
            attempt.setAttempts(attempt.getAttempts() + 1);
        }

        attempt.setLastAttempt(LocalDateTime.now());

        // Set new lockout
        if (attempt.getAttempts() >= MAX_ATTEMPTS && attempt.getLockoutTime() == null) {
            attempt.setLockoutTime(LocalDateTime.now().plus(LOCKOUT_DURATION));
        }

        loginAttemptDb.save(attempt);
    }

    @Override
    @Transactional
    public void loginSuccess(String username) {
        loginAttemptDb.deleteByUsername(username);
    }

    @Override
    public boolean isLocked(String username) {
        Optional<LoginAttempt> attemptOpt = loginAttemptDb.findById(username);
        if (attemptOpt.isPresent()) {
            LoginAttempt attempt = attemptOpt.get();
            return attempt.getLockoutTime() != null && attempt.getLockoutTime().isAfter(LocalDateTime.now());
        }
        return false;
    }

    @Override
    public int getRemainingAttempts(String username) {
        Optional<LoginAttempt> attemptOpt = loginAttemptDb.findById(username);
        if (attemptOpt.isPresent()) {
            return MAX_ATTEMPTS - attemptOpt.get().getAttempts();
        }
        return MAX_ATTEMPTS;
    }

    @Override
    public long getLockoutMinutes(String username) {
        Optional<LoginAttempt> attemptOpt = loginAttemptDb.findById(username);
        if (attemptOpt.isPresent()) {
            LoginAttempt attempt = attemptOpt.get();
            if (attempt.getLockoutTime() != null) {
                return Duration.between(LocalDateTime.now(), attempt.getLockoutTime()).toMinutes();
            }
        }
        return 0L;
    }
}
