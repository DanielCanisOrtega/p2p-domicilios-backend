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
public class CalificacionResponse {
    private Long idCalificacion;
    private Long idServicio;
    private Integer idCliente;
    private Integer idDomiciliario;
    private Integer puntuacion;
    private String comentario;
    private LocalDateTime fechaCreacion;
}
