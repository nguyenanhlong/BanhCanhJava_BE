package com.example.banhcanh.model;

import lombok.Data;
// Spring Boot 3 sử dụng jakarta.persistence.* (nếu dùng Spring Boot 2 hãy đổi thành javax.persistence.*)
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false, length = 50)
    private String role; // customer, admin, driver
}