package com.p2pdomicilios.P2pDomicilios.dto;

import com.p2pdomicilios.P2pDomicilios.entities.User;
import com.p2pdomicilios.P2pDomicilios.enums.Role;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminUserListItemDTO {
    private Integer id;
    private String username;
    private String email;
    private Role role;
    private String nombre;
    private String telefono;
    private String numeroDocumento;
    private String estado;
    private Boolean enabled;
    private LocalDateTime fechaRegistro;

    public static AdminUserListItemDTO fromEntity(User user) {
        return AdminUserListItemDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole())
            .nombre(user.getNombre())
            .telefono(user.getTelefono())
            .numeroDocumento(user.getNumeroDocumento())
            .estado(user.getEstado())
            .enabled(user.getEnabled())
            .fechaRegistro(user.getFechaRegistro())
            .build();
    }
}
