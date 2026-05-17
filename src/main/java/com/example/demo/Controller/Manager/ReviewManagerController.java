package com.example.demo.Controller.Manager;

import com.example.demo.DTOS.Respone.ReviewResponse;
import com.example.demo.Models.Review; // Import Model SQL
import com.example.demo.Repositories.ReviewRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/manager/review")
@PreAuthorize("hasAnyAuthority('Admin', 'Manager')")
public class ReviewManagerController {

    private final ReviewRepository reviewRepository;

    public ReviewManagerController(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @GetMapping
    public ResponseEntity<?> getStoreReviews() {
        try {
            // 1. Lấy danh sách Entity từ SQL
            List<Review> dbReviews = reviewRepository.findAll();
            
            // 2. Chuẩn bị đối tượng DTO Response
            ReviewResponse response = new ReviewResponse();
            response.setStoreId("ALL"); // Vì đã loại bỏ chi nhánh nên để ALL (toàn hệ thống)
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            // 3. Chuyển đổi (Mapping) từng Model SQL sang DTO (ReviewResponse.Review)
            for (Review r : dbReviews) {
                // Khởi tạo class Review nằm bên trong ReviewResponse
                ReviewResponse.Review dtoReview = new ReviewResponse.Review();
                
                dtoReview.setRating(r.getRating() != null ? r.getRating() : 0);
                dtoReview.setComment(r.getComment() != null ? r.getComment() : "Không bình luận");
                dtoReview.setStaffs(r.getStaffName());
                dtoReview.setCustommer(r.getCustomerName() != null ? r.getCustomerName() : "Ẩn danh");
                
                // Format ngày tháng từ kiểu LocalDateTime sang chuỗi String
                if (r.getCreatedAt() != null) {
                    dtoReview.setTime(r.getCreatedAt().format(formatter));
                } else {
                    dtoReview.setTime("");
                }
                
                // Thêm vào danh sách reviews của DTO
                response.getReviews().add(dtoReview);
            }

            if (response.getReviews().isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}