package com.example.Admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.SQLException;

@SpringBootApplication(scanBasePackages = "com.example.Admin")
@EntityScan(basePackages = {"com.example.Admin.Models", "com.example.Admin.Shop.Model"})
@EnableJpaRepositories(basePackages = {"com.example.Admin.Repositories", "com.example.Admin.Shop.Repository"})
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

        ensureDemoDatabaseExists();
        SpringApplication.run(Program.class, args);
    }

    private static void ensureDemoDatabaseExists() {
        String username = propertyOrEnv("DB_USERNAME", "sa");
        String password = propertyOrEnv("DB_PASSWORD", "123123");
        String masterUrl = "jdbc:sqlserver://127.0.0.1:1433;databaseName=master;encrypt=true;trustServerCertificate=true;";
        try (var connection = DriverManager.getConnection(masterUrl, username, password);
                var statement = connection.createStatement()) {
            statement.executeUpdate("IF DB_ID(N'StoreManagement1') IS NULL CREATE DATABASE StoreManagement1");
            System.out.println("StoreManagement1 database is ready.");
        } catch (SQLException ex) {
            System.out.println("Could not auto-create StoreManagement1. Run create-database.sql if the database is missing. "
                    + ex.getMessage());
        }

        String appUrl = "jdbc:sqlserver://127.0.0.1:1433;databaseName=StoreManagement1;encrypt=true;trustServerCertificate=true;";
        try (var connection = DriverManager.getConnection(appUrl, username, password);
                var statement = connection.createStatement()) {
            statement.executeUpdate("IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = 'management') EXEC('CREATE SCHEMA management')");
            statement.executeUpdate("IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = 'customers') EXEC('CREATE SCHEMA customers')");
        } catch (SQLException ex) {
            System.out.println("Could not auto-create support schemas. " + ex.getMessage());
        }

        ensureUnicodeShopColumns(username, password);
    }

    private static void ensureUnicodeShopColumns(String username, String password) {
        String appUrl = "jdbc:sqlserver://127.0.0.1:1433;databaseName=StoreManagement1;encrypt=true;trustServerCertificate=true;";
        try (var connection = DriverManager.getConnection(appUrl, username, password);
                var statement = connection.createStatement()) {
            for (String migrationSql : unicodeColumnMigrations()) {
                try {
                    statement.executeUpdate(migrationSql);
                } catch (SQLException ex) {
                    System.out.println("Skipped a shop Unicode column migration. " + ex.getMessage());
                }
            }
            System.out.println("Shop Unicode columns are ready.");
        } catch (SQLException ex) {
            System.out.println("Could not update shop Unicode columns. " + ex.getMessage());
        }
    }

    private static String[] unicodeColumnMigrations() {
        return new String[] {
                """
                IF OBJECT_ID(N'dbo.shop_users', N'U') IS NOT NULL
                BEGIN
                    IF OBJECT_ID(N'dbo.uk_shop_users_username', N'UQ') IS NOT NULL ALTER TABLE dbo.shop_users DROP CONSTRAINT uk_shop_users_username;
                    IF OBJECT_ID(N'dbo.uk_shop_users_email', N'UQ') IS NOT NULL ALTER TABLE dbo.shop_users DROP CONSTRAINT uk_shop_users_email;
                    IF COL_LENGTH(N'dbo.shop_users', N'username') IS NOT NULL ALTER TABLE dbo.shop_users ALTER COLUMN username NVARCHAR(60) NOT NULL;
                    IF COL_LENGTH(N'dbo.shop_users', N'email') IS NOT NULL ALTER TABLE dbo.shop_users ALTER COLUMN email NVARCHAR(120) NOT NULL;
                    IF COL_LENGTH(N'dbo.shop_users', N'passwordHash') IS NOT NULL ALTER TABLE dbo.shop_users ALTER COLUMN passwordHash NVARCHAR(100) NOT NULL;
                    IF COL_LENGTH(N'dbo.shop_users', N'fullName') IS NOT NULL ALTER TABLE dbo.shop_users ALTER COLUMN fullName NVARCHAR(120) NOT NULL;
                    IF COL_LENGTH(N'dbo.shop_users', N'phone') IS NOT NULL ALTER TABLE dbo.shop_users ALTER COLUMN phone NVARCHAR(20) NULL;
                    IF COL_LENGTH(N'dbo.shop_users', N'address') IS NOT NULL ALTER TABLE dbo.shop_users ALTER COLUMN address NVARCHAR(300) NULL;
                    IF OBJECT_ID(N'dbo.uk_shop_users_username', N'UQ') IS NULL AND COL_LENGTH(N'dbo.shop_users', N'username') IS NOT NULL ALTER TABLE dbo.shop_users ADD CONSTRAINT uk_shop_users_username UNIQUE (username);
                    IF OBJECT_ID(N'dbo.uk_shop_users_email', N'UQ') IS NULL AND COL_LENGTH(N'dbo.shop_users', N'email') IS NOT NULL ALTER TABLE dbo.shop_users ADD CONSTRAINT uk_shop_users_email UNIQUE (email);
                END
                """,
                """
                IF OBJECT_ID(N'dbo.shop_categories', N'U') IS NOT NULL
                BEGIN
                    IF OBJECT_ID(N'dbo.uk_shop_categories_name', N'UQ') IS NOT NULL ALTER TABLE dbo.shop_categories DROP CONSTRAINT uk_shop_categories_name;
                    IF COL_LENGTH(N'dbo.shop_categories', N'name') IS NOT NULL ALTER TABLE dbo.shop_categories ALTER COLUMN name NVARCHAR(120) NOT NULL;
                    IF COL_LENGTH(N'dbo.shop_categories', N'description') IS NOT NULL ALTER TABLE dbo.shop_categories ALTER COLUMN description NVARCHAR(500) NULL;
                    IF OBJECT_ID(N'dbo.uk_shop_categories_name', N'UQ') IS NULL AND COL_LENGTH(N'dbo.shop_categories', N'name') IS NOT NULL ALTER TABLE dbo.shop_categories ADD CONSTRAINT uk_shop_categories_name UNIQUE (name);
                END
                """,
                """
                IF OBJECT_ID(N'dbo.shop_brands', N'U') IS NOT NULL
                BEGIN
                    IF OBJECT_ID(N'dbo.uk_shop_brands_name', N'UQ') IS NOT NULL ALTER TABLE dbo.shop_brands DROP CONSTRAINT uk_shop_brands_name;
                    IF COL_LENGTH(N'dbo.shop_brands', N'name') IS NOT NULL ALTER TABLE dbo.shop_brands ALTER COLUMN name NVARCHAR(120) NOT NULL;
                    IF OBJECT_ID(N'dbo.uk_shop_brands_name', N'UQ') IS NULL AND COL_LENGTH(N'dbo.shop_brands', N'name') IS NOT NULL ALTER TABLE dbo.shop_brands ADD CONSTRAINT uk_shop_brands_name UNIQUE (name);
                END
                """,
                """
                IF OBJECT_ID(N'dbo.shop_products', N'U') IS NOT NULL
                BEGIN
                    IF OBJECT_ID(N'dbo.uk_shop_products_slug', N'UQ') IS NOT NULL ALTER TABLE dbo.shop_products DROP CONSTRAINT uk_shop_products_slug;
                    IF COL_LENGTH(N'dbo.shop_products', N'name') IS NOT NULL ALTER TABLE dbo.shop_products ALTER COLUMN name NVARCHAR(180) NOT NULL;
                    IF COL_LENGTH(N'dbo.shop_products', N'slug') IS NOT NULL ALTER TABLE dbo.shop_products ALTER COLUMN slug NVARCHAR(220) NOT NULL;
                    IF COL_LENGTH(N'dbo.shop_products', N'description') IS NOT NULL ALTER TABLE dbo.shop_products ALTER COLUMN description NVARCHAR(MAX) NULL;
                    IF OBJECT_ID(N'dbo.uk_shop_products_slug', N'UQ') IS NULL AND COL_LENGTH(N'dbo.shop_products', N'slug') IS NOT NULL ALTER TABLE dbo.shop_products ADD CONSTRAINT uk_shop_products_slug UNIQUE (slug);
                END
                """,
                """
                IF OBJECT_ID(N'dbo.shop_product_images', N'U') IS NOT NULL
                BEGIN
                    IF COL_LENGTH(N'dbo.shop_product_images', N'imagePath') IS NOT NULL ALTER TABLE dbo.shop_product_images ALTER COLUMN imagePath NVARCHAR(500) NOT NULL;
                END
                """,
                """
                IF OBJECT_ID(N'dbo.shop_coupons', N'U') IS NOT NULL
                BEGIN
                    IF OBJECT_ID(N'dbo.uk_shop_coupons_code', N'UQ') IS NOT NULL ALTER TABLE dbo.shop_coupons DROP CONSTRAINT uk_shop_coupons_code;
                    IF COL_LENGTH(N'dbo.shop_coupons', N'code') IS NOT NULL ALTER TABLE dbo.shop_coupons ALTER COLUMN code NVARCHAR(40) NOT NULL;
                    IF OBJECT_ID(N'dbo.uk_shop_coupons_code', N'UQ') IS NULL AND COL_LENGTH(N'dbo.shop_coupons', N'code') IS NOT NULL ALTER TABLE dbo.shop_coupons ADD CONSTRAINT uk_shop_coupons_code UNIQUE (code);
                END
                """,
                """
                IF OBJECT_ID(N'dbo.shop_orders', N'U') IS NOT NULL
                BEGIN
                    IF OBJECT_ID(N'dbo.uk_shop_orders_order_code', N'UQ') IS NOT NULL ALTER TABLE dbo.shop_orders DROP CONSTRAINT uk_shop_orders_order_code;
                    IF COL_LENGTH(N'dbo.shop_orders', N'paymentStatus') IS NULL ALTER TABLE dbo.shop_orders ADD paymentStatus VARCHAR(30) NOT NULL CONSTRAINT df_shop_orders_paymentStatus DEFAULT 'UNPAID';
                    IF COL_LENGTH(N'dbo.shop_orders', N'paymentReference') IS NULL ALTER TABLE dbo.shop_orders ADD paymentReference NVARCHAR(80) NULL;
                    IF COL_LENGTH(N'dbo.shop_orders', N'paidAt') IS NULL ALTER TABLE dbo.shop_orders ADD paidAt DATETIME2 NULL;
                    IF COL_LENGTH(N'dbo.shop_orders', N'orderCode') IS NOT NULL ALTER TABLE dbo.shop_orders ALTER COLUMN orderCode NVARCHAR(40) NOT NULL;
                    IF COL_LENGTH(N'dbo.shop_orders', N'receiverName') IS NOT NULL ALTER TABLE dbo.shop_orders ALTER COLUMN receiverName NVARCHAR(120) NOT NULL;
                    IF COL_LENGTH(N'dbo.shop_orders', N'receiverPhone') IS NOT NULL ALTER TABLE dbo.shop_orders ALTER COLUMN receiverPhone NVARCHAR(20) NOT NULL;
                    IF COL_LENGTH(N'dbo.shop_orders', N'shippingAddress') IS NOT NULL ALTER TABLE dbo.shop_orders ALTER COLUMN shippingAddress NVARCHAR(300) NOT NULL;
                    IF COL_LENGTH(N'dbo.shop_orders', N'paymentReference') IS NOT NULL ALTER TABLE dbo.shop_orders ALTER COLUMN paymentReference NVARCHAR(80) NULL;
                    IF OBJECT_ID(N'dbo.uk_shop_orders_order_code', N'UQ') IS NULL AND COL_LENGTH(N'dbo.shop_orders', N'orderCode') IS NOT NULL ALTER TABLE dbo.shop_orders ADD CONSTRAINT uk_shop_orders_order_code UNIQUE (orderCode);
                END
                """,
                """
                IF OBJECT_ID(N'dbo.shop_order_items', N'U') IS NOT NULL
                BEGIN
                    IF COL_LENGTH(N'dbo.shop_order_items', N'productName') IS NOT NULL ALTER TABLE dbo.shop_order_items ALTER COLUMN productName NVARCHAR(180) NOT NULL;
                END
                """,
                """
                IF OBJECT_ID(N'dbo.shop_reviews', N'U') IS NOT NULL
                BEGIN
                    IF COL_LENGTH(N'dbo.shop_reviews', N'comment') IS NOT NULL ALTER TABLE dbo.shop_reviews ALTER COLUMN comment NVARCHAR(1000) NULL;
                END
                """,
                """
                IF OBJECT_ID(N'dbo.shop_inventory_history', N'U') IS NOT NULL
                BEGIN
                    IF COL_LENGTH(N'dbo.shop_inventory_history', N'note') IS NOT NULL ALTER TABLE dbo.shop_inventory_history ALTER COLUMN note NVARCHAR(500) NULL;
                END
                """
        };
    }

    private static String propertyOrEnv(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
