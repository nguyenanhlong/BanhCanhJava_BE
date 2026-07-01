package com.example.banhcanh.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "membership_tiers")
public class MembershipTier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "min_total_spent")
    private Double minTotalSpent = 0.0;

    @Column(name = "min_total_orders")
    private Integer minTotalOrders = 0;

    @Column(name = "auto_discount_percent")
    private Double autoDiscountPercent = 0.0;

    @Column(name = "voucher_count")
    private Integer voucherCount = 0;

    @Column(name = "voucher_discount_percent")
    private Double voucherDiscountPercent = 0.0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
