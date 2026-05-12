package com.p2pdomicilios.P2pDomicilios.controllers;

import com.p2pdomicilios.P2pDomicilios.entities.Servicio;
import com.p2pdomicilios.P2pDomicilios.entities.User;
import com.p2pdomicilios.P2pDomicilios.enums.Role;
import com.p2pdomicilios.P2pDomicilios.services.ServicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/orders") // Ruta base para endpoints 
public class ServicioController {

    @Autowired
    private ServicioService service;

    @PostMapping("/create")
    public ResponseEntity<Servicio> create(@RequestBody Servicio servicio) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            User u = (User) principal;
            if (u.getRole() == Role.CLIENT) {
                servicio.setIdCliente(u.getId().longValue());
            }
        }
        Servicio nuevo = service.crearServicio(servicio);
        return ResponseEntity.ok(nuevo);
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<Servicio> accept(@PathVariable Long id) {
        Servicio s = service.obtenerEstado(id);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        User u = (User) principal;
        if (u.getRole() != Role.DOMICILIARIO) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo domiciliarios pueden aceptar servicios");
        if (s.getIdDomiciliario() == null || !s.getIdDomiciliario().equals(u.getId().longValue())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No estás asignado a este servicio");
        }
        Servicio actualizado = service.aceptarServicio(id);
        return ResponseEntity.ok(actualizado);
    }

    @PostMapping("/{id}/assign/{domiciliarioUserId}")
    public ResponseEntity<?> assign(@PathVariable Long id, @PathVariable Long domiciliarioUserId) {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!(principal instanceof User)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autenticado");

            User u = (User) principal;
            if (u.getRole() != Role.CLIENT) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(java.util.Map.of("error", "Solo clientes pueden asignar. Tu rol es: " + u.getRole()));
            }

            Servicio s = service.obtenerEstado(id);
            Long idClienteDelServicio = s.getIdCliente();
            Long idClienteAutenticado = u.getId().longValue();

            if (idClienteDelServicio == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(java.util.Map.of("error", "El servicio no tiene cliente asignado. idCliente es null"));
            }

            if (!idClienteDelServicio.equals(idClienteAutenticado)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(java.util.Map.of(
                        "error", "No eres el dueño de este servicio",
                        "idClienteDelServicio", idClienteDelServicio,
                        "idClienteAutenticado", idClienteAutenticado
                    ));
            }

            Servicio actualizado = service.asignarDomiciliario(id, domiciliarioUserId, idClienteAutenticado);
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(java.util.Map.of("error", "Exception: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Servicio> reject(@PathVariable Long id) {
        Servicio s = service.obtenerEstado(id);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        User u = (User) principal;
        if (u.getRole() != Role.DOMICILIARIO) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo domiciliarios pueden rechazar servicios");
        if (s.getIdDomiciliario() == null || !s.getIdDomiciliario().equals(u.getId().longValue())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No estás asignado a este servicio");
        }
        Servicio actualizado = service.rechazarServicio(id);
        return ResponseEntity.ok(actualizado);
    }

    @PostMapping("/{id}/counteroffer")
    public ResponseEntity<Servicio> counterOffer(@PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        Double monto = null;
        if (body.containsKey("monto")) {
            Object m = body.get("monto");
            if (m instanceof Number) monto = ((Number) m).doubleValue();
            else if (m instanceof String) monto = Double.parseDouble((String) m);
        }

        Servicio s = service.obtenerEstado(id);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        User u = (User) principal;

        boolean isClient = (u.getRole() == Role.CLIENT && s.getIdCliente() != null && s.getIdCliente().equals(u.getId().longValue()));
        boolean isDomic = (u.getRole() == Role.DOMICILIARIO && s.getIdDomiciliario() != null && s.getIdDomiciliario().equals(u.getId().longValue()));
        if (!isClient && !isDomic) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo cliente o domiciliario asignado pueden hacer contraofertas");

        String proposer = isClient ? "CLIENTE" : "DOMICILIARIO";
        Servicio actualizado = service.contraOferta(id, monto, proposer);
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<java.util.Map<String, Object>> status(@PathVariable Long id) {
        Servicio s = service.obtenerEstado(id);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        User u = (User) principal;

        boolean isClient = (u.getRole() == Role.CLIENT && s.getIdCliente() != null && s.getIdCliente().equals(u.getId().longValue()));
        boolean isDomic = (u.getRole() == Role.DOMICILIARIO && s.getIdDomiciliario() != null && s.getIdDomiciliario().equals(u.getId().longValue()));
        if (!isClient && !isDomic) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver el estado de este servicio");

        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("estado", s.getEstado());
        resp.put("oferta_actual", s.getOfertaActual());
        resp.put("tarifa", s.getTarifa());
        resp.put("ultima_oferta_por", s.getUltimaOfertaPor());
        return ResponseEntity.ok(resp);
    }
}
