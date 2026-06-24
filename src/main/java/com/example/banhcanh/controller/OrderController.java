package com.example.banhcanh.controller;

import com.example.banhcanh.model.Order;
import com.example.banhcanh.repository.OrderRepository;
import com.example.banhcanh.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DriverRepository driverRepository;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @GetMapping("/stats")
    public Map<String, Object> getOrderStats() {
        List<Order> allOrders = orderRepository.findAll();
        long totalOrders = allOrders.size();
        double totalRevenue = allOrders.stream()
                .filter(o -> "completed".equals(o.getStatus()))
                .mapToDouble(Order::getTotalAmount)
                .sum();
        long completedOrders = allOrders.stream().filter(o -> "completed".equals(o.getStatus())).count();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", totalOrders);
        stats.put("totalRevenue", totalRevenue);
        stats.put("completedOrders", completedOrders);
        return stats;
    }

    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        order.setStatus("pending");
        if ("cod".equals(order.getPaymentMethod())) {
            order.setPaymentStatus("pending");
        } else {
            order.setPaymentStatus("paid");
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
                driver.setStatus("busy");
                driverRepository.save(driver);
            });
            order.setStatus("shipping");
            return ResponseEntity.ok(orderRepository.save(order));
        }).orElse(ResponseEntity.notFound().build());
    }
}
