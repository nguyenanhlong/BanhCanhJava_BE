package com.example.banhcanh.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "order_type", length = 20)
    private String orderType = "delivery";

    @Column(nullable = false)
    private Double subtotal = 0.0;

    @Column(name = "discount_id")
    private Long discountId;

    @Column(name = "discount_amount")
    private Double discountAmount = 0.0;

    @Column(name = "shipping_fee")
    private Double shippingFee = 0.0;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "payment_method", length = 50, nullable = false)
    private String paymentMethod;

    @Column(name = "payment_status", length = 50, nullable = false)
    private String paymentStatus = "pending";

    @Column(nullable = false)
    private String status = "pending";

    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "delivery_progress")
    private Integer deliveryProgress = 0;

    @Column(name = "delivery_stage", length = 100)
    private String deliveryStage;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "delivery_area_id")
    private Long deliveryAreaId;

    @Column(name = "delivery_distance_km")
    private Double deliveryDistanceKm = 0.0;

    @Column(name = "delivery_fee")
    private Double deliveryFee = 0.0;

    @Column(name = "membership_discount")
    private Double membershipDiscount = 0.0;

    @Column(name = "membership_voucher_id")
    private Long membershipVoucherId;

    @Column(name = "out_of_area")
    private Boolean outOfArea = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "order_id", nullable = false)
    private List<OrderItem> items;
}
