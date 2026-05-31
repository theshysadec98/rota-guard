# RotaGuard

Hệ thống hỗ trợ quản lý **lịch trực** và **đánh giá rủi ro mệt mỏi** cho nhân viên y tế (điều dưỡng, bác sĩ) theo từng tuần.

## Dự án làm gì?

RotaGuard giúp trưởng khoa / điều phối ca:

- Nhập **nhân sự** và **lịch trực tuần** (thủ công, CSV hoặc Excel).
- Chạy **phân tích** theo quy định mệt mỏi (nghỉ giữa ca, đêm liên tiếp, tổng giờ tuần, mức độ chỉnh sửa lịch).
- Xem **mức rủi ro** từng người (xanh / vàng / đỏ), chi tiết vi phạm và so sánh quy định.
- **Thử đổi ca** (what-if) và nhận **gợi ý đổi ca** trước khi áp dụng lên lịch thật.

Ứng dụng không thay thế hoàn toàn quyết định của lãnh đạo: kết quả phân tích và gợi ý là công cụ hỗ trợ; người phụ trách vẫn quyết định lịch cuối cùng.

## Giải quyết công việc gì?

| Vấn đề thực tế | RotaGuard xử lý |
|----------------|-----------------|
| Khó nhận ra ca “quay vòng” quá nhanh (nghỉ ngắn giữa ca đêm và ca ngày) | Rule **quick return**, báo RED/YELLOW kèm bằng chứng |
| Trùng hoặc quá nhiều ca đêm liên tiếp | Rule **consecutive night** |
| Một người vượt giờ làm tối đa trong tuần | Rule **weekly hours** |
| Lịch bị sửa liên tục, khó truy vết | **Churn index** từ lịch sử `shift_revision` |
| Không biết siết quy định sẽ ảnh hưởng bao nhiêu người | **So sánh hai policy** (policy diff) |
| Muốn thử đổi người trực trước khi ban hành | **What-if** và **gợi ý đổi ca** |

## Công nghệ sử dụng

| Tầng | Công nghệ | Phiên bản (tham chiếu) |
|------|-----------|-------------------------|
| **Ngôn ngữ BE** | Java (LTS) | **21** |
| **Framework BE** | Spring Boot | 3.3.5 |
| **Build BE** | Gradle | 8.11.1 |
| **ORM / API** | Spring Data JPA, Hibernate | Jakarta EE |
| **Truy vấn** | QueryDSL | 5.1.0 |
| **Cơ sở dữ liệu** | PostgreSQL | 16 |
| **Migration DB** | Flyway | (kèm Spring Boot) |
| **Tài liệu API** | SpringDoc OpenAPI (Swagger UI) | 2.6.0 |
| **Import Excel** | Apache POI | 5.2.5 |
| **Format code** | Spotless + Google Java Format | 6.25.0 / 1.22.0 |
| **FE** | React + TypeScript | 18.3 / 5.7 |
| **Bundler FE** | Vite | 6.0 |
| **Container** | Docker Compose, Eclipse Temurin | JRE/JDK 21 |

Múi giờ nghiệp vụ: **Asia/Ho_Chi_Minh** — kiểu `ZonedDateTime`, offset `+07:00` trong JSON/CSV.

Chi tiết đầy đủ (cấu trúc thư mục, thư viện, yêu cầu môi trường): xem **[TECH.md](TECH.md)**.

## Luồng sử dụng (UI)

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│ 1. Nhân sự  │ ──► │ 2. Lịch tuần│ ──► │ 3. Phân tích│
│ CRUD / CSV  │     │ Excel / CSV │     │ Báo cáo     │
└─────────────┘     │ / thủ công  │     │ What-if     │
                    └─────────────┘     │ Gợi ý đổi ca│
                                        └─────────────┘
