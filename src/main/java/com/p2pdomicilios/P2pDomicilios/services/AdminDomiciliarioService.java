package com.p2pdomicilios.P2pDomicilios.services;

import com.p2pdomicilios.P2pDomicilios.dto.AdminDomiciliarioVerificationDTO;
import com.p2pdomicilios.P2pDomicilios.entities.Domiciliario;
import com.p2pdomicilios.P2pDomicilios.entities.User;
import com.p2pdomicilios.P2pDomicilios.enums.Role;
import com.p2pdomicilios.P2pDomicilios.repositories.DomiciliarioRepository;
import com.p2pdomicilios.P2pDomicilios.repositories.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDomiciliarioService {

    private final DomiciliarioRepository domiciliarioRepository;
    private final UserRepository userRepository;

    public List<AdminDomiciliarioVerificationDTO> listByVerification(Boolean verificado) {
        return domiciliarioRepository.findForAdmin(verificado)
            .stream()
            .map(AdminDomiciliarioVerificationDTO::fromEntity)
            .toList();
    }

    public AdminDomiciliarioVerificationDTO getDocumentsSummary(Integer userId) {
        Domiciliario domiciliario = getDomiciliarioByUserId(userId);
        return AdminDomiciliarioVerificationDTO.fromEntity(domiciliario);
    }

    @Transactional
    public AdminDomiciliarioVerificationDTO approve(Integer userId) {
        Domiciliario domiciliario = getDomiciliarioByUserId(userId);
        domiciliario.setVerificado(true);
        Domiciliario saved = domiciliarioRepository.save(domiciliario);

        User user = saved.getUser();
        user.setEstado("ACTIVO");
        user.setEnabled(true);
        userRepository.save(user);

        return AdminDomiciliarioVerificationDTO.fromEntity(saved);
    }

    @Transactional
    public AdminDomiciliarioVerificationDTO reject(Integer userId) {
        Domiciliario domiciliario = getDomiciliarioByUserId(userId);
        domiciliario.setVerificado(false);
        domiciliario.setDisponible(false);
        Domiciliario saved = domiciliarioRepository.save(domiciliario);

        User user = saved.getUser();
        user.setEstado("RECHAZADO");
        user.setEnabled(false);
        userRepository.save(user);

        return AdminDomiciliarioVerificationDTO.fromEntity(saved);
    }

    private Domiciliario getDomiciliarioByUserId(Integer userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.getRole() != Role.DOMICILIARIO) {
            throw new RuntimeException("El usuario no tiene rol DOMICILIARIO");
        }

        return domiciliarioRepository.findByUser_Id(userId)
            .orElseThrow(() -> new RuntimeException("No existe registro de domiciliario para este usuario"));
    }
}
