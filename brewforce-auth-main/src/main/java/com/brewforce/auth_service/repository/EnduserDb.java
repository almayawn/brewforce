package com.brewforce.auth_service.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.brewforce.auth_service.model.Enduser;

@Repository
public interface EnduserDb extends JpaRepository<Enduser, UUID> {
    Optional<Enduser> findById(UUID id);
    Enduser findByUsername(String email);
}
