package com.example.banhcanh.repository;

import com.example.banhcanh.model.ProductMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductMaterialRepository extends JpaRepository<ProductMaterial, Long> {
    List<ProductMaterial> findByProductId(Long productId);
}
