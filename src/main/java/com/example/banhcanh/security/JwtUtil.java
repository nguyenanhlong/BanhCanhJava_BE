package com.example.banhcanh.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(@Value("${jwt.secret:}") String secret,
                   @Value("${jwt.expiration-ms:86400000}") long expirationMs) {
        this.expirationMs = expirationMs;
        if (secret == null || secret.isBlank()) {
            log.warn("JWT_SECRET chưa được cấu hình — dùng khoá ký ngẫu nhiên chỉ tồn tại trong phiên chạy này " +
                    "(token sẽ mất hiệu lực mỗi khi server khởi động lại). Hãy đặt biến môi trường JWT_SECRET " +
                    "(tối thiểu 32 ký tự) trong môi trường production, ví dụ trên Render.");
            byte[] randomKey = new byte[32];
            new SecureRandom().nextBytes(randomKey);
            this.key = Keys.hmacShaKeyFor(randomKey);
        } else {
            this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
    }

    public String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
