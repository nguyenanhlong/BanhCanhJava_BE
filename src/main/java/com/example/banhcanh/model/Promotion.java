package com.example.banhcanh.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "promotions")
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "discount_type", length = 20, nullable = false)
    private String discountType = "percentage";

    @Column(name = "discount_value", nullable = false)
    private Double discountValue;

    @Column(name = "min_order_amount")
    private Double minOrderAmount = 0.0;

    @Column(name = "max_discount")
    private Double maxDiscount = 0.0;

    @Column(name = "usage_limit")
    private Integer usageLimit = 0;

    @Column(name = "used_count")
    private Integer usedCount = 0;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
