package com.example.banhcanh.controller;

import com.example.banhcanh.model.DeliveryTrip;
import com.example.banhcanh.model.Driver;
import com.example.banhcanh.repository.DeliveryTripRepository;
import com.example.banhcanh.repository.DriverRepository;
import com.example.banhcanh.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery-trips")
public class DeliveryTripController {

    @Autowired
    private DeliveryTripRepository repo;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping
    public List<DeliveryTrip> getAll() {
        return repo.findAll();
    }

    @GetMapping("/driver/{driverId}")
    public List<DeliveryTrip> getByDriver(@PathVariable String driverId) {
        Long longId;
        try { longId = Long.parseLong(driverId); return repo.findByDriverIdOrderByCreatedAtDesc(longId); }
        catch (NumberFormatException e) { return List.of(); }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getByOrder(@PathVariable String orderId) {
        Long longId;
        try { longId = Long.parseLong(orderId); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ")); }
        return ResponseEntity.ok(repo.findByOrderId(longId));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            Long orderId = Long.valueOf(body.get("orderId").toString());
            Long driverId = Long.valueOf(body.get("driverId").toString());

            List<DeliveryTrip> existing = repo.findByOrderId(orderId);
            if (!existing.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Đơn hàng đã có chuyến giao"));
            }

            DeliveryTrip trip = new DeliveryTrip();
            trip.setOrderId(orderId);
            trip.setDriverId(driverId);
            trip.setStatus("assigned");
            trip.setCreatedAt(LocalDateTime.now());

            return ResponseEntity.ok(repo.save(trip));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String id, @RequestParam String status,
                                          @RequestParam(required = false) Double lat,
                                          @RequestParam(required = false) Double lng) {
        Long longId;
        try { longId = Long.parseLong(id); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ")); }
        return repo.findById(longId).map(trip -> {
            trip.setStatus(status);
            trip.setUpdatedAt(LocalDateTime.now());

            switch (status) {
                case "accepted" -> trip.setAcceptedAt(LocalDateTime.now());
                case "picked_up" -> trip.setPickedUpAt(LocalDateTime.now());
                case "delivered" -> trip.setDeliveredAt(LocalDateTime.now());
            }

            if (lat != null && lng != null) {
                trip.setCurrentLat(lat);
                trip.setCurrentLng(lng);
            }

            return ResponseEntity.ok(repo.save(trip));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/location")
    public ResponseEntity<?> updateLocation(@PathVariable String id,
                                            @RequestParam Double lat,
                                            @RequestParam Double lng) {
        Long longId;
        try { longId = Long.parseLong(id); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ")); }
        return repo.findById(longId).map(trip -> {
            trip.setCurrentLat(lat);
            trip.setCurrentLng(lng);
            trip.setUpdatedAt(LocalDateTime.now());
            return ResponseEntity.ok(repo.save(trip));
        }).orElse(ResponseEntity.notFound().build());
    }
}
