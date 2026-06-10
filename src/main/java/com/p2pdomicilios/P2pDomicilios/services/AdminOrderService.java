package com.p2pdomicilios.P2pDomicilios.services;

import com.p2pdomicilios.P2pDomicilios.dto.AdminOrderDetailDTO;
import com.p2pdomicilios.P2pDomicilios.dto.AdminOrderListItemDTO;
import com.p2pdomicilios.P2pDomicilios.entities.Servicio;
import com.p2pdomicilios.P2pDomicilios.entities.User;
import com.p2pdomicilios.P2pDomicilios.repositories.ServicioRepository;
import com.p2pdomicilios.P2pDomicilios.repositories.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrderService {

    private final ServicioRepository servicioRepository;
    private final UserRepository userRepository;

    public List<AdminOrderListItemDTO> listOrders(String estado, LocalDate fechaDesde, LocalDate fechaHasta) {
        validateDateRange(fechaDesde, fechaHasta);

        List<Servicio> servicios = servicioRepository.findAll()
            .stream()
            .filter(s -> {
                if (StringUtils.hasText(estado) && (s.getEstado() == null || !s.getEstado().equalsIgnoreCase(estado.trim()))) {
                    return false;
                }
                if (fechaDesde != null) {
                    LocalDateTime inicio = fechaDesde.atStartOfDay();
                    if (s.getFechaSolicitud() == null || s.getFechaSolicitud().isBefore(inicio)) {
                        return false;
                    }
                }
                if (fechaHasta != null) {
                    LocalDateTime fin = fechaHasta.atTime(LocalTime.MAX);
                    if (s.getFechaSolicitud() == null || s.getFechaSolicitud().isAfter(fin)) {
                        return false;
                    }
                }
                return true;
            })
            .sorted((a, b) -> b.getFechaSolicitud().compareTo(a.getFechaSolicitud()))
            .toList();

        Map<Long, User> usersById = loadUsersFor(servicios);
        return servicios.stream()
            .map(servicio -> AdminOrderListItemDTO.fromEntity(
                servicio,
                usersById.get(servicio.getIdCliente()),
                usersById.get(servicio.getIdDomiciliario())
            ))
            .toList();
    }

    public AdminOrderDetailDTO getOrderDetail(Long id) {
        Servicio servicio = servicioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        Map<Long, User> usersById = loadUsersFor(List.of(servicio));
        return AdminOrderDetailDTO.fromEntity(
            servicio,
            usersById.get(servicio.getIdCliente()),
            usersById.get(servicio.getIdDomiciliario())
        );
    }

    private void validateDateRange(LocalDate fechaDesde, LocalDate fechaHasta) {
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser mayor que la fecha final");
        }
    }

    

    private Map<Long, User> loadUsersFor(List<Servicio> servicios) {
        Set<Integer> userIds = servicios.stream()
            .flatMap(servicio -> java.util.stream.Stream.of(servicio.getIdCliente(), servicio.getIdDomiciliario()))
            .filter(java.util.Objects::nonNull)
            .map(Long::intValue)
            .collect(Collectors.toSet());

        if (userIds.isEmpty()) {
            return Map.of();
        }

        return userRepository.findAllById(userIds)
            .stream()
            .collect(Collectors.toMap(user -> user.getId().longValue(), user -> user));
    }
}
