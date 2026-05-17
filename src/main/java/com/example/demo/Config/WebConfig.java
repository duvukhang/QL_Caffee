package com.example.demo.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Lấy đường dẫn tuyệt đối đến thư mục uploads
        String uploadPath = "file:///" + System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
        
        // Map URL /uploads/** vào thư mục vật lý này
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}