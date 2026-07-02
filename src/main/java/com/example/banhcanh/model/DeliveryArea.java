package com.example.banhcanh.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "delivery_areas")
public class DeliveryArea {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "center_lat", nullable = false)
    private Double centerLat;

    @Column(name = "center_lng", nullable = false)
    private Double centerLng;

    @Column(name = "radius_km", nullable = false)
    private Double radiusKm;

    @Column(name = "base_fee", nullable = false)
    private Double baseFee = 10000.0;

    @Column(name = "fee_per_km", nullable = false)
    private Double feePerKm = 5000.0;

    @Column(name = "max_distance_km", nullable = false)
    private Double maxDistanceKm = 15.0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
