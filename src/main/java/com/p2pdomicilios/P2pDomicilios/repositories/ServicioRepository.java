package com.p2pdomicilios.P2pDomicilios.repositories;

import com.p2pdomicilios.P2pDomicilios.entities.Servicio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {
	List<Servicio> findTop3ByEstadoOrderByIdServicioDesc(String estado);
	java.util.Optional<Servicio> findTopByIdClienteOrderByFechaSolicitudDesc(Long idCliente);
	List<Servicio> findByIdClienteOrderByFechaSolicitudDesc(Long idCliente);
	List<Servicio> findByIdDomiciliarioAndEstadoInOrderByFechaSolicitudDesc(Long idDomiciliario, List<String> estados);
}
