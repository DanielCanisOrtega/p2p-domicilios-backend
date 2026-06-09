package com.p2pdomicilios.P2pDomicilios.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicioPendienteDTO {
    private Long idServicio;
    private String direccionOrigen;
    private String direccionDestino;
    private String descripcion;
    private String estado;
    private LocalDateTime fechaSolicitud;
}
