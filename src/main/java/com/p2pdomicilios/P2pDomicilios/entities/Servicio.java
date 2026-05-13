package com.p2pdomicilios.P2pDomicilios.entities;

import jakarta.persistence.*;
import lombok.Data; 
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "servicio")
@Data 
public class Servicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idServicio;

    @JsonProperty("direccion_origen") 
    private String direccionOrigen;

    @JsonProperty("direccion_destino") 
    private String direccionDestino;

    @JsonProperty("lat_origen")
    private Double latOrigen;

    @JsonProperty("lon_origen")
    private Double lonOrigen;

    @JsonProperty("lat_destino")
    private Double latDestino;

    @JsonProperty("lon_destino")
    private Double lonDestino;

    // 🌟 Los nombres entre comillas deben ser IGUALES a los del frontend
    @JsonProperty("id_cliente")
    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    @JsonProperty("id_domiciliario")
    @Column(name = "id_domiciliario", nullable = true)
    private Long idDomiciliario;

    private Double tarifa;
    private Double ofertaActual;
    private String ultimaOfertaPor;
    private String estado;
    private String descripcion;
    
    @JsonProperty("tiempo_estimado")
    private Integer tiempoEstimado;

    @Column(name = "fecha_solicitud")
    private LocalDateTime fechaSolicitud;

    @PrePersist
    protected void onCreate() {
        this.fechaSolicitud = LocalDateTime.now();
    }
}