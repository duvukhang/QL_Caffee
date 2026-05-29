package com.example.Admin.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Admin.Models.Sysrole;

@Repository
public interface SysRoleRepository extends JpaRepository<Sysrole, String> {
    // Mặc định kiểu khóa chính RoleId là String theo cấu trúc chuỗi của bảng quyền
}