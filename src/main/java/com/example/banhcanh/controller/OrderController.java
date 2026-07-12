package com.example.banhcanh.controller;

import com.example.banhcanh.model.*;
import com.example.banhcanh.repository.*;
import com.example.banhcanh.security.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private OrderStatusHistoryRepository historyRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceDetailRepository invoiceDetailRepository;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private DeliveryTripRepository deliveryTripRepository;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        Long longId;
        try { longId = Long.parseLong(id); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ: '" + id + "' phải là số")); }
        return orderRepository.findById(longId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getByUser(@PathVariable String userId, @AuthenticationPrincipal AuthenticatedUser principal) {
        Long longUserId;
        try { longUserId = Long.parseLong(userId); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID người dùng không hợp lệ: '" + userId + "' phải là số")); }
        if (principal == null || (!principal.isAdmin() && !principal.userId().equals(longUserId))) {
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền xem đơn hàng của người dùng khác"));
        }
        return ResponseEntity.ok(orderRepository.findByUserIdOrderByCreatedAtDesc(longUserId));
    }

    @GetMapping("/stats")
    public Map<String, Object> getOrderStats() {
        List<Order> allOrders = orderRepository.findAll();
        long totalOrders = allOrders.size();
        double totalRevenue = allOrders.stream()
                .filter(o -> "completed".equals(o.getStatus()))
                .mapToDouble(Order::getTotalAmount)
                .sum();
        long completedOrders = allOrders.stream().filter(o -> "completed".equals(o.getStatus())).count();
        long pendingOrders = allOrders.stream().filter(o -> "pending".equals(o.getStatus())).count();
        return Map.of(
            "totalOrders", totalOrders,
            "totalRevenue", totalRevenue,
            "completedOrders", completedOrders,
            "pendingOrders", pendingOrders
        );
    }

    @PostMapping
    public Order createOrder(@RequestBody Order order, @AuthenticationPrincipal AuthenticatedUser principal) {
        // POST /api/orders is public (hỗ trợ khách vãng lai đặt hàng không cần đăng nhập), nhưng
        // nếu người gọi ĐÃ đăng nhập, luôn dùng userId từ JWT — không tin userId client tự gửi lên,
        // để không ai mạo danh gán đơn hàng cho tài khoản người khác.
        if (principal != null) {
            order.setUserId(principal.userId());
        }
        order.setStatus("pending");
        if ("cod".equals(order.getPaymentMethod())) {
            order.setPaymentStatus("pending");
        } else {
            order.setPaymentStatus("paid");
        }
        order.setCreatedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);
        saveHistory(saved.getId(), null, "pending", 0L, "Đơn hàng được tạo");

        Invoice invoice = new Invoice();
        invoice.setOrderId(saved.getId());
        invoice.setInvoiceNumber("INV-" + System.currentTimeMillis());
        invoice.setTotalAmount(saved.getTotalAmount());
        invoice.setCustomerName(saved.getCustomerName());
        invoice.setCustomerPhone(saved.getPhone());
        invoice.setAddress(saved.getAddress());
        invoice.setPaymentMethod(saved.getPaymentMethod());
        invoice.setSubtotal(saved.getSubtotal());
        invoice.setDiscountAmount(saved.getDiscountAmount());
        invoice.setShippingFee(saved.getShippingFee());
        invoice.setTaxAmount(0.0);
        invoice.setStatus("pending");
        invoice.setIssuedAt(LocalDateTime.now());
        invoice.setCreatedAt(LocalDateTime.now());
        Invoice savedInvoice = invoiceRepository.save(invoice);

        if (saved.getItems() != null) {
            for (OrderItem item : saved.getItems()) {
                InvoiceDetail detail = new InvoiceDetail();
                detail.setInvoiceId(savedInvoice.getId());
                detail.setProductName(item.getProductName());
                detail.setQuantity(item.getQuantity());
                detail.setUnitPrice(item.getPrice());
                detail.setTotalPrice(item.getSubtotal());
                invoiceDetailRepository.save(detail);
            }
        }

        PaymentTransaction txn = new PaymentTransaction();
        txn.setOrderId(saved.getId());
        txn.setTransactionCode("TXN-" + System.currentTimeMillis());
        txn.setPaymentMethod(saved.getPaymentMethod());
        txn.setAmount(saved.getTotalAmount());
        txn.setStatus(saved.getPaymentStatus());
        txn.setCreatedAt(LocalDateTime.now());
        paymentTransactionRepository.save(txn);

        return saved;
    }

    private static final java.util.Set<String> VALID_STATUSES = java.util.Set.of(
        "pending", "preparing", "shipping", "completed", "cancelled");

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String id, @RequestParam String status) {
        Long longId;
        try {
            longId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID đơn hàng không hợp lệ: '" + id + "' phải là số"));
        }
        if (!VALID_STATUSES.contains(status)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Trạng thái không hợp lệ: " + status));
        }
        return orderRepository.findById(longId).map(order -> {
            String oldStatus = order.getStatus();
            order.setStatus(status);
            // Đơn kết thúc: giải phóng tài xế và đóng chuyến giao
            if ("completed".equals(status) || "cancelled".equals(status)) {
                deliveryTripRepository.findByOrderId(order.getId()).forEach(trip -> {
                    if ("assigned".equals(trip.getStatus()) || "accepted".equals(trip.getStatus()) || "picked_up".equals(trip.getStatus())) {
                        trip.setStatus("completed".equals(status) ? "delivered" : "cancelled");
                        trip.setUpdatedAt(LocalDateTime.now());
                        deliveryTripRepository.save(trip);
                    }
                });
                if (order.getDriverId() != null) {
                    driverRepository.findById(order.getDriverId()).ifPresent(driver -> {
                        driver.setStatus("available");
                        driverRepository.save(driver);
                    });
                }
            }
            if ("shipping".equals(status)) {
                order.setDeliveryProgress(50);
            }
            if ("completed".equals(status)) {
                order.setDeliveryProgress(100);
            }
            Order saved = orderRepository.save(order);
            saveHistory(saved.getId(), oldStatus, status, 0L, "Cập nhật trạng thái");

            // Tự động phân công tài xế rảnh cho đơn khi chuyển sang chế biến
            if ("preparing".equals(status)) {
                autoAssignDriver(saved);
            }

            return ResponseEntity.ok(orderRepository.findById(saved.getId()).orElse(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/progress")
    public ResponseEntity<?> updateProgress(@PathVariable String id,
                                            @RequestParam Integer progress,
                                            @RequestParam(required = false) String stage) {
        Long longId;
        try {
            longId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID đơn hàng không hợp lệ: '" + id + "' phải là số"));
        }
        if (progress < 0 || progress > 100) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tiến trình phải nằm trong khoảng 0-100"));
        }
        return orderRepository.findById(longId).<ResponseEntity<?>>map(order -> {
            int current = order.getDeliveryProgress() != null ? order.getDeliveryProgress() : 0;
            if (progress < current) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Không thể giảm tiến trình giao hàng (hiện tại " + current + "%, yêu cầu " + progress + "%)"));
            }
            order.setDeliveryProgress(progress);
            if (stage != null && !stage.isBlank()) order.setDeliveryStage(stage);
            Order saved = orderRepository.save(order);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/assign-driver/{driverId}")
    public ResponseEntity<?> assignDriver(@PathVariable String id, @PathVariable String driverId) {
        Long longId, longDriverId;
        try {
            longId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID đơn hàng không hợp lệ: '" + id + "' phải là số"));
        }
        try {
            longDriverId = Long.parseLong(driverId);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID tài xế không hợp lệ: '" + driverId + "' phải là số"));
        }
        return orderRepository.findById(longId).<ResponseEntity<?>>map(order -> {
            // Mỗi đơn hàng chỉ được phân công đúng một tài xế.
            if (order.getDriverId() != null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Đơn hàng đã được phân công cho tài xế #" + order.getDriverId()));
            }
            if ("completed".equals(order.getStatus()) || "cancelled".equals(order.getStatus()) || "delivered".equals(order.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Đơn hàng đã kết thúc, không thể phân công tài xế"));
            }
            Driver driver = driverRepository.findById(longDriverId).orElse(null);
            if (driver == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy tài xế #" + longDriverId));
            }
            // Chỉ tài xế đang ở trạng thái sẵn sàng mới được nhận đơn.
            if (!"available".equals(driver.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Tài xế " + driver.getName() + " không ở trạng thái sẵn sàng (hiện tại: " + driver.getStatus() + ")"));
            }
            order.setDriverId(longDriverId);
            driver.setStatus("busy");
            driverRepository.save(driver);
            Order saved = orderRepository.save(order);
            saveHistory(saved.getId(), order.getStatus(), order.getStatus(), longDriverId, "Giao tài xế #" + longDriverId);

            DeliveryTrip trip = new DeliveryTrip();
            trip.setOrderId(longId);
            trip.setDriverId(longDriverId);
            trip.setStatus("assigned");
            trip.setCreatedAt(LocalDateTime.now());
            deliveryTripRepository.save(trip);

            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    private void saveHistory(Long orderId, String oldStatus, String newStatus, Long changedBy, String notes) {
        OrderStatusHistory h = new OrderStatusHistory();
        h.setOrderId(orderId);
        h.setOldStatus(oldStatus);
        h.setNewStatus(newStatus);
        h.setChangedBy(changedBy);
        h.setNotes(notes);
        h.setCreatedAt(LocalDateTime.now());
        historyRepository.save(h);
    }

    private void autoAssignDriver(Order order) {
        if (order.getDriverId() != null) return;

        List<Driver> availableDrivers = driverRepository.findByStatus("available");
        if (availableDrivers.isEmpty()) return;

        Driver driver = availableDrivers.get(0);
        order.setDriverId(driver.getId());
        driver.setStatus("busy");
        driverRepository.save(driver);
        orderRepository.save(order);

        DeliveryTrip trip = new DeliveryTrip();
        trip.setOrderId(order.getId());
        trip.setDriverId(driver.getId());
        trip.setStatus("assigned");
        trip.setCreatedAt(LocalDateTime.now());
        deliveryTripRepository.save(trip);

        saveHistory(order.getId(), order.getStatus(), order.getStatus(), driver.getId(),
                "Tự động giao tài xế #" + driver.getId() + " - " + driver.getName());
    }
}
