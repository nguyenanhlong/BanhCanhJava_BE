package com.example.banhcanh.controller;

import com.example.banhcanh.model.MembershipTier;
import com.example.banhcanh.repository.MembershipTierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/membership-tiers")
public class MembershipTierController {

    @Autowired
    private MembershipTierRepository repo;

    @GetMapping
    public List<MembershipTier> getAll() {
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
    public ResponseEntity<?> create(@RequestBody MembershipTier tier) {
        try {
            return ResponseEntity.ok(repo.save(tier));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody MembershipTier update) {
        Long longId;
        try { longId = Long.parseLong(id); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ")); }
        return repo.findById(longId).map(tier -> {
            if (update.getName() != null) tier.setName(update.getName());
            if (update.getDisplayName() != null) tier.setDisplayName(update.getDisplayName());
            if (update.getMinTotalSpent() != null) tier.setMinTotalSpent(update.getMinTotalSpent());
            if (update.getMinTotalOrders() != null) tier.setMinTotalOrders(update.getMinTotalOrders());
            if (update.getAutoDiscountPercent() != null) tier.setAutoDiscountPercent(update.getAutoDiscountPercent());
            if (update.getVoucherCount() != null) tier.setVoucherCount(update.getVoucherCount());
            if (update.getVoucherDiscountPercent() != null) tier.setVoucherDiscountPercent(update.getVoucherDiscountPercent());
            if (update.getIsActive() != null) tier.setIsActive(update.getIsActive());
            return ResponseEntity.ok(repo.save(tier));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        Long longId;
        try { longId = Long.parseLong(id); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ")); }
        if (repo.existsById(longId)) {
            repo.deleteById(longId);
            return ResponseEntity.ok(Map.of("message", "Đã xoá hạng thành viên"));
        }
        return ResponseEntity.notFound().build();
    }
}
