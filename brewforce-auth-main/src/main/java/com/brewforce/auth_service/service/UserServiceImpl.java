package com.brewforce.auth_service.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.brewforce.auth_service.model.Enduser;
import com.brewforce.auth_service.repository.EnduserDb;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private EnduserDb enduserDb;

    @Override
    public Enduser register(Enduser request) {
        // 1. Cek apakah username sudah ada
        Enduser existingUser = enduserDb.findByUsername(request.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("Username already exists!");
        }

        // 2. Jika role = ROLE_ADMIN, cek apakah admin sudah ada
        if ("ADMIN".equalsIgnoreCase(request.getRole())) {
            List<Enduser> admins = enduserDb.findAll().stream()
                .filter(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
                .toList();

            if (!admins.isEmpty()) {
                throw new RuntimeException("Admin already exists. Cannot create another admin!");
            }
        }

        Enduser newUser = new Enduser();
        newUser.setUsername(request.getUsername());
        newUser.setName(request.getName());
        newUser.setPassword(hashPassword(request.getPassword()));
        newUser.setRole(request.getRole().toUpperCase()); // misal: PEMBELI, KASIR, ADMIN

        return enduserDb.save(newUser);
    }

    @Override
    public String hashPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    @Override
    public Enduser getUserById(UUID userId) {
        return enduserDb.findById(userId).orElse(null);
    }

    @Override
    public Enduser getUserByUsername(String username) {
        return enduserDb.findByUsername(username);
    }
    
    @Override
    public List<Enduser> getAllUsers() {
        return enduserDb.findAll();
    }

    @Override
    public List<Enduser> getUsersByRole(String role) {
        // Filter berdasarkan field role yang ada di Enduser
        return enduserDb.findAll()
                .stream()
                .filter(user -> user.getRole() != null && user.getRole().equalsIgnoreCase(role))
                .collect(Collectors.toList());
    }
    
    @Override
    public String getUserRole(String username) {
        Enduser endUser = enduserDb.findByUsername(username);
        if (endUser != null) {
            return endUser.getRole();
        }
        return null;
    }

    @Override
    public String getUserName(String username) {
        Enduser endUser = enduserDb.findByUsername(username);
        if (endUser != null) {
            return endUser.getName();
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> getUsersWithRole(String role) {
        List<Enduser> users;
        if (role != null) {
            users = getUsersByRole(role);
        } else {
            users = getAllUsers();
        }

        List<Map<String, Object>> usersWithRole = new ArrayList<>();
        for (Enduser user : users) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userID", user.getUserID());
            userMap.put("name", user.getName());
            userMap.put("username", user.getUsername());
            userMap.put("role", user.getRole());
            usersWithRole.add(userMap);
        }
        return usersWithRole;
    }
}