package com.example.Admin.Security;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/products/**",
                                "/login",
                                "/register",
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/uploads/**",
                                "/error/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**")
                        .permitAll()
                        .requestMatchers("/admin/dashboard").hasAnyRole("MANAGER", "ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/admin/categories/**", "/admin/products/**", "/admin/inventory/**",
                                "/admin/coupons/**", "/admin/reviews/**")
                        .hasAnyRole("MANAGER", "ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/admin/users", "/admin/users/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/admin/staff/*/role").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/admin/staff", "/admin/staff/**").hasAnyRole("MANAGER", "ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/admin/orders", "/admin/orders/**").hasAnyRole("STAFF", "MANAGER", "ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/staff/pos", "/staff/pos/**").hasAnyRole("STAFF", "MANAGER", "ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/admin").hasAnyRole("STAFF", "MANAGER", "ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/orders/*/cancel", "/orders/*/products/**").hasRole("CUSTOMER")
                        .requestMatchers("/cart", "/cart/**", "/checkout", "/payments/**", "/orders/**")
                        .hasAnyRole("CUSTOMER", "STAFF", "MANAGER", "ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/my-coupons").hasRole("CUSTOMER")
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> {
                            boolean staff = authentication.getAuthorities().stream()
                                    .anyMatch(authority -> "ROLE_STAFF".equals(authority.getAuthority()));
                            boolean managerOrAdmin = authentication.getAuthorities().stream()
                                    .anyMatch(authority -> List.of("ROLE_MANAGER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN")
                                            .contains(authority.getAuthority()));
                            if (staff) {
                                response.sendRedirect("/staff/pos");
                                return;
                            }
                            if (managerOrAdmin) {
                                response.sendRedirect("/admin/dashboard");
                                return;
                            }

                            RequestCache requestCache = new HttpSessionRequestCache();
                            SavedRequest savedRequest = requestCache.getRequest(request, response);
                            response.sendRedirect(savedRequest == null ? "/" : savedRequest.getRedirectUrl());
                        })
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .permitAll())
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/login?denied"));

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
