package com.example.Admin.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
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

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/index.html").setViewName("index");
        registry.addViewController("/login.html").setViewName("login");
        registry.addViewController("/profile.html").setViewName("profile");
        registry.addViewController("/admin/product.html").setViewName("admin/product");
        registry.addViewController("/admin/staff.html").setViewName("admin/staff");
        registry.addViewController("/manager/kho-detail.html").setViewName("manager/kho-detail");
        registry.addViewController("/manager/kho-record.html").setViewName("manager/kho-record");
        registry.addViewController("/manager/kho-stock.html").setViewName("manager/kho-stock");
    }
}
