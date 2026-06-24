package com.example.banhcanh.model;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double price;

    @Column(name = "options_text", length = 500)
    private String optionsText;

    @Column(name = "noodle_type", length = 50)
    private String noodleType;

    @Column(length = 255)
    private String notes;

    @Column(nullable = false)
    private Double subtotal = 0.0;
}
