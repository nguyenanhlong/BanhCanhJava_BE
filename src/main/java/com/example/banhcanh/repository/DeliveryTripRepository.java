package com.example.banhcanh.repository;

import com.example.banhcanh.model.DeliveryTrip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeliveryTripRepository extends JpaRepository<DeliveryTrip, Long> {
    List<DeliveryTrip> findByDriverIdOrderByCreatedAtDesc(Long driverId);
    List<DeliveryTrip> findByOrderId(Long orderId);
}
