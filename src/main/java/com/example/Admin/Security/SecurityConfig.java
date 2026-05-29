package com.example.Admin.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
@EnableMethodSecurity 
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) 
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)) 
            
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", 
                    "/admin",
                    "/customer",
                    "/home/**",
                    "/cart/**",
                    "/order/**",
                    "/user/**",
                    "/*.html", 
                    "/web/**",
                    "/api/auth/**",  // 🛠️ ĐÃ FIX: MỞ KHÓA API ĐĂNG NHẬP, ĐĂNG KÝ
                    "/admin/*.html", 
                    "/manager/*.html", 
                    "/cashier/*.html",
                    "/css/**",
                    "/img/**",
                    "/js/**",
                    "/error/**",
                    "/uploads/**", 
                    "/public/**", 
                    "/test/**", 
                    "/v3/api-docs/**", 
                    "/swagger-ui/**"
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/Customer/Register").permitAll()
                .requestMatchers(HttpMethod.POST, "/Customer/ForgotPassword").permitAll()
                .requestMatchers("/Customer/**").hasAuthority("Customer")
                
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        String envWorkFactor = System.getenv("WorkFactor");
        int workFactor = (envWorkFactor != null) ? Integer.parseInt(envWorkFactor) : 9;
        return new BCryptPasswordEncoder(workFactor);
    }
}
