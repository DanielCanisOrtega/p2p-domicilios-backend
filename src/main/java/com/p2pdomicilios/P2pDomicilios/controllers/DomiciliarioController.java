package com.p2pdomicilios.P2pDomicilios.controllers;

import com.p2pdomicilios.P2pDomicilios.dto.DomiciliarioDTO;
import com.p2pdomicilios.P2pDomicilios.dto.DomiciliarioLocationRequest;
import com.p2pdomicilios.P2pDomicilios.dto.TrackingUpdate;
import com.p2pdomicilios.P2pDomicilios.entities.Domiciliario;
import com.p2pdomicilios.P2pDomicilios.entities.Servicio;
import com.p2pdomicilios.P2pDomicilios.entities.User;
import com.p2pdomicilios.P2pDomicilios.enums.Role;
import com.p2pdomicilios.P2pDomicilios.services.DomiciliarioService;
import com.p2pdomicilios.P2pDomicilios.services.ServicioService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@RestController
@RequestMapping("/drivers")
public class DomiciliarioController {

    private final DomiciliarioService service;
    private final ServicioService servicioService;
    private final SimpMessagingTemplate messagingTemplate;

    public DomiciliarioController(
        DomiciliarioService service,
        ServicioService servicioService,
        SimpMessagingTemplate messagingTemplate
    ) {
        this.service = service;
        this.servicioService = servicioService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<DomiciliarioDTO>> nearby(
        @RequestParam double lat,
        @RequestParam double lon,
        @RequestParam(required = false) Double radiusKm
    ) {
        return ResponseEntity.ok(service.findNearbyDTO(lat, lon, radiusKm));
    }

    @GetMapping("/orders/pending")
    public ResponseEntity<List<Servicio>> pendingOrders() {
        User user = currentDomiciliario();
        if (!service.isActiveDomiciliario(user.getId())) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(servicioService.listarServiciosPendientes());
    }

    @PostMapping("/orders/{id}/accept")
    public ResponseEntity<Servicio> accept(@PathVariable Long id) {
        User user = currentDomiciliario();
        service.requireActiveDomiciliario(user.getId());
        return ResponseEntity.ok(servicioService.aceptarServicio(id, user.getId().longValue()));
    }

    @PostMapping("/orders/{id}/reject")
    public ResponseEntity<Servicio> reject(@PathVariable Long id) {
        User user = currentDomiciliario();
        service.requireActiveDomiciliario(user.getId());
        return ResponseEntity.ok(servicioService.rechazarServicio(id, user.getId().longValue()));
    }

    @PostMapping("/location")
    public ResponseEntity<DomiciliarioDTO> updateLocation(@Valid @RequestBody DomiciliarioLocationRequest request) {
        User user = currentDomiciliario();
        Domiciliario updated = service.updateLocation(
            user,
            request.getLatitud(),
            request.getLongitud(),
            request.getDisponible()
        );

        if (request.getIdServicio() != null) {
            Servicio servicio = servicioService.obtenerEstado(request.getIdServicio());
            if (servicio.getIdDomiciliario() == null
                || !servicio.getIdDomiciliario().equals(user.getId().longValue())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes actualizar tracking de este servicio");
            }

            servicio = servicioService.actualizarTiempoEstimado(
                request.getIdServicio(),
                request.getLatitud(),
                request.getLongitud()
            );
            TrackingUpdate update = TrackingUpdate.builder()
                .idServicio(servicio.getIdServicio())
                .latitud(request.getLatitud())
                .longitud(request.getLongitud())
                .tiempoEstimado(servicio.getTiempoEstimado())
                .build();
            messagingTemplate.convertAndSend("/topic/servicio/" + servicio.getIdServicio(), update);
        }

        return ResponseEntity.ok(DomiciliarioDTO.fromEntity(updated));
    }

    @GetMapping("/orders/{id}/tracking")
    public ResponseEntity<TrackingUpdate> tracking(@PathVariable Long id) {
        Servicio servicio = servicioService.obtenerEstado(id);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User u)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        boolean isClient = (u.getRole() == Role.CLIENT
            && servicio.getIdCliente() != null
            && servicio.getIdCliente().equals(u.getId().longValue()));
        boolean isDomic = (u.getRole() == Role.DOMICILIARIO
            && servicio.getIdDomiciliario() != null
            && servicio.getIdDomiciliario().equals(u.getId().longValue()));
        if (!isClient && !isDomic) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver el tracking de este servicio");
        }

        if (servicio.getIdDomiciliario() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio sin domiciliario asignado");
        }

        Domiciliario domiciliario = service.findByUserId(servicio.getIdDomiciliario().intValue())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Domiciliario no encontrado"));

        TrackingUpdate update = TrackingUpdate.builder()
            .idServicio(servicio.getIdServicio())
            .latitud(domiciliario.getLatitud())
            .longitud(domiciliario.getLongitud())
            .tiempoEstimado(servicio.getTiempoEstimado())
            .build();
        return ResponseEntity.ok(update);
    }

    private User currentDomiciliario() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autenticado");
        }
        if (user.getRole() != Role.DOMICILIARIO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo domiciliarios pueden usar este recurso");
        }
        return user;
    }
}
