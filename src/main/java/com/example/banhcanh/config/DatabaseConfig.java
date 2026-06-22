package com.example.banhcanh.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {
    // Để Spring Boot tự động kết nối và cấu hình DataSource (sử dụng HikariCP tối ưu hơn)
    // từ các thông số được cấu hình trong file application.properties.
    // Lớp này để trống giúp ngăn chặn xung đột hoặc rò rỉ kết nối khi chạy lặp lại.
}