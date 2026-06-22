package com.example.banhcanh.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Cho phép React frontend (localhost:3000 hoặc localhost:5173 / hoặc bất kỳ đâu) kết nối tới các ngõ API
                registry.addMapping("/**") // Thay đổi ở đây để phủ cả những đường dẫn khác
                        .allowedOriginPatterns("*") // Cho phép mọi nguồn (hoặc điền cụ thể "http://localhost:3000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}