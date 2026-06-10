package com.p2pdomicilios.P2pDomicilios.dto;

import com.p2pdomicilios.P2pDomicilios.entities.Servicio;
import com.p2pdomicilios.P2pDomicilios.entities.User;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminOrderDetailDTO {
    private Long idServicio;
    private Long clienteId;
    private String clienteNombre;
    private String clienteTelefono;
    private Long domiciliarioId;
    private String domiciliarioNombre;
    private String domiciliarioTelefono;
    private LocalDateTime fechaSolicitud;
    private String estado;
    private String direccionOrigen;
    private String direccionDestino;
    private Double latOrigen;
    private Double lonOrigen;
    private Double latDestino;
    private Double lonDestino;
    private Double tarifa;
    private Double ofertaActual;
    private String ultimaOfertaPor;
    private String descripcion;
    private Integer tiempoEstimado;

    public static AdminOrderDetailDTO fromEntity(Servicio servicio, User cliente, User domiciliario) {
        return AdminOrderDetailDTO.builder()
            .idServicio(servicio.getIdServicio())
            .clienteId(servicio.getIdCliente())
            .clienteNombre(cliente != null ? cliente.getNombre() : null)
            .clienteTelefono(cliente != null ? cliente.getTelefono() : null)
            .domiciliarioId(servicio.getIdDomiciliario())
            .domiciliarioNombre(domiciliario != null ? domiciliario.getNombre() : null)
            .domiciliarioTelefono(domiciliario != null ? domiciliario.getTelefono() : null)
            .fechaSolicitud(servicio.getFechaSolicitud())
            .estado(servicio.getEstado())
            .direccionOrigen(servicio.getDireccionOrigen())
            .direccionDestino(servicio.getDireccionDestino())
            .latOrigen(servicio.getLatOrigen())
            .lonOrigen(servicio.getLonOrigen())
            .latDestino(servicio.getLatDestino())
            .lonDestino(servicio.getLonDestino())
            .tarifa(servicio.getTarifa())
            .ofertaActual(servicio.getOfertaActual())
            .ultimaOfertaPor(servicio.getUltimaOfertaPor())
            .descripcion(servicio.getDescripcion())
            .tiempoEstimado(servicio.getTiempoEstimado())
            .build();
    }
}
