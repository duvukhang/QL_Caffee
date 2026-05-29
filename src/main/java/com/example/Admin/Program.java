package com.example.Admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication(scanBasePackages = "com.example.Admin")
@EntityScan(basePackages = "com.example.Admin.Models")
@EnableJpaRepositories(basePackages = "com.example.Admin.Repositories")
public class Program {

    public static void main(String[] args) {
        try {
            Path envPath = Paths.get(".env");
            if (Files.exists(envPath)) {
                Files.lines(envPath)
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .forEach(line -> {
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2) {
                            String key = parts[0].trim();
                            String value = parts[1].trim();
                            
                            // 🛠️ BẪY SỐ 1: Quét sạch ký tự ẩn BOM (\uFEFF) ở đầu file do Windows sinh ra
                            if (key.startsWith("\uFEFF")) {
                                key = key.substring(1);
                            }
                            // Dự phòng thêm khoảng cách trắng đặc biệt
                            key = key.replace("\uFEFF", "").trim();
                            
                            // 🛠️ BẪY SỐ 2: Xóa bỏ dấu ngoặc kép bọc chuỗi (C# Env thường bọc "", nhưng Java properties sẽ giữ nguyên gây lỗi)
                            if (value.startsWith("\"") && value.endsWith("\"")) {
                                value = value.substring(1, value.length() - 1);
                            } else if (value.startsWith("'") && value.endsWith("'")) {
                                value = value.substring(1, value.length() - 1);
                            }
                            
                            // Nạp biến sạch vào hệ thống
                            System.setProperty(key, value);
                            System.out.println("✨  Đã ghi nhận biến: " + key);
                        }
                    });
                System.out.println("✅  Hệ thống đã đồng bộ hóa file .env sạch sẽ!");
            } else {
                System.out.println("⚠️  Không tìm thấy file .env tại thư mục gốc!");
            }
        } catch (IOException e) {
            System.err.println("❌ Lỗi cấu hình môi trường: " + e.getMessage());
        }

        SpringApplication.run(Program.class, args);
    }
}
