package com.brewforce.auth_service.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.brewforce.auth_service.dto.response.BaseResponseDTO;
import com.brewforce.auth_service.dto.response.DetailUserResponseDTO;
import com.brewforce.auth_service.model.Enduser;
import com.brewforce.auth_service.security.jwt.JwtUtils;
import com.brewforce.auth_service.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/cashiers")
    public ResponseEntity<?> getAllCashiers() {
        var baseResponseDTO = new BaseResponseDTO<List<Map<String, Object>>>();
        try {
            List<Map<String, Object>> users = userService.getUsersWithRole("KASIR");

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setMessage("Data KASIR berhasil diambil");
            baseResponseDTO.setData(users);
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception e) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan: " + e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        var baseResponseDTO = new BaseResponseDTO<DetailUserResponseDTO>();

        try {

            String tokenWithoutBearer = token.startsWith("Bearer ") ? token.substring(7) : token;

            if (!jwtUtils.validateJwtToken(tokenWithoutBearer)) {
                baseResponseDTO.setStatus(HttpStatus.UNAUTHORIZED.value());
                baseResponseDTO.setMessage("Token is not valid or expired.");
                baseResponseDTO.setData(null);
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.UNAUTHORIZED);
            }

            String username = jwtUtils.getUsernameJwtToken(tokenWithoutBearer);
            Enduser user = userService.getUserByUsername(username);

            if (user == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("User not found.");
                baseResponseDTO.setData(null);
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            DetailUserResponseDTO responseDTO = new DetailUserResponseDTO(user.getUserID(), user.getUsername(), user.getName(), user.getRole());

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setMessage("User data retrieved successfully.");
            baseResponseDTO.setData(responseDTO);
            baseResponseDTO.setTimestamp(new Date());

            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception e) {
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage("An error occurred: " + e.getMessage());
            baseResponseDTO.setData(null);
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }
        
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable UUID id,
            @RequestHeader(value = "Authorization") String token) {
        var baseResponseDTO = new BaseResponseDTO<Enduser>();

        try {
            String tokenWithoutBearer = token.startsWith("Bearer ") ? token.substring(7) : token;

            if (!jwtUtils.validateJwtToken(tokenWithoutBearer)) {
                baseResponseDTO.setStatus(HttpStatus.UNAUTHORIZED.value());
                baseResponseDTO.setMessage("Token is not valid or expired.");
                baseResponseDTO.setData(null);
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.UNAUTHORIZED);
            }

            Enduser user = userService.getUserById(id);

            if (user == null) {
                baseResponseDTO.setStatus(HttpStatus.NOT_FOUND.value());
                baseResponseDTO.setMessage("User not found.");
                baseResponseDTO.setData(null);
                baseResponseDTO.setTimestamp(new Date());
                return new ResponseEntity<>(baseResponseDTO, HttpStatus.NOT_FOUND);
            }

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setMessage("User data retrieved successfully.");
            baseResponseDTO.setData(user);
            baseResponseDTO.setTimestamp(new Date());

            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception e) {
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage("An error occurred: " + e.getMessage());
            baseResponseDTO.setData(null);
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }
    }
}
