package com.p2pdomicilios.P2pDomicilios.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MensajeDTO {
    private Long idMensaje;
    private Long idChat;
    private Integer idUsuario;
    private String contenido;
    private LocalDateTime fechaEnvio;
    private String nombreUsuario;
}
