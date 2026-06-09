package com.p2pdomicilios.P2pDomicilios.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUserStatusUpdateRequest {
    @NotNull(message = "La acción es requerida")
    private AccountAction action;

    public enum AccountAction {
        ACTIVAR,
        DESACTIVAR,
        SUSPENDER
    }
}
