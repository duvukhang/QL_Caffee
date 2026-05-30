package com.example.Admin.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Admin.Models.Good;

@Repository
public interface GoodRepository extends JpaRepository<Good, String> {
    // Kế thừa JpaRepository<TênModel, KiểuDữLiệuCủaKhóaChính>
    // Bạn đã tự động có các hàm: findById, save, delete, findAll...
}