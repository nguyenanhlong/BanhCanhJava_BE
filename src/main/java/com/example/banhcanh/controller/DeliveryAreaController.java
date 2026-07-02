package com.example.banhcanh.controller;

import com.example.banhcanh.model.DeliveryArea;
import com.example.banhcanh.repository.DeliveryAreaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery-areas")
public class DeliveryAreaController {

    @Autowired
    private DeliveryAreaRepository repo;

    @GetMapping
    public List<DeliveryArea> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        Long longId;
        try { longId = Long.parseLong(id); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ")); }
        return repo.findById(longId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody DeliveryArea area) {
        try {
            return ResponseEntity.ok(repo.save(area));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody DeliveryArea update) {
        Long longId;
        try { longId = Long.parseLong(id); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ")); }
        return repo.findById(longId).map(area -> {
            if (update.getName() != null) area.setName(update.getName());
            if (update.getCenterLat() != null) area.setCenterLat(update.getCenterLat());
            if (update.getCenterLng() != null) area.setCenterLng(update.getCenterLng());
            if (update.getRadiusKm() != null) area.setRadiusKm(update.getRadiusKm());
            if (update.getBaseFee() != null) area.setBaseFee(update.getBaseFee());
            if (update.getFeePerKm() != null) area.setFeePerKm(update.getFeePerKm());
            if (update.getMaxDistanceKm() != null) area.setMaxDistanceKm(update.getMaxDistanceKm());
            if (update.getIsActive() != null) area.setIsActive(update.getIsActive());
            return ResponseEntity.ok(repo.save(area));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        Long longId;
        try { longId = Long.parseLong(id); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ")); }
        if (repo.existsById(longId)) {
            repo.deleteById(longId);
            return ResponseEntity.ok(Map.of("message", "Đã xoá khu vực giao hàng"));
        }
        return ResponseEntity.notFound().build();
    }
}
