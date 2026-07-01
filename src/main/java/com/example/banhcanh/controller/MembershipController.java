package com.example.banhcanh.controller;

import com.example.banhcanh.model.MembershipVoucher;
import com.example.banhcanh.model.UserMembership;
import com.example.banhcanh.repository.MembershipVoucherRepository;
import com.example.banhcanh.repository.UserMembershipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/memberships")
public class MembershipController {

    @Autowired
    private MembershipVoucherRepository voucherRepo;

    @Autowired
    private UserMembershipRepository membershipRepo;

    // --- UserMembership ---
    @GetMapping("/{userId}")
    public ResponseEntity<?> getMembership(@PathVariable String userId) {
        Long longId;
        try { longId = Long.parseLong(userId); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ")); }
        return membershipRepo.findByUserId(longId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.ok(null));
    }

    @PostMapping("/{userId}")
    public ResponseEntity<?> createOrUpdateMembership(@PathVariable String userId,
                                                       @RequestBody Map<String, Object> body) {
        Long longId;
        try { longId = Long.parseLong(userId); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ")); }
        UserMembership m = membershipRepo.findByUserId(longId).orElse(new UserMembership());
        m.setUserId(longId);
        m.setTierId(body.get("tierId") != null ? Long.valueOf(body.get("tierId").toString()) : 1L);
        m.setCurrentPoints(body.get("currentPoints") != null ? Double.valueOf(body.get("currentPoints").toString()) : 0.0);
        m.setTotalOrders(body.get("totalOrders") != null ? Integer.valueOf(body.get("totalOrders").toString()) : 0);
        if (m.getId() == null) m.setUpgradedAt(LocalDateTime.now());
        return ResponseEntity.ok(membershipRepo.save(m));
    }

    // --- Vouchers ---
    @GetMapping("/{userId}/vouchers")
    public ResponseEntity<?> getVouchers(@PathVariable String userId) {
        Long longId;
        try { longId = Long.parseLong(userId); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ")); }
        return ResponseEntity.ok(voucherRepo.findByUserIdOrderByIssuedAtDesc(longId));
    }

    @PostMapping("/{userId}/vouchers/claim")
    public ResponseEntity<?> claimVoucher(@PathVariable String userId, @RequestParam Long tierId) {
        Long longId;
        try { longId = Long.parseLong(userId); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ")); }

        long voucherCount = voucherRepo.findByUserIdAndStatus(longId, "available").size();
        if (voucherCount >= 5) {
            return ResponseEntity.badRequest().body(Map.of("error", "Bạn đã có quá nhiều voucher chưa sử dụng"));
        }

        MembershipVoucher v = new MembershipVoucher();
        v.setUserId(longId);
        v.setTierId(tierId);
        v.setCode("VC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        v.setStatus("available");
        v.setIssuedAt(LocalDateTime.now());
        v.setExpiresAt(LocalDateTime.now().plusDays(30));

        if (tierId == 2) {
            v.setDiscountPercent(5.0);
            v.setMaxDiscount(20000.0);
            v.setMinOrderAmount(50000.0);
        } else if (tierId == 3) {
            v.setDiscountPercent(10.0);
            v.setMaxDiscount(50000.0);
            v.setMinOrderAmount(30000.0);
        } else {
            v.setDiscountPercent(3.0);
            v.setMaxDiscount(10000.0);
            v.setMinOrderAmount(0.0);
        }

        return ResponseEntity.ok(voucherRepo.save(v));
    }

    @PutMapping("/vouchers/{voucherId}/use")
    public ResponseEntity<?> useVoucher(@PathVariable String voucherId) {
        Long longId;
        try { longId = Long.parseLong(voucherId); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ")); }
        return voucherRepo.findById(longId).map(v -> {
            v.setStatus("used");
            v.setUsedAt(LocalDateTime.now());
            return ResponseEntity.ok(voucherRepo.save(v));
        }).orElse(ResponseEntity.notFound().build());
    }
}
