package com.p2pdomicilios.P2pDomicilios.services;

import com.p2pdomicilios.P2pDomicilios.dto.DomiciliarioDTO;
import com.p2pdomicilios.P2pDomicilios.entities.Domiciliario;
import com.p2pdomicilios.P2pDomicilios.repositories.DomiciliarioRepository;
import java.util.List;
import java.util.stream.Collectors;
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

    public List<DomiciliarioDTO> findNearbyDTO(double lat, double lon, Double radiusKm) {
        return findNearby(lat, lon, radiusKm).stream()
                .map(domiciliario -> {
                    DomiciliarioDTO dto = DomiciliarioDTO.fromEntity(domiciliario);
                    // Calcular distancia en metros
                    double distance = calculateDistance(lat, lon, domiciliario.getLatitud(), domiciliario.getLongitud());
                    dto.setDistancia(distance);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Radio de la Tierra en metros
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distancia en metros
    }
}
