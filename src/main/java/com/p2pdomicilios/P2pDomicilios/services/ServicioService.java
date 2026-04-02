package com.p2pdomicilios.P2pDomicilios.services;

import com.p2pdomicilios.P2pDomicilios.entities.Servicio;
import com.p2pdomicilios.P2pDomicilios.repositories.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServicioService {

    @Autowired
    private ServicioRepository repository;

    public Servicio crearPedido(Servicio nuevoServicio) {
        // Aquí podrías validar disponibilidad (RF6) antes de guardar [cite: 61]
        return repository.save(nuevoServicio);
    }
}