CREATE DATABASE IF NOT EXISTS user_service_db;

-- tạo user cho app
CREATE USER IF NOT EXISTS 'app_user'@'%' IDENTIFIED BY '123456';

GRANT ALL PRIVILEGES ON user_service_db.* TO 'app_user'@'%';

FLUSH PRIVILEGES;