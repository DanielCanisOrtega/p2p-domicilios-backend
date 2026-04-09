package com.p2pdomicilios.P2pDomicilios.controllers;

import com.p2pdomicilios.P2pDomicilios.entities.Servicio;
import com.p2pdomicilios.P2pDomicilios.services.ServicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders") // Ruta base para endpoints 
public class ServicioController {

    @Autowired
    private ServicioService service;

    @PostMapping("/create")
public ResponseEntity<Servicio> create(@RequestBody Servicio servicio) {
    Servicio nuevo = service.crearServicio(servicio); 
    return ResponseEntity.ok(nuevo);
}
}
