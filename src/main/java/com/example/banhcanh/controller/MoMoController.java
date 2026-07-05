package com.example.banhcanh.controller;

import com.example.banhcanh.model.PaymentTransaction;
import com.example.banhcanh.repository.PaymentTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class MoMoController {

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Value("${momo.partner-code:MOMO}")
    private String partnerCode;

    @Value("${momo.access-key:}")
    private String accessKey;

    @Value("${momo.secret-key:}")
    private String secretKey;

    @Value("${momo.endpoint:https://test-payment.momo.vn/v2/gateway/api/create}")
    private String momoEndpoint;

    @Value("${app.base-url:https://banhcanhjavabe-production.up.railway.app}")
    private String baseUrl;

    @PostMapping("/momo/create")
    public ResponseEntity<?> createMoMoPayment(@RequestBody Map<String, Object> body) {
        try {
            Long orderId = Long.valueOf(body.get("orderId").toString());
            double amount = Double.parseDouble(body.get("amount").toString());
            String orderInfo = body.getOrDefault("orderInfo", "Thanh toan don hang").toString();

            String requestId = UUID.randomUUID().toString();
            String orderCode = "MOMO_" + orderId + "_" + System.currentTimeMillis();

            String returnUrl = baseUrl + "/api/payments/momo/callback?orderId=" + orderId;
            String notifyUrl = baseUrl + "/api/payments/momo/ipn";

            String rawSignature = "accessKey=" + accessKey +
                    "&amount=" + String.format("%.0f", amount) +
                    "&extraData=" +
                    "&ipnUrl=" + notifyUrl +
                    "&orderId=" + orderCode +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + partnerCode +
                    "&redirectUrl=" + returnUrl +
                    "&requestId=" + requestId +
                    "&requestType=captureWallet";

            // In production, sign with HmacSHA256(secretKey, rawSignature)
            String signature = "mock_signature_" + UUID.randomUUID().toString().substring(0, 8);

            PaymentTransaction tx = new PaymentTransaction();
            tx.setOrderId(orderId);
            tx.setTransactionCode(orderCode);
            tx.setPaymentMethod("momo");
            tx.setAmount(amount);
            tx.setStatus("pending");
            tx.setGateway("MoMo");
            tx.setCreatedAt(LocalDateTime.now());
            paymentTransactionRepository.save(tx);

            return ResponseEntity.ok(Map.of(
                "payUrl", returnUrl + "&mockPayment=true",
                "qrCodeUrl", "https://api.vietqr.io/image/970422-09411058801-Jo1SpmC.jpg?amount=" + String.format("%.0f", amount) + "&addInfo=DH" + orderId + "&accountName=TRAN%20VAN%20A",
                "orderCode", orderCode,
                "requestId", requestId,
                "signature", signature,
                "amount", amount,
                "message", "Tạo yêu cầu thanh toán MoMo thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/momo/callback")
    public ResponseEntity<?> momoCallback(
            @RequestParam Long orderId,
            @RequestParam(required = false) String mockPayment) {
        if ("true".equals(mockPayment)) {
            paymentTransactionRepository.findByOrderId(orderId).stream()
                .filter(tx -> "momo".equals(tx.getPaymentMethod()) && "pending".equals(tx.getStatus()))
                .findFirst().ifPresent(tx -> {
                    tx.setStatus("completed");
                    tx.setPaidAt(LocalDateTime.now());
                    paymentTransactionRepository.save(tx);
                });
            return ResponseEntity.ok(Map.of(
                "status", "completed",
                "message", "Thanh toán MoMo thành công!",
                "orderId", orderId
            ));
        }
        return ResponseEntity.ok(Map.of("status", "pending", "orderId", orderId));
    }

    @PostMapping("/momo/ipn")
    public ResponseEntity<?> momoIpn(@RequestBody Map<String, Object> body) {
        String orderCode = (String) body.get("orderId");
        String resultCode = String.valueOf(body.getOrDefault("resultCode", "0"));
        if ("0".equals(resultCode)) {
            paymentTransactionRepository.findByTransactionCode(orderCode).ifPresent(tx -> {
                tx.setStatus("completed");
                tx.setPaidAt(LocalDateTime.now());
                tx.setGatewayResponse(body.toString());
                paymentTransactionRepository.save(tx);
            });
        }
        return ResponseEntity.ok(Map.of("message", "IPN received"));
    }

    @GetMapping("/momo/status/{orderId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable Long orderId) {
        var tx = paymentTransactionRepository.findByOrderId(orderId).stream()
            .filter(t -> "momo".equals(t.getPaymentMethod()))
            .findFirst();
        if (tx.isPresent()) {
            return ResponseEntity.ok(Map.of(
                "status", tx.get().getStatus(),
                "orderId", orderId,
                "transactionCode", tx.get().getTransactionCode()
            ));
        }
        return ResponseEntity.ok(Map.of("status", "not_found", "orderId", orderId));
    }
}
