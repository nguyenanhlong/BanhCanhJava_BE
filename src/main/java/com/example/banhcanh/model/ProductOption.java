package com.example.banhcanh.model;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "product_options")
public class ProductOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "option_group", length = 50)
    private String optionGroup = "topping";

    @Column
    private Double price = 0.0;

    @Column(name = "is_required")
    private Boolean isRequired = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "display_order")
    private Integer displayOrder = 0;
}
