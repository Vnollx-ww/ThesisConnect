# 毕业论文选题双向选择系统 - 后端

## 项目简介

这是一个基于Spring Boot + MyBatis-Plus的毕业论文选题双向选择系统后端项目，支持学生选题、教师审核、管理员管理等完整功能。

## 技术栈

- **框架**: Spring Boot 3.2.5
- **数据库**: MySQL 8.0+
- **ORM**: MyBatis-Plus 3.5.5
- **缓存**: Redis
- **认证**: JWT
- **连接池**: Druid
- **工具**: Lombok, Jackson
- **Java版本**: 21

## 项目结构

```
src/main/java/com/example/thesisconnectback/
├── common/                 # 公共类
│   └── Result.java        # 统一响应结果类
├── config/                # 配置类
│   ├── MybatisPlusConfig.java
│   ├── RedisConfig.java
│   ├── WebConfig.java
│   └── FileUploadConfig.java
├── controller/            # 控制器层
│   ├── AuthController.java
│   ├── UserController.java
│   ├── TopicController.java
│   └── SelectionController.java
├── entity/               # 实体类
│   ├── User.java
│   ├── Topic.java
│   ├── Selection.java
│   ├── Document.java
│   ├── Progress.java
│   └── SystemLog.java
├── exception/            # 异常处理
│   ├── BusinessException.java
│   └── GlobalExceptionHandler.java
├── interceptor/          # 拦截器
│   └── JwtAuthenticationInterceptor.java
├── mapper/              # Mapper接口
│   ├── UserMapper.java
│   ├── TopicMapper.java
│   ├── SelectionMapper.java
│   ├── DocumentMapper.java
│   ├── ProgressMapper.java
│   └── SystemLogMapper.java
├── service/             # 服务层
│   ├── UserService.java
│   ├── TopicService.java
│   ├── SelectionService.java
│   └── impl/           # 服务实现
├── util/               # 工具类
│   └── JwtUtil.java
└── ThesisConnectBackApplication.java
```

## 数据库设计

### 主要表结构

1. **sys_user** - 用户表
   - 支持学生、教师、管理员三种角色
   - 包含用户基本信息、状态管理

2. **sys_topic** - 课题表
   - 课题基本信息、难度等级、专业要求
   - 支持状态管理和浏览量统计

3. **sys_selection** - 选题记录表
   - 学生选题记录、审核状态、进度管理
   - 支持双向选择机制

4. **sys_document** - 文档表
   - 文档上传、审核、下载管理
   - 支持多种文档类型

5. **sys_progress** - 进度记录表
   - 学生进度跟踪、里程碑管理
   - 支持进度可视化

6. **sys_log** - 系统日志表
   - 操作日志记录、审计跟踪

## 快速开始

### 1. 环境要求

- JDK 21+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

### 2. 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE thesis_connect DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本：
```bash
mysql -u root -p thesis_connect < sql/thesis_connect_init.sql
```

### 3. 配置文件

修改 `application.properties` 中的数据库和Redis配置：

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/thesis_connect?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=123456

