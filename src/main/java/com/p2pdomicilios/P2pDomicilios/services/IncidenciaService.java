package com.p2pdomicilios.P2pDomicilios.services;

import com.p2pdomicilios.P2pDomicilios.dto.IncidenciaRequest;
import com.p2pdomicilios.P2pDomicilios.dto.IncidenciaResponse;
import com.p2pdomicilios.P2pDomicilios.dto.IncidenciaStatusUpdateRequest;
import com.p2pdomicilios.P2pDomicilios.entities.Incidencia;
import com.p2pdomicilios.P2pDomicilios.repositories.IncidenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidenciaService {

    private final IncidenciaRepository incidenciaRepository;

    @Transactional
    public IncidenciaResponse create(IncidenciaRequest request, Integer idCliente, Integer idDomiciliario) {
        Incidencia incidencia = Incidencia.builder()
                .idCliente(idCliente)
                .idDomiciliario(idDomiciliario)
                .idServicio(request.getIdServicio())
                .descripcion(request.getDescripcion())
                .estado("ABIERTO")
                .fechaCreacion(LocalDateTime.now())
                .build();
        incidencia = incidenciaRepository.save(incidencia);
        return toResponse(incidencia);
    }

    public List<IncidenciaResponse> listAll() {
        return incidenciaRepository.findAllByOrderByIdDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public IncidenciaResponse updateStatus(Long id, IncidenciaStatusUpdateRequest request) {
        Incidencia incidencia = incidenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada"));
        incidencia.setEstado(request.getEstado());
        incidencia.setFechaActualizacion(LocalDateTime.now());
        return toResponse(incidenciaRepository.save(incidencia));
    }

    private IncidenciaResponse toResponse(Incidencia incidencia) {
        return IncidenciaResponse.builder()
                .id(incidencia.getId())
                .idCliente(incidencia.getIdCliente())
                .idDomiciliario(incidencia.getIdDomiciliario())
                .idServicio(incidencia.getIdServicio())
                .descripcion(incidencia.getDescripcion())
                .estado(incidencia.getEstado())
                .fechaCreacion(incidencia.getFechaCreacion())
                .fechaActualizacion(incidencia.getFechaActualizacion())
                .build();
    }
}
