# Staff Manager

Staff Manager là ứng dụng desktop Java dùng Swing và JDBC để quản lý nhân sự trên một bảng `staff`.
Chương trình hỗ trợ 5 thao tác cơ bản: hiển thị, thêm, cập nhật, xóa và reset dữ liệu trên giao diện `JFrame`.

## Yêu cầu

- JDK 21
- Docker và Docker Compose
- Với Windows nếu muốn dùng MySQL local: cài MySQL Server và công cụ `mysql` trong `PATH`

## Cấu trúc chính

```text
staff-manager/
├── README.md
├── TECH.md
├── dev.sh
├── dev.bat
├── docker-compose.yml
└── be/
    ├── build.gradle.kts
    ├── sql/schema.sql
    ├── spotless/license-header.java
    └── src/main/java/com/staffmanager/jdbc/
```

## Bảng `staff`

| Cột | Kiểu | Ý nghĩa |
|-----|------|---------|
| `ma_nv` | `VARCHAR(20)` | Mã nhân viên, khóa chính |
| `ho_ten` | `VARCHAR(255)` | Họ tên |
| `khoa` | `VARCHAR(100)` | Khoa / đơn vị |
| `he_so` | `DOUBLE` | Hệ số |
| `vai_tro` | `VARCHAR(20)` | `DOCTOR` hoặc `NURSE` |
| `sdt` | `VARCHAR(15)` | Số điện thoại |
| `email` | `VARCHAR(255)` | Email |
| `nam_kn` | `INT` | Năm kinh nghiệm |

File [schema.sql](/Users/hoang/my-app/rota-guard/be/sql/schema.sql:1) tạo bảng và seed sẵn 1 dòng dữ liệu mẫu.

## Cách chạy

### Ubuntu / macOS

```bash
./dev.sh
```

Script sẽ dùng MySQL trong Docker.

### Windows

```bat
dev.bat
```

Script Windows sẽ:
1. thử dùng MySQL local ở `127.0.0.1:3306` với tài khoản `staffmanager/staffmanager`
2. nếu không dùng được thì tự mở MySQL bằng Docker ở cổng `3307`
3. nạp dữ liệu mẫu và chạy ứng dụng desktop

## Build

Ubuntu / macOS:

```bash
./dev.sh build
```

Windows:

```bat
dev.bat build
```

## Tắt MySQL Docker

Ubuntu / macOS:

```bash
./dev.sh down
```

Windows:

```bat
dev.bat down
```

## Sử dụng giao diện

1. Bấm `Hiển thị` để tải danh sách nhân sự.
2. Nhập đầy đủ thông tin rồi bấm `Thêm` để tạo mới.
3. Chọn một dòng trong bảng để đổ dữ liệu lên form.
4. Sửa thông tin và bấm `Cập nhật`.
5. Chọn nhân sự và bấm `Xóa` để xóa.
6. Bấm `Reset` để đưa form về giá trị mặc định.

## Cấu hình kết nối cơ sở dữ liệu

File mặc định: `be/src/main/resources/db.properties`

```properties
db.url=jdbc:mysql://localhost:3306/staffmanager?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh
db.username=staffmanager
db.password=staffmanager
```

Có thể ghi đè bằng biến môi trường `STAFF_MANAGER_DB_URL`, `STAFF_MANAGER_DB_USERNAME`, `STAFF_MANAGER_DB_PASSWORD`.
