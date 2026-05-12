package com.p2pdomicilios.P2pDomicilios.repositories;

import com.p2pdomicilios.P2pDomicilios.entities.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {
    Optional<Calificacion> findByIdServicio(Long idServicio);
    List<Calificacion> findByIdDomiciliario(Integer idDomiciliario);
    boolean existsByIdServicio(Long idServicio);
}
