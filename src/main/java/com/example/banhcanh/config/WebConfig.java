package com.example.banhcanh.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// CORS is now configured centrally in SecurityConfig (CorsConfigurationSource bean),
// since Spring Security's filter chain runs before MVC and must own CORS handling.
@Configuration
public class WebConfig {

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    @Bean
    public WebMvcConfigurer resourceConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/api/uploads/**")
                        .addResourceLocations("file:" + uploadDir + "/");
            }
        };
    }
}