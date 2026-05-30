package com.example.Admin.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Lấy đường dẫn tuyệt đối đến thư mục uploads
        String uploadPath = Path.of(System.getProperty("user.dir"), "uploads").toUri().toString();

        // Map URL /uploads/** vào thư mục vật lý này
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }

}
