package com.example.banhcanh.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "transaction_code", length = 100)
    private String transactionCode;

    @Column(name = "payment_method", length = 50, nullable = false)
    private String paymentMethod;

    @Column(nullable = false)
    private Double amount;

    @Column(length = 50, nullable = false)
    private String status = "pending";

    @Column(length = 50)
    private String gateway;

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
