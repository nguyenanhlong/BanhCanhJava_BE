package com.example.banhcanh.repository;

import com.example.banhcanh.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByOrderIdOrderByCreatedAtAsc(Long orderId);
}