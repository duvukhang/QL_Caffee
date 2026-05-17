package com.example.demo.Models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @Column(name = "OrderId", length = 10, nullable = false, columnDefinition = "char(10)")
    private String orderId;

    @Column(name = "Status", length = 50, nullable = false)
    private String status;

    @Column(name = "RecivingDate", nullable = false)
    private LocalDateTime recivingDate;

    @Column(name = "UpdateStatusDate", nullable = false)
    private LocalDateTime updateStatusDate;

    @Column(name = "CompleteDate")
    private LocalDateTime completeDate;

    @Column(name = "CustomerId", length = 10, columnDefinition = "char(10)")
    private String customerId;

    @Column(name = "SysUserId")
    private Integer sysUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", referencedColumnName = "UserId") 
    private Sysuser sysuser; 

    // 🛠️ ĐÃ FIX LỖI: Bổ sung mối quan hệ 1-Nhiều kết nối đến danh sách chi tiết hóa đơn
    // Tên biến đặt là orderDetails (CamelCase) giúp Lombok tự động sinh hàm getOrderDetails() 
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Collection<OrderDetail> orderDetails = new ArrayList<>();
}