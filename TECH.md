# Staff Manager — Ghi chú kỹ thuật

## Tổng quan

| Hạng mục | Giá trị |
|----------|---------|
| Tên dự án | Staff Manager |
| Phiên bản | `0.1.0` |
| Kiến trúc | Ứng dụng desktop Java trong `be/`, cơ sở dữ liệu MySQL |

## Công nghệ sử dụng

| Thành phần | Công nghệ |
|------------|-----------|
| Ngôn ngữ | Java 21 |
| Build tool | Gradle wrapper |
| UI | Swing |
| Truy cập dữ liệu | JDBC thuần |
| Driver DB | MySQL Connector/J `8.4.0` |
| Cơ sở dữ liệu | MySQL 8 |
| Format mã | Spotless `6.25.0` + Google Java Format `1.22.0` |

Package mã nguồn: `com.staffmanager.jdbc`

## Cấu trúc mã nguồn

```text
be/src/main/java/com/staffmanager/jdbc/
├── Main.java
├── db/
│   ├── DatabaseConfig.java
│   ├── SchemaInitializer.java
│   └── StaffDao.java
├── model/
│   └── Staff.java
└── ui/
    └── StaffFrame.java
```

## Dữ liệu

| Hạng mục | Giá trị |
|----------|---------|
| Bảng duy nhất | `staff` |
| Schema | `be/sql/schema.sql` |
| Cấu hình DB | `be/src/main/resources/db.properties` |
| Kết nối mặc định | `jdbc:mysql://localhost:3306/staffmanager` |
| User / password | `staffmanager` / `staffmanager` |

## Script chạy

| Hệ điều hành | File |
|--------------|------|
| Ubuntu / macOS | `dev.sh` |
| Windows | `dev.bat` |

`dev.bat` ưu tiên MySQL local. Nếu không dùng được, script sẽ chuyển sang MySQL trong Docker.
