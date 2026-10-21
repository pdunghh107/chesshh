# ♟️ ChessHH Platform - Backend Microservices

> **Dự án:** Hệ thống chơi cờ vua trực tuyến thời gian thực tích hợp ghép trận tự động (Matchmaking) và hệ thống xếp hạng & tính điểm Elo chuẩn quốc tế.

---

## 📌 1. Tổng quan Kiến trúc (Architecture Overview)

Dự án được xây dựng theo kiến trúc **Microservices** trên nền tảng **Java 21** và **Spring Boot 3.4.x**, áp dụng các nguyên lý **Clean Architecture**, **Domain-Driven Design (DDD)** và **Transactional Outbox Pattern**:

```
                       ┌─────────────────────────┐
                       │   Client / Frontend     │
                       └────────────┬────────────┘
                                    │ HTTP / WebSocket
            ┌───────────────────────┴───────────────────────┐
            ▼                                               ▼
   ┌───────────────────┐                         ┌───────────────────┐
   │   auth-service    │                         │   chess-service   │
   │    (Port 8081)    │                         │    (Port 8082)    │
   └────────┬──────────┘                         └─────────┬─────────┘
            │                                              │
            │─── Outbox Event (RabbitMQ / AMQP) ──────────▶│
            │                                              │
    ┌───────┴───────┐                              ┌───────┴───────┐
    │ Postgres (auth)│                              │Postgres(chess)│
    │ Redis (Cache) │                              │ Redis (State) │
    └───────────────┘                              └───────────────┘
            ▲                                              ▲
            └──────────────────────┬───────────────────────┘
                                   │ Pull metrics
                        ┌──────────┴──────────┐
                        │     Prometheus      │
                        │    (Port: 9090)     │
                        └──────────┬──────────┘
                                   │
                        ┌──────────┴──────────┐
                        │      Grafana        │
                        │    (Port: 3000)     │
                        └─────────────────────┘
```

### Các Module trong Repository:
1. **`shared-kernel`**: Thư viện dùng chung chứa Core Exceptions, Base Entity, API Response Envelope, Events (`UserRegisteredEvent`), Context Security.
2. **`auth-service` (Port: 8081)**:
   - Quản lý tài khoản người dùng, xác thực danh tính qua **JWT (Access Token & Refresh Token)**.
   - Cơ chế Blacklist Token phân tán trên **Redis** (hỗ trợ Logout tức thì, Logout All, Deactivate Account).
   - Đảm bảo tính nhất quán dữ liệu giữa Auth và Chess qua **Transactional Outbox Pattern**.
3. **`chess-service` (Port: 8082)**:
   - Xử lý bàn cờ, nước đi, luật cờ vua thời gian thực qua **WebSocket / STOMP**.
   - Hàng đợi tìm trận đấu ngẫu nhiên (**Matchmaking Queue**).
   - Quản lý thông tin kỳ thủ, lịch sử đấu và cập nhật xếp hạng **Elo**.

---

## 🛠️ 2. Yêu cầu Môi trường (Prerequisites)

- **Java Development Kit (JDK):** Version 21 trở lên.
- **Docker & Docker Compose:** Để khởi chạy hệ thống hạ tầng (Postgres, Redis, RabbitMQ, Prometheus, Grafana).
- **Maven Wrapper:** Có sẵn trong thư mục dự án (`.\mvnw.cmd` trên Windows hoặc `./mvnw` trên Linux/macOS).

---

## 🚀 3. Khởi động Hạ tầng (Infrastructure Setup)

