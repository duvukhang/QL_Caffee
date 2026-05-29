package com.example.Admin.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Admin.Models.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    // Spring Data JPA sẽ tự động tạo sẵn các hàm findAll(), findById(), save(),...
}