package com.example.banhcanh.controller;

import com.example.banhcanh.model.User;
import com.example.banhcanh.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updated) {
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
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập mật khẩu cũ và mới"));
        }
        return userRepository.findById(id).map(user -> {
            if (!user.getPassword().equals(oldPassword)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu cũ không đúng"));
            }
            user.setPassword(newPassword);
            user.setUpdatedAt(java.time.LocalDateTime.now());
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Đã đổi mật khẩu thành công"));
        }).orElse(ResponseEntity.notFound().build());
    }
}
