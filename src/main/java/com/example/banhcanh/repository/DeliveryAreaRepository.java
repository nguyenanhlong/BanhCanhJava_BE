package com.example.banhcanh.repository;

import com.example.banhcanh.model.DeliveryArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryAreaRepository extends JpaRepository<DeliveryArea, Long> {
}
