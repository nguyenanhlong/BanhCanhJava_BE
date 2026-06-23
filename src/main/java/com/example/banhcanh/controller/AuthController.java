package com.example.banhcanh.controller;

import com.example.banhcanh.model.User;
import com.example.banhcanh.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body("Tên đăng nhập đã tồn tại!");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("Email đã được đăng ký!");
        }
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        return userRepository.findByUsername(username)
            .map(user -> {
                if (user.getPassword().equals(password)) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("id", user.getId().toString());
                    response.put("username", user.getUsername());
                    response.put("email", user.getEmail());
                    response.put("role", user.getRole());
                    rep
                    return ResponseEntity.ok(response);
                }
                return ResponseEntity.badRequest().body("Mật khẩu không chính xác!");
            })
            .orElseGet(() -> ResponseEntity.badRequest().body("Tài khoản không tồn tại!"));
    }
}