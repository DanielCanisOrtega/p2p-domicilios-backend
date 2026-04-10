package com.p2pdomicilios.P2pDomicilios.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

@Entity
@Table(name = "DOMICILIARIO")
@Data
public class Domiciliario {

    private static final GeometryFactory GEOMETRY_FACTORY =
        new GeometryFactory(new PrecisionModel(), 4326);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_domiciliario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private Boolean disponible = false;
    private Double latitud;
    private Double longitud;
    private Boolean verificado = false;

    private String vehiculo;
    private String placa;
    private Double calificacion;

    @JsonIgnore
    @Column(columnDefinition = "geometry(Point,4326)")
    private Point location;

    @PrePersist
    @PreUpdate
    private void syncLocation() {
        if (latitud == null || longitud == null) {
            this.location = null;
            return;
        }

        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(longitud, latitud));
        point.setSRID(4326);
        this.location = point;
    }
}
