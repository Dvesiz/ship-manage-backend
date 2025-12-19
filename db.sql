-- 1. 创建数据库 (如果不存在)
CREATE DATABASE IF NOT EXISTS ship_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. 使用数据库
USE ship_management;

-- ==========================================
--  1. 用户表 (Users)
--  对应实训要求: 包含邮箱、头像、密码、角色
-- ==========================================
DROP TABLE IF EXISTS users;
CREATE TABLE users (
                       id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                       username VARCHAR(50) NOT NULL COMMENT '用户名',
                       password_hash VARCHAR(255) NOT NULL COMMENT '加密后的密码',
                       nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
                       email VARCHAR(100) DEFAULT NULL COMMENT '邮箱 (实训要求)',
                       avatar_url VARCHAR(255) DEFAULT NULL COMMENT '头像地址 (实训要求)',
                       role VARCHAR(20) DEFAULT 'USER' COMMENT '角色: USER-普通用户, ADMIN-管理员',
                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                       PRIMARY KEY (id),
                       UNIQUE KEY uk_username (username),
                       UNIQUE KEY uk_email (email)
) COMMENT '用户信息表';

-- ==========================================
--  2. 船舶类型表 (Ship Categories)
--  对应实训要求: 图书分类表 (book_category)
-- ==========================================
DROP TABLE IF EXISTS ship_categories;
CREATE TABLE ship_categories (
                                 id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 name VARCHAR(50) NOT NULL COMMENT '类型名称 (如: 散货船)',
                                 alias VARCHAR(50) DEFAULT NULL COMMENT '类型别名/编码',
                                 created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 PRIMARY KEY (id),
                                 UNIQUE KEY uk_name (name)
) COMMENT '船舶类型表';

-- ==========================================
--  3. 船舶信息表 (Ships)
--  对应实训要求: 图书表 (books)
-- ==========================================
DROP TABLE IF EXISTS ships;
CREATE TABLE ships (
                       id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                       name VARCHAR(100) NOT NULL COMMENT '船舶名称',
                       category_id BIGINT DEFAULT NULL COMMENT '所属类型ID (外键)',
                       tonnage DECIMAL(10, 2) DEFAULT NULL COMMENT '吨位',
                       status VARCHAR(30) DEFAULT 'IN_SERVICE' COMMENT '状态: IN_SERVICE(在役), MAINTENANCE(维修), STOPPED(停运), RUNNING(航行中)',
                       cover_img VARCHAR(255) DEFAULT NULL COMMENT '船舶照片 (对应图书封面)',
                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       PRIMARY KEY (id),
                       KEY idx_status (status),
                       KEY idx_name (name),
                       CONSTRAINT fk_ships_category FOREIGN KEY (category_id) REFERENCES ship_categories (id) ON DELETE SET NULL
) COMMENT '船舶信息表';

-- ==========================================
--  4. 船员表 (Crew)
--  额外模块: 用于丰富系统功能
-- ==========================================
DROP TABLE IF EXISTS crew;
CREATE TABLE crew (
                      id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                      name VARCHAR(50) NOT NULL COMMENT '船员姓名',
                      position VARCHAR(50) DEFAULT NULL COMMENT '职位 (船长/大副/轮机长)',
                      ship_id BIGINT DEFAULT NULL COMMENT '当前所属船舶ID',
                      phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      PRIMARY KEY (id),
                      CONSTRAINT fk_crew_ship FOREIGN KEY (ship_id) REFERENCES ships (id) ON DELETE SET NULL
) COMMENT '船员表';

-- ==========================================
--  5. 航次表 (Voyages)
--  对应实训要求: 借阅记录表 (borrow_record)
--  核心业务: 记录船舶的“忙碌”状态
-- ==========================================
DROP TABLE IF EXISTS voyages;
CREATE TABLE voyages (
                         id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                         ship_id BIGINT NOT NULL COMMENT '船舶ID',
                         start_port VARCHAR(100) DEFAULT NULL COMMENT '出发港',
                         end_port VARCHAR(100) DEFAULT NULL COMMENT '目的港',
                         status VARCHAR(30) DEFAULT 'PLANNED' COMMENT '航次状态: PLANNED(计划), RUNNING(执行中), FINISHED(已完成)',
                         start_time DATETIME DEFAULT NULL COMMENT '开航时间',
                         end_time DATETIME DEFAULT NULL COMMENT '结束/抵达时间',
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         PRIMARY KEY (id),
                         KEY idx_ship_id (ship_id),
                         CONSTRAINT fk_voyages_ship FOREIGN KEY (ship_id) REFERENCES ships (id) ON DELETE CASCADE
) COMMENT '航次记录表';

-- ==========================================
--  6. 维修记录表 (Maintenance)
--  额外模块
-- ==========================================
DROP TABLE IF EXISTS maintenance;
CREATE TABLE maintenance (
                             id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                             ship_id BIGINT NOT NULL COMMENT '船舶ID',
                             description VARCHAR(255) DEFAULT NULL COMMENT '维修描述',
                             cost DECIMAL(10, 2) DEFAULT NULL COMMENT '维修费用',
                             maintenance_time DATETIME DEFAULT NULL COMMENT '维修时间',
                             created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                             PRIMARY KEY (id),
                             CONSTRAINT fk_maintenance_ship FOREIGN KEY (ship_id) REFERENCES ships (id) ON DELETE CASCADE
) COMMENT '维修记录表';

-- ==========================================
--  数据初始化 (Mock Data)
-- ==========================================

-- 1. 初始化用户 (密码通常是加密的，这里模拟 '123456' 的BCrypt哈希或明文，开发时请注意)
-- 假设后端会处理加密，这里先存明文方便你测试，或者存一个固定的Hash
-- $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOc LR/keO (示例Hash)
INSERT INTO users (username, password_hash, nickname, email, role) VALUES
                                                                       ('admin', '$2a$10$xXw...MockHash...', '系统管理员', 'admin@ship.com', 'ADMIN'),
                                                                       ('captain_jack', '$2a$10$xXw...MockHash...', '杰克船长', 'jack@blackpearl.com', 'USER');

-- 2. 初始化船舶类型
INSERT INTO ship_categories (name, alias) VALUES
                                              ('散货船', 'BULK_CARRIER'),
                                              ('集装箱船', 'CONTAINER_SHIP'),
                                              ('油轮', 'OIL_TANKER'),
                                              ('液化气船', 'LNG_LPG');

-- 3. 初始化船舶
INSERT INTO ships (name, category_id, tonnage, status) VALUES
                                                           ('远洋一号', 1, 50000.00, 'IN_SERVICE'),
                                                           ('太平洋勇士', 2, 85000.50, 'IN_SERVICE'),
                                                           ('深海探索者', 3, 120000.00, 'MAINTENANCE'),
                                                           ('极地之星', 4, 60000.00, 'RUNNING');

-- 4. 初始化船员
INSERT INTO crew (name, position, ship_id, phone) VALUES
                                                      ('张三', '船长', 1, '13800138000'),
                                                      ('李四', '大副', 1, '13900139000'),
                                                      ('王五', '轮机长', 2, '13700137000');

-- 5. 初始化航次 (对应借阅记录)
INSERT INTO voyages (ship_id, start_port, end_port, status, start_time) VALUES
    (4, '上海港', '新加坡港', 'RUNNING', NOW());