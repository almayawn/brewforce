package com.brewforce.auth_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditLogServiceImpl {
    private static final Logger LOG = LoggerFactory.getLogger(AuditLogService.class);

    public void logSecurityEvent(String username, String eventType, String message) {
        String logMessage = String.format(
                "[SECURITY] User: %s | Event: %s | Details: %s",
                username, eventType, message);
        LOG.warn(logMessage);
    }

    public void logFailedLogin(String username) {
        logSecurityEvent(username, "LOGIN_FAILED", "Invalid credentials");
    }
}