# Redis配置
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=
```

### 4. 启动应用

```bash
mvn spring-boot:run
```

或者打包后运行：

```bash
mvn clean package
java -jar target/ThesisConnect-Back-0.0.1-SNAPSHOT.jar
```

### 5. 访问地址

- 应用地址: http://localhost:8080
- API文档: http://localhost:8080/swagger-ui.html

## API接口说明

### 认证接口

- `POST /api/auth/login` - 用户登录
- `GET /api/auth/userinfo` - 获取当前用户信息
- `POST /api/auth/logout` - 用户登出
- `POST /api/auth/change-password` - 修改密码

### 用户管理接口

- `GET /api/users` - 获取用户列表（分页）
- `GET /api/users/{id}` - 获取用户详情
- `POST /api/users` - 创建用户
- `PUT /api/users/{id}` - 更新用户信息
- `DELETE /api/users/{id}` - 删除用户
- `POST /api/users/{id}/reset-password` - 重置密码
- `POST /api/users/{id}/toggle-status` - 启用/禁用用户
- `GET /api/users/stats` - 获取用户统计信息

### 课题管理接口

- `GET /api/topics` - 获取课题列表（分页）
- `GET /api/topics/{id}` - 获取课题详情
- `POST /api/topics` - 创建课题
- `PUT /api/topics/{id}` - 更新课题信息
- `DELETE /api/topics/{id}` - 删除课题
- `GET /api/topics/teacher/{teacherId}` - 获取教师课题列表
- `GET /api/topics/major/{major}` - 获取专业课题列表
- `GET /api/topics/difficulty/{difficulty}` - 获取难度课题列表
- `GET /api/topics/search` - 搜索课题
- `GET /api/topics/popular` - 获取热门课题
- `GET /api/topics/stats` - 获取课题统计信息

### 选题管理接口

- `GET /api/selections` - 获取选题列表（分页）
- `GET /api/selections/{id}` - 获取选题详情
- `POST /api/selections` - 学生选择课题
- `DELETE /api/selections/{id}` - 取消选择课题
- `POST /api/selections/{id}/review` - 审核选题
- `PUT /api/selections/{id}/progress` - 更新进度
- `GET /api/selections/student/{studentId}` - 获取学生选题记录
- `GET /api/selections/teacher/{teacherId}` - 获取教师选题记录
- `GET /api/selections/topic/{topicId}` - 获取课题选题记录
- `GET /api/selections/stats` - 获取选题统计信息

## 默认账户

系统初始化后提供以下默认账户：

### 管理员
- 用户名: `admin`
- 密码: `123456`
- 角色: `admin`

### 教师
- 用户名: `teacher001`
- 密码: `123456`
- 角色: `teacher`

### 学生
- 用户名: `student001`
- 密码: `123456`
- 角色: `student`

## 功能特性

### 1. 用户管理
- 支持学生、教师、管理员三种角色
- 用户注册、登录、权限控制
- 用户信息管理、状态控制

### 2. 课题管理
- 教师发布课题、设置要求
- 课题分类、难度等级
- 课题搜索、筛选功能

### 3. 选题管理
- 学生选择课题
- 教师审核选题
- 双向选择机制

### 4. 进度管理
- 学生进度跟踪
- 里程碑管理
- 进度可视化

### 5. 文档管理
- 文档上传、下载
- 文档审核流程
- 版本控制

### 6. 系统管理
- 数据统计、报表
- 系统日志、审计
- 权限管理

## 安全特性

- JWT Token认证
- 密码BCrypt加密
- 接口权限控制
- SQL注入防护
- XSS攻击防护
- CSRF防护

## 部署说明

### Docker部署

1. 构建镜像：
```bash
docker build -t thesis-connect-back .
```

2. 运行容器：
```bash
docker run -d -p 8080:8080 --name thesis-connect-back thesis-connect-back
```

### 生产环境配置

1. 修改数据库连接为生产环境
2. 配置Redis集群
3. 设置JWT密钥
4. 配置日志级别
5. 启用HTTPS

## 开发说明

### 代码规范
- 使用Lombok减少样板代码
- 统一异常处理
- 统一响应格式
- 完善的日志记录

### 扩展功能
- 支持文件上传
- 支持邮件通知
- 支持消息推送
- 支持数据导出

## 常见问题

### 1. 数据库连接失败
检查MySQL服务是否启动，用户名密码是否正确。

### 2. Redis连接失败
检查Redis服务是否启动，端口是否正确。

### 3. JWT Token无效
检查JWT密钥配置，Token是否过期。

### 4. 权限不足
检查用户角色，接口权限配置。

## 联系方式

如有问题，请联系开发团队。

## 许可证

本项目采用MIT许可证。
