package com.brewforce.pkpl.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateMenuRequest {
    @NotNull(message = "Nama menu tidak boleh null")
    private String namaMenu;

    @NotNull(message = "Deskripsi menu tidak boleh null")
    private String deskripsiMenu;

    @NotNull(message = "Harga menu tidak boleh null")
    @Min(value = 0, message = "Harga menu tidak boleh kurang dari 0")
    private Integer hargaMenu;

    @NotNull(message = "Stok tidak boleh null")
    @Min(value = 0, message = "Stok tidak boleh kurang dari 0")
    private Integer stok;

    @NotNull(message = "Foto path tidak boleh null")
    private String fotoPath;
}
