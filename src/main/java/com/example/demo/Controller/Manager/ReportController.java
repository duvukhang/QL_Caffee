package com.example.demo.Controller.Manager;

import com.example.demo.Repositories.OrderRepository;
import com.example.demo.Repositories.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public ReportController(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        long totalOrders = orderRepository.count();
        long totalProducts = productRepository.count();
        
        stats.put("totalOrders", totalOrders);
        stats.put("totalProducts", totalProducts);
        stats.put("revenue", "15,500,000"); // Có thể cấu hình SUM doanh thu thực tế sau
        
        return ResponseEntity.ok(stats);
    }
}