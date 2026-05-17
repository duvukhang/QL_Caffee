package com.example.demo.Repositories;

import com.example.demo.Models.Category; // Hoặc tên file Model danh mục cha của bạn (có thể là Syscategory)
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> { 
    // Mặc định kiểu khóa chính CategoryId là String theo như file C# (CateId dạng string). 
    // Nếu trong file Model Category.java bạn đặt @Id là Integer/int thì nhớ đổi chữ String ở trên thành Integer nhé!
}