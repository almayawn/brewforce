package com.brewforce.auth_service.service;

public interface AuditLogService {

    public void logSecurityEvent(String username, String eventType, String message);

    public void logFailedLogin(String username);
    
}
