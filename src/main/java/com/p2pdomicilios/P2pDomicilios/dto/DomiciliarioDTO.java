package com.p2pdomicilios.P2pDomicilios.dto;

import com.p2pdomicilios.P2pDomicilios.entities.Domiciliario;
import lombok.Data;

@Data
public class DomiciliarioDTO {
    private Integer id;
    private String nombre;
    private String email;
    private Double latitud;
    private Double longitud;
    private Boolean disponible;
    private Boolean verificado;
    private String vehiculo;
    private String placa;
    private Double calificacion;
    private Double distancia; // En metros

    public static DomiciliarioDTO fromEntity(Domiciliario domiciliario) {
        DomiciliarioDTO dto = new DomiciliarioDTO();
        dto.setId(domiciliario.getId_domiciliario());
        dto.setLatitud(domiciliario.getLatitud());
        dto.setLongitud(domiciliario.getLongitud());
        dto.setDisponible(domiciliario.getDisponible());
        dto.setVerificado(domiciliario.getVerificado());
        dto.setVehiculo(domiciliario.getVehiculo());
        dto.setPlaca(domiciliario.getPlaca());
        dto.setCalificacion(domiciliario.getCalificacion());

        if (domiciliario.getUser() != null) {
            dto.setNombre(domiciliario.getUser().getNombre());
            dto.setEmail(domiciliario.getUser().getEmail());
        }

        return dto;
    }
}