```

1. **Nhân sự** — Thêm hoặc import danh sách (tên, vai trò, khoa, hệ số nhạy cảm).
2. **Lịch tuần** — Chọn **thứ Hai** đầu tuần (ISO), import file Excel tuần hoặc nhập/CSV từng ca; có thể thay thế cả tuần khi import.
3. **Phân tích** — Chọn quy định (policy), chạy phân tích, xem bảng rủi ro, vi phạm, so sánh policy, thử đổi ca và gợi ý.

Tuần demo trong database (sau khi migrate): **2025-05-12** (có sẵn ca vi phạm để thử nghiệm).

## Cách chạy project

### Yêu cầu

- Docker & Docker Compose (khuyến nghị), hoặc PostgreSQL local + JDK 21 + Node.js cho dev từng phần.

### Chạy full stack (Docker)

```bash
docker compose up --build -d
./dev.sh health
```

| Dịch vụ | Địa chỉ |
|---------|---------|
| PostgreSQL | `localhost:5432` (user/pass/db: `rotaguard`) |
| Backend | http://localhost:8080 |
| Giao diện RotaGuard (React) | http://localhost:5173 |
| **UI mới (prototype Hospia)** | http://localhost:5500 — `./dev.sh prototype` |
| Swagger | http://localhost:8080/swagger-ui.html |

### Lệnh `dev.sh`

```bash
./dev.sh prototype   # UI mới: specs PTIT-OOP (HTML tĩnh, không cần BE)
./dev.sh docker      # postgres + be + fe (nền)
./dev.sh postgres    # chỉ database
./dev.sh be          # postgres + backend (gradlew bootRun)
./dev.sh fe          # postgres + be container + vite dev (UI React)
./dev.sh health      # kiểm tra API health
./dev.sh build       # spotlessApply + build backend
./dev.sh up          # docker compose foreground (build + log)
```

**UI mới** (`docs/PTIT-OOP/prototype/`): demo ST/MG/SA theo spec — sidebar chuyển màn, đổi theme, modal; **không** gọi API RotaGuard.

### Dev từng phần

**Chỉ backend + DB:**

```bash
./dev.sh be
# API: http://localhost:8080/api/v1
```

**UI local, backend trong Docker:**

```bash
docker compose up -d postgres be
cd fe && npm install && npm run dev
# Mặc định FE gọi http://localhost:8080/api/v1
```

**Ghi đè URL API:**

```bash
VITE_API_URL=http://localhost:8080/api/v1 npm run dev
```

### Format mã Java

```bash
cd be && ./gradlew spotlessApply
```

## Thử nhanh bằng API (tuần demo)

```bash
# Chạy phân tích tuần seed
curl -X POST "http://localhost:8080/api/v1/analysis/run?weekStart=2025-05-12&policyId=1"

# Xem báo cáo (thay runId bằng id trả về)
curl "http://localhost:8080/api/v1/analysis/1/report"

# So sánh policy A vs B
curl -X POST http://localhost:8080/api/v1/analysis/policy-diff \
  -H "Content-Type: application/json" \
  -d '{"weekStart":"2025-05-12","policyIdA":1,"policyIdB":2}'

# Thử đổi ca (what-if)
curl -X POST http://localhost:8080/api/v1/analysis/what-if \
  -H "Content-Type: application/json" \
  -d '{"weekStart":"2025-05-12","policyId":1,"shiftChanges":[{"shiftId":2,"newStaffId":2}]}'
```

Tập lệnh HTTP đầy đủ: `be/http/apis.http`

## Dữ liệu mẫu

- Excel lịch tuần: `be/samples/roster-weekly.xlsx`, `fe/public/samples/`
- CSV ca / nhân sự: `be/samples/`, `fe/public/samples/`

Sau lần đầu chạy, Flyway tạo schema (`V1`), extension (`V2`), seed demo (`V3`). Nếu đổi migration và bị lỗi checksum:

```bash
docker compose down -v
docker compose up -d --build postgres be
```
