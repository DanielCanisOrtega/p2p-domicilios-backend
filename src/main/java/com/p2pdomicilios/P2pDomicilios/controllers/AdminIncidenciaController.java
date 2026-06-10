package com.p2pdomicilios.P2pDomicilios.controllers;

import com.p2pdomicilios.P2pDomicilios.dto.IncidenciaResponse;
import com.p2pdomicilios.P2pDomicilios.dto.IncidenciaStatusUpdateRequest;
import com.p2pdomicilios.P2pDomicilios.services.IncidenciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/incidents")
@RequiredArgsConstructor
public class AdminIncidenciaController {

    private final IncidenciaService incidenciaService;

    @GetMapping
    public ResponseEntity<List<IncidenciaResponse>> list() {
        return ResponseEntity.ok(incidenciaService.listAll());
    }

    @PostMapping("/{id}")
    public ResponseEntity<IncidenciaResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody IncidenciaStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(incidenciaService.updateStatus(id, request));
    }
}
