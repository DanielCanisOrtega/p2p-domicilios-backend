package com.p2pdomicilios.P2pDomicilios.controllers;

import com.p2pdomicilios.P2pDomicilios.dto.AdminDomiciliarioVerificationDTO;
import com.p2pdomicilios.P2pDomicilios.services.AdminDomiciliarioService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/drivers")
@RequiredArgsConstructor
public class AdminDomiciliarioController {

    private final AdminDomiciliarioService adminDomiciliarioService;

    @GetMapping("/pending")
    public ResponseEntity<List<AdminDomiciliarioVerificationDTO>> pending(
        @RequestParam(required = false, defaultValue = "false") Boolean verificado
    ) {
        return ResponseEntity.ok(adminDomiciliarioService.listByVerification(verificado));
    }

    @GetMapping("/{userId}/documents")
    public ResponseEntity<AdminDomiciliarioVerificationDTO> documents(@PathVariable Integer userId) {
        return ResponseEntity.ok(adminDomiciliarioService.getDocumentsSummary(userId));
    }

    @PostMapping("/{userId}/approve")
    public ResponseEntity<AdminDomiciliarioVerificationDTO> approve(@PathVariable Integer userId) {
        return ResponseEntity.ok(adminDomiciliarioService.approve(userId));
    }

    @PostMapping("/{userId}/reject")
    public ResponseEntity<AdminDomiciliarioVerificationDTO> reject(@PathVariable Integer userId) {
        return ResponseEntity.ok(adminDomiciliarioService.reject(userId));
    }
}
