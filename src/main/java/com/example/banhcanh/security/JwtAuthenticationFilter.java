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

/** Reads "Authorization: Bearer <token>" and, if valid, populates the SecurityContext. */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

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

                // Build authorities from DB (direct query to user_roles table, bypassing JPA @ManyToMany)
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                // 1) Roles from user_roles table (direct query)
                for (UserRole ur : userRoleRepository.findByUserId(userId)) {
                    roleRepository.findById(ur.getRoleId()).ifPresent(role -> {
                        if (Boolean.TRUE.equals(role.getIsActive())) {
                            String rn = role.getName().toUpperCase();
                            if (!rn.startsWith("ROLE_")) rn = "ROLE_" + rn;
                            authorities.add(new SimpleGrantedAuthority(rn));
                        }
                    });
                }

                // 2) Role from User.role String field (always included)
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent() && userOpt.get().getRole() != null) {
                    String stringRole = "ROLE_" + userOpt.get().getRole().toUpperCase();
                    boolean alreadyHas = authorities.stream()
                            .anyMatch(a -> a.getAuthority().equals(stringRole));
                    if (!alreadyHas) {
                        authorities.add(new SimpleGrantedAuthority(stringRole));
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
