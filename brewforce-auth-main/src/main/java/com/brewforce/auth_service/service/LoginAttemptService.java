package com.brewforce.auth_service.service;

public interface LoginAttemptService {
    public void loginFailed(String username);

    public void loginSuccess(String username);

    public boolean isLocked(String username);

    public int getRemainingAttempts(String username);

    public long getLockoutMinutes(String username);

}
