package com.example.banhcanh.controller;

import com.example.banhcanh.model.ChatMessage;
import com.example.banhcanh.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    @Autowired
    private ChatMessageRepository repo;

    @GetMapping("/{orderId}")
    public List<ChatMessage> getMessages(@PathVariable Long orderId) {
        return repo.findByOrderIdOrderByCreatedAtAsc(orderId);
    }

    @PostMapping("/{orderId}")
    public ResponseEntity<?> sendMessage(@PathVariable Long orderId, @RequestBody Map<String, String> body) {
        ChatMessage msg = new ChatMessage();
        msg.setOrderId(orderId);
        msg.setSender(body.getOrDefault("sender", "customer"));
        msg.setText(body.get("text"));
        msg.setCreatedAt(LocalDateTime.now());
        return ResponseEntity.ok(repo.save(msg));
    }
}