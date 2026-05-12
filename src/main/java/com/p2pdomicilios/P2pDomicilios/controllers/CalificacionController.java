package com.p2pdomicilios.P2pDomicilios.controllers;

import com.p2pdomicilios.P2pDomicilios.dto.CalificacionRequest;
import com.p2pdomicilios.P2pDomicilios.dto.CalificacionResponse;
import com.p2pdomicilios.P2pDomicilios.entities.User;
import com.p2pdomicilios.P2pDomicilios.enums.Role;
import com.p2pdomicilios.P2pDomicilios.services.CalificacionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/calificaciones")
public class CalificacionController {

    @Autowired
    private CalificacionService calificacionService;

    @PostMapping
    public ResponseEntity<CalificacionResponse> createCalificacion(
            @Valid @RequestBody CalificacionRequest request
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        User user = (User) principal;
        if (user.getRole() != Role.CLIENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo clientes pueden calificar");
        }

        CalificacionResponse response = calificacionService.createCalificacion(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/servicio/{idServicio}")
    public ResponseEntity<CalificacionResponse> getCalificacion(@PathVariable Long idServicio) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        User user = (User) principal;
        CalificacionResponse response = calificacionService.getCalificacion(idServicio, user.getId());
        return ResponseEntity.ok(response);
    }
}
