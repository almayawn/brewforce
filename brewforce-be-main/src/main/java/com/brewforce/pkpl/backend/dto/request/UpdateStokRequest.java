package com.brewforce.pkpl.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateStokRequest {
    @NotNull(message = "Stok tidak boleh null")
    private int stok;
}
