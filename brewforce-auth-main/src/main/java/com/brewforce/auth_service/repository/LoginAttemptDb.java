package com.brewforce.auth_service.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.brewforce.auth_service.model.LoginAttempt;

@Repository
public interface LoginAttemptDb extends JpaRepository<LoginAttempt, String> {
    Optional<LoginAttempt> findById(String username);
    
    @Modifying
    void deleteByUsername(String username);
}
