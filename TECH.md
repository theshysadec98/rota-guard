# RotaGuard — Ghi chú kỹ thuật

## Tổng quan

| Hạng mục | Giá trị |
|----------|---------|
| Tên dự án | RotaGuard |
| Phiên bản | `0.1.0` |
| Kiến trúc | Ứng dụng desktop Java trong `be/`, PostgreSQL chạy bằng Docker Compose |

## Công nghệ sử dụng

| Thành phần | Công nghệ |
|------------|-----------|
| Ngôn ngữ | Java 21 |
| Build tool | Gradle wrapper |
| UI | Swing |
| Truy cập dữ liệu | JDBC thuần |
| Driver DB | PostgreSQL JDBC `42.7.4` |
| Cơ sở dữ liệu | PostgreSQL 16 |
| Format mã | Spotless `6.25.0` + Google Java Format `1.22.0` |

Package mã nguồn: `com.rotaguard.jdbc`

## Cấu trúc mã nguồn

```text
be/src/main/java/com/rotaguard/jdbc/
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
| Kết nối mặc định | `jdbc:postgresql://localhost:5432/rotaguard` |
| User / password | `rotaguard` / `rotaguard` |

## Lệnh chính

```bash
./dev.sh up
./dev.sh schema
./dev.sh run
./dev.sh build
./dev.sh down
```

Dự án chỉ gồm module desktop phục vụ CRUD nhân sự.
