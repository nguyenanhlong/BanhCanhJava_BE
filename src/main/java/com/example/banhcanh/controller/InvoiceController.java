package com.example.banhcanh.controller;

import com.example.banhcanh.model.Invoice;
import com.example.banhcanh.model.InvoiceDetail;
import com.example.banhcanh.model.Order;
import com.example.banhcanh.repository.InvoiceDetailRepository;
import com.example.banhcanh.repository.InvoiceRepository;
import com.example.banhcanh.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private InvoiceDetailRepository detailRepository;

    @GetMapping
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getInvoice(@PathVariable String id) {
        Long longId;
        try { longId = Long.parseLong(id); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ")); }
        return invoiceRepository.findById(longId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<?> getInvoiceDetails(@PathVariable String id) {
        Long longId;
        try { longId = Long.parseLong(id); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ")); }
        return ResponseEntity.ok(detailRepository.findByInvoiceId(longId));
    }

    @PostMapping
    public ResponseEntity<?> createInvoice(@RequestBody Map<String, Object> body) {
        try {
            Long orderId = Long.valueOf(body.get("orderId").toString());
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

            String invoiceNumber = "HD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(10000));

            Invoice invoice = new Invoice();
            invoice.setOrderId(orderId);
            invoice.setInvoiceNumber(invoiceNumber);
            invoice.setCustomerName(order.getCustomerName());
            invoice.setCustomerPhone(order.getPhone());
            invoice.setAddress(order.getAddress());
            invoice.setSubtotal(order.getSubtotal() != null ? order.getSubtotal() : 0.0);
            invoice.setDiscountAmount(order.getDiscountAmount() != null ? order.getDiscountAmount() : 0.0);
            invoice.setShippingFee(order.getShippingFee() != null ? order.getShippingFee() : 0.0);
            invoice.setMembershipDiscount(order.getMembershipDiscount() != null ? order.getMembershipDiscount() : 0.0);
            invoice.setDeliveryFee(order.getDeliveryFee() != null ? order.getDeliveryFee() : 0.0);
            invoice.setTotalAmount(order.getTotalAmount());
            invoice.setPaymentMethod(order.getPaymentMethod());
            invoice.setStatus("issued");
            invoice.setIssuedAt(LocalDateTime.now());
            invoice.setTripId(order.getDriverId());

            Invoice saved = invoiceRepository.save(invoice);

            // Create invoice details from order items
            if (order.getItems() != null) {
                order.getItems().forEach(item -> {
                    InvoiceDetail detail = new InvoiceDetail();
                    detail.setInvoiceId(saved.getId());
                    detail.setProductName(item.getProductName() != null ? item.getProductName() : "Sản phẩm");
                    detail.setQuantity(item.getQuantity());
                    detail.setUnitPrice(item.getPrice());
                    detail.setTotalPrice(item.getSubtotal());
                    detailRepository.save(detail);
                });
            }

            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelInvoice(@PathVariable String id) {
        Long longId;
        try { longId = Long.parseLong(id); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ")); }
        return invoiceRepository.findById(longId).map(invoice -> {
            invoice.setStatus("cancelled");
            return ResponseEntity.ok(invoiceRepository.save(invoice));
        }).orElse(ResponseEntity.notFound().build());
    }
}
