package com.example.demo.Repositories;

import com.example.demo.Models.Good;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoodRepository extends JpaRepository<Good, String> {
    // Kế thừa JpaRepository<TênModel, KiểuDữLiệuCủaKhóaChính>
    // Bạn đã tự động có các hàm: findById, save, delete, findAll...
}