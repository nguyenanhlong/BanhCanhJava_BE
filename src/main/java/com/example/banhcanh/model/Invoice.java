package com.example.banhcanh.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "invoices")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @Column(name = "invoice_series", length = 20)
    private String invoiceSeries;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "customer_tax_code", length = 50)
    private String customerTaxCode;

    @Column(name = "customer_address", columnDefinition = "TEXT")
    private String customerAddress;

    private String address;

    @Column(nullable = false)
    private Double subtotal = 0.0;

    @Column(name = "discount_amount")
    private Double discountAmount = 0.0;

    @Column(name = "shipping_fee")
    private Double shippingFee = 0.0;

    @Column(name = "tax_rate")
    private Double taxRate = 0.0;

    @Column(name = "tax_amount")
    private Double taxAmount = 0.0;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(length = 50)
    private String status = "pending";

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "membership_discount")
    private Double membershipDiscount = 0.0;

    @Column(name = "delivery_fee")
    private Double deliveryFee = 0.0;

    @Column(name = "trip_id")
    private Long tripId;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
