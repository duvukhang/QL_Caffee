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

Mở: `http://localhost:8088`

## Tài khoản mẫu

- Admin cao nhất: `admin111 / 123123`
- Nhân viên thu ngân: `staff1 / 123123`
- Khách hàng: `customer1 / 123123`
- Khách hàng: `customer2 / 123123`

## Chức năng đã dựng cho demo

- Khách hàng: đăng ký, đăng nhập, xem sản phẩm, tìm/lọc/sắp xếp, xem chi tiết, quick view, giỏ hàng, áp coupon, checkout, lịch sử đơn, hủy đơn chờ xác nhận, mã khuyến mãi của tôi, gửi đánh giá sau khi mua.
- Staff: tạo đơn tại quầy, thêm sản phẩm, kiểm tra tồn kho, áp mã khuyến mãi, chọn tiền mặt/chuyển khoản QR/thẻ/ví điện tử, hoàn thành đơn và in hóa đơn.
- Admin: dashboard, quản lý danh mục, sản phẩm, tồn kho, coupon, gán coupon riêng cho khách, đơn hàng, người dùng, duyệt/xóa đánh giá.
- Database: dùng `StoreManagement1`, Hibernate tự sinh/cập nhật bảng `shop_*`, seed dữ liệu mẫu bằng `DataInitializer`.
- Coupon mẫu: `SALE10`, `GIAM50K`, `VIP20`, `EXPIRED10`.

## Thanh toán QR local

- Checkout hỗ trợ `COD` và `BANK_QR_MANUAL` (`Chuyển khoản QR`).
- Khi khách chọn chuyển khoản QR, hệ thống tạo đơn với trạng thái thanh toán `Chờ thanh toán`, hiển thị mã đơn dạng `QLCF000001` làm nội dung chuyển khoản bắt buộc và QR tĩnh tại `src/main/resources/static/img/payment/qr-bank.png`.
- Vì chạy local cho đồ án nên thanh toán QR dùng cơ chế admin xác nhận thủ công trong trang quản lý đơn hàng.
- Khi deploy thật có thể tích hợp payOS/SePay webhook để tự động xác nhận giao dịch.

## Bán hàng tại quầy

- Staff đăng nhập sẽ vào `/staff/pos` để tạo đơn tại quầy.
- Đơn tại quầy dùng `orderType = POS` hoặc `TAKE_AWAY`, `paymentStatus = PAID`, `status = COMPLETED`, có `createdByStaff`, `paidAt`, `completedAt`.
- Backend tự tính giá, giảm giá, tổng tiền và trừ tồn kho trong transaction; frontend không được gửi tổng tiền để lưu trực tiếp.
- Hóa đơn đã hoàn thành xem tại `/staff/pos/orders/{id}` và in tại `/staff/pos/orders/{id}/receipt`.
- Đơn tại quầy xuất hiện trong `/admin/orders`, có cột loại đơn và nhân viên tạo; doanh thu dashboard tính theo đơn hoàn thành nên bao gồm đơn POS.

## Ghi chú kỹ thuật

- Phần bán hàng mới dùng package `com.example.Admin.Shop.*` và bảng prefix `shop_` để không va chạm với entity cũ.
- JWT filter vẫn được giữ cho API cũ, đồng thời web MVC dùng Spring Security form-login/session.
- Entity cũ vẫn còn trong repo để tránh phá các controller cũ, nhưng flow demo chính nằm ở các route `/`, `/products`, `/cart`, `/checkout`, `/orders`, `/my-coupons`, `/admin/**`.
