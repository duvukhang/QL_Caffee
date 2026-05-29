package com.example.Admin.Models;

import jakarta.persistence.*;
import lombok.*;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerId", referencedColumnName = "CustomerId", insertable = false, updatable = false)
    private Customer customer;

    @Column(name = "SysUserId")
    private Integer sysUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", referencedColumnName = "UserId") 
    private Sysuser sysuser; 

    // 🛠️ ĐÃ FIX LỖI: Bổ sung mối quan hệ 1-Nhiều kết nối đến danh sách chi tiết hóa đơn
    // Tên biến đặt là orderDetails (CamelCase) giúp Lombok tự động sinh hàm getOrderDetails() 
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Collection<OrderDetail> orderDetails = new ArrayList<>();

    public String getDisplayStatus() {
        return status;
    }

    public boolean isCancelable() {
        String normalized = normalizeStatus(status);
        return "tiep nhan".equals(normalized) || "cho xac nhan".equals(normalized);
    }

    public boolean getCancelable() {
        return isCancelable();
    }

    private String normalizeStatus(String value) {
        if (value == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(value.trim(), Normalizer.Form.NFD);
        return DIACRITICS.matcher(decomposed)
                .replaceAll("")
                .replace('\u0111', 'd')
                .replace('\u0110', 'd')
                .toLowerCase();
    }
}
