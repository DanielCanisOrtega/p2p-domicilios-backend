package com.p2pdomicilios.P2pDomicilios.services;

import com.p2pdomicilios.P2pDomicilios.entities.Servicio;
import com.p2pdomicilios.P2pDomicilios.repositories.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServicioService {

    @Autowired
    private ServicioRepository repositorio;

    
    public Servicio crearServicio(Servicio servicio) {
        
        
        if (servicio.getLatOrigen() == null || servicio.getLonOrigen() == null || 
            servicio.getLatDestino() == null || servicio.getLonDestino() == null) {
            
            servicio.setTarifa(3500.0); // Tarifa mínima por defecto en Cúcuta
            servicio.setEstado("ERROR_COORDENADAS");
            return repositorio.save(servicio);
        }

        // 2. CÁLCULO DE DISTANCIA (Haversine)
        double distanciaKm = calcularDistancia(
            servicio.getLatOrigen(), servicio.getLonOrigen(),
            servicio.getLatDestino(), servicio.getLonDestino()
        );

        // 3. LÓGICA DE TARIFA DINÁMICA
        // Ejemplo: $3.000 (Base) + ($1.500 por cada kilómetro)
        double tarifaBase = 3000.0;
        double precioPorKm = 1500.0;
        double tarifaCalculada = tarifaBase + (distanciaKm * precioPorKm);
        
        // Redondeamos para que no queden decimales 
        servicio.setTarifa(Math.round(tarifaCalculada * 100.0) / 100.0);
        
        // 4. ESTADO INICIAL
        servicio.setEstado("CREADO");

        // 5. GUARDAR EN NEON DB
        return repositorio.save(servicio);
    }


    private double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        double radioTierra = 6371; // Radio de la Tierra en kilómetros
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
                   
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return radioTierra * c; // Resultado en Kilómetros
    }
}