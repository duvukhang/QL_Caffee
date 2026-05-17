package com.example.demo.Repositories;

import com.example.demo.Models.Sysrole; // Hoặc SysRole tùy thuộc vào tên file Model quyền của bạn
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysRoleRepository extends JpaRepository<Sysrole, String> {
    // Mặc định kiểu khóa chính RoleId là String theo cấu trúc chuỗi của bảng quyền
}