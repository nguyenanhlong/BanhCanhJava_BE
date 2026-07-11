package com.example.banhcanh.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "drivers")
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Transient
    private String name;

    @Transient
    private String phone;

    @Column(name = "vehicle_type", length = 50)
    private String vehicleType = "Xe máy";

    @Column(name = "vehicle_plate", length = 20)
    private String vehiclePlate;

    @Column(name = "vehicle_color", length = 50)
    private String vehicleColor;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Transient
    private String vehicle;

    @Column(length = 50, nullable = false)
    private String status = "offline";

    @Column(name = "current_lat")
    private Double currentLat;

    @Column(name = "current_lng")
    private Double currentLng;

    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;

    @Column(name = "total_deliveries")
    private Integer totalDeliveries = 0;

    @Column
    private Double rating = 5.0;

    @Column(name = "current_trip_id")
    private Long currentTripId;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PostLoad
    public void loadFromUser() {
        if (user != null) {
            if (this.name == null || this.name.isBlank()) {
                this.name = user.getFullName() != null ? user.getFullName() : user.getUsername();
            }
            if (this.phone == null || this.phone.isBlank()) {
                this.phone = user.getPhone();
            }
            if (this.avatarUrl == null || this.avatarUrl.isBlank()) {
                this.avatarUrl = user.getAvatarUrl();
            }
        }
        if (this.vehicle == null || this.vehicle.isBlank()) {
            this.vehicle = (vehicleType != null ? vehicleType : "Xe máy")
                    + (vehiclePlate != null && !vehiclePlate.isBlank() ? " - " + vehiclePlate : "");
        }
    }
}
