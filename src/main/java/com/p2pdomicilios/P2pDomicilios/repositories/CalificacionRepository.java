package com.p2pdomicilios.P2pDomicilios.repositories;

import com.p2pdomicilios.P2pDomicilios.entities.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {
    Optional<Calificacion> findByIdServicio(Long idServicio);
    Optional<Calificacion> findByIdServicioAndRoleCalificador(Long idServicio, String roleCalificador);
    List<Calificacion> findByIdDomiciliario(Integer idDomiciliario);
    List<Calificacion> findByIdDomiciliarioAndRoleCalificador(Integer idDomiciliario, String roleCalificador);
    boolean existsByIdServicio(Long idServicio);
    boolean existsByIdServicioAndRoleCalificador(Long idServicio, String roleCalificador);
}
