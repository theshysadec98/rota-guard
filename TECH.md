# RotaGuard — Mô tả kỹ thuật & công nghệ

Tài liệu tham chiếu phiên bản và stack của dự án. Cập nhật theo `be/build.gradle.kts`, `fe/package.json`, `docker-compose.yml`.

## Tổng quan

| Hạng mục | Giá trị |
|----------|---------|
| Tên dự án | RotaGuard |
| Phiên bản | 0.1.0 |
| Nhóm Maven/Gradle | `com.rotaguard` |
| Kiến trúc | Monorepo: `be/` (API), `fe/` (SPA), PostgreSQL |

## Backend (Java)

### Runtime & build

| Công nghệ | Phiên bản | Ghi chú |
|-----------|-----------|---------|
| **Java** | **21** (LTS) | Toolchain trong Gradle; Docker: `eclipse-temurin:21` |
| Spring Boot | 3.3.5 | Web, JPA, Validation, Actuator |
| Gradle | 8.11.1 | Wrapper trong `be/gradle/wrapper` |
| Lombok | (BOM Spring Boot) | Giảm boilerplate entity/DTO |

### Spring & Jakarta

- `spring-boot-starter-web` — REST API `/api/v1`
- `spring-boot-starter-data-jpa` — Hibernate 6.x (Jakarta Persistence)
- `spring-boot-starter-validation` — Bean Validation (`@Valid`, …)
- `spring-boot-starter-actuator` — health endpoint

### Cơ sở dữ liệu

| Công nghệ | Phiên bản | Vai trò |
|-----------|-----------|---------|
| PostgreSQL | **16** (Alpine image) | Lưu nhân sự, ca, phân tích, vi phạm |
| Flyway | (managed) | Migration: `V1__schema`, `V2__extensions`, `V3__seed` |
| Extension `unaccent` | — | Hỗ trợ tìm kiếm (PostgreSQL) |

Connection mặc định (Docker): `jdbc:postgresql://postgres:5432/rotaguard`, user/pass `rotaguard`.

Cấu hình timezone JDBC: `Asia/Ho_Chi_Minh`.

### Thư viện chính

| Thư viện | Phiên bản | Mục đích |
|----------|-----------|----------|
| QueryDSL JPA | 5.1.0 | Tìm kiếm/sort nhân sự, lọc ca theo tuần |
| SpringDoc OpenAPI | 2.6.0 | Swagger UI: `/swagger-ui.html` |
| Apache POI OOXML | 5.2.5 | Đọc file Excel lịch tuần (`.xlsx`) |
| PostgreSQL JDBC | (runtime) | Driver |

### Chất lượng mã

- **Spotless** 6.25.0 — format Java, header `Nhom I`
- **Google Java Format** 1.22.0
- Task `check` gắn `spotlessCheck`

### Cấu trúc package (gợi ý)

```
com.rotaguard
├── config          # CORS, OpenAPI, rule beans
├── domain          # entity, enum
├── repository      # JPA + QueryDSL impl
├── rule            # fatigue rules (quick return, …)
├── service         # nghiệp vụ
├── support         # CSV/Excel, timezone, week utils
└── web             # controller, request/response, mapper
```

## Frontend

| Công nghệ | Phiên bản | Ghi chú |
|-----------|-----------|---------|
| React | 18.3.x | UI wizard 3 bước |
| TypeScript | 5.7.x | `tsc --noEmit` khi build |
| Vite | 6.0.x | Dev server port 5173 |
| @vitejs/plugin-react | 4.3.x | JSX/TSX |

Biến môi trường: `VITE_API_URL` (mặc định `http://localhost:8080/api/v1`).

## DevOps / chạy local

| Thành phần | Image / công cụ |
|------------|-----------------|
| PostgreSQL | `postgres:16-alpine` |
| Backend build | `eclipse-temurin:21-jdk-alpine` |
| Backend run | `eclipse-temurin:21-jre-alpine` |
| Orchestration | Docker Compose v2 |

Script tiện ích: `./dev.sh` (postgres, be, fe, docker, health, build).

## Yêu cầu môi trường dev

| Kịch bản | Cần có |
|----------|--------|
| Full Docker | Docker, Docker Compose |
| Backend local | JDK **21**, PostgreSQL 16 (hoặc container postgres) |
| Frontend local | Node.js 20+ (khuyến nghị LTS), npm |
| Gọi API thử | `curl`, Python 3 (cho script tùy chọn) |

## API & tích hợp

- Base path: `/api/v1`
- Định dạng: JSON; thời gian ca: ISO-8601 có offset (`+07:00`)
- File mẫu HTTP: `be/http/apis.http`
- CORS: origins cấu hình trong `application.yml` (`localhost:5173`, …)

## Không dùng trong repo hiện tại

- Kafka, Redis, microservices tách service
- NestJS BFF (FE gọi thẳng Spring Boot)
- Kubernetes manifest (chỉ Compose cho local)
