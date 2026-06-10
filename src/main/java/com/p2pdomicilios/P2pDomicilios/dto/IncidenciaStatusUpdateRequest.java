package com.p2pdomicilios.P2pDomicilios.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IncidenciaStatusUpdateRequest {
    @NotBlank(message = "El estado es requerido")
    private String estado;
}
