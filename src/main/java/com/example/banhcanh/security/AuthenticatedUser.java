package com.example.banhcanh.security;

/** Principal populated by {@link JwtAuthenticationFilter} from a validated JWT's claims. */
public record AuthenticatedUser(Long userId, String username, String role) {

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role) || "super_admin".equalsIgnoreCase(role);
    }
}
