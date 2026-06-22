package com.example.banhcanh.model;

import lombok.Data;
// Spring Boot 3 sử dụng jakarta.persistence.* (nếu dùng Spring Boot 2 hãy đổi thành javax.persistence.*)
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
    
    @Column(name = "customer_name", nullable = false)
    private String customerName;
    
    @Column(nullable = false)
    private String phone;
    
    @Column(nullable = false)
    private String address;
    
    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;
    
    @Column(name = "payment_method", nullable = false)
    private String paymentMethod; // cod, momo, vnpay
    
    @Column(name = "payment_status", nullable = false)
    private String paymentStatus; // pending, paid, failed
    
    @Column(nullable = false)
    private String status; // pending, preparing, shipping, completed, cancelled
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "driver_id")
    private Long driverId;
    
    @Column(name = "driver_name")
    private String driverName;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "order_id", nullable = false)
    private List<OrderItem> items;
}