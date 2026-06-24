package com.example.banhcanh.model;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "permissions")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 50)
    private String module = "common";

    @Column(columnDefinition = "TEXT")
    private String description;
}
