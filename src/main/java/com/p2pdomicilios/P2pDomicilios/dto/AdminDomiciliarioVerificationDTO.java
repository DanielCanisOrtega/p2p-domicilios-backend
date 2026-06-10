package com.p2pdomicilios.P2pDomicilios.dto;

import com.p2pdomicilios.P2pDomicilios.entities.Domiciliario;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDomiciliarioVerificationDTO {
    private Integer userId;
    private Integer domiciliarioId;
    private String username;
    private String email;
    private String nombre;
    private String telefono;
    private String numeroDocumento;
    private String estadoUsuario;
    private Boolean enabled;
    private Boolean verificado;
    private Boolean disponible;
    private String vehiculo;
    private String placa;

    public static AdminDomiciliarioVerificationDTO fromEntity(Domiciliario d) {
        return AdminDomiciliarioVerificationDTO.builder()
            .userId(d.getUser().getId())
            .domiciliarioId(d.getId_domiciliario())
            .username(d.getUser().getUsername())
            .email(d.getUser().getEmail())
            .nombre(d.getUser().getNombre())
            .telefono(d.getUser().getTelefono())
            .numeroDocumento(d.getUser().getNumeroDocumento())
            .estadoUsuario(d.getUser().getEstado())
            .enabled(d.getUser().getEnabled())
            .verificado(d.getVerificado())
            .disponible(d.getDisponible())
            .vehiculo(d.getVehiculo())
            .placa(d.getPlaca())
            .build();
    }
}
