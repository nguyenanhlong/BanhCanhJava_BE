package com.example.banhcanh.model;

import lombok.Data;
// Spring Boot 3 sử dụng jakarta.persistence.* (nếu dùng Spring Boot 2 hãy đổi thành javax.persistence.*)
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private Double price;
    
    @Column(nullable = false)
    private String category; // main, extra, drink
    
    @Column(name = "is_best_seller")
    private Boolean isBestSeller = false;

    @Column(name = "image_url", length = 500)
    private String imageUrl; // URL to the food/topping image
}