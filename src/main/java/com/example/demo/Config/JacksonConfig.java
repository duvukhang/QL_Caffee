package com.example.demo.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary // Tiên phong ưu tiên dùng Bean này khi có class yêu cầu Autowired
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 🌟 ĐĂNG KÝ MODULE XỬ LÝ DATE/TIME
        // Vì trong Model của bạn (như Order.java) có dùng LocalDateTime, 
        // dòng này giúp Jackson biên dịch mượt mà các kiểu ngày tháng sang JSON mà không bị lỗi
        mapper.registerModule(new JavaTimeModule());
        
        return mapper;
    }
}