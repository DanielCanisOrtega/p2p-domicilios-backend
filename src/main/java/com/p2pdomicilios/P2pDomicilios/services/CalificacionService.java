package com.p2pdomicilios.P2pDomicilios.services;

import com.p2pdomicilios.P2pDomicilios.dto.CalificacionRequest;
import com.p2pdomicilios.P2pDomicilios.dto.CalificacionResponse;
import com.p2pdomicilios.P2pDomicilios.entities.Calificacion;
import com.p2pdomicilios.P2pDomicilios.entities.Domiciliario;
import com.p2pdomicilios.P2pDomicilios.entities.Servicio;
import com.p2pdomicilios.P2pDomicilios.repositories.CalificacionRepository;
import com.p2pdomicilios.P2pDomicilios.repositories.DomiciliarioRepository;
import com.p2pdomicilios.P2pDomicilios.repositories.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CalificacionService {

    @Autowired
    private CalificacionRepository calificacionRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private DomiciliarioRepository domiciliarioRepository;

    @Transactional
    public CalificacionResponse createCalificacion(CalificacionRequest request, Integer idCliente) {
        Servicio servicio = servicioRepository.findById(request.getIdServicio())
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        if (!servicio.getIdCliente().equals(idCliente.longValue())) {
            throw new RuntimeException("No eres el cliente de este servicio");
        }

        String estado = servicio.getEstado();
        if (!"ENTREGADO".equalsIgnoreCase(estado) && !"COMPLETADO".equalsIgnoreCase(estado) && !"ACEPTADO".equalsIgnoreCase(estado)) {
            throw new RuntimeException("El servicio debe estar completado para calificar");
        }

        if (calificacionRepository.existsByIdServicio(request.getIdServicio())) {
            throw new RuntimeException("Este servicio ya ha sido calificado");
        }

        if (servicio.getIdDomiciliario() == null) {
            throw new RuntimeException("Este servicio no tiene un domiciliario asignado");
        }

        Integer idDomiciliario = servicio.getIdDomiciliario().intValue();

        Calificacion calificacion = Calificacion.builder()
                .idServicio(request.getIdServicio())
                .idCliente(idCliente)
                .idDomiciliario(idDomiciliario)
                .puntuacion(request.getPuntuacion())
                .comentario(request.getComentario())
                .build();

        calificacion = calificacionRepository.save(calificacion);

        updateDomiciliarioRating(idDomiciliario);

        return toResponse(calificacion);
    }

    public CalificacionResponse getCalificacion(Long idServicio, Integer userId) {
        Calificacion calificacion = calificacionRepository.findByIdServicio(idServicio)
                .orElseThrow(() -> new RuntimeException("Calificación no encontrada"));

        if (!calificacion.getIdCliente().equals(userId) &&
            !calificacion.getIdDomiciliario().equals(userId)) {
            throw new RuntimeException("No tienes acceso a esta calificación");
        }

        return toResponse(calificacion);
    }

    private void updateDomiciliarioRating(Integer idDomiciliario) {
        List<Calificacion> calificaciones = calificacionRepository.findByIdDomiciliario(idDomiciliario);

        if (!calificaciones.isEmpty()) {
            double promedio = calificaciones.stream()
                    .mapToInt(Calificacion::getPuntuacion)
                    .average()
                    .orElse(0.0);

            domiciliarioRepository.findByUser_Id(idDomiciliario)
                    .ifPresent(domiciliario -> {
                        domiciliario.setCalificacion(promedio);
                        domiciliarioRepository.save(domiciliario);
                    });
        }
    }

    private CalificacionResponse toResponse(Calificacion calificacion) {
        return CalificacionResponse.builder()
                .idCalificacion(calificacion.getIdCalificacion())
                .idServicio(calificacion.getIdServicio())
                .idCliente(calificacion.getIdCliente())
                .idDomiciliario(calificacion.getIdDomiciliario())
                .puntuacion(calificacion.getPuntuacion())
                .comentario(calificacion.getComentario())
                .fechaCreacion(calificacion.getFechaCreacion())
                .build();
    }
}
