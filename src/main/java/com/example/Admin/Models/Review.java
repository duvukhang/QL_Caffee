package com.example.Admin.Models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Reviews") // Tên bảng trong SQL Server
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Khóa chính tự tăng
    @Column(name = "ReviewId")
    private Integer reviewId;

    @Column(name = "CustomerName", length = 100)
    private String customerName;

    @Column(name = "Rating")
    private Integer rating;

    @Column(name = "StaffName", length = 100)
    private String staffName;

    @Column(name = "Comment", columnDefinition = "NVARCHAR(MAX)")
    private String comment;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;
}