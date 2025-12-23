# 🚢 Ship Management System Backend (船舶管理系统后端)

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-green)
![MyBatis Plus](https://img.shields.io/badge/MyBatis%20Plus-3.5.7-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.0-lightgrey)
![Redis](https://img.shields.io/badge/Redis-Latest-red)





## 📖 项目简介

**Ship Management System Backend** 是一个基于 **Spring Boot 3** 和 **Java 17** 构建的高效、现代化的船舶管理系统后端服务。本项目采用 RESTful API 架构风格，旨在为海运物流、船舶调度及船员管理提供稳定可靠的数据支撑。

系统集成了 **JWT 认证**、**RBAC 权限控制**、**文件对象存储 (S3/OSS)** 以及 **邮件服务**，涵盖了从船舶基础信息管理到航次调度、维修记录的全生命周期管理功能。





## ✨ 核心功能

* **👥 用户与权限管理**
  * 支持用户注册、登录（JWT Token 鉴权）。
  * 基于角色的权限控制（RBAC）：区分普通用户（USER）与管理员（ADMIN）。
  * 个人信息管理：头像上传、资料修改。
* **🚢 船舶全生命周期管理**
  * 船舶分类管理：支持散货船、集装箱船等类型配置。
  * 船舶档案管理：吨位、状态监控（在役、维修、停运、航行中）。
* **🌏 航次调度系统**
  * 航次计划与执行：记录出发港、目的港、开航及抵达时间。
  * 实时状态跟踪：计划中、执行中、已完成。
* **Dg 船员管理**
  * 船员档案录入与职位管理（船长、大副、轮机长等）。
  * 船员上船分配与调度。
* **🛠️ 维修与维护**
  * 船舶维修记录申报与费用统计。
* **☁️ 基础设施集成**
  * **对象存储**：集成 AWS S3 协议（兼容 ClawCloud 等）及 Aliyun OSS，实现图片/文件云存储。
  * **邮件服务**：集成 Resend SMTP 服务，支持系统通知邮件发送。
  * **缓存加速**：使用 Redis 对热点数据进行缓存，提升系统响应速度。





## 🛠️ 技术栈

| 类别         | 技术/框架                    | 版本      | 说明                        |
| :----------- | :--------------------------- | :-------- | :-------------------------- |
| **开发语言** | Java                         | 17        | LTS 版本，强类型支持        |
| **核心框架** | Spring Boot                  | 3.3.0     | 现代化 Web 开发框架         |
| **持久层**   | MyBatis Plus                 | 3.5.7     | 简化 SQL 操作，增强开发效率 |
| **数据库**   | MySQL                        | 8.0+      | 关系型数据存储              |
| **缓存**     | Redis                        | Latest    | 高性能键值存储              |
| **鉴权安全** | JWT + Spring Security Crypto | 4.4.0     | 无状态认证与密码加密        |
| **工具库**   | Hutool                       | 5.8.26    | 强大的 Java 工具包          |
| **对象存储** | AWS SDK S3 / Aliyun OSS      | 2.x / 3.x | 云存储支持                  |
| **构建工具** | Maven                        | 3.8+      | 依赖管理与项目构建          |





## 📂 项目结构

```text
com.dhy.shipmanagebackend
├── config          # 配置类 (WebConfig, MyBatisPlusConfig 等)
├── controller      # 控制层 (API 接口定义)
├── entity          # 实体类 (POJO, 与数据库表映射)
├── mapper          # 持久层接口 (DAO)
├── service         # 业务逻辑层接口
│   └── impl        # 业务逻辑实现
├── utils           # 工具类 (JWT, Md5, S3Util, AliOssUtil 等)
├── interceptors    # 拦截器 (登录校验)
├── exception       # 全局异常处理
└── ShipManageBackendApplication.java # 启动类
```





## 🚀 快速开始

### 1. 环境准备

在运行项目之前，请确保您的开发环境满足以下要求：

* **JDK**: OpenJDK 17+
* **Database**: MySQL 8.0+
* **Cache**: Redis Server
* **Build Tool**: Maven 3.6+

### 2. 数据库初始化

1. 登录 MySQL 数据库。
2. 创建一个新的数据库命名为 `ship_management`。
3. 执行项目根目录下的 `db.sql` 脚本，完成表结构创建及初始数据导入。

```bash
# 示例：命令行导入
mysql -u root -p ship_management < db.sql
```

### 3. 项目配置

修改 `src/main/resources/application.yml` 文件，填入您的本地环境配置及第三方服务密钥。

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ship_management?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: your_db_username  # 修改为你的数据库账号
    password: your_db_password  # 修改为你的数据库密码
  data:
    redis:
      host: localhost           # Redis 地址
      port: 6379
      password: your_redis_pwd  # 如果 Redis 设置了密码

# 第三方服务配置 (根据实际情况填写)
resend:
  api-key: your_resend_api_key
  from-email: system@yourdomain.com

s3:
  endpoint: [https://your-s3-endpoint.com](https://your-s3-endpoint.com)
  access-key: your_access_key
  secret-key: your_secret_key
  bucket-name: your_bucket_name
```

### 4. 编译与运行

使用 Maven 或者是 IDE (IntelliJ IDEA) 启动项目。

**命令行启动：**

```bash
# 下载依赖并打包
mvn clean package -DskipTests

# 运行 JAR 包
java -jar target/ship-manage-backend-0.0.1-SNAPSHOT.jar

```

启动成功后，控制台将显示 Spring Boot 启动日志。默认端口为 8080。



## 📝 接口文档与测试

项目集成了 RESTful API。建议使用 Postman 或 Apifox 进行接口测试。

**主要接口前缀示例：**

* **用户模块**: `/user/login`, `/user/register`, `/user/info`
* **船舶模块**: `/ship/list`, `/ship/add`, `/ship/update`
* **船员模块**: `/crew/list`, `/crew/add`
* **航次模块**: `/voyage/list`
* **文件上传**: `/upload`

> ⚠️ **注**：大部分 POST/PUT/DELETE 接口需要在 Header 中携带 `Authorization` 字段（JWT Token）进行访问。

## 🤝 贡献指南

欢迎提交 Issue 或 Pull Request 来改进本项目！

1. Fork 本仓库
2. 创建您的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交您的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启一个 Pull Request

## 📄 版权说明

本项目采用 MIT 许可证，详情请参阅 [LICENSE](LICENSE) 文件。
