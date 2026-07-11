package com.example.banhcanh.controller;

import com.example.banhcanh.model.PasswordResetToken;
import com.example.banhcanh.model.User;
import com.example.banhcanh.repository.PasswordResetTokenRepository;
import com.example.banhcanh.repository.UserRepository;
import com.example.banhcanh.security.JwtUtil;
import com.example.banhcanh.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body("Tên đăng nhập đã tồn tại!");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("Email đã được đăng ký!");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        return userRepository.findByUsername(username)
            .map(user -> {
                if (passwordEncoder.matches(password, user.getPassword())) {
                    // Determine effective role from user_roles table (RBAC), fallback to User.role String
                    String effectiveRole = resolveEffectiveRole(user);
                    String token = jwtUtil.generateToken(user.getId(), user.getUsername(), effectiveRole);
                    Map<String, Object> response = new HashMap<>();
                    response.put("token", token);
                    response.put("id", user.getId().toString());
                    response.put("username", user.getUsername());
                    response.put("email", user.getEmail());
                    response.put("role", effectiveRole);
                    response.put("fullName", user.getFullName());
                    response.put("phone", user.getPhone());
                    response.put("address", user.getAddress());
                    response.put("avatarUrl", user.getAvatarUrl());
                    return ResponseEntity.ok(response);
                }
                return ResponseEntity.badRequest().body("Mật khẩu không chính xác!");
            })
            .orElseGet(() -> ResponseEntity.badRequest().body("Tài khoản không tồn tại!"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập email"));
        }
        return userRepository.findByEmail(email).map(user -> {
            tokenRepository.deleteByUserId(user.getId());
            String otp = emailService.generateOtp();
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUserId(user.getId());
            resetToken.setToken(otp);
            resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));
            tokenRepository.save(resetToken);
            try {
                emailService.sendOtpEmail(email, otp);
            } catch (Exception e) {
                return ResponseEntity.ok(Map.of(
                    "message", "Không thể gửi email. Mã OTP của bạn: " + otp,
                    "resetToken", otp,
                    "email", email
                ));
            }
            return ResponseEntity.ok(Map.of(
                "message", "Mã OTP đã được gửi đến email " + email + ". Vui lòng kiểm tra hộp thư đến.",
                "email", email
            ));
        }).orElse(ResponseEntity.badRequest().body(Map.of("error", "Email không tồn tại trong hệ thống")));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        if (token == null || newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mã token hoặc mật khẩu không hợp lệ (tối thiểu 6 ký tự)"));
        }
        return tokenRepository.findByToken(token).map(resetToken -> {
            if (resetToken.isUsed()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mã đặt lại mật khẩu đã được sử dụng"));
            }
            if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mã đặt lại mật khẩu đã hết hạn"));
            }
            return userRepository.findById(resetToken.getUserId()).map(user -> {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
                resetToken.setUsed(true);
                tokenRepository.save(resetToken);
                return ResponseEntity.ok(Map.of("message", "Mật khẩu đã được đặt lại thành công"));
            }).orElse(ResponseEntity.badRequest().body(Map.of("error", "Người dùng không tồn tại")));
        }).orElse(ResponseEntity.badRequest().body(Map.of("error", "Mã đặt lại mật khẩu không hợp lệ")));
    }

    /**
     * Resolve the effective role from User.roles (RBAC table).
     * Priority: SUPER_ADMIN > ADMIN > DRIVER > CUSTOMER.
     * Falls back to User.role String field if no roles in user_roles table.
     */
    private String resolveEffectiveRole(User user) {
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            for (String priority : List.of("SUPER_ADMIN", "ADMIN", "DRIVER")) {
                for (var r : user.getRoles()) {
                    if (r.getName().equalsIgnoreCase(priority) && Boolean.TRUE.equals(r.getIsActive())) {
                        return r.getName().toUpperCase();
                    }
                }
            }
            // Return the first active role found
            for (var r : user.getRoles()) {
                if (Boolean.TRUE.equals(r.getIsActive())) {
                    return r.getName().toUpperCase();
                }
            }
        }
        return user.getRole();
    }
}