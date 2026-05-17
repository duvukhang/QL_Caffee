package com.example.demo.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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

import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Kích hoạt @PreAuthorize("hasAnyRole(...)") ở tầng Controller
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Vô hiệu hóa CSRF cho API Stateless
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Kích hoạt cấu hình CORS toàn hệ thống
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Không lưu Session bên server
            
            // 🛠️ PHÂN QUYỀN ENDPOINT THEO ĐÚNG MAPPING GROUP CỦA .NET PROGRAM.CS
            .authorizeHttpRequests(auth -> auth
                // Cổng public, test và tài liệu Swagger được phép truy cập tự do công cộng
                .requestMatchers("/public/**", "/test/**", "/", "/index.html", "/static/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                
                // Ép điều kiện khớp chuỗi phân quyền chính xác tuyệt đối (Admin, Manager, Cashier)
                .requestMatchers("/admin/**").hasAuthority("Admin")
                .requestMatchers("/manager/**").hasAuthority("Manager")
                .requestMatchers("/cashier/**").hasAuthority("Cashier")
                
                // Mọi request còn lại bắt buộc phải truyền Token hợp lệ mới cho qua
                .anyRequest().authenticated()
            )
            // Đặt bộ lọc kiểm tra JWT Token lên trước khi thực hiện phân quyền truy cập
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 🛠️ ĐỒNG BỘ CẤU HÌNH DỊCH VỤ CORS (Cho phép mọi nguồn gọi tự do giống .NET)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Collections.singletonList("*")); // AllowAnyOrigin
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")); // AllowAnyMethod
        configuration.setAllowedHeaders(Collections.singletonList("*")); // AllowAnyHeader
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // 🛠️ ĐỒNG BỘ CƠ CHẾ MÃ HÓA MẬT KHẨU BCRYPT VỚI WORKFACTOR = 9 TỰ ĐỘNG
    @Bean
    public PasswordEncoder passwordEncoder() {
        String envWorkFactor = System.getenv("WorkFactor");
        int workFactor = (envWorkFactor != null) ? Integer.parseInt(envWorkFactor) : 9;
        return new BCryptPasswordEncoder(workFactor);
    }
}