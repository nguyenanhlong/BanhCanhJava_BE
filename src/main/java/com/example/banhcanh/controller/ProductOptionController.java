package com.example.banhcanh.controller;

import com.example.banhcanh.model.ProductOption;
import com.example.banhcanh.repository.ProductOptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product-options")
public class ProductOptionController {

    @Autowired
    private ProductOptionRepository repo;

    @GetMapping
    public List<ProductOption> getAll() {
        return repo.findAll();
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getByProduct(@PathVariable String productId) {
        Long longId;
        try { longId = Long.parseLong(productId); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body("ID không hợp lệ"); }
        return ResponseEntity.ok(repo.findByProductId(longId));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProductOption option) {
        try {
            return ResponseEntity.ok(repo.save(option));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody ProductOption update) {
        Long longId;
        try { longId = Long.parseLong(id); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ")); }
        return repo.findById(longId).map(opt -> {
            if (update.getName() != null) opt.setName(update.getName());
            if (update.getPrice() != null) opt.setPrice(update.getPrice());
            if (update.getOptionGroup() != null) opt.setOptionGroup(update.getOptionGroup());
            if (update.getIsRequired() != null) opt.setIsRequired(update.getIsRequired());
            if (update.getIsActive() != null) opt.setIsActive(update.getIsActive());
            if (update.getDisplayOrder() != null) opt.setDisplayOrder(update.getDisplayOrder());
            return ResponseEntity.ok(repo.save(opt));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        Long longId;
        try { longId = Long.parseLong(id); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ")); }
        if (repo.existsById(longId)) {
            repo.deleteById(longId);
            return ResponseEntity.ok(Map.of("message", "Đã xoá tuỳ chọn"));
        }
        return ResponseEntity.notFound().build();
    }
}
