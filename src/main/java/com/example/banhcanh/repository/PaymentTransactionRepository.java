package com.example.banhcanh.repository;

import com.example.banhcanh.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findByOrderId(Long orderId);
    Optional<PaymentTransaction> findByTransactionCode(String transactionCode);
}
