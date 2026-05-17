package com.example.demo.MidWare.Filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class GlobalLoggerFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(GlobalLoggerFilter.class);
    
    // Sử dụng ConcurrentHashMap để an toàn khi chạy đa luồng (Multi-threading) trong Spring Boot
    private static final Map<String, AtomicInteger> APITotalCounter = new ConcurrentHashMap<>();
    private static final Map<String, List<LocalDateTime>> IpRequestLog = new ConcurrentHashMap<>();

    private static final int LIMIT = 10000;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        
        // Giả lập test chặn Rate Limit theo code cũ của bạn
        if (path.contains("/test/RateLimit")) {
            sendCustomError(response, 429, "Rate Limit", "Rate limit exceeded. Try again later.");
            return;
        }

        String ip = request.getRemoteAddr();
        if (ip == null) ip = "Unknown";

        LocalDateTime now = LocalDateTime.now();

        // 1. Đếm tổng số lần API được gọi
        APITotalCounter.computeIfAbsent(path, k -> new AtomicInteger(0)).incrementAndGet();

        // 2. Log request theo IP để tính Rate Limit (lọc trong vòng 1 giờ qua)
        List<LocalDateTime> timestamps = IpRequestLog.computeIfAbsent(ip, k -> new ArrayList<>());
        synchronized (timestamps) {
            timestamps.add(now);
            // Giữ lại các lượt gọi trong vòng 1 tiếng
            timestamps.removeIf(t -> Duration.between(t, now).toHours() >= 1);
        }

        int countLastHour = timestamps.size();

        // Kiểm tra nếu vượt ngưỡng LIMIT
        if (countLastHour > LIMIT) {
            sendCustomError(response, 429, "Rate Limit", "Rate limit exceeded. Try again later.");
            return;
        }

        int totalCall = APITotalCounter.get(path).get();
        long start = System.currentTimeMillis();

        // Cho phép request tiếp tục chạy vào Controller (Tương đương await _next(context))
        filterChain.doFilter(request, response);

        long elapse = System.currentTimeMillis() - start;

        // In log ra Console chuẩn format cũ
        log.isInfoEnabled();
        log.info("API: {}; User: {}; Process Time: {}ms; Call (1h): {}; Total call: {}", 
                path, ip, elapse, countLastHour, totalCall);
    }

    // Hàm phụ trợ xuất JSON trực tiếp từ Filter khi xảy ra lỗi chặn IP
    private void sendCustomError(HttpServletResponse response, int status, String error, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String json = String.format("{\"status\":%d,\"Error\":\"%s\",\"message\":\"%s\"}", status, error, message);
        response.getWriter().write(json);
    }
}
