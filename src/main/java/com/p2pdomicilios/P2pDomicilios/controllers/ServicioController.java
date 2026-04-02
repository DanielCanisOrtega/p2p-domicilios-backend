package com.p2pdomicilios.P2pDomicilios.controllers;

import com.p2pdomicilios.P2pDomicilios.entities.Servicio;
import com.p2pdomicilios.P2pDomicilios.services.ServicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders") // Ruta base para tus endpoints [cite: 179]
public class ServicioController {

    @Autowired
    private ServicioService service;

    @PostMapping("/create") // RF6: Crear solicitud [cite: 58]
    public ResponseEntity<Servicio> crear(@RequestBody Servicio pedido) {
        return ResponseEntity.ok(service.crearPedido(pedido));
    }
}
