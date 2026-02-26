package com.brewforce.auth_service.controller;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.brewforce.auth_service.dto.request.LoginJwtRequestDTO;
import com.brewforce.auth_service.dto.response.BaseResponseDTO;
import com.brewforce.auth_service.dto.response.LoginJwtResponseDTO;
import com.brewforce.auth_service.model.Enduser;
import com.brewforce.auth_service.security.jwt.JwtUtils;
import com.brewforce.auth_service.service.AuditLogServiceImpl;
import com.brewforce.auth_service.service.LoginAttemptServiceImpl;
import com.brewforce.auth_service.service.UserServiceImpl;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = { "https://kelompok-7-brewforce-fe.pkpl.cs.ui.ac.id" })
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private LoginAttemptServiceImpl loginAttemptService;

    @Autowired
    private AuditLogServiceImpl auditLogService;

    @PostMapping("/users")
    public ResponseEntity<?> registerUser(@Valid @RequestBody Enduser registerRequest,
            BindingResult bindingResult,
            @RequestHeader(value = "Authorization", required = false) String token) {
        var baseResponseDTO = new BaseResponseDTO<Enduser>();

        // Cek apakah ada validation errors
        if (bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.toList());

            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(String.join("; ", errorMessages));
            baseResponseDTO.setData(null);
            baseResponseDTO.setTimestamp(new Date());

            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }

        try {
            // Jika role yang diminta adalah KASIR, perlu validasi token untuk memastikan
            // hanya ADMIN yang bisa membuat akun kasir
            if ("KASIR".equals(registerRequest.getRole())) {
                if (token == null || !"ADMIN".equals(jwtUtils.getRoleFromJwtToken(token.substring(7)))) {
                    baseResponseDTO.setStatus(HttpStatus.FORBIDDEN.value());
                    baseResponseDTO.setMessage("Hanya ADMIN yang dapat membuat akun kasir.");
                    baseResponseDTO.setData(null);
                    baseResponseDTO.setTimestamp(new Date());
                    return new ResponseEntity<>(baseResponseDTO, HttpStatus.FORBIDDEN);
                }
            }

            Enduser newUser = userService.register(registerRequest);

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setMessage("Register berhasil!");
            baseResponseDTO.setData(newUser);
            baseResponseDTO.setTimestamp(new Date());

            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception e) {
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage("Register gagal: " + e.getMessage());
            baseResponseDTO.setData(null);
            baseResponseDTO.setTimestamp(new Date());

            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginJwtRequestDTO loginRequest) {
        var baseResponseDTO = new BaseResponseDTO<LoginJwtResponseDTO>();
        try {
            // Check status
            if (loginAttemptService.isLocked(loginRequest.getUsername())) {
                long lockedMinutes = loginAttemptService.getLockoutMinutes(loginRequest.getUsername());
                throw new LockedException("Akun terkunci karena 3 kali percobaan login gagal. Silakan coba lagi setelah " + lockedMinutes + " menit.");
            }
    
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));
    
            // Reset login attempts
            loginAttemptService.loginSuccess(loginRequest.getUsername());
            Enduser user = userService.getUserByUsername(loginRequest.getUsername());
    
            // Generate token JWT
            String token = jwtUtils.generateJwtToken(authentication.getName());
            String role = userService.getUserRole(user.getUsername());
    
            LoginJwtResponseDTO responseDTO = new LoginJwtResponseDTO(token, user.getUsername(), user.getName(), role);
            
            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setMessage("Login berhasil!");
            baseResponseDTO.setData(responseDTO);
            baseResponseDTO.setTimestamp(new Date());
    
            return ResponseEntity.ok(baseResponseDTO);
    
        } catch (LockedException e) {
            // Handle locked account
            baseResponseDTO.setStatus(HttpStatus.UNAUTHORIZED.value());
            baseResponseDTO.setMessage(e.getMessage());
            baseResponseDTO.setData(null);
            baseResponseDTO.setTimestamp(new Date());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(baseResponseDTO);
    
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            // Handle failed attempts
            loginAttemptService.loginFailed(loginRequest.getUsername());

            auditLogService.logFailedLogin(loginRequest.getUsername());
            
            int remainingAttempts = loginAttemptService.getRemainingAttempts(loginRequest.getUsername());
            long lockedMinutes = loginAttemptService.getLockoutMinutes(loginRequest.getUsername());
            String message = remainingAttempts > 0
                ? "Username atau password salah!"
                : "Akun terkunci karena 3 kali percobaan login gagal. Silakan coba lagi setelah " + lockedMinutes + " menit.";
    
            baseResponseDTO.setStatus(HttpStatus.UNAUTHORIZED.value());
            baseResponseDTO.setMessage(message);
            baseResponseDTO.setData(null);
            baseResponseDTO.setTimestamp(new Date());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(baseResponseDTO);
    
        } catch (Exception e) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Terjadi kesalahan pada sistem" + e.getMessage());
            baseResponseDTO.setData(null);
            baseResponseDTO.setTimestamp(new Date());
            return ResponseEntity.internalServerError().body(baseResponseDTO);
        }
    }
}
