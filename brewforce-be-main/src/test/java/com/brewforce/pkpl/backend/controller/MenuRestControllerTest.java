

package com.brewforce.pkpl.backend.controller;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.brewforce.pkpl.backend.dto.request.UpdateMenuRequest;
import com.brewforce.pkpl.backend.dto.request.UpdateStokRequest;
import com.brewforce.pkpl.backend.model.Menu;
import com.brewforce.pkpl.backend.repository.MenuRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class MenuRestControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MenuRepository menuRepository;

    @Value("${brewforce.app.jwtSecret}")
    private String jwtSecret;

    @Value("${brewforce.app.jwtExpirationMs}")
    private long jwtExpirationMs;

    private String generateToken(String name, String role) {
        return Jwts.builder()
                .subject(name)
                .claim("name", name)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .compact();
    }

    private Menu generateMenu() {
        Menu menu = new Menu();
        menu.setNamaMenu("Test Menu");
        menu.setDeskripsiMenu("Test Desc");
        menu.setHargaMenu(10000);
        menu.setStok(10);
        menu.setFotoPath("path/to/foto.jpg");

        menuRepository.save(menu);
        return menu;
    }
        

    // Create Menu -----------------------------------------------------------------
    
    @Test
    void createMenu_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        Menu request = new Menu();

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(
                post("/api/menus")
                        .header("Authorization", "Bearer invalidToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Token JWT tidak valid atau sudah kedaluwarsa"))); // cek message persis
                ;
    }

    @Test
    void createMenu_WithWrongRole_ShouldReturnForbidden() throws Exception {
        Menu request = new Menu();
        request.setNamaMenu("New Menu");
        request.setDeskripsiMenu("New Desc");
        request.setHargaMenu(1);
        request.setStok(20);
        request.setFotoPath("new/path/to/foto.jpg");

        String requestJson = objectMapper.writeValueAsString(request);

        String token = generateToken("pembeli", "PEMBELI");

        mockMvc.perform(
                post("/api/menus/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer " + token)
                        .content(requestJson))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", is("Akses ditolak, anda tidak memiliki izin untuk mengakses halaman ini"))); // cek message persis
                ;
    }

    @Test
    void createMenu_WithInvalidPrice_ShouldReturnBadRequest()  throws Exception {
        Menu request = new Menu();
        request.setNamaMenu("New Menu");
        request.setDeskripsiMenu("New Desc");
        request.setHargaMenu(-1);
        request.setStok(20);
        request.setFotoPath("new/path/to/foto.jpg");

        String requestJson = objectMapper.writeValueAsString(request);

        String token = generateToken("admin", "ADMIN");

        mockMvc.perform(
                post("/api/menus/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer " + token)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Harga menu harus lebih dari 0"));
                ;
    }

    @Test
    void createMenu_WithInvalidStock_ShouldReturnBadRequest()  throws Exception {

        Menu request = new Menu();
        request.setNamaMenu("Updated Menu");
        request.setDeskripsiMenu("Updated Desc");
        request.setHargaMenu(1);
        request.setStok(-20);
        request.setFotoPath("updated/path/to/foto.jpg");

        String requestJson = objectMapper.writeValueAsString(request);

        String token = generateToken("admin", "ADMIN");

        mockMvc.perform(
            post("/api/menus/")
            .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer " + token)
                        .content(requestJson))
                        .andExpect(status().isBadRequest())
                        .andExpect(content().string("Stok menu tidak boleh negatif"));
                ;
    }

    @Test
    void createMenu_WithValidToken_ShouldReturnOk()  throws Exception {
        Menu request = new Menu();
        request.setNamaMenu("New Menu");
        request.setDeskripsiMenu("New Desc");
        request.setHargaMenu(1);
        request.setStok(20);
        request.setFotoPath("new/path/to/foto.jpg");

        String requestJson = objectMapper.writeValueAsString(request);

        String token = generateToken("admin", "ADMIN");

        mockMvc.perform(
                post("/api/menus/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer " + token)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMenu", notNullValue()));
                ;
    }

    // Get All Menu -----------------------------------------------------------------

    @Test
    void getAllMenus_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(
                get("/api/menus/")
                .header("Authorization", "Bearer invalidToken")
                .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.message", is("Token JWT tidak valid atau sudah kedaluwarsa")));
                ;
    }

    @Test
    void getAllMenus_WithValidToken_ShouldReturnUnauthorized() throws Exception {
        String token = generateToken("kasir", "KASIR");

        mockMvc.perform(
                get("/api/menus/")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                ;
    }

    // Get Menu by ID -----------------------------------------------------------------

    @Test
    void getMenuById_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(
            get("/api/menus/{id}", id)
                .header("Authorization", "Bearer invalidToken")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message", is("Token JWT tidak valid atau sudah kedaluwarsa")));
                ;
    }

    @Test
    void getMenuById_WithNonExistentId_ShouldReturnNotFound()  throws Exception {
        UUID id = UUID.randomUUID();

        String token = generateToken("kasir", "KASIR");

        mockMvc.perform(
            get("/api/menus/{id}", id)
                    .header("Authorization","Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Menu not found with id: " + id)); // cek message persis
                ;
    }

    @Test
    void getMenuById_WithValidIdAndToken_ShouldReturnOk()  throws Exception {
        Menu menu = generateMenu();

        UUID id = menu.getIdMenu();
        
        String token = generateToken("kasir", "KASIR");

        mockMvc.perform(
                get("/api/menus/{id}", id)
                    .header("Authorization","Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                ;
    }

    // Update Menu -----------------------------------------------------------------

    @Test
    void updateMenu_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateMenuRequest request = new UpdateMenuRequest();

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(
                put("/api/menus/{id}", id)
                        .header("Authorization", "Bearer invalidToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Token JWT tidak valid atau sudah kedaluwarsa"))); // cek message persis
                ;
    }

    @Test
    void updateMenu_WithWrongRole_ShouldReturnForbidden() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateMenuRequest request = new UpdateMenuRequest();
        request.setNamaMenu("Updated Menu");
        request.setDeskripsiMenu("Updated Desc");
        request.setHargaMenu(15000);
        request.setStok(20);
        request.setFotoPath("updated/path/to/foto.jpg");

        String requestJson = objectMapper.writeValueAsString(request);

        String token = generateToken("pembeli", "PEMBELI");

        mockMvc.perform(
                put("/api/menus/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer " + token)
                        .content(requestJson))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", is("Akses ditolak, anda tidak memiliki izin untuk mengakses halaman ini"))); // cek message persis
                ;
    }

    @Test
    void updateMenu_WithNonExistentId_ShouldReturnNotFound()  throws Exception {
        UUID id = UUID.randomUUID();
        UpdateMenuRequest request = new UpdateMenuRequest();
        request.setNamaMenu("Updated Menu");
        request.setDeskripsiMenu("Updated Desc");
        request.setHargaMenu(15000);
        request.setStok(20);
        request.setFotoPath("updated/path/to/foto.jpg");

        String requestJson = objectMapper.writeValueAsString(request);

        String token = generateToken("admin", "ADMIN");

        mockMvc.perform(
                put("/api/menus/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer " + token)
                        .content(requestJson))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Menu dengan ID " + id + " tidak ditemukan"))); // cek message persis
                ;
    }

    @Test
    void updateMenu_WithInvalidPrice_ShouldReturnBadRequest()  throws Exception {
        Menu menu = generateMenu();

        UUID id = menu.getIdMenu();
        UpdateMenuRequest request = new UpdateMenuRequest();
        request.setNamaMenu("Updated Menu");
        request.setDeskripsiMenu("Updated Desc");
        request.setHargaMenu(-1);
        request.setStok(20);
        request.setFotoPath("updated/path/to/foto.jpg");

        String requestJson = objectMapper.writeValueAsString(request);

        String token = generateToken("admin", "ADMIN");

        mockMvc.perform(
                put("/api/menus/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer " + token)
                        .content(requestJson))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Harga menu harus lebih dari 0"))); // cek message persis
                ;
    }

    @Test
    void updateMenu_WithInvalidStock_ShouldReturnBadRequest()  throws Exception {
        Menu menu = generateMenu();

        UUID id = menu.getIdMenu();
        UpdateMenuRequest request = new UpdateMenuRequest();
        request.setNamaMenu("Updated Menu");
        request.setDeskripsiMenu("Updated Desc");
        request.setHargaMenu(1);
        request.setStok(-20);
        request.setFotoPath("updated/path/to/foto.jpg");

        String requestJson = objectMapper.writeValueAsString(request);

        String token = generateToken("admin", "ADMIN");

        mockMvc.perform(
                put("/api/menus/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer " + token)
                        .content(requestJson))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Stok menu tidak boleh negatif"))); // cek message persis
                ;
    }

    @Test
    void updateMenu_WithInvalidData_ShouldReturnInternalServerError()  throws Exception {
        Menu menu = generateMenu();

        UUID id = menu.getIdMenu();
        UpdateMenuRequest request = new UpdateMenuRequest();

        String requestJson = objectMapper.writeValueAsString(request);

        String token = generateToken("admin", "ADMIN");

        mockMvc.perform(
                put("/api/menus/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer " + token)
                        .content(requestJson))
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.message", is("Terjadi kesalahan saat memperbarui menu"))); // cek message persis
                ;
    }

    @Test
    void updateMenu_WithValidIdAndToken_ShouldReturnOk()  throws Exception {
        Menu menu = generateMenu();

        UUID id = menu.getIdMenu();
        UpdateMenuRequest request = new UpdateMenuRequest();
        request.setNamaMenu("Updated Menu");
        request.setDeskripsiMenu("Updated Desc");
        request.setHargaMenu(1);
        request.setStok(20);
        request.setFotoPath("updated/path/to/foto.jpg");

        String requestJson = objectMapper.writeValueAsString(request);

        String token = generateToken("admin", "ADMIN");

        mockMvc.perform(
                put("/api/menus/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer " + token)
                        .content(requestJson))
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.message", is("Menu dengan id " + id + " berhasil diperbarui"))); // cek message persis
                ;
    }

    // Update Stok -----------------------------------------------------------------
    @Test
    void updateStok_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateStokRequest request = new UpdateStokRequest();

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(
                put("/api/menus/{id}/stock", id)
                        .header("Authorization", "Bearer invalidToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Token JWT tidak valid atau sudah kedaluwarsa"))); // cek message persis
                ;
    }

    @Test
    void updateStok_WithWrongRole_ShouldReturnForbidden() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateStokRequest request = new UpdateStokRequest();

        String token = generateToken("pembeli", "PEMBELI");

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(
                put("/api/menus/{id}/stock", id)
                    .header("Authorization","Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", is("Akses ditolak, anda tidak memiliki izin untuk mengakses halaman ini"))); // cek message persis
                ;
                
    }

    @Test
    void updateStok_WithNonExistentId_ShouldReturnNotFound()  throws Exception {
        UUID id = UUID.randomUUID();
        UpdateStokRequest request = new UpdateStokRequest();

        request.setStok(20);

        String token = generateToken("kasir", "KASIR");

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(
                put("/api/menus/{id}/stock", id)
                    .header("Authorization","Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Menu dengan ID " + id + " tidak ditemukan"))); // cek message persis
                ;
    }

    @Test
    void updateStok_WithInvalidStock_ShouldReturnBadRequest()  throws Exception {
        Menu menu = generateMenu();

        UUID id = menu.getIdMenu();
        UpdateStokRequest request = new UpdateStokRequest();

        request.setStok(20);

        String token = generateToken("kasir", "KASIR");

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(
                put("/api/menus/{id}/stock", id)
                    .header("Authorization","Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                        .andExpect(jsonPath("$.status", is(200)))
                        .andExpect(jsonPath("$.message", is("Menu dengan id " + id + " berhasil diperbarui"))); // cek message persis
                ;
    }

    @Test
    void updateStok_WithValidIdAndToken_ShouldReturnOk()  throws Exception {
        Menu menu = generateMenu();

        UUID id = menu.getIdMenu();
        UpdateStokRequest request = new UpdateStokRequest();

        request.setStok(-20);

        String token = generateToken("kasir", "KASIR");

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(
                put("/api/menus/{id}/stock", id)
                    .header("Authorization","Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Stok menu tidak boleh negatif"))); // cek message persis
                ;
    }

    // Delete Menu -----------------------------------------------------------------
    @Test
    void deleteMenu_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(
                delete("/api/menus/{id}", id)
                        .header("Authorization", "Bearer invalidToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Token JWT tidak valid atau sudah kedaluwarsa"))); // cek message persis
                ;
    }

    @Test
    void deleteMenu_WithWrongRole_ShouldReturnUnauthorized() throws Exception {
        UUID id = UUID.randomUUID();

        String token = generateToken("pembeli", "PEMBELI");

        mockMvc.perform(
                delete("/api/menus/{id}", id)
                    .header("Authorization","Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", is("Akses ditolak, anda tidak memiliki izin untuk mengakses halaman ini"))); // cek message persis
                ;
    }
    
    @Test
    void deleteMenu_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        String token = generateToken("admin", "ADMIN");

        mockMvc.perform(
                delete("/api/menus/{id}", id)
                    .header("Authorization","Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.status", is(404)))
                        .andExpect(jsonPath("$.message", is("Menu dengan ID " + id + " tidak ditemukan"))); // cek message persis
                ;
    }

    @Test
    void deleteMenu_WithValidIdAndToken_ShouldReturnOk() throws Exception {
        Menu menu = generateMenu();

        UUID id = menu.getIdMenu();

        String token = generateToken("admin", "ADMIN");

        mockMvc.perform(
                delete("/api/menus/{id}", id)
                    .header("Authorization","Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.status", is(200)))
                        .andExpect(jsonPath("$.message", is("Menu dengan id " + id + " berhasil dihapus"))); // cek message persis
                ;
    }
}