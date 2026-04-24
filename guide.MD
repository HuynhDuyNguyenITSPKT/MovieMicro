# Chạy project bằng 1 file .env duy nhất

## 0) Phân tách trách nhiệm service

- `auth-service`: quản lý `Account`, đăng ký OTP email, đăng nhập password, đăng nhập Google, phát hành JWT.
- `user_service`: chỉ quản lý CRUD bảng `users` (profile), nhận đồng bộ profile nội bộ từ `auth-service`.
- `gateway`: route `/api/auth/**` và `/api/users/**`.

## 1) File cấu hình dùng chung

Tất cả service đọc biến từ file `.env` ở thư mục gốc project.

Chuẩn cấu hình hiện tại:

- Service kết nối MySQL trong Docker bằng `DB_HOST=mysql`, `DB_PORT=3306`
- Kết nối MySQL từ máy host bằng `localhost:${MYSQL_HOST_PORT}` (mặc định `3307`)
- App dùng user DB riêng: `DB_USERNAME=app_user` (không dùng root)
- `database/init.sql` sẽ tạo DB và cấp quyền cho `app_user` khi khởi tạo volume mới

Biến môi trường mới cần có cho auth + user:

```env
# Port
AUTH_PORT=8081
USER_PORT=8082
GATEWAY_PORT=8080

# DB
DB_HOST=mysql
DB_PORT=3306
DB_NAME=user_service_db
AUTH_DB_NAME=auth_service_db
DB_USERNAME=app_user
DB_PASSWORD=123456

# JWT
JWT_SECRET=replace-with-a-very-long-random-secret-key-at-least-32-bytes
JWT_EXPIRATION_MS=86400000
JWT_REFRESH_EXPIRATION_MS=604800000

# OTP / MAIL
OTP_EXPIRATION_MINUTES=5
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=no-reply@movieticket.local
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true

# Google OAuth token validation
GOOGLE_CLIENT_ID=

# Internal service communication
INTERNAL_API_KEY=internal-auth-key
USER_SERVICE_BASE_URL=http://user-service:8082

# Frontend call gateway
VITE_API_BASE_URL=http://localhost:8080
```

## 2) Build jar trước khi compose up

Chạy lần lượt:

```bash
cd eureka && mvn clean package -DskipTests
cd ../auth-service && mvn clean package -DskipTests
cd ../user_service && mvn clean package -DskipTests
cd ../gateway && mvn clean package -DskipTests
```

## 3) Chạy hệ thống

Lần đầu hoặc sau khi đổi code backend:

```bash
docker compose up -d --build
```

Những lần sau:

```bash
docker compose up -d
```

Tắt hệ thống:

```bash
docker compose down
```

Xóa luôn volume database:

```bash
docker compose down -v
```

Lưu ý: nếu bạn chỉnh `database/init.sql`, cần chạy lại `docker compose down -v` để MySQL khởi tạo lại dữ liệu từ đầu.

## 4) Chạy lại riêng user-service

```bash
docker compose up -d --build user-service
```

## 5) Xem log để debug

```bash
docker compose logs -f user-service
docker compose logs -f mysql
docker compose logs -f gateway
```

## 6) API chính

Auth service:

- `POST /api/auth/register/request-otp`
- `POST /api/auth/register/verify-otp`
- `POST /api/auth/login`
- `POST /api/auth/google-login`

User service:

- `POST /api/users`
- `GET /api/users`
- `GET /api/users/{id}`
- `PUT /api/users/{id}`
- `DELETE /api/users/{id}`
- `POST /api/users/internal/profiles` (nội bộ, cần header `X-Internal-Api-Key`)