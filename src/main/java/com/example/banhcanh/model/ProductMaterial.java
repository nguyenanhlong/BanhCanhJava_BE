package com.example.banhcanh.model;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "product_materials")
public class ProductMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "material_id", nullable = false)
    private Long materialId;

    @Column(nullable = false)
    private Double quantity;
}
