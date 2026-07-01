package com.example.banhcanh.controller;

import com.example.banhcanh.model.InvoiceDetail;
import com.example.banhcanh.repository.InvoiceDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoice-details")
public class InvoiceDetailController {

    @Autowired
    private InvoiceDetailRepository repo;

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<?> getByInvoice(@PathVariable String invoiceId) {
        Long longId;
        try { longId = Long.parseLong(invoiceId); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body("ID không hợp lệ"); }
        return ResponseEntity.ok(repo.findByInvoiceId(longId));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody InvoiceDetail detail) {
        try {
            if (detail.getTotalPrice() == null) {
                detail.setTotalPrice(detail.getUnitPrice() * detail.getQuantity());
            }
            return ResponseEntity.ok(repo.save(detail));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
