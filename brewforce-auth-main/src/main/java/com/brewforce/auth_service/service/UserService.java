package com.brewforce.auth_service.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.brewforce.auth_service.model.Enduser;

public interface UserService {
    Enduser register(Enduser request);

    String hashPassword(String password);

    Enduser getUserById(UUID UUID);

    Enduser getUserByUsername(String username);

    List<Enduser> getAllUsers();

    List<Enduser> getUsersByRole(String role);

    String getUserRole(String email);

    String getUserName(String email);

    List<Map<String, Object>> getUsersWithRole(String role);
}
