# Chạy project bằng 1 file .env duy nhất

## 1) File cấu hình dùng chung

Tất cả service đọc biến từ file `.env` ở thư mục gốc project.

Chuẩn cấu hình hiện tại:

- Service kết nối MySQL trong Docker bằng `DB_HOST=mysql`, `DB_PORT=3306`
- Kết nối MySQL từ máy host bằng `localhost:${MYSQL_HOST_PORT}` (mặc định `3307`)
- App dùng user DB riêng: `DB_USERNAME=app_user` (không dùng root)
- `database/init.sql` sẽ tạo DB và cấp quyền cho `app_user` khi khởi tạo volume mới

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