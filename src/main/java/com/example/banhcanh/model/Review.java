package com.example.banhcanh.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_approved")
    private Boolean isApproved = false;

    @Column(name = "admin_reply", columnDefinition = "TEXT")
    private String adminReply;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
