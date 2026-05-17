package com.example.demo.Repositories;

import com.example.demo.Models.Sysuser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional; // Nhớ thêm import này

@Repository
public interface SysUserRepository extends JpaRepository<Sysuser, Integer> {
    
    // ĐÃ THÊM: Khai báo hàm để Spring Boot tự sinh câu lệnh tìm kiếm theo UserName
    Optional<Sysuser> findByUserName(String userName);
}