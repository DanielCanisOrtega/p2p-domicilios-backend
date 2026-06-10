package com.p2pdomicilios.P2pDomicilios.dto;

import com.p2pdomicilios.P2pDomicilios.entities.Servicio;
import com.p2pdomicilios.P2pDomicilios.entities.User;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminOrderListItemDTO {
    private Long idServicio;
    private Long clienteId;
    private String clienteNombre;
    private Long domiciliarioId;
    private String domiciliarioNombre;
    private LocalDateTime fechaSolicitud;
    private String estado;
    private String direccionOrigen;
    private String direccionDestino;
    private Double tarifa;

    public static AdminOrderListItemDTO fromEntity(Servicio servicio, User cliente, User domiciliario) {
        return AdminOrderListItemDTO.builder()
            .idServicio(servicio.getIdServicio())
            .clienteId(servicio.getIdCliente())
            .clienteNombre(cliente != null ? cliente.getNombre() : null)
            .domiciliarioId(servicio.getIdDomiciliario())
            .domiciliarioNombre(domiciliario != null ? domiciliario.getNombre() : null)
            .fechaSolicitud(servicio.getFechaSolicitud())
            .estado(servicio.getEstado())
            .direccionOrigen(servicio.getDireccionOrigen())
            .direccionDestino(servicio.getDireccionDestino())
            .tarifa(servicio.getOfertaActual() != null ? servicio.getOfertaActual() : servicio.getTarifa())
            .build();
    }
}
