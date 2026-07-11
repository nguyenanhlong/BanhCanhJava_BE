package com.example.banhcanh.security;

import com.example.banhcanh.model.User;
import com.example.banhcanh.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Reads "Authorization: Bearer <token>" and, if valid, populates the SecurityContext. */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtUtil.parseClaims(token);
                Long userId = claims.get("userId", Long.class);
                String jwtRole = claims.get("role", String.class);
                String username = claims.getSubject();

                // Load roles from DB (User.roles via user_roles table)
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    // 1) Roles from RBAC table (user_roles)
                    if (user.getRoles() != null) {
                        for (var role : user.getRoles()) {
                            if (Boolean.TRUE.equals(role.getIsActive())) {
                                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()));
                            }
                        }
                    }
                    // 2) Role from User.role String field (always included)
                    if (user.getRole() != null) {
                        String stringRole = "ROLE_" + user.getRole().toUpperCase();
                        boolean alreadyHas = authorities.stream()
                                .anyMatch(a -> a.getAuthority().equals(stringRole));
                        if (!alreadyHas) {
                            authorities.add(new SimpleGrantedAuthority(stringRole));
                        }
                    }
                }
                // Final fallback: use JWT role claim
                if (authorities.isEmpty()) {
                    String fallbackRole = (jwtRole == null) ? "CUSTOMER" : jwtRole.toUpperCase();
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + fallbackRole));
                }

                String displayRole = jwtRole != null ? jwtRole : "customer";
                AuthenticatedUser principal = new AuthenticatedUser(userId, username, displayRole);
                var authToken = new UsernamePasswordAuthenticationToken(
                        principal, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception e) {
                // Token không hợp lệ/hết hạn — bỏ qua, request đi tiếp như chưa xác thực.
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
