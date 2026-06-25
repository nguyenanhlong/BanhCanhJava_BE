package com.example.banhcanh.controller;

import com.example.banhcanh.model.Product;
import com.example.banhcanh.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/category/{category}")
    public List<Product> getProductsByCategory(@PathVariable String category) {
        return productRepository.findByCategoryName(category);
    }

    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        return productRepository.save(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable String id, @RequestBody Product productDetails) {
        Long longId;
        try {
            longId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID sản phẩm không hợp lệ: '" + id + "' phải là số"));
        }
        return productRepository.findById(longId).map(product -> {
            product.setName(productDetails.getName());
            product.setDescription(productDetails.getDescription());
            product.setPrice(productDetails.getPrice());
            product.setCategoryId(productDetails.getCategoryId());
            product.setCategoryName(productDetails.getCategoryName());
            product.setIsBestSeller(productDetails.getIsBestSeller());
            product.setIsAvailable(productDetails.getIsAvailable());
            product.setImageUrl(productDetails.getImageUrl());
            product.setPreparationTime(productDetails.getPreparationTime());
            return ResponseEntity.ok(productRepository.save(product));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable String id) {
        Long longId;
        try {
            longId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID sản phẩm không hợp lệ: '" + id + "' phải là số"));
        }
        return productRepository.findById(longId).map(product -> {
            productRepository.delete(product);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
