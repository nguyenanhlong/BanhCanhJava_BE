package com.example.banhcanh.model;

import lombok.Data;
// Spring Boot 3 sử dụng jakarta.persistence.* (nếu dùng Spring Boot 2 hãy đổi thành javax.persistence.*)
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "drivers")
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String phone;
    
    @Column(nullable = false)
    private String vehicle;
    
    @Column(nullable = false)
    private String status; // available, busy, offline
    
    private Double rating = 5.0;
}