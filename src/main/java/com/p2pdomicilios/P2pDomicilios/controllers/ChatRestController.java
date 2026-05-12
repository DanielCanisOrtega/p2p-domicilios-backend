package com.p2pdomicilios.P2pDomicilios.controllers;

import com.p2pdomicilios.P2pDomicilios.dto.MensajeDTO;
import com.p2pdomicilios.P2pDomicilios.entities.Chat;
import com.p2pdomicilios.P2pDomicilios.entities.User;
import com.p2pdomicilios.P2pDomicilios.services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/{idServicio}/messages")
    public ResponseEntity<List<MensajeDTO>> getMessages(@PathVariable Long idServicio) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        User user = (User) principal;
        List<MensajeDTO> mensajes = chatService.getMessages(idServicio, user.getId());
        return ResponseEntity.ok(mensajes);
    }

    @PostMapping("/{idServicio}/init")
    public ResponseEntity<Map<String, Object>> initChat(@PathVariable Long idServicio) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        User user = (User) principal;
        Chat chat = chatService.getOrCreateChat(idServicio);

        Map<String, Object> response = new HashMap<>();
        response.put("idChat", chat.getIdChat());
        response.put("idServicio", chat.getIdServicio());

        return ResponseEntity.ok(response);
    }
}
