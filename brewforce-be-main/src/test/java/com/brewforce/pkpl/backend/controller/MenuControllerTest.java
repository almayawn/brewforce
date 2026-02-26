package com.brewforce.pkpl.backend.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;

import com.brewforce.pkpl.backend.dto.request.UpdateMenuRequest;
import com.brewforce.pkpl.backend.dto.request.UpdateStokRequest;
import com.brewforce.pkpl.backend.dto.response.BaseResponseDTO;
import com.brewforce.pkpl.backend.dto.response.MenuResponse;
import com.brewforce.pkpl.backend.model.Menu;
import com.brewforce.pkpl.backend.security.JwtUtils;
import com.brewforce.pkpl.backend.service.MenuService;

public class MenuControllerTest {

    @Mock
    private MenuService menuService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private MenuController menuController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // 1. Broken Access Control
    @Test
    void createMenu_WithInvalidToken_ShouldReturnForbidden() {
        String token = "Bearer validToken";
        Menu menu = new Menu();
        ResponseEntity<?> response = menuController.createMenu(menu, bindingResult, null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak ditemukan", response.getBody());
    }

    // 2. Cryptographic Failures
    @Test
    void createMenu_WithMissingToken_ShouldReturnUnauthorized() {
        Menu menu = new Menu();
        String token = null;

        ResponseEntity<?> response = menuController.createMenu(menu, bindingResult, token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak ditemukan", response.getBody());
    }

    // 3. Injection
    @Test
    void createMenu_WithInvalidData_ShouldReturnBadRequest() {
        String token = "Bearer validToken";
        Menu menu = new Menu();
        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(bindingResult.hasFieldErrors()).thenReturn(true);

        ResponseEntity<?> response = menuController.createMenu(menu, bindingResult, token);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // 4. Insecure Design
    @Test
    void createMenu_WithInvalidPrice_ShouldReturnBadRequest() {
        String token = "Bearer validToken";
        Menu menu = new Menu();
        menu.setHargaMenu(-1000);

        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(bindingResult.hasFieldErrors()).thenReturn(true);

        ResponseEntity<?> response = menuController.createMenu(menu, bindingResult, token);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // 5. Security Misconfiguration
    @Test
    void createMenu_WithInvalidToken_ShouldReturnUnauthorized() {
        String token = "Bearer invalidToken";
        Menu menu = new Menu();

        when(jwtUtils.validateJwtToken("invalidToken")).thenReturn(false);

        ResponseEntity<?> response = menuController.createMenu(menu, bindingResult, token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak valid", response.getBody());
    }

    // 7. Identification and Authentication Failures
    @Test
    void createMenu_WithTamperedToken_ShouldReturnUnauthorized() {
        String token = "Bearer tamperedToken";
        Menu menu = new Menu();

        when(jwtUtils.validateJwtToken("tamperedToken")).thenReturn(false);

        ResponseEntity<?> response = menuController.createMenu(menu, bindingResult, token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak valid", response.getBody());
    }

    // 8. Software and Data Integrity Failures
    @Test
    void createMenu_WithTamperedData_ShouldReturnBadRequest() {
        String token = "Bearer validToken";
        Menu menu = new Menu();
        menu.setIdMenu(UUID.randomUUID()); // Attempting to set ID manually

        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(menuService.createMenu(any())).thenThrow(
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Menu ID should not be set manually"));

        ResponseEntity<?> response = menuController.createMenu(menu, bindingResult, token);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // Successful Creation Test
    @Test
    void createMenu_WithValidData_ShouldReturnCreated() {
        String token = "Bearer validToken";
        Menu menu = new Menu();
        menu.setNamaMenu("Espresso");
        menu.setHargaMenu(25000);

        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(jwtUtils.getRoleFromJwtToken("validToken")).thenReturn("ADMIN");
        when(bindingResult.hasFieldErrors()).thenReturn(false);
        when(menuService.createMenu(any())).thenReturn(menu);

        ResponseEntity<?> response = menuController.createMenu(menu, bindingResult, token);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // Tests for getAllMenus

    // 1. Broken Access Control test for getAllMenus
    @Test
    void getAllMenus_WithoutToken_ShouldReturnUnauthorized() {
        String token = null;

        ResponseEntity<?> response = menuController.getAllMenus(token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak ditemukan", response.getBody());
    }

    // 1. Broken Access Control test for getAllMenus with empty token
    @Test
    void getAllMenus_WithEmptyToken_ShouldReturnUnauthorized() {
        String token = "";

        ResponseEntity<?> response = menuController.getAllMenus(token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak ditemukan", response.getBody());
    }

    // 5. Security Misconfiguration test for getAllMenus
    @Test
    void getAllMenus_WithInvalidToken_ShouldReturnUnauthorized() {
        String token = "Bearer invalidToken";

        when(jwtUtils.validateJwtToken("invalidToken")).thenReturn(false);

        ResponseEntity<?> response = menuController.getAllMenus(token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak valid", response.getBody());
    }

    // Successful getAllMenus with valid token
    @Test
    void getAllMenus_WithValidToken_ShouldReturnOk() {
        String token = "Bearer validToken";
        List<Menu> menuList = new ArrayList<>();
        Menu menu = new Menu();
        menu.setIdMenu(UUID.randomUUID());
        menu.setNamaMenu("Espresso");
        menu.setHargaMenu(25000);
        menu.setDeskripsiMenu("Strong coffee");
        menu.setStok(10);
        menu.setFotoPath("/images/espresso.jpg");
        menuList.add(menu);

        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(menuService.getAllMenus()).thenReturn(menuList);

        ResponseEntity<?> response = menuController.getAllMenus(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // Tests for getMenuById

    // 1. Broken Access Control test for getMenuById
    @Test
    void getMenuById_WithoutToken_ShouldReturnUnauthorized() {
        UUID id = UUID.randomUUID();
        String token = null;

        ResponseEntity<?> response = menuController.getMenuById(id, token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak ditemukan", response.getBody());
    }

    // 3. Injection test for getMenuById (malformed UUID)
    @Test
    void getMenuById_WithInvalidId_ShouldThrowException() {
        UUID id = UUID.randomUUID();
        String token = "Bearer validToken";

        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(menuService.getMenuById(id)).thenThrow(new NoSuchElementException("Menu not found"));

        ResponseEntity<?> response = menuController.getMenuById(id, token);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Menu not found"));
    }

    // 4. Insecure Design - Test error handling for unexpected exceptions
    @Test
    void getMenuById_WithServiceException_ShouldReturnInternalServerError() {
        UUID id = UUID.randomUUID();
        String token = "Bearer validToken";

        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(menuService.getMenuById(id)).thenThrow(new RuntimeException("Database connection failed"));

        ResponseEntity<?> response = menuController.getMenuById(id, token);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Error retrieving menu"));
    }

    // 6. Vulnerable and Outdated Components - Test with malformed token
    @Test
    void getMenuById_WithMalformedToken_ShouldReturnUnauthorized() {
        UUID id = UUID.randomUUID();
        String token = "MalformedTokenWithoutBearer";

        when(jwtUtils.validateJwtToken("MalformedTokenWithoutBearer")).thenReturn(false);

        ResponseEntity<?> response = menuController.getMenuById(id, token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak valid", response.getBody());
    }

    // 7. Identification and Authentication Failures test for getMenuById
    @Test
    void getMenuById_WithTamperedToken_ShouldReturnUnauthorized() {
        UUID id = UUID.randomUUID();
        String token = "Bearer tamperedToken";

        when(jwtUtils.validateJwtToken("tamperedToken")).thenReturn(false);

        ResponseEntity<?> response = menuController.getMenuById(id, token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token tidak valid", response.getBody());
    }

    // 8. Software and Data Integrity Failures for getMenuById
    @Test
    void getMenuById_WithNonExistentId_ShouldReturnNotFound() {
        UUID id = UUID.randomUUID();
        String token = "Bearer validToken";

        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(menuService.getMenuById(id)).thenThrow(new NoSuchElementException("Menu not found with id: " + id));

        ResponseEntity<?> response = menuController.getMenuById(id, token);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Menu not found with id"));
    }

    // Successful getMenuById test
    @Test
    void getMenuById_WithValidIdAndToken_ShouldReturnOk() {
        UUID id = UUID.randomUUID();
        String token = "Bearer validToken";
        Menu menu = new Menu();
        menu.setIdMenu(id);
        menu.setNamaMenu("Espresso");
        menu.setHargaMenu(25000);
        menu.setDeskripsiMenu("Strong coffee");
        menu.setStok(10);
        menu.setFotoPath("/images/espresso.jpg");

        when(jwtUtils.validateJwtToken("validToken")).thenReturn(true);
        when(menuService.getMenuById(id)).thenReturn(menu);

        ResponseEntity<?> response = menuController.getMenuById(id, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void updateMenu_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateMenuRequest request = new UpdateMenuRequest();
        request.setNamaMenu("Updated Menu");
        request.setDeskripsiMenu("Updated Desc");
        request.setHargaMenu(15000);
        request.setStok(20);
        request.setFotoPath("updated/path/to/foto.jpg");

        when(menuService.updateMenu(id, request))
                .thenThrow(new NoSuchElementException("Menu dengan ID " + id + " tidak ditemukan"));

        BaseResponseDTO<MenuResponse> response = menuController.updateMenu(id, request);
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
        assertEquals("Menu dengan ID " + id + " tidak ditemukan", response.getMessage());
    }

    @Test
    void updateMenu_WithInvalidPrice_ShouldReturnBadRequest() throws Exception {
        UUID id = UUID.randomUUID();
        Menu menu = new Menu();
        menu.setIdMenu(id);
        menu.setNamaMenu("Espresso");
        menu.setHargaMenu(25000);
        menu.setDeskripsiMenu("Strong coffee");
        menu.setStok(10);
        menu.setFotoPath("/images/espresso.jpg");

        UpdateMenuRequest request = new UpdateMenuRequest();
        request.setNamaMenu("Updated Menu");
        request.setDeskripsiMenu("Updated Desc");
        request.setHargaMenu(-1);
        request.setStok(20);
        request.setFotoPath("updated/path/to/foto.jpg");

        when(menuService.updateMenu(id, request))
                .thenThrow(new IllegalArgumentException("Harga menu harus lebih dari 0"));

        BaseResponseDTO<MenuResponse> response = menuController.updateMenu(id, request);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertEquals("Harga menu harus lebih dari 0", response.getMessage());
    }

    @Test
    void updateMenu_WithInvalidStock_ShouldReturnBadRequest() throws Exception {
        UUID id = UUID.randomUUID();
        Menu menu = new Menu();
        menu.setIdMenu(id);
        menu.setNamaMenu("Espresso");
        menu.setHargaMenu(25000);
        menu.setDeskripsiMenu("Strong coffee");
        menu.setStok(10);
        menu.setFotoPath("/images/espresso.jpg");

        UpdateMenuRequest request = new UpdateMenuRequest();
        request.setNamaMenu("Updated Menu");
        request.setDeskripsiMenu("Updated Desc");
        request.setHargaMenu(1);
        request.setStok(-20);
        request.setFotoPath("updated/path/to/foto.jpg");

        when(menuService.updateMenu(id, request))
                .thenThrow(new IllegalArgumentException("Stok menu tidak boleh negatif"));

        BaseResponseDTO<MenuResponse> response = menuController.updateMenu(id, request);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertEquals("Stok menu tidak boleh negatif", response.getMessage());
    }

    @Test
    void updateMenu_WithValidData_ShouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        Menu menu = new Menu();
        menu.setIdMenu(id);
        menu.setNamaMenu("Espresso");
        menu.setHargaMenu(25000);
        menu.setDeskripsiMenu("Strong coffee");
        menu.setStok(10);
        menu.setFotoPath("/images/espresso.jpg");

        UpdateMenuRequest request = new UpdateMenuRequest();
        request.setNamaMenu("Updated Menu");
        request.setDeskripsiMenu("Updated Desc");
        request.setHargaMenu(30000);
        request.setStok(20);
        request.setFotoPath("updated/path/to/foto.jpg");

        MenuResponse menuResponse = new MenuResponse();
        menuResponse.setIdMenu(id);
        menuResponse.setNamaMenu(request.getNamaMenu());
        menuResponse.setHargaMenu(request.getHargaMenu());
        menuResponse.setDeskripsiMenu(request.getDeskripsiMenu());
        menuResponse.setStok(request.getStok());
        menuResponse.setFotoPath(request.getFotoPath());

        when(menuService.updateMenu(id, request)).thenReturn(menuResponse);

        BaseResponseDTO<MenuResponse> response = menuController.updateMenu(id, request);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Menu dengan id " + id + " berhasil diperbarui", response.getMessage());
    }

    @Test
    void updateStok_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateStokRequest request = new UpdateStokRequest();
        request.setStok(20);

        when(menuService.updateStok(id, request))
                .thenThrow(new NoSuchElementException("Menu dengan ID " + id + " tidak ditemukan"));

        BaseResponseDTO<MenuResponse> response = menuController.updateStok(id, request);
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
        assertEquals("Menu dengan ID " + id + " tidak ditemukan", response.getMessage());
    }

    @Test
    void updateStok_WithInvalidStock_ShouldReturnBadRequest() throws Exception {
        UUID id = UUID.randomUUID();
        Menu menu = new Menu();
        menu.setIdMenu(id);
        menu.setNamaMenu("Espresso");
        menu.setHargaMenu(25000);
        menu.setDeskripsiMenu("Strong coffee");
        menu.setStok(10);
        menu.setFotoPath("/images/espresso.jpg");

        UpdateStokRequest request = new UpdateStokRequest();
        request.setStok(-20);

        when(menuService.updateStok(id, request))
                .thenThrow(new IllegalArgumentException("Stok menu tidak boleh negatif"));

        BaseResponseDTO<MenuResponse> response = menuController.updateStok(id, request);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertEquals("Stok menu tidak boleh negatif", response.getMessage());
    }

    @Test
    void updateStok_WithValidData_ShouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        Menu menu = new Menu();
        menu.setIdMenu(id);
        menu.setNamaMenu("Espresso");
        menu.setHargaMenu(25000);
        menu.setDeskripsiMenu("Strong coffee");
        menu.setStok(10);
        menu.setFotoPath("/images/espresso.jpg");

        UpdateStokRequest request = new UpdateStokRequest();
        request.setStok(-20);

        MenuResponse menuResponse = new MenuResponse();
        menuResponse.setIdMenu(id);
        menuResponse.setNamaMenu(menu.getNamaMenu());
        menuResponse.setHargaMenu(menu.getHargaMenu());
        menuResponse.setDeskripsiMenu(menu.getDeskripsiMenu());
        menuResponse.setStok(request.getStok());

        when(menuService.updateStok(id, request)).thenReturn(menuResponse);

        BaseResponseDTO<MenuResponse> response = menuController.updateStok(id, request);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Menu dengan id " + id + " berhasil diperbarui", response.getMessage());
    }

    @Test
    void deleteMenu_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        doThrow(new NoSuchElementException("Menu dengan ID " + id + " tidak ditemukan"))
                .when(menuService).deleteMenu(id);

        BaseResponseDTO<MenuResponse> response = menuController.deleteMenu(id);
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
        assertEquals("Menu dengan ID " + id + " tidak ditemukan", response.getMessage());
    }

    @Test
    void deleteMenu_WithValidId_ShouldReturnNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        Menu menu = new Menu();
        menu.setIdMenu(id);
        menu.setNamaMenu("Espresso");
        menu.setHargaMenu(25000);
        menu.setDeskripsiMenu("Strong coffee");
        menu.setStok(10);
        menu.setFotoPath("/images/espresso.jpg");

        doNothing().when(menuService).deleteMenu(id);

        BaseResponseDTO<MenuResponse> response = menuController.deleteMenu(id);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Menu dengan id " + id + " berhasil dihapus", response.getMessage());
    }

}