package com.example.Admin.Models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "sub_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubCategory {

    @Id
    // 🛠️ ĐÃ FIX LỖI CHÍNH TẢ: Thêm chữ 'o' và sửa thành SubcategoryId cho khớp CSDL
    @Column(name = "SubcategoryId", nullable = false)
    private String subCategory;

    @Column(name = "SubCategoryName", nullable = false)
    private String subCategoryName;

    @Column(name = "CategoryId", insertable = false, updatable = false)
    private String categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryId", nullable = false)
    private Category category;

    public String getSubCategoryId() {
        return subCategory;
    }

    public void setSubCategoryId(String subCategoryId) {
        this.subCategory = subCategoryId;
    }
    
}
