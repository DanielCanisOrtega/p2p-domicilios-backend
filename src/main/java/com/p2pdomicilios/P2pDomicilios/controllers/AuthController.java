package com.p2pdomicilios.P2pDomicilios.controllers;

import com.p2pdomicilios.P2pDomicilios.dto.AuthResponse;
import com.p2pdomicilios.P2pDomicilios.dto.LoginRequest;
import com.p2pdomicilios.P2pDomicilios.dto.RegisterRequest;
import com.p2pdomicilios.P2pDomicilios.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
