package com.p2pdomicilios.P2pDomicilios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidenciaRequest {

    @NotNull(message = "El ID del servicio es requerido")
    private Long idServicio;

    @NotBlank(message = "La descripción es requerida")
    private String descripcion;
}
