# 统计API测试说明

## 测试步骤

1. 启动后端服务
2. 使用管理员账号登录获取token
3. 调用统计API测试

## API测试

### 1. 获取系统总览统计
```
GET /api/stats/overview
Headers: Authorization: Bearer {token}
```

### 2. 获取用户统计
```
GET /api/stats/users
Headers: Authorization: Bearer {token}
```

### 3. 获取课题统计
```
GET /api/stats/topics
Headers: Authorization: Bearer {token}
```

### 4. 获取选题统计
```
GET /api/stats/selections
Headers: Authorization: Bearer {token}
```

### 5. 获取文档统计
```
GET /api/stats/documents
Headers: Authorization: Bearer {token}
```

### 6. 获取进度统计
```
GET /api/stats/progress
Headers: Authorization: Bearer {token}
```

### 7. 获取用户增长趋势
```
GET /api/stats/user-growth-trend?period=month
Headers: Authorization: Bearer {token}
```

### 8. 获取课题难度分布
```
GET /api/stats/topic-difficulty-distribution
Headers: Authorization: Bearer {token}
```

## 预期结果

统计API应该返回真实的数据，包括：
- 用户总数、各角色用户数、用户增长率
- 课题总数、各状态课题数、课题增长率
- 选题总数、各状态选题数、选题增长率、完成率
- 文档总数、各状态文档数
- 进度统计信息
- 用户增长趋势数据
- 课题难度分布数据

## 注意事项

1. 所有统计API都需要管理员权限
2. 增长率是基于本月与上月的数据对比计算
3. 完成率是基于已完成选题数与总选题数的比例计算
4. 数据应该从真实数据库记录中计算，不使用模拟数据

