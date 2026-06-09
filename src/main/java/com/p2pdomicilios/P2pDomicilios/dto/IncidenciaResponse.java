package com.p2pdomicilios.P2pDomicilios.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidenciaResponse {
    private Long id;
    private Integer idCliente;
    private Integer idDomiciliario;
    private Long idServicio;
    private String descripcion;
    private String estado;
}
