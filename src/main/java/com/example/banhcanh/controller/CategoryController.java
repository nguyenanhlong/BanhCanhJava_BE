package com.example.banhcanh.controller;

import com.example.banhcanh.model.Category;
import com.example.banhcanh.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    public List<Category> getAllCategories() {
        return categoryRepository.findByIsActiveTrue();
    }

    @PostMapping
    public Category createCategory(@RequestBody Category category) {
        return categoryRepository.save(category);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable String id, @RequestBody Category categoryDetails) {
        Long longId;
        try {
            longId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID danh mục không hợp lệ: '" + id + "' phải là số"));
        }
        return categoryRepository.findById(longId).map(category -> {
            category.setName(categoryDetails.getName());
            category.setSlug(categoryDetails.getSlug());
            category.setDescription(categoryDetails.getDescription());
            category.setImageUrl(categoryDetails.getImageUrl());
            category.setDisplayOrder(categoryDetails.getDisplayOrder());
            category.setIsActive(categoryDetails.getIsActive());
            return ResponseEntity.ok(categoryRepository.save(category));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable String id) {
        Long longId;
        try {
            longId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID danh mục không hợp lệ: '" + id + "' phải là số"));
        }
        return categoryRepository.findById(longId).map(category -> {
            categoryRepository.delete(category);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
