package com.p2pdomicilios.P2pDomicilios.controllers;

import com.p2pdomicilios.P2pDomicilios.entities.Domiciliario;
import com.p2pdomicilios.P2pDomicilios.services.DomiciliarioService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/drivers")
public class DomiciliarioController {

    private final DomiciliarioService service;

    public DomiciliarioController(DomiciliarioService service) {
        this.service = service;
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<Domiciliario>> nearby(
        @RequestParam double lat,
        @RequestParam double lon,
        @RequestParam(required = false) Double radiusKm
    ) {
        return ResponseEntity.ok(service.findNearby(lat, lon, radiusKm));
    }
}
