package com.example.banhcanh.security;

import com.example.banhcanh.model.Role;
import com.example.banhcanh.model.User;
import com.example.banhcanh.model.UserRole;
import com.example.banhcanh.repository.RoleRepository;
import com.example.banhcanh.repository.UserRepository;
import com.example.banhcanh.repository.UserRoleRepository;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Reads "Authorization: Bearer <token>" and, if valid, populates the SecurityContext. */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository,
                                   UserRoleRepository userRoleRepository, RoleRepository roleRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
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

                log.debug("[AUTH] userId={}, jwtRole={}, username={}", userId, jwtRole, username);

                // Build authorities from DB (direct query to user_roles table, bypassing JPA @ManyToMany)
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                // 1) Roles from user_roles table (direct query)
                List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
                log.debug("[AUTH] userRoles count={} for userId={}", userRoles.size(), userId);
                for (UserRole ur : userRoles) {
                    roleRepository.findById(ur.getRoleId()).ifPresent(role -> {
                        if (Boolean.TRUE.equals(role.getIsActive())) {
                            String rn = role.getName().toUpperCase();
                            if (!rn.startsWith("ROLE_")) rn = "ROLE_" + rn;
                            log.debug("[AUTH] RBAC authority added: {} (from role={})", rn, role.getName());
                            authorities.add(new SimpleGrantedAuthority(rn));
                        }
                    });
                }

                // 2) Role from User.role String field (always included)
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent() && userOpt.get().getRole() != null) {
                    String stringRole = "ROLE_" + userOpt.get().getRole().toUpperCase();
                    log.debug("[AUTH] User.role String = '{}' -> authority = {}", userOpt.get().getRole(), stringRole);
                    boolean alreadyHas = authorities.stream()
                            .anyMatch(a -> a.getAuthority().equals(stringRole));
                    if (!alreadyHas) {
                        authorities.add(new SimpleGrantedAuthority(stringRole));
                    }
                } else {
                    log.warn("[AUTH] User not found in DB for userId={}", userId);
                }

                // Final fallback: use JWT role claim
                if (authorities.isEmpty()) {
                    String fallbackRole = (jwtRole == null) ? "CUSTOMER" : jwtRole.toUpperCase();
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + fallbackRole));
                    log.debug("[AUTH] Fallback authority: ROLE_{}", fallbackRole);
                }

                log.info("[AUTH] user={} authorities={}", username, authorities);
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
