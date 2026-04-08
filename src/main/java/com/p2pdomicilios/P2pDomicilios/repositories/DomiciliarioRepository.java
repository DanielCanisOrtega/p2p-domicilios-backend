package com.p2pdomicilios.P2pDomicilios.repositories;

import com.p2pdomicilios.P2pDomicilios.entities.Domiciliario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DomiciliarioRepository extends JpaRepository<Domiciliario, Integer> {

    @Query(
        value = """
            SELECT d.*
            FROM domiciliario d
            WHERE d.disponible = true
              AND d.verificado = true
              AND d.location IS NOT NULL
              AND ST_DWithin(
                  d.location::geography,
                  ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
                  :radiusMeters
              )
            ORDER BY ST_Distance(
                d.location::geography,
                ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography
            )
            """,
        nativeQuery = true
    )
    List<Domiciliario> findNearbyAvailableAndVerified(
        @Param("lat") double lat,
        @Param("lon") double lon,
        @Param("radiusMeters") double radiusMeters
    );
}
