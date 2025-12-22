# ATK-DEF Backend

Backend API cho hệ thống **Attack-Defense CTF** sử dụng Spring Boot.

## 🚀 Tính năng

- **Xác thực JWT** - Đăng nhập, phân quyền (Teacher/Student/Team)
- **Quản lý User** - CRUD users với role-based access
- **Quản lý Team** - CRUD teams, import bulk từ CSV
- **Upload Files** - Upload checker scripts (.py) và VulnBox (.zip)
- **Game Control** - Start/Stop/Status game (proxy tới Python server)
- **Scoreboard** - Real-time scoreboard (proxy tới Python server)

## 📋 Yêu cầu

- Java 21+
- Maven 3.8+
- PostgreSQL 15+
- (Optional) Python Game Server cho Game APIs

## ⚙️ Cài đặt

### 1. Clone repository
```bash
git clone <repo-url>
cd atk-def-backend
```

### 2. Cấu hình database
Sửa file `src/main/resources/application.yaml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb
    username: admin
    password: admin123
```

### 3. Chạy ứng dụng
```bash
mvn spring-boot:run
```

Server sẽ chạy tại: `http://localhost:8080`

## 📡 API Endpoints

### Authentication
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/auth/login` | Đăng nhập, nhận JWT token |
| POST | `/api/auth/signup` | Đăng ký tài khoản |
| GET | `/api/auth/me` | Lấy thông tin user hiện tại |

### User Management (Teacher only)
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/users` | Danh sách users |
| POST | `/api/users` | Tạo user mới |
| DELETE | `/api/users/{id}` | Xóa user |

### Team Management
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/teams` | Danh sách teams (public) |
| POST | `/api/teams` | Tạo team (Teacher) |
| POST | `/api/teams/bulk` | Import teams từ CSV (Teacher) |
| PUT | `/api/teams/{id}` | Sửa team (Teacher) |
| DELETE | `/api/teams/{id}` | Xóa team (Teacher) |

### File Upload (Teacher only)
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/upload/checker` | Upload checker script (.py) |
| POST | `/api/upload/vulnbox` | Upload VulnBox source (.zip) |

### Game Control (Teacher only)
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/game/start` | Bắt đầu game |
| POST | `/api/game/stop` | Dừng game |
| GET | `/api/game/status` | Trạng thái game |

### Scoreboard (Public)
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/scoreboard` | Bảng xếp hạng |

## 🔐 Authentication

Sử dụng JWT token trong header:
```bash
curl -H "Authorization: Bearer <your-jwt-token>" http://localhost:8080/api/auth/me
```

### Lấy token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "teacher1", "password": "123456"}'
```

## 📁 Cấu trúc dự án

```
src/main/java/com/tool/atkdefbackend/
├── config/security/     # JWT, Security config
├── controller/          # REST Controllers
├── entity/              # JPA Entities
├── model/               # DTOs (Request/Response)
├── repository/          # JPA Repositories
└── service/             # Business Logic
```

## 👥 Roles

| Role | Quyền hạn |
|------|-----------|
| TEACHER | Full access - quản lý users, teams, game |
| STUDENT | Xem thông tin, tham gia game |
| TEAM | Đại diện đội trong game |

## 🧪 Test APIs

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "teacher1", "password": "123456"}'

# Upload checker (với token)
curl -X POST http://localhost:8080/api/upload/checker \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@checker.py" -F "challengeId=1"

# Start game
curl -X POST http://localhost:8080/api/game/start \
  -H "Authorization: Bearer $TOKEN"

# Get scoreboard
curl http://localhost:8080/api/scoreboard
```

## 📝 License

MIT License
