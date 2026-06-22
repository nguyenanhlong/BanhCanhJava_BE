package com.example.banhcanh.model;

import lombok.Data;
// Spring Boot 3 sử dụng jakarta.persistence.* (nếu dùng Spring Boot 2 hãy đổi thành javax.persistence.*)
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_name", nullable = false)
    private String productName;
    
    @Column(nullable = false)
    private Double price;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "noodle_type", length = 50)
    private String noodleType;
    
    @Column(length = 255)
    private String notes;
}