package com.example.Admin.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Admin.Models.Store;

@Repository
public interface StoreRepository extends JpaRepository<Store, String> {
    // Chỉ cần kế thừa JpaRepository, Spring Boot sẽ tự động "viết" sẵn cho bạn 
    // các hàm như: save(), findById(), findAll(), deleteById()... 
    // Bạn không cần phải code thêm bất kỳ dòng nào ở đây cả!
}