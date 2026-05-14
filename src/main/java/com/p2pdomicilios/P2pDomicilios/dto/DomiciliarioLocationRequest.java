package com.p2pdomicilios.P2pDomicilios.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DomiciliarioLocationRequest {

    @NotNull(message = "La latitud es requerida")
    private Double latitud;

    @NotNull(message = "La longitud es requerida")
    private Double longitud;

    private Boolean disponible;

    private Long idServicio;
}
