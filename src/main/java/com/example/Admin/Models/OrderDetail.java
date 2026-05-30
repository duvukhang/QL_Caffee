package com.example.Admin.Models;

import com.example.Admin.Models.Key.OrderDetailId;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_detail")
@IdClass(OrderDetailId.class) // Khai báo sử dụng khóa chính kết hợp
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    // Đặt @Id ngay trên khóa ngoại luôn
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderId", nullable = false)
    private Order order;

    // Đặt @Id ngay trên khóa ngoại luôn
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductId", nullable = false)
    private Product product;
}