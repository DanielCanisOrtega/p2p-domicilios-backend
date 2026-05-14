package com.p2pdomicilios.P2pDomicilios.services;

import com.p2pdomicilios.P2pDomicilios.entities.Domiciliario;
import com.p2pdomicilios.P2pDomicilios.entities.Servicio;
import com.p2pdomicilios.P2pDomicilios.repositories.DomiciliarioRepository;
import com.p2pdomicilios.P2pDomicilios.repositories.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicioService {

    private static final double AVG_SPEED_KMH = 30.0;

    @Autowired
    private ServicioRepository repositorio;

    @Autowired
    private DomiciliarioRepository domiciliarioRepository;

    
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

        // 3. LÓGICA DE TARIFA
        // Si el cliente envió una `tarifa` válida, respetarla; si no, calcularla dinámicamente.
        Double tarifaProporcionada = servicio.getTarifa();
        if (tarifaProporcionada != null && tarifaProporcionada > 0) {
            servicio.setTarifa(Math.round(tarifaProporcionada * 100.0) / 100.0);
        } else {
            // Ejemplo: $3.000 (Base) + ($1.500 por cada kilómetro)
            double tarifaBase = 3000.0;
            double precioPorKm = 1500.0;
            double tarifaCalculada = tarifaBase + (distanciaKm * precioPorKm);
            // Redondeamos para que no queden decimales
            servicio.setTarifa(Math.round(tarifaCalculada * 100.0) / 100.0);
        }

        servicio.setOfertaActual(servicio.getTarifa());
        servicio.setUltimaOfertaPor(null);
        
        // 4. ESTADO INICIAL
        servicio.setEstado("CREADO");

        // 5. GUARDAR EN NEON DB
        return repositorio.save(servicio);
    }

    public Servicio obtenerEstado(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
    }

    public Servicio obtenerUltimoPorCliente(Long idCliente) {
        return repositorio.findTopByIdClienteOrderByFechaSolicitudDesc(idCliente)
                .orElseThrow(() -> new RuntimeException("No se encontró ningún servicio para el cliente"));
    }

    public List<Servicio> listarServiciosPendientes() {
        return repositorio.findTop3ByEstadoOrderByIdServicioDesc("CREADO");
    }

    public Servicio aceptarServicio(Long id, Long idUsuarioDomiciliario) {
        Servicio servicio = obtenerEstado(id);
        if (!"CREADO".equals(servicio.getEstado()) && !"OFERTA_EN_CURSO".equals(servicio.getEstado())) {
            throw new RuntimeException("El servicio no está disponible para aceptar");
        }

        servicio.setIdDomiciliario(idUsuarioDomiciliario);
        servicio.setEstado("ACEPTADO");
        servicio.setTarifa(servicio.getOfertaActual() != null ? servicio.getOfertaActual() : servicio.getTarifa());
        servicio.setTiempoEstimado(calcularTiempoEstimadoMinFromDomiciliario(servicio, idUsuarioDomiciliario));
        return repositorio.save(servicio);
    }

    public Servicio rechazarServicio(Long id, Long idUsuarioDomiciliario) {
        Servicio servicio = obtenerEstado(id);
        if (!"CREADO".equals(servicio.getEstado()) && !"OFERTA_EN_CURSO".equals(servicio.getEstado())) {
            throw new RuntimeException("El servicio no está disponible para rechazar");
        }

        // El rechazo saca el pedido de la cola pendiente para que pase al siguiente flujo.
        servicio.setEstado("RECHAZADO");
        return repositorio.save(servicio);
    }

    public Servicio contraOferta(Long id, Double monto, String proposer) {
        if (monto == null || monto <= 0) {
            throw new RuntimeException("El monto de la contraoferta debe ser mayor a cero");
        }

        Servicio servicio = obtenerEstado(id);

        if (servicio.getIdDomiciliario() == null && !"CREADO".equals(servicio.getEstado()) && !"OFERTA_EN_CURSO".equals(servicio.getEstado())) {
            throw new RuntimeException("El servicio no está disponible para contraoferta");
        }

        servicio.setOfertaActual(monto);
        servicio.setUltimaOfertaPor(proposer);
        servicio.setEstado("OFERTA_EN_CURSO");
        return repositorio.save(servicio);
    }

    public Servicio actualizarTiempoEstimado(Long idServicio, double latDomiciliario, double lonDomiciliario) {
        Servicio servicio = obtenerEstado(idServicio);

        if (servicio.getLatOrigen() == null || servicio.getLonOrigen() == null) {
            return servicio;
        }

        int minutos = calcularTiempoEstimadoMin(
            latDomiciliario,
            lonDomiciliario,
            servicio.getLatOrigen(),
            servicio.getLonOrigen()
        );
        servicio.setTiempoEstimado(minutos);
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

    private int calcularTiempoEstimadoMinFromDomiciliario(Servicio servicio, Long idUsuarioDomiciliario) {
        if (servicio.getLatOrigen() == null || servicio.getLonOrigen() == null) {
            return 0;
        }

        if (idUsuarioDomiciliario == null) {
            return 0;
        }

        Domiciliario domiciliario = domiciliarioRepository.findByUser_Id(idUsuarioDomiciliario.intValue())
                .orElse(null);
        if (domiciliario == null || domiciliario.getLatitud() == null || domiciliario.getLongitud() == null) {
            return 0;
        }

        return calcularTiempoEstimadoMin(
            domiciliario.getLatitud(),
            domiciliario.getLongitud(),
            servicio.getLatOrigen(),
            servicio.getLonOrigen()
        );
    }

    private int calcularTiempoEstimadoMin(double latOrigen, double lonOrigen, double latDestino, double lonDestino) {
        double distanciaKm = calcularDistancia(latOrigen, lonOrigen, latDestino, lonDestino);
        return calcularTiempoEstimadoMin(distanciaKm);
    }

    private int calcularTiempoEstimadoMin(double distanciaKm) {
        if (distanciaKm <= 0) {
            return 0;
        }

        double minutos = (distanciaKm / AVG_SPEED_KMH) * 60.0;
        return (int) Math.max(1, Math.round(minutos));
    }
}
