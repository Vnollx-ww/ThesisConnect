# API测试文档

## 测试环境
- 基础URL: http://localhost:8080
- 认证方式: JWT Bearer Token

## 1. 认证接口测试

### 1.1 用户登录
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456",
    "role": "admin"
  }'
```

**响应示例:**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "email": "admin@thesisconnect.com",
      "role": "admin",
      "status": 1
    },
    "role": "admin"
  },
  "timestamp": 1703123456789
}
```

### 1.2 获取用户信息
```bash
curl -X GET http://localhost:8080/api/auth/userinfo \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 2. 用户管理接口测试

### 2.1 获取用户列表
```bash
curl -X GET "http://localhost:8080/api/users?page=1&size=10&role=student" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 2.2 创建用户
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456",
    "realName": "测试用户",
    "email": "test@example.com",
    "phone": "13800000000",
    "role": "student",
    "department": "计算机科学与技术",
    "studentId": "2021001999"
  }'
```

## 3. 课题管理接口测试

### 3.1 获取课题列表
```bash
curl -X GET "http://localhost:8080/api/topics?page=1&size=10&major=计算机科学与技术" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3.2 创建课题
```bash
curl -X POST http://localhost:8080/api/topics \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "基于Spring Boot的微服务架构设计",
    "description": "设计并实现一个基于Spring Boot的微服务架构系统",
    "major": "计算机科学与技术",
    "difficulty": "medium",
    "maxStudents": 3,
    "requirements": "熟悉Spring Boot,了解微服务架构,有Java开发经验",
    "expectedOutcome": "完成一个可用的微服务系统",
    "deadline": "2024-06-30T23:59:59"
  }'
```

### 3.3 搜索课题
```bash
curl -X GET "http://localhost:8080/api/topics/search?keyword=深度学习" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 4. 选题管理接口测试

### 4.1 学生选择课题
```bash
curl -X POST http://localhost:8080/api/selections \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "topicId": 1
  }'
```

### 4.2 获取学生选题记录
```bash
curl -X GET http://localhost:8080/api/selections/student/5 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4.3 审核选题
```bash
curl -X POST http://localhost:8080/api/selections/1/review \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "approved",
    "comment": "选题通过，请开始项目开发"
  }'
```

### 4.4 更新进度
```bash
curl -X PUT http://localhost:8080/api/selections/1/progress \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "progress": 50,
    "description": "已完成需求分析和系统设计，正在进行核心功能开发",
    "problems": "遇到了一些技术难点，正在研究解决方案"
  }'
```

## 5. 统计接口测试

### 5.1 获取用户统计
```bash
curl -X GET http://localhost:8080/api/users/stats \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 5.2 获取课题统计
```bash
curl -X GET http://localhost:8080/api/topics/stats \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 5.3 获取选题统计
```bash
curl -X GET http://localhost:8080/api/selections/stats \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 6. 错误处理测试

### 6.1 无效Token
```bash
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer invalid_token"
```

**响应示例:**
```json
{
  "code": 401,
  "message": "无效的认证token",
  "timestamp": 1703123456789
}
```

### 6.2 权限不足
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer STUDENT_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username": "test"}'
```

**响应示例:**
```json
{
  "code": 403,
  "message": "权限不足",
  "timestamp": 1703123456789
}
```

## 7. 测试用例

### 7.1 完整流程测试

1. **管理员登录**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "123456", "role": "admin"}'
```

2. **创建教师用户**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "teacher_test",
    "password": "123456",
    "realName": "测试教师",
    "email": "teacher@test.com",
    "role": "teacher",
    "department": "计算机学院"
  }'
```

3. **教师登录**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "teacher_test", "password": "123456", "role": "teacher"}'
```

4. **教师发布课题**
```bash
curl -X POST http://localhost:8080/api/topics \
  -H "Authorization: Bearer TEACHER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "测试课题",
    "description": "这是一个测试课题",
    "major": "计算机科学与技术",
    "difficulty": "easy",
    "maxStudents": 2
  }'
```

5. **学生登录**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "student001", "password": "123456", "role": "student"}'
```

6. **学生选择课题**
```bash
curl -X POST http://localhost:8080/api/selections \
  -H "Authorization: Bearer STUDENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"topicId": 1}'
```

7. **教师审核选题**
```bash
curl -X POST http://localhost:8080/api/selections/1/review \
  -H "Authorization: Bearer TEACHER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status": "approved", "comment": "通过"}'
```

## 8. 性能测试

### 8.1 并发登录测试
```bash
# 使用Apache Bench进行并发测试
ab -n 100 -c 10 -p login.json -T "application/json" http://localhost:8080/api/auth/login
```

### 8.2 数据库查询性能测试
```bash
# 测试分页查询性能
ab -n 1000 -c 50 -H "Authorization: Bearer YOUR_JWT_TOKEN" http://localhost:8080/api/topics?page=1&size=20
```

## 9. 注意事项

1. **Token管理**: JWT Token有效期为24小时，过期后需要重新登录
2. **权限控制**: 不同角色有不同的接口访问权限
3. **数据验证**: 所有输入数据都会进行验证
4. **错误处理**: 统一的错误响应格式
5. **日志记录**: 所有操作都会记录到系统日志

## 10. 测试工具推荐

- **Postman**: 图形化API测试工具
- **curl**: 命令行测试工具
- **Apache Bench**: 性能测试工具
- **JMeter**: 压力测试工具
