# QL_Caffee

## Cau truc folder

```text
QL_Caffee/
|-- .mvn/                         # Cau hinh Maven wrapper
|-- Logs/                         # File log he thong
|-- src/
|   |-- main/
|   |   |-- java/com/example/Admin/
|   |   |   |-- Config/            # Cau hinh ung dung
|   |   |   |-- Controller/        # Controller cho Admin, Cashier, Customer, Manager, Public
|   |   |   |-- DTOS/              # Lop request/response/form DTO
|   |   |   |-- MidWare/           # Filter, JWT, xu ly loi
|   |   |   |-- Models/            # Entity/model cua he thong
|   |   |   |-- Repositories/      # Tang truy xuat du lieu
|   |   |   |-- Security/          # Cau hinh bao mat va JWT
|   |   |   |-- Service/           # Xu ly nghiep vu
|   |   |   |-- Util/              # Ham tien ich
|   |   |   `-- DemoApplication.java
|   |   `-- resources/
|   |       |-- static/
|   |       |   |-- css/            # File CSS
|   |       |   |-- img/            # Hinh anh tinh
|   |       |   |-- js/             # File JavaScript
|   |       |   `-- web/            # Trang HTML tinh cho admin/customer/manager
|   |       |-- templates/         # Giao dien Thymeleaf
|   |       |-- application.properties
|   |       `-- logback-spring.xml
|   `-- test/                      # Unit/integration test
|-- target/                        # Thu muc build sinh ra boi Maven
|-- uploads/                       # Anh/file nguoi dung upload
|-- pom.xml                        # Cau hinh dependency va build Maven
|-- mvnw, mvnw.cmd                 # Maven wrapper
`-- README.md
```

## Mo ta nhanh

- `Controller`: nhan request tu client va dieu huong xu ly.
- `Service`: chua logic nghiep vu cua ung dung.
- `Repositories`: lam viec voi database thong qua Spring Data.
- `Models`: dinh nghia cac bang/entity chinh.
- `templates` va `static`: chua giao dien, CSS, JavaScript va hinh anh.
