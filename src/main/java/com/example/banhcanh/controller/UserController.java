package com.example.banhcanh.controller;

import com.example.banhcanh.model.User;
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
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private boolean isSelfOrAdmin(AuthenticatedUser principal, Long targetId) {
        return principal != null && (principal.isAdmin() || principal.userId().equals(targetId));
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser principal) {
        if (!isSelfOrAdmin(principal, id)) {
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền xem thông tin tài khoản này"));
        }
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updated,
                                         @AuthenticationPrincipal AuthenticatedUser principal) {
        if (!isSelfOrAdmin(principal, id)) {
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền sửa tài khoản này"));
        }
        return userRepository.findById(id).map(user -> {
            if (updated.getFullName() != null) user.setFullName(updated.getFullName());
            if (updated.getPhone() != null) user.setPhone(updated.getPhone());
            if (updated.getAddress() != null) user.setAddress(updated.getAddress());
            if (updated.getEmail() != null) user.setEmail(updated.getEmail());
            user.setUpdatedAt(java.time.LocalDateTime.now());
            return ResponseEntity.ok(userRepository.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody Map<String, String> body,
                                             @AuthenticationPrincipal AuthenticatedUser principal) {
        if (!isSelfOrAdmin(principal, id)) {
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền đổi mật khẩu tài khoản này"));
        }
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập mật khẩu cũ và mới"));
        }
        return userRepository.findById(id).map(user -> {
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu cũ không đúng"));
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setUpdatedAt(java.time.LocalDateTime.now());
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Đã đổi mật khẩu thành công"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // === Admin operations ===

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            userRepository.delete(user);
            return ResponseEntity.ok(Map.of("message", "Đã xoá tài khoản " + user.getUsername()));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<?> changeUserRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newRole = body.get("role");
        if (newRole == null || (!newRole.equals("customer") && !newRole.equals("driver") && !newRole.equals("admin") && !newRole.equals("super_admin"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vai trò không hợp lệ. Chấp nhận: customer, driver, admin, super_admin"));
        }
        return userRepository.findById(id).map(user -> {
            user.setRole(newRole);
            user.setUpdatedAt(java.time.LocalDateTime.now());
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Đã đổi vai trò thành " + newRole));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/toggle-active")
    public ResponseEntity<?> toggleUserActive(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            user.setIsActive(!Boolean.TRUE.equals(user.getIsActive()));
            user.setUpdatedAt(java.time.LocalDateTime.now());
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("isActive", user.getIsActive()));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/admin-reset-password")
    public ResponseEntity<?> adminResetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu mới phải có ít nhất 6 ký tự"));
        }
        return userRepository.findById(id).map(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setUpdatedAt(java.time.LocalDateTime.now());
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Đã đặt lại mật khẩu thành công"));
        }).orElse(ResponseEntity.notFound().build());
    }
}
