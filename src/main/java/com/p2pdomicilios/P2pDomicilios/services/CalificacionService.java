package com.p2pdomicilios.P2pDomicilios.services;

import com.p2pdomicilios.P2pDomicilios.dto.CalificacionRequest;
import com.p2pdomicilios.P2pDomicilios.dto.CalificacionResponse;
import com.p2pdomicilios.P2pDomicilios.entities.Calificacion;
import com.p2pdomicilios.P2pDomicilios.entities.Domiciliario;
import com.p2pdomicilios.P2pDomicilios.entities.Servicio;
import com.p2pdomicilios.P2pDomicilios.repositories.CalificacionRepository;
import com.p2pdomicilios.P2pDomicilios.repositories.DomiciliarioRepository;
import com.p2pdomicilios.P2pDomicilios.repositories.ServicioRepository;
import com.p2pdomicilios.P2pDomicilios.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import com.p2pdomicilios.P2pDomicilios.dto.ServicioPendienteDTO;

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

        if (calificacionRepository.existsByIdServicioAndRoleCalificador(request.getIdServicio(), Role.CLIENT.name())) {
            throw new RuntimeException("Este servicio ya ha sido calificado por el cliente");
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
                .roleCalificador(Role.CLIENT.name())
                .build();

        calificacion = calificacionRepository.save(calificacion);

        updateDomiciliarioRating(idDomiciliario);

        return toResponse(calificacion);
    }

    @Transactional
    public CalificacionResponse createCalificacionFromDomiciliario(CalificacionRequest request, Integer idDomiciliario) {
        Servicio servicio = servicioRepository.findById(request.getIdServicio())
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        if (servicio.getIdDomiciliario() == null || !servicio.getIdDomiciliario().equals(idDomiciliario.longValue())) {
            throw new RuntimeException("No eres el domiciliario de este servicio");
        }

        String estado = servicio.getEstado();
        if (!"ENTREGADO".equalsIgnoreCase(estado)
            && !"COMPLETADO".equalsIgnoreCase(estado)
            && !"ACEPTADO".equalsIgnoreCase(estado)) {
            throw new RuntimeException("El servicio debe estar completado para calificar");
        }

        if (calificacionRepository.existsByIdServicioAndRoleCalificador(request.getIdServicio(), Role.DOMICILIARIO.name())) {
            throw new RuntimeException("Este servicio ya ha sido calificado por el domiciliario");
        }

        if (servicio.getIdCliente() == null) {
            throw new RuntimeException("Este servicio no tiene un cliente asignado");
        }

        if (request.getIdCliente() != null && !servicio.getIdCliente().equals(request.getIdCliente().longValue())) {
            throw new RuntimeException("El cliente indicado no coincide con el servicio");
        }

        Calificacion calificacion = Calificacion.builder()
                .idServicio(request.getIdServicio())
                .idCliente(servicio.getIdCliente().intValue())
                .idDomiciliario(idDomiciliario)
                .puntuacion(request.getPuntuacion())
                .comentario(request.getComentario())
                .roleCalificador(Role.DOMICILIARIO.name())
                .build();

        calificacion = calificacionRepository.save(calificacion);
        return toResponse(calificacion);
    }

    public CalificacionResponse getCalificacion(Long idServicio, Integer userId, Role role) {
        String roleName = role.name();
        Calificacion calificacion = calificacionRepository.findByIdServicioAndRoleCalificador(idServicio, roleName)
                .orElseThrow(() -> new RuntimeException("Calificación no encontrada"));

        if (!calificacion.getIdCliente().equals(userId) &&
            !calificacion.getIdDomiciliario().equals(userId)) {
            throw new RuntimeException("No tienes acceso a esta calificación");
        }

        return toResponse(calificacion);
    }

    private void updateDomiciliarioRating(Integer idDomiciliario) {
        List<Calificacion> calificaciones = calificacionRepository.findByIdDomiciliarioAndRoleCalificador(
            idDomiciliario,
            Role.CLIENT.name()
        );

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

    public List<ServicioPendienteDTO> getPendientes(Integer userId, Role role) {
        List<String> estados = List.of("ENTREGADO", "COMPLETADO", "ACEPTADO");
        List<Servicio> servicios;

        if (role == Role.CLIENT) {
            servicios = servicioRepository.findByIdClienteAndEstadoInOrderByFechaSolicitudDesc(
                userId.longValue(), estados);
        } else if (role == Role.DOMICILIARIO) {
            servicios = servicioRepository.findByIdDomiciliarioAndEstadoInOrderByFechaSolicitudDesc(
                userId.longValue(), estados);
        } else {
            throw new RuntimeException("Rol no válido para calificar");
        }

        return servicios.stream()
            .filter(s -> !calificacionRepository.existsByIdServicioAndRoleCalificador(
                s.getIdServicio(), role.name()))
            .map(this::toServicioPendienteDTO)
            .collect(Collectors.toList());
    }

    private ServicioPendienteDTO toServicioPendienteDTO(Servicio servicio) {
        return ServicioPendienteDTO.builder()
                .idServicio(servicio.getIdServicio())
                .direccionOrigen(servicio.getDireccionOrigen())
                .direccionDestino(servicio.getDireccionDestino())
                .descripcion(servicio.getDescripcion())
                .estado(servicio.getEstado())
                .fechaSolicitud(servicio.getFechaSolicitud())
                .build();
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
                .roleCalificador(calificacion.getRoleCalificador())
                .build();
    }
}
