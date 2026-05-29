package com.example.Admin.Service.SQL.Product;



import com.example.Admin.Models.Product;


public interface SqlProductService {
    Product createProducts(Product spMoi, String imgPath);

    int softDeleteProduct(String productId);
}

