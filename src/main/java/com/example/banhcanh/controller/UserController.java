package com.example.banhcanh.controller;

import com.example.banhcanh.model.Role;
import com.example.banhcanh.model.User;
import com.example.banhcanh.model.UserRole;
import com.example.banhcanh.repository.RoleRepository;
import com.example.banhcanh.repository.UserRepository;
import com.example.banhcanh.repository.UserRoleRepository;
import com.example.banhcanh.security.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private boolean isSelfOrAdmin(AuthenticatedUser principal, Long targetId) {
        return principal != null && (principal.isAdmin() || principal.userId().equals(targetId));
    }

    @GetMapping
    public List<User> getAllUsers(@AuthenticationPrincipal AuthenticatedUser principal) {
        List<User> allUsers = userRepository.findAll();
        if (principal == null || !principal.isSuperAdmin()) {
            return allUsers.stream()
                .filter(u -> !"super_admin".equals(u.getRole()))
                .collect(Collectors.toList());
        }
        return allUsers;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser principal) {
        if (!isSelfOrAdmin(principal, id)) {
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền xem thông tin tài khoản này"));
        }
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if ("super_admin".equals(user.getRole()) && principal != null && !principal.isSuperAdmin()) {
                return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền xem thông tin tài khoản này"));
            }
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updated,
                                         @AuthenticationPrincipal AuthenticatedUser principal) {
        if (!isSelfOrAdmin(principal, id)) {
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền sửa tài khoản này"));
        }
        return userRepository.findById(id).map(user -> {
            if ("super_admin".equals(user.getRole()) && principal != null && !principal.isSuperAdmin()) {
                return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền sửa tài khoản này"));
            }
            String newEmail = updated.getEmail();
            if (newEmail != null && !newEmail.equals(user.getEmail())) {
                if (userRepository.findByEmail(newEmail).isPresent()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Email đã được sử dụng bởi tài khoản khác"));
                }
                user.setEmail(newEmail);
            }
            if (updated.getFullName() != null) user.setFullName(updated.getFullName());
            if (updated.getPhone() != null) user.setPhone(updated.getPhone());
            if (updated.getAddress() != null) user.setAddress(updated.getAddress());
            if (updated.getAvatarUrl() != null) user.setAvatarUrl(updated.getAvatarUrl());
            user.setUpdatedAt(LocalDateTime.now());
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
            if ("super_admin".equals(user.getRole()) && principal != null && !principal.isSuperAdmin()) {
                return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền đổi mật khẩu tài khoản này"));
            }
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu cũ không đúng"));
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Đã đổi mật khẩu thành công"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // === Admin operations ===

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser principal) {
        return userRepository.findById(id).map(user -> {
            if ("super_admin".equals(user.getRole()) && principal != null && !principal.isSuperAdmin()) {
                return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền xoá tài khoản Super Admin"));
            }
            userRoleRepository.deleteByUserId(id);
            userRepository.delete(user);
            return ResponseEntity.ok(Map.of("message", "Đã xoá tài khoản " + user.getUsername()));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Transactional
    @PutMapping("/{id}/role")
    public ResponseEntity<?> changeUserRole(@PathVariable Long id, @RequestBody Map<String, String> body,
                                             @AuthenticationPrincipal AuthenticatedUser principal) {
        if (principal == null || !principal.isSuperAdmin()) {
            return ResponseEntity.status(403).body(Map.of("error", "Chỉ Super Admin mới được phân quyền"));
        }
        String newRole = body.get("role");
        if (newRole == null || (!newRole.equals("customer") && !newRole.equals("driver") && !newRole.equals("admin") && !newRole.equals("super_admin"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vai trò không hợp lệ. Chấp nhận: customer, driver, admin, super_admin"));
        }
        return userRepository.findById(id).map(user -> {
            // Update User.role String field
            user.setRole(newRole);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            // Sync RBAC: clear existing user_roles and create the appropriate one
            userRoleRepository.deleteByUserId(id);

            String roleName = "ROLE_" + newRole.toUpperCase();
            Optional<Role> roleOpt = roleRepository.findByName(roleName);
            Role role;
            if (roleOpt.isPresent()) {
                role = roleOpt.get();
            } else {
                role = new Role();
                role.setName(roleName);
                role.setDisplayName(newRole.substring(0, 1).toUpperCase() + newRole.substring(1));
                role.setIsActive(true);
                role = roleRepository.save(role);
            }

            UserRole userRole = new UserRole();
            userRole.setUserId(id);
            userRole.setRoleId(role.getId());
            userRoleRepository.save(userRole);

            return ResponseEntity.ok(Map.of("message", "Đã đổi vai trò thành " + newRole));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/toggle-active")
    public ResponseEntity<?> toggleUserActive(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser principal) {
        return userRepository.findById(id).map(user -> {
            if ("super_admin".equals(user.getRole()) && principal != null && !principal.isSuperAdmin()) {
                return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền khoá tài khoản Super Admin"));
            }
            user.setIsActive(!Boolean.TRUE.equals(user.getIsActive()));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("isActive", user.getIsActive()));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/admin-reset-password")
    public ResponseEntity<?> adminResetPassword(@PathVariable Long id, @RequestBody Map<String, String> body,
                                                  @AuthenticationPrincipal AuthenticatedUser principal) {
        if (principal == null || !principal.isSuperAdmin()) {
            return ResponseEntity.status(403).body(Map.of("error", "Chỉ Super Admin mới được đặt lại mật khẩu"));
        }
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu mới phải có ít nhất 6 ký tự"));
        }
        return userRepository.findById(id).map(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Đã đặt lại mật khẩu thành công"));
        }).orElse(ResponseEntity.notFound().build());
    }
}
