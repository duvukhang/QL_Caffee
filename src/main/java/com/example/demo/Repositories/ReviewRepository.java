package com.example.demo.Repositories;

import com.example.demo.Models.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    // Spring Data JPA sẽ tự động tạo sẵn các hàm findAll(), findById(), save(),...
}