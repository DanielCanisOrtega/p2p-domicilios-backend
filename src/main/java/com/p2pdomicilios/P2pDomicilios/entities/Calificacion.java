package com.p2pdomicilios.P2pDomicilios.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "calificacion",
    uniqueConstraints = @UniqueConstraint(columnNames = {"id_servicio", "role_calificador"})
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Calificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_calificacion")
    private Long idCalificacion;

    @Column(name = "id_servicio", nullable = false)
    private Long idServicio;

    @Column(name = "id_cliente", nullable = false)
    private Integer idCliente;

    @Column(name = "id_domiciliario", nullable = false)
    private Integer idDomiciliario;

    @Column(name = "role_calificador", nullable = false)
    private String roleCalificador;

    @Column(name = "puntuacion", nullable = false)
    private Integer puntuacion;

    @Column(name = "comentario", columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
