# 🛡️ ATK-DEF Backend

**Spring Boot 3.5 Backend API** cho hệ thống **Attack-Defense CTF Platform**.

Backend này đóng vai trò như một **API Gateway** với các chức năng:
- 🔐 **Authentication & Authorization** - JWT-based security
- 👥 **Team Management** - CRUD với auto-registration
- 📤 **File Upload** - Checker scripts & VulnBox
- 🎮 **Game Control Proxy** - Forward requests tới Python Game Server
- 📊 **Scoreboard Proxy** - Real-time scoreboard

---

## 📚 Mục lục

- [Tính năng](#-tính-năng)
- [Công nghệ](#-công-nghệ-sử-dụng)
- [Cài đặt](#️-cài-đặt)
- [Cấu hình](#-cấu-hình)
- [API Reference](#-api-reference)
- [Database Schema](#-database-schema)
- [Kiến trúc](#-kiến-trúc-hệ-thống)
- [Deployment](#-deployment)

---

## 🚀 Tính năng

### Authentication & Security
| Tính năng | Mô tả |
|-----------|-------|
| JWT Token | Bearer token authentication với expiry 24h |
| Role-based Access | ADMIN, TEACHER, TEAM permissions |
| Password Hashing | BCrypt encryption |
| Stateless Auth | Token-based, không dùng session |

### Team Management
| Tính năng | Mô tả |
|-----------|-------|
| Self-Registration | Teams tự đăng ký tài khoản |
| Admin Create | Admin tạo team với auto-generated credentials |
| Bulk Import | Import nhiều teams từ CSV file |
| CRUD Operations | Create, Read, Update, Delete teams |

### File Upload
| Tính năng | Mô tả |
|-----------|-------|
| Checker Upload | Upload Python checker scripts (.py) |
| VulnBox Upload | Upload VulnBox source code (.zip) |
| Challenge Association | Link files với challenge ID |

### Game Control (Proxy)
| Tính năng | Mô tả |
|-----------|-------|
| Start Game | Trigger game start qua Python server |
| Stop Game | Stop game gracefully |
| Game Status | Get current game state |
| Scoreboard | Real-time team rankings |

---

## 🔧 Công nghệ sử dụng

| Component | Technology | Version |
|-----------|------------|---------|
| **Framework** | Spring Boot | 3.5.6 |
| **Language** | Java | 21 |
| **Database** | PostgreSQL | 15+ |
| **ORM** | Spring Data JPA / Hibernate | 6.x |
| **Security** | Spring Security | 6.x |
| **Auth** | JWT (jjwt) | 0.12.6 |
| **Build** | Maven | 3.8+ |
| **Utils** | Lombok | 1.18.42 |

---

## ⚙️ Cài đặt

### Prerequisites

```bash
# Kiểm tra Java version
java -version    # Cần >= 21

# Kiểm tra Maven
mvn -version     # Cần >= 3.8

# Kiểm tra PostgreSQL
psql --version   # Cần >= 15
```

### 1. Clone repository

```bash
git clone https://github.com/qthanh04/atk-def-backend.git
cd atk-def-backend
```

### 2. Tạo Database

```sql
-- Kết nối PostgreSQL
psql -U postgres

-- Tạo database (nếu chưa có)
CREATE DATABASE adg_core;

-- Hoặc tạo schema trong database existing
\c postgres
CREATE SCHEMA IF NOT EXISTS adg_core;
```

### 3. Cấu hình environment

```bash
# Copy file cấu hình mẫu
cp src/main/resources/application.yaml.example src/main/resources/application.yaml

# Hoặc set environment variables
export DB_USERNAME=admin
export DB_PASSWORD=admin123
export JWT_SECRET=yourSuperSecretKeyMinimum32Characters
```

### 4. Build & Run

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

### application.yaml

```yaml
spring:
  application:
    name: atk-def-backend
    
  datasource:
    url: jdbc:postgresql://localhost:5432/adg_core
    username: ${DB_USERNAME:admin}
    password: ${DB_PASSWORD:admin123}
    driver-class-name: org.postgresql.Driver
    
  jpa:
    hibernate:
      ddl-auto: update           # auto-create tables
    show-sql: true               # log SQL queries
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        default_schema: adg_core

# JWT Configuration
jwt:
  secret: ${JWT_SECRET:mySecretKeyThatIsLongEnoughForJWT256Bits}
  expirationMs: 86400000         # 24 hours

# File Upload Paths
upload:
  checker-path: ./uploads/checkers
  vulnbox-path: ./uploads/vulnbox

# Python Game Server
python:
  server-url: ${PYTHON_SERVER_URL:http://localhost:8000}
```

### Environment Variables

| Variable | Default | Required | Mô tả |
|----------|---------|----------|-------|
| `DB_USERNAME` | admin | Yes | PostgreSQL username |
| `DB_PASSWORD` | admin123 | Yes | PostgreSQL password |
| `DATABASE_URL` | localhost:5432/adg_core | No | Full JDBC URL |
| `JWT_SECRET` | (fallback value) | **Yes (prod)** | JWT signing key (min 32 chars) |
| `PYTHON_SERVER_URL` | http://localhost:8000 | No | Python Game Server URL |
| `PORT` | 8080 | No | Server port |

---

## 📡 API Reference

### Base URL
```
http://localhost:8080/api
```

### Response Format
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation successful"
}
```

---

### 🔓 Public Endpoints (Không cần auth)

#### POST `/api/auth/login` - Đăng nhập

**Request:**
```json
{
  "username": "team1",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "team1",
  "teamName": "Team Alpha",
  "roles": ["ROLE_TEAM"]
}
```

**Error (401):**
```json
{
  "message": "Bad credentials"
}
```

---

#### POST `/api/auth/signup` - Đăng ký tài khoản

**Request:**
```json
{
  "username": "newteam",
  "password": "securePassword123",
  "teamName": "New Team",
  "country": "Vietnam",
  "affiliation": "HUST"
}
```

**Response (200 OK):**
```json
{
  "message": "Team registered successfully!",
  "teamId": 5
}
```

**Error (400):**
```json
{
  "message": "Username already taken!"
}
```

---

#### GET `/api/teams` - Danh sách teams (Public)

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Team Alpha",
    "country": "Vietnam",
    "affiliation": "HUST"
  },
  {
    "id": 2,
    "name": "Team Beta",
    "country": "Japan",
    "affiliation": "Tokyo University"
  }
]
```

---

#### GET `/api/scoreboard` - Bảng xếp hạng

**Response (200 OK):** *(Proxy từ Python server)*
```json
{
  "game_id": "abc-123",
  "current_tick": 15,
  "teams": [
    {
      "team_id": 1,
      "name": "Team Alpha",
      "score": 1500,
      "attack_points": 800,
      "defense_points": 700
    }
  ]
}
```

---

### 🔐 Protected Endpoints (Cần Bearer Token)

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

---

#### GET `/api/auth/me` - Thông tin user hiện tại

**Response (200 OK):**
```json
{
  "id": 1,
  "username": "team1",
  "teamName": "Team Alpha",
  "role": "TEAM"
}
```

---

### 👑 Admin/Teacher Only Endpoints

Yêu cầu role: `ROLE_ADMIN` hoặc `ROLE_TEACHER`

---

#### POST `/api/teams` - Tạo team mới (Auto-generate credentials)

**Request:**
```json
{
  "name": "Blue Team",
  "country": "Vietnam",
  "affiliation": "HUST",
  "ipAddress": "10.0.0.5"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "id": 10,
  "name": "Blue Team",
  "username": "blue_team",
  "defaultPassword": "blue_team123",
  "message": "Team created with auto-generated credentials"
}
```

> ⚠️ **Note:** Credentials chỉ hiển thị 1 lần. Hãy lưu lại!

---

#### PUT `/api/teams/{id}` - Cập nhật team

**Request:**
```json
{
  "name": "Blue Team Updated",
  "ipAddress": "10.0.0.10"
}
```

**Response (200 OK):**
```json
{
  "id": 10,
  "updated": true
}
```

---

#### DELETE `/api/teams/{id}` - Xóa team

**Response (200 OK):**
```json
{
  "message": "Team deleted successfully"
}
```

---

#### POST `/api/teams/bulk` - Import teams từ CSV

**Request:** `multipart/form-data`
```
file: teams.csv
```

**CSV Format:**
```csv
name,country,affiliation,ip_address
Team A,Vietnam,HUST,10.0.0.1
Team B,Japan,Tokyo U,10.0.0.2
```

**Response (200 OK):**
```json
{
  "success": true,
  "imported_count": 2,
  "teams": [
    {"id": 1, "name": "Team A", "username": "team_a", "password": "team_a123"},
    {"id": 2, "name": "Team B", "username": "team_b", "password": "team_b123"}
  ]
}
```

---

#### POST `/api/upload/checker` - Upload checker script

**Request:** `multipart/form-data`
```
file: checker.py
challengeId: 1
```

**Response (200 OK):**
```json
{
  "success": true,
  "filename": "checker_1_20241229.py",
  "path": "./uploads/checkers/checker_1_20241229.py",
  "challengeId": 1
}
```

---

#### POST `/api/upload/vulnbox` - Upload VulnBox

**Request:** `multipart/form-data`
```
file: vulnbox.zip
challengeId: 1
```

**Response (202 Accepted):**
```json
{
  "success": true,
  "filename": "vulnbox_1_20241229.zip",
  "path": "./uploads/vulnbox/vulnbox_1_20241229.zip",
  "challengeId": 1
}
```

---

#### GET `/api/game/status` - Trạng thái game

**Response (200 OK):**
```json
{
  "id": "game-uuid-123",
  "status": "running",
  "current_tick": 15,
  "total_ticks": 100,
  "tick_duration": 60,
  "teams_count": 10,
  "created_at": "2024-12-29T10:00:00Z"
}
```

---

#### POST `/api/game/start` - Bắt đầu game

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Game started",
  "game_id": "game-uuid-123"
}
```

**Error (400):**
```json
{
  "detail": "Vulnbox not uploaded",
  "status": 400,
  "success": false
}
```

---

#### POST `/api/game/stop` - Dừng game

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Game stopped"
}
```

---

## 🗃️ Database Schema

### Entity Relationship Diagram

```
┌─────────────────┐         ┌─────────────────┐
│     teams       │         │     games       │
├─────────────────┤         ├─────────────────┤
│ id (PK)         │         │ id (PK - UUID)  │
│ username        │         │ name            │
│ password        │    ┌───>│ status          │
│ role            │    │    │ current_tick    │
│ name            │    │    │ total_ticks     │
│ affiliation     │    │    │ tick_duration   │
│ country         │    │    │ created_at      │
│ ip_address      │    │    └─────────────────┘
│ created_at      │    │
└────────┬────────┘    │
         │             │
         │ 1:N         │
         ▼             │
┌─────────────────┐    │
│   game_teams    │────┘
├─────────────────┤
│ id (PK)         │
│ team_id (FK)    │
│ game_id (FK)    │
│ container_id    │
│ ssh_username    │
│ ssh_password    │
│ token           │
│ created_at      │
└─────────────────┘
```

### Tables Overview

| Table | Mô tả |
|-------|-------|
| `teams` | Team accounts & info |
| `games` | Game sessions |
| `game_teams` | Team participation in games |
| `ticks` | Game tick records |
| `flags` | Generated flags |
| `flag_submissions` | Submitted flags |
| `scoreboard` | Score snapshots |
| `service_status` | Service health checks |

---

## 📁 Kiến trúc hệ thống

### Project Structure

```
src/main/java/com/tool/atkdefbackend/
│
├── 📂 config/
│   └── security/
│       ├── AuthEntryPointJwt.java    # 401 handler
│       ├── AuthTokenFilter.java      # JWT filter
│       ├── JwtUtils.java             # JWT utility
│       └── WebSecurityConfig.java    # Security config
│
├── 📂 controller/
│   ├── AuthController.java           # /api/auth/*
│   ├── TeamController.java           # /api/teams/*
│   ├── GameController.java           # /api/game/*
│   ├── ScoreboardController.java     # /api/scoreboard
│   ├── UploadController.java         # /api/upload/*
│   └── TestController.java           # /api/test/*
│
├── 📂 entity/
│   ├── TeamEntity.java               # Team + Auth
│   ├── GameEntity.java               # Game session
│   ├── GameTeamEntity.java           # Team ↔ Game
│   ├── TickEntity.java               # Tick records
│   ├── FlagEntity.java               # Flags
│   ├── FlagSubmissionEntity.java     # Submissions
│   ├── ScoreboardEntity.java         # Scores
│   └── ServiceStatusEntity.java      # Health checks
│
├── 📂 enums/
│   ├── GameStatus.java               # PENDING, RUNNING, STOPPED
│   ├── TickStatus.java               # IN_PROGRESS, COMPLETED
│   ├── FlagType.java                 # ATTACK, DEFENSE
│   ├── SubmissionStatus.java         # VALID, INVALID, DUPLICATE
│   └── CheckStatus.java              # UP, DOWN, ERROR
│
├── 📂 model/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── SignUpRequest.java
│   │   ├── TeamSignUpRequest.java
│   │   ├── CreateTeamRequest.java
│   │   ├── UpdateTeamRequest.java
│   │   └── CreateUserRequest.java
│   │
│   └── response/
│       ├── JwtResponse.java
│       ├── MessageResponse.java
│       ├── TeamResponse.java
│       ├── TeamInfoResponse.java
│       ├── UserResponse.java
│       ├── UserInfoResponse.java
│       └── UploadResponse.java
│
├── 📂 repository/
│   └── TeamRepository.java
│
├── 📂 service/
│   ├── auth/
│   │   ├── AuthService.java          # Auth logic
│   │   ├── UserDetailsImpl.java      # UserDetails
│   │   └── UserDetailsServiceImpl.java
│   │
│   ├── TeamService.java              # Team CRUD
│   ├── FileUploadService.java        # File handling
│   └── PythonProxyService.java       # Python proxy
│
└── AtkDefBackendApplication.java     # Main entry point
```

### Request Flow

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENT                               │
└─────────────────────────┬───────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                   Spring Security                           │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              AuthTokenFilter                         │   │
│  │  • Extract JWT from Authorization header             │   │
│  │  • Validate token                                    │   │
│  │  • Set SecurityContext                               │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────┬───────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                   Controller Layer                          │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │    Auth     │ │    Team     │ │    Game     │           │
│  │ Controller  │ │ Controller  │ │ Controller  │           │
│  └──────┬──────┘ └──────┬──────┘ └──────┬──────┘           │
└─────────┼───────────────┼───────────────┼───────────────────┘
          │               │               │
          ▼               ▼               ▼
┌─────────────────────────────────────────────────────────────┐
│                    Service Layer                            │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────┐       │
│  │ AuthService │ │ TeamService │ │ PythonProxy     │       │
│  │             │ │             │ │ Service         │       │
│  └──────┬──────┘ └──────┬──────┘ └────────┬────────┘       │
└─────────┼───────────────┼─────────────────┼─────────────────┘
          │               │                 │
          ▼               ▼                 ▼
┌─────────────────┐ ┌─────────────┐ ┌─────────────────────────┐
│   PostgreSQL    │ │ File System │ │    Python Game Server   │
│   (teams, etc)  │ │  (uploads)  │ │    (localhost:8000)     │
└─────────────────┘ └─────────────┘ └─────────────────────────┘
```

---

## 🚢 Deployment

### Docker

**Dockerfile:**
```dockerfile
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/atk-def-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Build & Run:**
```bash
# Build
mvn clean package -DskipTests
docker build -t atk-def-backend .

# Run
docker run -p 8080:8080 \
  -e DB_USERNAME=admin \
  -e DB_PASSWORD=secret \
  -e DATABASE_URL=jdbc:postgresql://host:5432/db \
  -e JWT_SECRET=your-secret-key-min-32-chars \
  -e PYTHON_SERVER_URL=http://python-server:8000 \
  atk-def-backend
```


## 📄 License

MIT License - Free for educational purposes.

---

## 👨‍💻 Authors

- **AnD Platform Team**

---

## 🔗 Related Projects

- **[AnD.platform](../AnD.platform)** - Python Game Server
- **Frontend** - (Coming soon)
