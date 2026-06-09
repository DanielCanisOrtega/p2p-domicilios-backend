package com.p2pdomicilios.P2pDomicilios.controllers;

import com.p2pdomicilios.P2pDomicilios.dto.IncidenciaRequest;
import com.p2pdomicilios.P2pDomicilios.dto.IncidenciaResponse;
import com.p2pdomicilios.P2pDomicilios.entities.Servicio;
import com.p2pdomicilios.P2pDomicilios.entities.User;
import com.p2pdomicilios.P2pDomicilios.enums.Role;
import com.p2pdomicilios.P2pDomicilios.repositories.ServicioRepository;
import com.p2pdomicilios.P2pDomicilios.services.IncidenciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/incidents")
@RequiredArgsConstructor
public class IncidenciaController {

    private final IncidenciaService incidenciaService;
    private final ServicioRepository servicioRepository;

    @PostMapping
    public ResponseEntity<IncidenciaResponse> create(@Valid @RequestBody IncidenciaRequest request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Servicio servicio = servicioRepository.findById(request.getIdServicio())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio no encontrado"));

        Integer idCliente;
        Integer idDomiciliario;

        if (user.getRole() == Role.CLIENT) {
            if (!servicio.getIdCliente().equals(user.getId().longValue())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No eres el cliente de este servicio");
            }
            idCliente = user.getId();
            idDomiciliario = servicio.getIdDomiciliario() != null ? servicio.getIdDomiciliario().intValue() : null;
        } else if (user.getRole() == Role.DOMICILIARIO) {
            if (servicio.getIdDomiciliario() == null || !servicio.getIdDomiciliario().equals(user.getId().longValue())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No eres el domiciliario de este servicio");
            }
            idCliente = servicio.getIdCliente().intValue();
            idDomiciliario = user.getId();
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rol no permitido para crear incidencias");
        }

        if (idDomiciliario == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El servicio no tiene un domiciliario asignado");
        }

        IncidenciaResponse response = incidenciaService.create(request, idCliente, idDomiciliario);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
