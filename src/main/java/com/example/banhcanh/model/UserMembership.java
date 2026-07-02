package com.example.banhcanh.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_memberships")
public class UserMembership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "tier_id", nullable = false)
    private Long tierId;

    @Column(name = "current_points")
    private Double currentPoints = 0.0;

    @Column(name = "total_orders")
    private Integer totalOrders = 0;

    @Column(name = "upgraded_at")
    private LocalDateTime upgradedAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}
