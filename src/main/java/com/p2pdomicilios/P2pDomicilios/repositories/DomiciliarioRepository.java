package com.p2pdomicilios.P2pDomicilios.repositories;

import com.p2pdomicilios.P2pDomicilios.entities.Domiciliario;
import java.util.List;
import java.util.Optional;
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
                  CAST(d.location AS geography),
                  CAST(ST_SetSRID(ST_MakePoint(:lon, :lat), 4326) AS geography),
                  :radiusMeters
              )
            ORDER BY ST_Distance(
                CAST(d.location AS geography),
                CAST(ST_SetSRID(ST_MakePoint(:lon, :lat), 4326) AS geography)
            )
            """,
        nativeQuery = true
    )
    List<Domiciliario> findNearbyAvailableAndVerified(
        @Param("lat") double lat,
        @Param("lon") double lon,
        @Param("radiusMeters") double radiusMeters
    );

    Optional<Domiciliario> findByUser_Id(Integer userId);

    @Query("""
        SELECT d
        FROM Domiciliario d
        JOIN FETCH d.user u
        WHERE (:verificado IS NULL OR d.verificado = :verificado)
        ORDER BY u.fechaRegistro DESC
        """)
    List<Domiciliario> findForAdmin(@Param("verificado") Boolean verificado);
}
