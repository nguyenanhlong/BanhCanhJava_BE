package com.example.banhcanh.controller;

import com.example.banhcanh.model.Promotion;
import com.example.banhcanh.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    @Autowired
    private PromotionRepository repo;

    @GetMapping
    public List<Promotion> getAll() {
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
    public ResponseEntity<?> create(@RequestBody Promotion promo) {
        try {
            if (promo.getCode() == null || promo.getCode().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mã khuyến mãi không được để trống"));
            }
            return ResponseEntity.ok(repo.save(promo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Promotion update) {
        Long longId;
        try { longId = Long.parseLong(id); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ")); }
        return repo.findById(longId).map(promo -> {
            if (update.getCode() != null) promo.setCode(update.getCode());
            if (update.getName() != null) promo.setName(update.getName());
            if (update.getDescription() != null) promo.setDescription(update.getDescription());
            if (update.getDiscountType() != null) promo.setDiscountType(update.getDiscountType());
            if (update.getDiscountValue() != null) promo.setDiscountValue(update.getDiscountValue());
            if (update.getMinOrderAmount() != null) promo.setMinOrderAmount(update.getMinOrderAmount());
            if (update.getMaxDiscount() != null) promo.setMaxDiscount(update.getMaxDiscount());
            if (update.getUsageLimit() != null) promo.setUsageLimit(update.getUsageLimit());
            if (update.getUsedCount() != null) promo.setUsedCount(update.getUsedCount());
            if (update.getStartDate() != null) promo.setStartDate(update.getStartDate());
            if (update.getEndDate() != null) promo.setEndDate(update.getEndDate());
            if (update.getIsActive() != null) promo.setIsActive(update.getIsActive());
            return ResponseEntity.ok(repo.save(promo));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        Long longId;
        try { longId = Long.parseLong(id); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ")); }
        if (repo.existsById(longId)) {
            repo.deleteById(longId);
            return ResponseEntity.ok(Map.of("message", "Đã xoá khuyến mãi"));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{code}/validate")
    public ResponseEntity<?> validateCode(@PathVariable String code, @RequestParam(required = false) Double orderAmount) {
        return repo.findByCode(code).map(promo -> {
            if (!promo.getIsActive()) return ResponseEntity.badRequest().body(Map.of("valid", false, "error", "Mã đã bị vô hiệu"));
            if (orderAmount != null && promo.getMinOrderAmount() > 0 && orderAmount < promo.getMinOrderAmount()) {
                return ResponseEntity.badRequest().body(Map.of("valid", false, "error", "Đơn tối thiểu " + promo.getMinOrderAmount() + "đ"));
            }
            return ResponseEntity.ok(Map.of("valid", true, "promo", promo));
        }).orElse(ResponseEntity.badRequest().body(Map.of("valid", false, "error", "Mã không tồn tại")));
    }
}
