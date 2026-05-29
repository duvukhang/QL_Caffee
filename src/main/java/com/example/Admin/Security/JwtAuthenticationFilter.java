package com.example.Admin.Security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = tokenProvider.validateAndParseToken(token);
                
                if (tokenProvider.isTokenValid(claims)) {
                    String userId = claims.getSubject();
                    
                    // Đọc Role từ Claim quy định (thường .NET lưu ở claim mang tên "role" hoặc loại định danh URI)
                    String role = claims.get("role", String.class);
                    if (role == null) {
                        role = claims.get("http://schemas.microsoft.com/ws/2008/06/identity/claims/role", String.class);
                    }

                    if (role != null) {
                        // Tạo danh sách quyền (Đảm bảo chuỗi trùng khớp chính xác chữ với cấu trúc DB cũ: Admin, Manager, Cashier)
                        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
                        
                        UsernamePasswordAuthenticationToken authentication = 
                                new UsernamePasswordAuthenticationToken(userId, null, authorities);
                                
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (Exception e) {
                // Token lỗi hoặc giả mạo -> Bỏ qua để bộ lọc SecurityFilterChain chặn lại ở bước sau
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}