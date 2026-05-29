package com.example.Admin.DTOS.Respone;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ReviewResponse {
    private String storeId;
    private List<Review> reviews = new ArrayList<>();

    @Data
    public static class Review {
        private int rating;
        private String comment;
        private String staffs;
        private String time;
        private String custommer; // Giữ nguyên chính tả "custommer" của bạn
    }
}