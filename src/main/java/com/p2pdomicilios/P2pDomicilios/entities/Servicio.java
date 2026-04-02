package com.p2pdomicilios.P2pDomicilios.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "SERVICIO")
@Data
public class Servicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_servicio;

    // Aquí irían las relaciones con Usuario y Domiciliario (RF6)
    // Por ahora las dejamos como IDs para que te compile sin errores
    private Integer id_cliente; 
    private Integer id_domiciliario;

    private String estado = "CREADO"; // RF6, RF12
    private LocalDateTime fecha_solicitud = LocalDateTime.now();
    
    private String direccion_origen;
    private String direccion_destino;
    private Double lat_origen;
    private Double lon_origen;
    private Double lat_destino;
    private Double lon_destino;
}