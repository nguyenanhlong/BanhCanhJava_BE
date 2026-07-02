package com.example.banhcanh.controller;

import com.example.banhcanh.model.PaymentTransaction;
import com.example.banhcanh.repository.PaymentTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentTransactionController {

    @Autowired
    private PaymentTransactionRepository repo;

    @GetMapping
    public List<PaymentTransaction> getAll() {
        return repo.findAll();
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getByOrder(@PathVariable String orderId) {
        Long longId;
        try { longId = Long.parseLong(orderId); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body("ID không hợp lệ"); }
        return ResponseEntity.ok(repo.findByOrderId(longId));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody PaymentTransaction tx) {
        try {
            return ResponseEntity.ok(repo.save(tx));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String id, @RequestParam String status) {
        Long longId;
        try { longId = Long.parseLong(id); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ")); }
        return repo.findById(longId).map(tx -> {
            tx.setStatus(status);
            return ResponseEntity.ok(repo.save(tx));
        }).orElse(ResponseEntity.notFound().build());
    }
}