Toàn bộ dịch vụ hạ tầng được định nghĩa trong file [docker-compose.yml](file:///d:/dung/chesshh/docker-compose.yml).

### 3.1. Chạy tất cả container hạ tầng:
```powershell
docker compose up -d
```

### 3.2. Danh sách dịch vụ & Cổng truy cập (Ports):

| Dịch vụ | Container Name | Cổng (Port) | Tài khoản / Thông tin |
| :--- | :--- | :--- | :--- |
| **PostgreSQL** | `chesshh-postgres` | `5432` | User: `postgres` / Pass: `123456` / DB: `chesshh` |
| **Redis** | `chesshh-redis` | `6379` | Cache & Blacklist Store |
| **RabbitMQ** | `chesshh-rabbitmq` | `5672` (AMQP)<br>`15672` (UI) | Web UI: `http://localhost:15672`<br>User: `guest` / Pass: `guest` |
| **Prometheus** | `chesshh-prometheus` | `9090` | Web UI: `http://localhost:9090` |
| **Grafana** | `chesshh-grafana` | `3000` | Web UI: `http://localhost:3000`<br>User: `admin` / Pass: `admin` |

---

## 💻 4. Build & Chạy Ứng dụng (Build & Run)

### 4.1. Build toàn bộ dự án:
```powershell
# Windows:
.\mvnw.cmd clean install

# Linux / macOS:
./mvnw clean install
```

### 4.2. Chạy từng Microservice:
Mở các terminal riêng biệt để chạy:

- **Khởi chạy Auth Service:**
  ```powershell
  .\mvnw.cmd spring-boot:run -pl auth-service
  ```

- **Khởi chạy Chess Service:**
  ```powershell
  .\mvnw.cmd spring-boot:run -pl chess-service
  ```

### 4.3. Chạy Kiểm thử (Unit & Integration Tests):
```powershell
# Chạy toàn bộ test trong dự án:
.\mvnw.cmd test

# Chạy riêng test cho auth-service:
.\mvnw.cmd test -pl auth-service
```

---

## 📖 5. Hướng dẫn sử dụng Swagger / OpenAPI 3

Hệ thống đã tích hợp sẵn **OpenAPI 3 (SpringDoc UI)** cho phép xem thông tin chi tiết các API endpoint, model DTO và thử nghiệm gọi API trực tiếp.

### 5.1. Đường dẫn Swagger UI:
- **Auth Service:** [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
  - OpenAPI Spec JSON: [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)
- **Chess Service:** [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
  - OpenAPI Spec JSON: [http://localhost:8082/v3/api-docs](http://localhost:8082/v3/api-docs)

### 5.2. Cách Authorize JWT trên Swagger UI:
1. Gọi API `POST /api/v1/auth/login` để lấy `accessToken`.
2. Bấm vào nút **Authorize** (biểu tượng chiếc ổ khóa) ở góc trên bên phải màn hình Swagger UI.
3. Dán Access Token vào ô input theo định dạng: `Bearer <your_token>`.
4. Bấm **Authorize** để xác thực các API yêu cầu đăng nhập.

---

## 📊 6. Hướng dẫn Giám sát với Prometheus & Grafana

### 6.1. Kiểm tra Prometheus Scrape Targets:
1. Truy cập: [http://localhost:9090/targets](http://localhost:9090/targets)
2. Bạn sẽ thấy 2 target:
   - `auth-service` (cào từ `http://host.docker.internal:8081/actuator/prometheus`)
   - `chess-service` (cào từ `http://host.docker.internal:8082/actuator/prometheus`)
3. Trạng thái hiển thị màu xanh lá cây **UP** tức là metrics đang được thu thập theo chu kỳ 5 giây/lần.

### 6.2. Thiết lập Dashboard trên Grafana:
1. Đăng nhập [http://localhost:3000](http://localhost:3000) (Tài khoản: `admin` / `admin`).
2. **Prometheus Data Source** đã được tự động kết nối (provisioning qua file `infra/grafana/provisioning`).
3. **Import Dashboard chuyên dụng cho Spring Boot:**
   - Vào menu góc trái: **Dashboards** ➔ **New** ➔ **Import**.
   - Nhập Dashboard ID từ cộng đồng Grafana:
     - **`4701`** (JVM Micrometer): Giám sát chi tiết CPU, Heap/Non-Heap Memory, GC Pauses, HikariCP Connection Pool, Thread Pool.
     - **`19004`** (Spring Boot 3.x System Dashboard): Giám sát API Request Throughput, Latency P95/P99, Tỷ lệ mã lỗi HTTP 4xx/5xx.
   - Chọn Data Source là **Prometheus** và bấm **Import**.

---

## 📚 7. Hướng dẫn Sinh Tài liệu Javadoc

Dự án tuân thủ tiêu chuẩn tài liệu hóa Javadoc quy định tại [skills/writing-javadoc](file:///d:/dung/chesshh/skills/writing-javadoc).

### 7.1. Lệnh tạo toàn bộ Javadoc HTML:
Chạy lệnh sau tại thư mục gốc của dự án:
```powershell
# Windows:
.\mvnw.cmd javadoc:aggregate

# Linux / macOS:
./mvnw javadoc:aggregate
```

### 7.2. Xem tài liệu:
Sau khi lệnh thực thi thành công, mở tệp sau bằng trình duyệt web:
```text
target/reports/apidocs/index.html
```
*(Đường dẫn tuyệt đối: `d:\dung\chesshh\target\reports\apidocs\index.html`)*

Tài liệu Javadoc hiển thị cấu trúc package, sơ đồ phân cấp lớp, mô tả chi tiết `@param`, `@return`, `@throws` và các lưu ý kỹ thuật.

---

## 🛡️ 8. Quy chuẩn Lập trình (Coding Guidelines)

Mọi đóng góp mã nguồn mới cho dự án **ChessHH** cần tuân thủ nghiêm ngặt các điều khoản trong [.agents/AGENTS.md](file:///d:/dung/chesshh/.agents/AGENTS.md):
- **Brainstorming First:** Bắt buộc thảo luận và thống nhất giải pháp trước khi code.
- **DTO Pattern:** Không trả trực tiếp Entity ra Controller; sử dụng Java `record` cho dữ liệu bất biến (immutable).
- **Dependency Injection:** Sử dụng Constructor Injection thông qua `@RequiredArgsConstructor`, tuyệt đối không dùng `@Autowired` trên field.
- **Bảo mật:** Không lưu Access Token vào `localStorage` ở Frontend; Refresh Token luôn đặt trong `HttpOnly Cookie`.
