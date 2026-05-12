package com.p2pdomicilios.P2pDomicilios.services;

import com.p2pdomicilios.P2pDomicilios.dto.MensajeDTO;
import com.p2pdomicilios.P2pDomicilios.entities.Chat;
import com.p2pdomicilios.P2pDomicilios.entities.Mensaje;
import com.p2pdomicilios.P2pDomicilios.entities.Servicio;
import com.p2pdomicilios.P2pDomicilios.entities.User;
import com.p2pdomicilios.P2pDomicilios.repositories.ChatRepository;
import com.p2pdomicilios.P2pDomicilios.repositories.MensajeRepository;
import com.p2pdomicilios.P2pDomicilios.repositories.ServicioRepository;
import com.p2pdomicilios.P2pDomicilios.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Chat getOrCreateChat(Long idServicio) {
        return chatRepository.findByIdServicio(idServicio)
                .orElseGet(() -> {
                    Servicio servicio = servicioRepository.findById(idServicio)
                            .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

                    Chat newChat = Chat.builder()
                            .idServicio(idServicio)
                            .build();
                    return chatRepository.save(newChat);
                });
    }

    public List<MensajeDTO> getMessages(Long idServicio, Integer userId) {
        Servicio servicio = servicioRepository.findById(idServicio)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        if (!servicio.getIdCliente().equals(userId.longValue()) &&
            (servicio.getIdDomiciliario() == null || !servicio.getIdDomiciliario().equals(userId.longValue()))) {
            throw new RuntimeException("No tienes acceso a este chat");
        }

        Chat chat = getOrCreateChat(idServicio);
        List<Mensaje> mensajes = mensajeRepository.findByIdChatOrderByFechaEnvioAsc(chat.getIdChat());

        return mensajes.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public MensajeDTO saveMessage(Long idServicio, Integer idUsuario, String contenido) {
        Servicio servicio = servicioRepository.findById(idServicio)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        if (!servicio.getIdCliente().equals(idUsuario.longValue()) &&
            (servicio.getIdDomiciliario() == null || !servicio.getIdDomiciliario().equals(idUsuario.longValue()))) {
            throw new RuntimeException("No tienes acceso a este chat");
        }

        Chat chat = getOrCreateChat(idServicio);

        Mensaje mensaje = Mensaje.builder()
                .idChat(chat.getIdChat())
                .idUsuario(idUsuario)
                .contenido(contenido)
                .build();

        mensaje = mensajeRepository.save(mensaje);
        return toDTO(mensaje);
    }

    private MensajeDTO toDTO(Mensaje mensaje) {
        User user = userRepository.findById(mensaje.getIdUsuario()).orElse(null);

        return MensajeDTO.builder()
                .idMensaje(mensaje.getIdMensaje())
                .idChat(mensaje.getIdChat())
                .idUsuario(mensaje.getIdUsuario())
                .contenido(mensaje.getContenido())
                .fechaEnvio(mensaje.getFechaEnvio())
                .nombreUsuario(user != null ? user.getNombre() : "Usuario")
                .build();
    }
}
