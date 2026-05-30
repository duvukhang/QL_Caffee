package com.example.Admin.Models;

import java.io.Serializable;
import java.math.BigDecimal;

public class CartItem implements Serializable {

    private String productId;
    private String productName;
    private String img;
    private BigDecimal price = BigDecimal.ZERO;
    private int quantity = 1;

    public CartItem() {
    }

    public CartItem(Product product) {
        this.productId = product.getProductId();
        this.productName = product.getProductName();
        this.img = product.getImg();
        this.price = product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();
        this.quantity = 1;
    }

    public BigDecimal getTotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price == null ? BigDecimal.ZERO : price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(quantity, 0);
    }
}
