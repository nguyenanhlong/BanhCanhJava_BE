package com.example.banhcanh.controller;

import com.example.banhcanh.model.Order;
import com.example.banhcanh.repository.OrderRepository;
import com.example.banhcanh.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

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
        return Map.of(
            "totalOrders", totalOrders,
            "totalRevenue", totalRevenue,
            "completedOrders", completedOrders
        );
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
    public ResponseEntity<?> updateStatus(@PathVariable String id, @RequestParam String status) {
        Long longId;
        try {
            longId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID đơn hàng không hợp lệ: '" + id + "' phải là số"));
        }
        return orderRepository.findById(longId).map(order -> {
            order.setStatus(status);
            if ("completed".equals(status) || "cancelled".equals(status)) {
                if (order.getDriverId() != null) {
                    driverRepository.findById(order.getDriverId()).ifPresent(driver -> {
                        driver.setStatus("available");
                        driverRepository.save(driver);
                    });
                }
            }
            return ResponseEntity.ok(orderRepository.save(order));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/assign-driver/{driverId}")
    public ResponseEntity<?> assignDriver(@PathVariable String id, @PathVariable String driverId) {
        Long longId, longDriverId;
        try {
            longId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID đơn hàng không hợp lệ: '" + id + "' phải là số"));
        }
        try {
            longDriverId = Long.parseLong(driverId);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID tài xế không hợp lệ: '" + driverId + "' phải là số"));
        }
        return orderRepository.findById(longId).map(order -> {
            order.setDriverId(longDriverId);
            driverRepository.findById(longDriverId).ifPresent(driver -> {
                driver.setStatus("busy");
                driverRepository.save(driver);
            });
            order.setStatus("shipping");
            return ResponseEntity.ok(orderRepository.save(order));
        }).orElse(ResponseEntity.notFound().build());
    }
}
