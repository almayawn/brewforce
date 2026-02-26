package com.brewforce.pkpl.backend.controller;

import java.util.Date;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.brewforce.pkpl.backend.dto.response.BaseResponseDTO;
import com.brewforce.pkpl.backend.model.Menu;
import com.brewforce.pkpl.backend.security.JwtUtils;

import com.brewforce.pkpl.backend.dto.request.UpdateMenuRequest;
import com.brewforce.pkpl.backend.dto.request.UpdateStokRequest;
import com.brewforce.pkpl.backend.dto.response.MenuResponse;
import com.brewforce.pkpl.backend.service.MenuService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/menus")
@CrossOrigin("*")
public class MenuController {

    @Autowired
    private MenuService menuService;
    @Autowired
    private JwtUtils jwtUtils;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/")
    public ResponseEntity<?> createMenu(@RequestBody Menu menu, BindingResult bindingResult,
            @RequestHeader(value = "Authorization") String token) {
        try {
            if (token == null || token.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token tidak ditemukan");
            }

            String tokenWithoutBearer = token.startsWith("Bearer ") ? token.substring(7) : token;

            if (!jwtUtils.validateJwtToken(tokenWithoutBearer)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token tidak valid");
            }

            if (bindingResult.hasFieldErrors()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Request body memiliki kesalahan atau field yang hilang");
            }
            Menu newMenu = menuService.createMenu(menu);
            return new ResponseEntity<>(newMenu, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getReason());
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getReason());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("An unexpected error occurred: " + e.getMessage());
            }
        }
    }

    @CrossOrigin("*")
    @GetMapping("/")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getAllMenus(@RequestHeader(value = "Authorization") String token) {
        try {
            if (token != null && !token.isBlank()) {
                String tokenWithoutBearer = token.startsWith("Bearer ") ? token.substring(7) : token;
                if (!jwtUtils.validateJwtToken(tokenWithoutBearer)) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token tidak valid");
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token tidak ditemukan");
            }

            List<Menu> menus = menuService.getAllMenus();
            List<MenuResponse> menuResponses = menus.stream()
                    .map(this::convertToDto)
                    .toList();
            return ResponseEntity.ok(menuResponses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving menus: " + e.getMessage());
        }
    }

    @CrossOrigin("*")
    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getMenuById(@PathVariable("id") UUID id,
            @RequestHeader(value = "Authorization") String token) {
        try {
            if (token != null && !token.isBlank()) {
                String tokenWithoutBearer = token.startsWith("Bearer ") ? token.substring(7) : token;
                if (!jwtUtils.validateJwtToken(tokenWithoutBearer)) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token tidak valid");
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token tidak ditemukan");
            }

            Menu menu = menuService.getMenuById(id);
            MenuResponse menuResponse = convertToDto(menu);
            return ResponseEntity.ok(menuResponse);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Menu not found with id: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving menu: " + e.getMessage());
        }
    }

    private MenuResponse convertToDto(Menu menu) {
        return MenuResponse.builder()
                .idMenu(menu.getIdMenu())
                .namaMenu(menu.getNamaMenu())
                .deskripsiMenu(menu.getDeskripsiMenu())
                .hargaMenu(menu.getHargaMenu())
                .stok(menu.getStok())
                .fotoPath(menu.getFotoPath())
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public BaseResponseDTO<MenuResponse> updateMenu(@PathVariable UUID id,
            @RequestBody @Valid UpdateMenuRequest request) {
        try {
            MenuResponse menuResponse = menuService.updateMenu(id, request);
            return BaseResponseDTO.<MenuResponse>builder()
                    .data(menuResponse)
                    .status(HttpStatus.OK.value())
                    .message("Menu dengan id " + menuResponse.getIdMenu() + " berhasil diperbarui")
                    .timestamp(new Date())
                    .build();
        } catch (NoSuchElementException e) {
            return BaseResponseDTO.<MenuResponse>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(e.getMessage())
                    .timestamp(new Date())
                    .build();
        } catch (IllegalArgumentException e) {
            return BaseResponseDTO.<MenuResponse>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .timestamp(new Date())
                    .build();
        } catch (Exception e) {
            return BaseResponseDTO.<MenuResponse>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Terjadi kesalahan saat memperbarui menu")
                    .timestamp(new Date())
                    .build();
        }
    }

    @PreAuthorize("hasRole('KASIR')")
    @PutMapping("/{id}/stock")
    public BaseResponseDTO<MenuResponse> updateStok(@PathVariable UUID id,
            @RequestBody @Valid UpdateStokRequest request) {
        try {
            MenuResponse menuResponse = menuService.updateStok(id, request);
            return BaseResponseDTO.<MenuResponse>builder()
                    .data(menuResponse)
                    .status(HttpStatus.OK.value())
                    .message("Menu dengan id " + menuResponse.getIdMenu() + " berhasil diperbarui")
                    .timestamp(new Date())
                    .build();
        } catch (NoSuchElementException e) {
            return BaseResponseDTO.<MenuResponse>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(e.getMessage())
                    .timestamp(new Date())
                    .build();
        } catch (IllegalArgumentException e) {
            return BaseResponseDTO.<MenuResponse>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .timestamp(new Date())
                    .build();
        } catch (Exception e) {
            return BaseResponseDTO.<MenuResponse>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Terjadi kesalahan saat memperbarui menu")
                    .timestamp(new Date())
                    .build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public BaseResponseDTO<MenuResponse> deleteMenu(@PathVariable UUID id) {
        try {
            menuService.deleteMenu(id);
            return BaseResponseDTO.<MenuResponse>builder()
                    .status(HttpStatus.OK.value())
                    .message("Menu dengan id " + id + " berhasil dihapus")
                    .timestamp(new Date())
                    .build();
        } catch (NoSuchElementException e) {
            return BaseResponseDTO.<MenuResponse>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(e.getMessage())
                    .timestamp(new Date())
                    .build();
        } catch (Exception e) {
            return BaseResponseDTO.<MenuResponse>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Terjadi kesalahan saat menghapus menu")
                    .timestamp(new Date())
                    .build();
        }
    }

}