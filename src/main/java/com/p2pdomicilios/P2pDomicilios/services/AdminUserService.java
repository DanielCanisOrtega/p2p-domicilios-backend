package com.p2pdomicilios.P2pDomicilios.services;

import com.p2pdomicilios.P2pDomicilios.dto.AdminUserListItemDTO;
import com.p2pdomicilios.P2pDomicilios.dto.AdminUserStatusUpdateRequest;
import com.p2pdomicilios.P2pDomicilios.entities.User;
import com.p2pdomicilios.P2pDomicilios.enums.Role;
import com.p2pdomicilios.P2pDomicilios.repositories.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    public List<AdminUserListItemDTO> listUsers(Role role, String estado, Boolean enabled, String q) {
        String normalizedQ = normalizeBlank(q);
        String normalizedEstado = normalizeEstado(estado);
        return userRepository.searchForAdmin(role != null ? role.name() : null, normalizedEstado, enabled, normalizedQ)
            .stream()
            .map(AdminUserListItemDTO::fromEntity)
            .toList();
    }

    @Transactional
    public AdminUserListItemDTO updateAccountStatus(Integer userId, AdminUserStatusUpdateRequest.AccountAction action) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        switch (action) {
            case ACTIVAR -> {
                user.setEnabled(true);
                user.setEstado("ACTIVO");
            }
            case DESACTIVAR -> {
                user.setEnabled(false);
                user.setEstado("INACTIVO");
            }
            case SUSPENDER -> {
                user.setEnabled(false);
                user.setEstado("SUSPENDIDO");
            }
        }

        return AdminUserListItemDTO.fromEntity(userRepository.save(user));
    }

    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeEstado(String value) {
        String normalized = normalizeBlank(value);
        if (normalized == null) {
            return null;
        }
        return normalized.toUpperCase();
    }
}
