package com.example.banhcanh.controller;

import com.example.banhcanh.model.Driver;
import com.example.banhcanh.model.DeliveryTrip;
import com.example.banhcanh.model.User;
import com.example.banhcanh.repository.DriverRepository;
import com.example.banhcanh.repository.DeliveryTripRepository;
import com.example.banhcanh.repository.UserRepository;
import com.example.banhcanh.security.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeliveryTripRepository tripRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> registerDriver(@RequestBody Map<String, Object> body) {
        try {
            String username = (String) body.get("username");
            String password = (String) body.get("password");
            String email = (String) body.get("email");

            if (username == null || password == null || email == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Thiếu username, password hoặc email"));
            }
            if (userRepository.existsByUsername(username)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tên đăng nhập đã tồn tại"));
            }
            if (userRepository.existsByEmail(email)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email đã tồn tại"));
            }

            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email);
            user.setRole("driver");
            user.setFullName((String) body.getOrDefault("fullName", body.get("name")));
            user.setPhone((String) body.get("phone"));
            user.setAddress((String) body.get("address"));
            user.setIsActive(true);
            User savedUser = userRepository.save(user);

            Driver driver = new Driver();
            driver.setUserId(savedUser.getId());
            driver.setName(user.getFullName() != null ? user.getFullName() : username);
            driver.setPhone(user.getPhone() != null ? user.getPhone() : "");
            driver.setVehicleType((String) body.getOrDefault("vehicleType", "Xe máy"));
            driver.setVehiclePlate((String) body.getOrDefault("vehiclePlate", ""));
            driver.setVehicleColor((String) body.getOrDefault("vehicleColor", ""));
            driver.setAvatarUrl((String) body.getOrDefault("avatarUrl", ""));
            String vehicle = (String) body.get("vehicle");
            if (vehicle == null || vehicle.isBlank()) {
                vehicle = driver.getVehicleType() + " - " + driver.getVehiclePlate();
            }
            driver.setVehicle(vehicle);
            driver.setStatus("offline");
            driver.setIsActive(true);
            Driver savedDriver = driverRepository.save(driver);

            return ResponseEntity.ok(Map.of(
                "user", savedUser,
                "driver", savedDriver
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        Long longId;
        try { longId = Long.parseLong(id); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ")); }
        return driverRepository.findById(longId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Driver createDriver(@RequestBody Driver driver) {
        driver.setStatus("offline");
        return driverRepository.save(driver);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDriver(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Long longId;
        try { longId = Long.parseLong(id); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ")); }
        return driverRepository.findById(longId).map(driver -> {
            if (body.containsKey("vehicleType")) driver.setVehicleType((String) body.get("vehicleType"));
            if (body.containsKey("vehiclePlate")) driver.setVehiclePlate((String) body.get("vehiclePlate"));
            if (body.containsKey("vehicleColor")) driver.setVehicleColor((String) body.get("vehicleColor"));
            if (body.containsKey("avatarUrl")) driver.setAvatarUrl((String) body.get("avatarUrl"));
            if (body.containsKey("vehicle")) driver.setVehicle((String) body.get("vehicle"));

            // Sync name/phone/avatar/password with the associated User record
            Long userId = driver.getUserId();
            if (userId != null) {
                userRepository.findById(userId).ifPresent(user -> {
                    boolean userUpdated = false;
                    if (body.containsKey("name")) {
                        driver.setName((String) body.get("name"));
                        user.setFullName((String) body.get("name"));
                        userUpdated = true;
                    }
                    if (body.containsKey("phone")) {
                        driver.setPhone((String) body.get("phone"));
                        user.setPhone((String) body.get("phone"));
                        userUpdated = true;
                    }
                    if (body.containsKey("avatarUrl")) {
                        user.setAvatarUrl((String) body.get("avatarUrl"));
                        userUpdated = true;
                    }
                    if (body.containsKey("password")) {
                        String newPassword = (String) body.get("password");
                        if (newPassword != null && !newPassword.isBlank()) {
                            user.setPassword(passwordEncoder.encode(newPassword));
                            userUpdated = true;
                        }
                    }
                    if (userUpdated) {
                        user.setUpdatedAt(java.time.LocalDateTime.now());
                        userRepository.save(user);
                    }
                });
            }

            return ResponseEntity.ok(driverRepository.save(driver));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateDriverStatus(@PathVariable Long id, @RequestParam String status,
                                                 @AuthenticationPrincipal AuthenticatedUser principal) {
        // Tài xế chỉ được tự chọn "available" (sẵn sàng) hoặc "offline" (nghỉ).
        // "busy" do hệ thống tự đặt khi được phân công đơn — không cho đặt tay.
        if (!"available".equals(status) && !"offline".equals(status)) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Trạng thái không hợp lệ: chỉ được chọn 'available' hoặc 'offline'"));
        }
        return driverRepository.findById(id).<ResponseEntity<?>>map(driver -> {
            if (principal == null || (!principal.isAdmin() && !principal.userId().equals(driver.getUserId()))) {
                return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền đổi trạng thái của tài xế này"));
            }
            boolean hasActiveTrip = tripRepository.findByDriverIdOrderByCreatedAtDesc(id).stream()
                .anyMatch(t -> "assigned".equals(t.getStatus()) || "accepted".equals(t.getStatus()) || "picked_up".equals(t.getStatus()));
            if (hasActiveTrip) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Tài xế đang có chuyến giao hàng chưa hoàn tất, không thể đổi trạng thái"));
            }
            driver.setStatus(status);
            return ResponseEntity.ok(driverRepository.save(driver));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/location")
    public ResponseEntity<?> updateLocation(@PathVariable String id,
                                            @RequestParam Double lat,
                                            @RequestParam Double lng) {
        Long longId;
        try { longId = Long.parseLong(id); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ")); }
        return driverRepository.findById(longId).map(driver -> {
            driver.setCurrentLat(lat);
            driver.setCurrentLng(lng);
            driver.setLastLocationUpdate(java.time.LocalDateTime.now());
            return ResponseEntity.ok(driverRepository.save(driver));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/trips")
    public ResponseEntity<?> getTrips(@PathVariable String id) {
        Long longId;
        try { longId = Long.parseLong(id); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ")); }
        return ResponseEntity.ok(tripRepository.findByDriverIdOrderByCreatedAtDesc(longId));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<?> getStats(@PathVariable String id) {
        Long longId;
        try { longId = Long.parseLong(id); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ")); }
        return driverRepository.findById(longId).map(driver -> {
            List<DeliveryTrip> trips = tripRepository.findByDriverIdOrderByCreatedAtDesc(longId);
            long activeTrips = trips.stream().filter(t -> !"delivered".equals(t.getStatus()) && !"cancelled".equals(t.getStatus())).count();
            long completed = trips.stream().filter(t -> "delivered".equals(t.getStatus())).count();
            double totalEarnings = completed * 15000;
            return ResponseEntity.ok(Map.of(
                "totalTrips", trips.size(),
                "activeTrips", activeTrips,
                "completedTrips", completed,
                "totalEarnings", totalEarnings,
                "rating", driver.getRating() != null ? driver.getRating() : 5.0
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        Long longId;
        try { longId = Long.parseLong(id); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("error", "ID không hợp lệ")); }
        if (driverRepository.existsById(longId)) {
            driverRepository.deleteById(longId);
            return ResponseEntity.ok(Map.of("message", "Đã xoá tài xế"));
        }
        return ResponseEntity.notFound().build();
    }
}
