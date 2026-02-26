package com.brewforce.auth_service.security.jwt;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.brewforce.auth_service.service.UserService;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${brewforce.app.jwtSecret}")
    private String jwtSecret;

    @Value("${brewforce.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Autowired
    UserService userService;


    public String generateJwtToken(String username) {
        return Jwts.builder()
                .subject(username)
                .claim("name", userService.getUserName(username))
                .claim("role", userService.getUserRole(username))
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .compact();
    }

    public String getUsernameJwtToken(String token) {
        JwtParser jwtParser = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
            .build();
    
        Jws<Claims> claimsJws = jwtParser.parseSignedClaims(token);
        return claimsJws.getPayload().getSubject();
}

    public String getRoleFromJwtToken(String token) {
        JwtParser jwtParser = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes())).build();
        Jws<Claims> claimsJws = jwtParser.parseSignedClaims(token);
        Claims claims = claimsJws.getPayload();
        Object role = claims.get("role");

        if (role instanceof String) {
            return (String) role;
        }

        return null;
    }

    public String getNameFromJwtToken(String token) {
        JwtParser jwtParser = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes())).build();
        Jws<Claims> claimsJws = jwtParser.parseSignedClaims(token);
        Claims claims = claimsJws.getPayload();
        Object name = claims.get("name");

        if (name instanceof String) {
            return (String) name;
        }

        return null;
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes())).build().parse(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        }
        return false;
    }
}
