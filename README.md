# QL_Caffee

Spring Boot + Thymeleaf + SQL Server demo website bán hàng/cửa hàng.

## Yêu cầu

- Java 21
- SQL Server chạy ở `127.0.0.1:1433`
- Maven wrapper có sẵn trong repo

## Tạo database

Chạy script:

```sql
create-database.sql
```

Script tạo database `StoreManagement1` và các schema phụ còn cần cho một số entity cũ trong repo.

## Cấu hình

`src/main/resources/application.properties` mặc định dùng:

```properties
spring.datasource.url=jdbc:sqlserver://127.0.0.1:1433;databaseName=StoreManagement1;encrypt=true;trustServerCertificate=true;
spring.datasource.username=${DB_USERNAME:sa}
spring.datasource.password=${DB_PASSWORD:123123}
spring.jpa.hibernate.ddl-auto=update
```

Có thể override bằng biến môi trường:

```powershell
$env:DB_USERNAME="sa"
$env:DB_PASSWORD="123123"
```

## Chạy app

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

Mở: `http://localhost:8082`

## Tài khoản mẫu

- Admin cao nhất: `admin111 / 123123`
- Khách hàng: `customer1 / 123123`
- Khách hàng: `customer2 / 123123`

## Chức năng đã dựng cho demo

- Khách hàng: đăng ký, đăng nhập, xem sản phẩm, tìm/lọc/sắp xếp, xem chi tiết, quick view, giỏ hàng, áp coupon, checkout, lịch sử đơn, hủy đơn chờ xác nhận, mã khuyến mãi của tôi, gửi đánh giá sau khi mua.
- Admin: dashboard, quản lý danh mục, sản phẩm, tồn kho, coupon, gán coupon riêng cho khách, đơn hàng, người dùng, duyệt/xóa đánh giá.
- Database: dùng `StoreManagement1`, Hibernate tự sinh/cập nhật bảng `shop_*`, seed dữ liệu mẫu bằng `DataInitializer`.
- Coupon mẫu: `SALE10`, `GIAM50K`, `VIP20`, `EXPIRED10`.

## Ghi chú kỹ thuật

- Phần bán hàng mới dùng package `com.example.Admin.Shop.*` và bảng prefix `shop_` để không va chạm với entity cũ.
- JWT filter vẫn được giữ cho API cũ, đồng thời web MVC dùng Spring Security form-login/session.
- Entity cũ vẫn còn trong repo để tránh phá các controller cũ, nhưng flow demo chính nằm ở các route `/`, `/products`, `/cart`, `/checkout`, `/orders`, `/my-coupons`, `/admin/**`.
