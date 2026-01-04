# 🛡️ ATK-DEF Backend

**Spring Boot 3.5 Backend API** cho hệ thống **Attack-Defense CTF Platform**.

Backend này đóng vai trò như một **API Gateway** với các chức năng:
- 🔐 **Authentication & Authorization** - JWT-based security
- 👥 **Team Management** - CRUD với auto-registration (quản lý trực tiếp `teams` table)
- 📤 **File Upload** - Checker scripts & VulnBox
- 🎮 **Game Control Proxy** - Forward requests tới Python Game Server (Logic xử lý game core)
- 📊 **Scoreboard Proxy** - Proxy tới Python service để lấy Real-time scoreboard
- 📖 **Swagger UI** - API Documentation tích hợp sẵn

---

## 📚 Mục lục

- [Tính năng](#-tính-năng)
- [Kiến trúc hệ thống](#-kiến-trúc-hệ-thống)
- [Cài đặt](#-cài-đặt)
- [Cấu hình](#-cấu-hình)
- [Swagger UI](#-swagger-ui)
- [API Reference](#-api-reference)
- [Database Shared Schema](#-database-shared-schema)
- [Deployment](#-deployment)

---

## 🚀 Tính năng

### Authentication & Security
| Tính năng | Mô tả |
|-----------|-------|
| JWT Token | Bearer token authentication (expiry 24h) |
| Role-based Access | ADMIN, TEACHER, TEAM permissions |
| Password Hashing | BCrypt encryption |
| Stateless Auth | Token-based, không dùng session |

### Team Management
| Tính năng | Mô tả |
|-----------|-------|
| Self-Registration | Teams tự đăng ký tài khoản |
| Admin Create | Admin tạo team với auto-generated credentials |
| CRUD Operations | Create, Read, Update, Delete teams |

### Game Control (Proxy Architecture)
| Tính năng | Mô tả |
|-----------|-------|
| Proxy Logic | Java backend đóng vai trò Proxy (Gateway) |
| Python Core | Mọi logic game (start/stop/tick/score) do Python xử lý |
| Shared DB | Java và Python dùng chung Database (schema `public`) |

---

## 📁 Kiến trúc hệ thống

```
┌─────────────────────────────────────────────────────────────┐
│                          CLIENT                             │
└─────────────────────────────┬───────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   Spring Boot (Proxy)                       │
│  ┌────────────────────────┐   ┌──────────────────────────┐  │
│  │    Auth / Team Mgt     │   │       Proxy APIs         │  │
│  │   (Direct DB Access)   │   │  (Forward to Python)     │  │
│  └───────────┬────────────┘   └────────────┬─────────────┘  │
└──────────────┼─────────────────────────────┼────────────────┘
               │ request                     │ request
               ▼                             ▼
┌─────────────────────────────┐  ┌────────────────────────────┐
│         PostgreSQL          │  │     Python Game Core       │
│      (Shared Schema)        │  │     (Game Logic)           │
│                             │  │                            │
│  Tables: teams (Shared)     │  │  Accesses DB Directly      │
│          games, flags...    │◄─┤  (games, flags, etc.)      │
└─────────────────────────────┘  └────────────────────────────┘
```

---

## ⚙️ Cài đặt

### Prerequisites

```bash
# Kiểm tra Java version (>= 21)
java -version

# Kiểm tra Maven (>= 3.8)
mvn -version

# Kiểm tra PostgreSQL (>= 15)
psql --version
```

### 1. Clone repository

```bash
git clone https://github.com/qthanh04/atk-def-backend.git
cd atk-def-backend
```

### 2. Cấu hình & Database

Hệ thống sử dụng **Shared Database** với Python backend.

```sql
-- Kết nối PostgreSQL
psql -U postgres

-- Tạo database
CREATE DATABASE adg_core;

-- Chúng ta sử dụng schema 'public' mặc định cho cả Java và Python
```

### 3. Build & Run

```bash
# Development mode
mvn spring-boot:run

# Hoặc build JAR
mvn clean package -DskipTests
java -jar target/atk-def-backend-0.0.1-SNAPSHOT.jar
```

🎉 Server chạy tại: `http://localhost:8080`

---

## 🔧 Cấu hình

File: `src/main/resources/application.yaml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/adg_core
    username: admin
    password: admin123
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
      properties:
        hibernate:
          dialect: org.hibernate.dialect.PostgreSQLDialect
          default_schema: public  # Quan trọng: Dùng public schema để share với Python

# Proxy Target
python:
  server-url: http://localhost:8000
```

---

## 📖 Swagger UI

Java Backend tích hợp sẵn **Swagger UI** để test API trực quan.

🔗 URL: **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

### Cách Authorize (Login) trên Swagger:
1. Vào mục **Auth** -> `POST /api/auth/signin`
2. Login để lấy `token`.
3. Click nút **Authorize** (ổ khóa) ở góc phải trên.
4. Paste token vào ô value (không cần prefix `Bearer `).
5. Sau đó có thể gọi các API protected.

---

## 📡 API Reference

### Auth & Teams (Java Managed)

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `POST` | `/api/auth/signin` | Login lấy JWT |
| `POST` | `/api/auth/signup` | Đăng ký Team mới |
| `GET` | `/api/teams` | Lấy danh sách Teams |

### Game Proxy (Forward to Python)

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `GET` | `/api/proxy/games` | Lấy danh sách games |
| `POST` | `/api/proxy/games` | Tạo game mới (Admin) |
| `POST` | `/api/proxy/games/{id}/start` | Start game |
| `POST` | `/api/proxy/submissions` | Submit Flag (Team) |
| `GET` | `/api/proxy/scoreboard` | Xem bảng điểm |

---

## 🗃️ Database Shared Schema

Bảng `teams` là bảng quan trọng nhất được chia sẻ:

```sql
CREATE TABLE public.teams (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    role VARCHAR(20) DEFAULT 'TEAM',
    ...
);
```

- **Java**: Quản lý ghi/đọc `teams` (User management).
- **Python**: Đọc `teams` để map vào Game, ghi điểm, v.v.
- Các bảng khác (`games`, `flags`, `ticks`...) do **Python** quản lý chính (Java chỉ truy cập qua Proxy API, không chọc thẳng DB entity).

---

## 👨‍💻 Authors

- **AnD Platform Team**
