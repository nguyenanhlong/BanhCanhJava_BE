package com.example.banhcanh.config;

import com.example.banhcanh.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ==== CÔNG KHAI: đăng ký/đăng nhập, duyệt xem sản phẩm/danh mục/khuyến mãi/đánh giá,
                //      xem ảnh, tạo đơn hàng (hỗ trợ khách vãng lai), cổng thanh toán MoMo (không gắn được JWT) ====
                .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
                .requestMatchers(HttpMethod.GET,
                        "/api/products/**", "/api/categories/**", "/api/promotions", "/api/promotions/*",
                        "/api/reviews/**", "/api/product-options/**", "/api/delivery-areas/**",
                        "/api/membership-tiers/**", "/api/upload/presigned").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/promotions/*/validate").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/orders").permitAll()
                .requestMatchers("/api/payments/momo/**").permitAll()
                .requestMatchers("/api/uploads/**").permitAll()

                // ==== Tự phục vụ của tài xế: cập nhật trạng thái/vị trí của CHÍNH mình
                //      (phải khai báo trước các luật admin-only /api/drivers/** và /api/delivery-trips/** bên dưới) ====
                .requestMatchers(HttpMethod.PUT, "/api/drivers/*/status", "/api/drivers/*/location").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/delivery-trips/*/status", "/api/delivery-trips/*/location").authenticated()

                // ==== CHỈ ADMIN/SUPER_ADMIN: quản trị nội dung & vận hành back-office ====
                .requestMatchers(HttpMethod.POST,
                        "/api/products", "/api/categories", "/api/product-options", "/api/promotions",
                        "/api/membership-tiers", "/api/delivery-areas", "/api/drivers/register", "/api/drivers",
                        "/api/upload/image", "/api/delivery-trips", "/api/invoices", "/api/invoice-details",
                        "/api/memberships/admin/vouchers", "/api/memberships/admin/vouchers/batch")
                        .hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.PUT,
                        "/api/products/**", "/api/categories/**", "/api/product-options/**", "/api/promotions/**",
                        "/api/membership-tiers/**", "/api/delivery-areas/**", "/api/drivers/**",
                        "/api/orders/*/assign-driver/**", "/api/orders/*/progress",
                        "/api/users/*/role", "/api/users/*/toggle-active", "/api/users/*/admin-reset-password",
                        "/api/invoices/**", "/api/payments/*/status", "/api/memberships/vouchers/**")
                        .hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.DELETE,
                        "/api/products/**", "/api/categories/**", "/api/product-options/**", "/api/promotions/**",
                        "/api/membership-tiers/**", "/api/delivery-areas/**", "/api/drivers/**", "/api/users/**")
                        .hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.GET,
                        "/api/users", "/api/orders/stats", "/api/invoices/**",
                        "/api/invoice-details/**", "/api/order-history/**", "/api/payments", "/api/payments/order/**",
                        "/api/delivery-trips", "/api/memberships/admin/**")
                        .hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers("/api/roles/**", "/api/permissions/**", "/api/role-permissions/**",
                        "/api/users/*/roles/**", "/api/users/current/permissions")
                        .hasAnyRole("ADMIN", "SUPER_ADMIN")

                // ==== Còn lại: chỉ cần đăng nhập (khách hàng, tài xế, admin đều dùng được) —
                //      quyền sở hữu (chỉ xem/sửa dữ liệu của chính mình) được kiểm tra thêm trong controller. ====
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
