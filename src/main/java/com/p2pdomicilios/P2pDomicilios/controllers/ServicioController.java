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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/orders") // Ruta base para endpoints
public class ServicioController {

    private static final Logger log = LoggerFactory.getLogger(ServicioController.class);

    @Autowired
    private ServicioService service;

    @PostMapping("/create")
    public ResponseEntity<Servicio> create(@RequestBody Servicio servicio) {
        log.info("POST /api/orders/create - tarifa recibida: {}", servicio.getTarifa());
        log.debug("POST /api/orders/create - payload: {}", servicio);
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

    @PostMapping("/{id}/counteroffer")
    public ResponseEntity<Servicio> counterOffer(
            @PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, Object> body,
            @RequestParam(required = false) Double monto) {
        Double montoFinal = monto;
        if (montoFinal == null && body != null && body.containsKey("monto")) {
            Object m = body.get("monto");
            if (m instanceof Number)
                montoFinal = ((Number) m).doubleValue();
            else if (m instanceof String)
                montoFinal = Double.parseDouble((String) m);
        }

        if (montoFinal == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debes enviar monto en el body JSON {\"monto\": 7000} o como query param monto=7000");
        }

        Servicio s = service.obtenerEstado(id);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        User u = (User) principal;

        boolean isClient = (u.getRole() == Role.CLIENT && s.getIdCliente() != null
                && s.getIdCliente().equals(u.getId().longValue()));
        boolean isDomic = u.getRole() == Role.DOMICILIARIO;
        if (!isClient && !isDomic)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo cliente o domiciliario pueden hacer contraofertas");

        if (isDomic && !"CREADO".equals(s.getEstado()) && !"OFERTA_EN_CURSO".equals(s.getEstado())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El servicio no está disponible para contraoferta");
        }

        String proposer = isClient ? "CLIENTE" : "DOMICILIARIO";
        Servicio actualizado = service.contraOferta(id, montoFinal, proposer);
        return ResponseEntity.ok(actualizado);
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<Servicio> accept(@PathVariable Long id) {
        Servicio s = service.obtenerEstado(id);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        User u = (User) principal;

        if (u.getRole() != Role.DOMICILIARIO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo domiciliarios pueden aceptar servicios");
        }

        if (!"CREADO".equals(s.getEstado()) && !"OFERTA_EN_CURSO".equals(s.getEstado())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El servicio no está disponible para aceptar");
        }

        Servicio actualizado = service.aceptarServicio(id, u.getId().longValue());
        return ResponseEntity.ok(actualizado);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Servicio> reject(@PathVariable Long id) {
        Servicio s = service.obtenerEstado(id);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        User u = (User) principal;

        if (u.getRole() != Role.DOMICILIARIO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo domiciliarios pueden rechazar servicios");
        }

        if (!"CREADO".equals(s.getEstado()) && !"OFERTA_EN_CURSO".equals(s.getEstado())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El servicio no está disponible para rechazar");
        }

        Servicio actualizado = service.rechazarServicio(id, u.getId().longValue());
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<java.util.Map<String, Object>> status(@PathVariable Long id) {
        Servicio s;
        try {
            s = service.obtenerEstado(id);
        } catch (RuntimeException e) {
            // Si no existe servicio con ese id, intentamos obtener el último creado por
            // cliente (id como idCliente)
            s = service.obtenerUltimoPorCliente(id);
        }
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        User u = (User) principal;

        boolean isClient = (u.getRole() == Role.CLIENT
                && s.getIdCliente() != null
                && s.getIdCliente().equals(u.getId().longValue()));

        boolean isDomic = false;

        if (u.getRole() == Role.DOMICILIARIO) {

            // Permitir polling mientras el pedido sigue abierto
            if ("CREADO".equals(s.getEstado())
                    || "OFERTA_EN_CURSO".equals(s.getEstado())) {
                isDomic = true;
            }

            // Si ya fue asignado, solo el domiciliario asignado puede verlo
            if (s.getIdDomiciliario() != null) {
                isDomic = s.getIdDomiciliario().equals(u.getId().longValue());
            }
        }

        if (!isClient && !isDomic) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permiso para ver el estado de este servicio");
        }

        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("estado", s.getEstado());
        resp.put("oferta_actual", s.getOfertaActual());
        resp.put("tarifa", s.getTarifa());
        resp.put("ultima_oferta_por", s.getUltimaOfertaPor());
        resp.put("tiempo_estimado", s.getTiempoEstimado());
        return ResponseEntity.ok(resp);
    }
}
