package com.p2pdomicilios.P2pDomicilios.services;

import com.p2pdomicilios.P2pDomicilios.entities.Domiciliario;
import com.p2pdomicilios.P2pDomicilios.repositories.DomiciliarioRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DomiciliarioService {

    private static final double DEFAULT_RADIUS_KM = 3.0;

    private final DomiciliarioRepository repository;

    public DomiciliarioService(DomiciliarioRepository repository) {
        this.repository = repository;
    }

    public List<Domiciliario> findNearby(double lat, double lon, Double radiusKm) {
        double finalRadiusKm = (radiusKm == null || radiusKm <= 0) ? DEFAULT_RADIUS_KM : radiusKm;
        double radiusMeters = finalRadiusKm * 1000.0;
        return repository.findNearbyAvailableAndVerified(lat, lon, radiusMeters);
    }
}
