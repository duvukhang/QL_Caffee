package com.example.Admin.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Admin.Models.SubCategory;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory, String> {

    Optional<SubCategory> findFirstBySubCategoryContaining(String value);
}
