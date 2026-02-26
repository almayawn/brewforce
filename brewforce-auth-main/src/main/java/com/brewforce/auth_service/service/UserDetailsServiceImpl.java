package com.brewforce.auth_service.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.brewforce.auth_service.model.Enduser;
import com.brewforce.auth_service.model.LoginAttempt;
import com.brewforce.auth_service.repository.EnduserDb;
import com.brewforce.auth_service.repository.LoginAttemptDb;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private EnduserDb enduserDb;

    @Autowired
    private LoginAttemptDb loginAttemptDb;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Enduser user = enduserDb.findByUsername(username);

        LoginAttempt attempt = loginAttemptDb.findById(username).orElse(null);

        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        if (attempt != null && attempt.getLockoutTime() != null && attempt.getLockoutTime().isAfter(LocalDateTime.now())) {
            throw new LockedException("Account locked due to 3 failed attempts. Please try again later.");
        }
        
        return createUserDetails(user);
    }

    private UserDetails createUserDetails(Enduser user) {
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        grantedAuthorities.add(new SimpleGrantedAuthority(user.getRole()));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(), grantedAuthorities);
    }
}
