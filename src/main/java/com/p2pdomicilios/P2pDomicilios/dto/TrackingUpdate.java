package com.p2pdomicilios.P2pDomicilios.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingUpdate {
    private Long idServicio;
    private Double latitud;
    private Double longitud;
    private Integer tiempoEstimado;
}
