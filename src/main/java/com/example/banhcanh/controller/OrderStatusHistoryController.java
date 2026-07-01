package com.example.banhcanh.controller;

import com.example.banhcanh.model.OrderStatusHistory;
import com.example.banhcanh.repository.OrderStatusHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-history")
public class OrderStatusHistoryController {

    @Autowired
    private OrderStatusHistoryRepository repo;

    @GetMapping
    public List<OrderStatusHistory> getAll() {
        return repo.findAll();
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getByOrder(@PathVariable String orderId) {
        Long longId;
        try { longId = Long.parseLong(orderId); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body("ID không hợp lệ"); }
        return ResponseEntity.ok(repo.findByOrderIdOrderByCreatedAtAsc(longId));
    }
}
