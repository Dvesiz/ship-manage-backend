# 🚢 Ship Manage Backend

船舶管理系统后端服务，基于 **Spring Boot** 构建，提供标准 RESTful API，用于船舶信息的统一管理与数据持久化，适合作为前后端分离项目的后端支撑。

---

## 📌 项目背景

本项目用于实现船舶信息的集中化管理，支持船舶基础数据的增删改查操作。  
系统采用经典的分层架构设计，具备良好的可维护性与扩展性，可作为中小型后台管理系统或教学 / 简历展示项目使用。

---

## ✨ 功能特性

- 🚢 船舶信息管理（CRUD）
- 🔍 船舶数据查询
- 🧩 RESTful API 设计
- 💾 数据持久化存储
- 📦 独立后端服务部署

---

## 🛠 技术栈

| 技术 | 说明 |
|----|----|
| Java | 核心开发语言 |
| Spring Boot | 后端应用框架 |
| Spring MVC | REST API |
| Spring Data JPA | ORM 持久层 |
| MySQL | 关系型数据库 |
| Maven | 构建与依赖管理 |

---

## 🗂 项目结构

```text
ship-manage-backend
├── .mvn/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/xxx/
│       │       ├── controller/    # 接口层
│       │       ├── service/       # 业务层
│       │       ├── repository/    # 数据访问层
│       │       ├── entity/        # 实体类
│       │       └── exception/     # 异常处理
│       └── resources/
│           ├── application.properties
│           └── application.yml
├── db.sql
├── pom.xml
├── mvnw
└── mvnw.cmd

---

## ⚙️ 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 5.7+

---

## 🚀 快速启动

### 1️⃣ 克隆项目

```bash
git clone https://github.com/Dvesiz/ship-manage-backend.git
cd ship-manage-backend


### 2️⃣ 初始化数据库

执行项目根目录下的 `db.sql` 文件，用于创建数据库及表结构：

```sql
SOURCE db.sql;

###3️⃣ 配置数据库连接

在 application.properties 或 application.yml 中配置数据库连接信息。

application.properties 示例
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ship_manage_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect

###4️⃣ 启动项目
方式一：使用 Maven 直接启动
```bash
./mvnw spring-boot:run

方式二：打包后运行
```bash
./mvnw clean package
java -jar target/ship-manage-backend-*.jar


服务默认启动端口为：
```bash
http://localhost:8080

---

##📡 API 接口示例
| 方法     | 接口路径          | 描述     |
| ------ | ------------- | ------ |
| GET    | `/ships`      | 查询船舶列表 |
| GET    | `/ships/{id}` | 查询船舶详情 |
| POST   | `/ships`      | 新增船舶   |
| PUT    | `/ships/{id}` | 更新船舶   |
| DELETE | `/ships/{id}` | 删除船舶   |

---

##🧠 系统设计说明
采用 Controller / Service / Repository 分层架构

Controller 层负责请求处理与参数校验

Service 层承载核心业务逻辑

Repository 层负责数据库访问

Entity 映射数据库表结构

Exception 层统一处理系统异常

---

##⭐ 技术亮点（简历友好）

使用 Spring Boot 构建 RESTful 后端服务

严格遵循分层架构设计，职责清晰

使用 Spring Data JPA 实现 ORM 持久化

提供数据库初始化脚本，支持快速部署

Maven Wrapper 保证构建环境一致性

项目结构符合企业级 Java 后端开发规范

---

