package com.example.banhcanh.controller;

import com.example.banhcanh.model.Order;
import com.example.banhcanh.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private com.example.banhcanh.repository.DriverRepository driverRepository;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        order.setStatus("pending");
        if (order.getPaymentMethod().equals("cod")) {
            order.setPaymentStatus("pending");
        } else {
            order.setPaymentStatus("paid"); // Simulated card/momo
        }
        return orderRepository.save(order);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return orderRepository.findById(id).map(order -> {
            order.setStatus(status);
            return ResponseEntity.ok(orderRepository.save(order));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/assign-driver/{driverId}")
    public ResponseEntity<Order> assignDriver(@PathVariable Long id, @PathVariable Long driverId) {
        return orderRepository.findById(id).map(order -> {
            order.setDriverId(driverId);
            driverRepository.findById(driverId).ifPresent(driver -> {
                order.setDriverName(driver.getName());
                driver.setStatus("busy");
                driverRepository.save(driver);
            });
            order.setStatus("shipping");
            return ResponseEntity.ok(orderRepository.save(order));
        }).orElse(ResponseEntity.notFound().build());
    }
}