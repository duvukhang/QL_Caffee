package com.example.demo.Service.SQL.Product;



import com.example.demo.Models.Product;


public interface SqlProductService {
    Product createProducts(Product spMoi, String imgPath);

    int softDeleteProduct(String productId);
}

