package com.p2pdomicilios.P2pDomicilios.controllers;

import com.p2pdomicilios.P2pDomicilios.dto.ChatMessageRequest;
import com.p2pdomicilios.P2pDomicilios.dto.MensajeDTO;
import com.p2pdomicilios.P2pDomicilios.entities.User;
import com.p2pdomicilios.P2pDomicilios.repositories.UserRepository;
import com.p2pdomicilios.P2pDomicilios.services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;

    @MessageMapping("/chat.send/{idServicio}")
    public void sendMessage(
            @DestinationVariable Long idServicio,
            @Payload ChatMessageRequest request,
            Principal principal
    ) {
        try {
            String username = principal.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            MensajeDTO mensaje = chatService.saveMessage(idServicio, user.getId(), request.getContenido());

            messagingTemplate.convertAndSend("/topic/chat/" + idServicio, mensaje);
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
