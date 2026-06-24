package com.example.banhcanh.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "dining_tables")
public class DiningTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_number", length = 20, nullable = false, unique = true)
    private String tableNumber;

    @Column
    private Integer capacity = 4;

    @Column(length = 100)
    private String position;

    @Column(length = 20)
    private String status = "available";

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
