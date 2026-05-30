package com.example.Admin.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Admin.Models.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> { 
    // Mặc định kiểu khóa chính CategoryId là String theo như file C# (CateId dạng string). 
    // Nếu trong file Model Category.java bạn đặt @Id là Integer/int thì nhớ đổi chữ String ở trên thành Integer nhé!
}