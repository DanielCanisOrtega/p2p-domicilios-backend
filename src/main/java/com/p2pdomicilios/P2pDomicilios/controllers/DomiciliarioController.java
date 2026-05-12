package com.p2pdomicilios.P2pDomicilios.controllers;

import com.p2pdomicilios.P2pDomicilios.dto.DomiciliarioDTO;
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

@RestController
@RequestMapping("/drivers")
public class DomiciliarioController {

    private final DomiciliarioService service;
    private final ServicioService servicioService;

    public DomiciliarioController(DomiciliarioService service, ServicioService servicioService) {
        this.service = service;
        this.servicioService = servicioService;
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
