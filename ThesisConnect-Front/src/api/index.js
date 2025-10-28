import request from '@/utils/request'

// 用户相关API
export const userApi = {
  // 获取用户列表
  getUserList(params) {
    return request.get('/api/users', { params })
  },
  
  // 获取用户详情
  getUserById(id) {
    return request.get(`/api/users/${id}`)
  },
  
  // 创建用户
  createUser(userData) {
    return request.post('/api/users', userData)
  },
  
  // 更新用户
  updateUser(id, userData) {
    return request.put(`/api/users/${id}`, userData)
  },
  
  // 删除用户
  deleteUser(id) {
    return request.delete(`/api/users/${id}`)
  },
  
  // 批量删除用户
  batchDeleteUsers(userIds) {
    return request.delete('/api/users/batch', { data: userIds })
  },
  
  // 重置密码
  resetPassword(id, passwordData) {
    return request.post(`/api/users/${id}/reset-password`, passwordData)
  },
  
  // 切换用户状态
  toggleUserStatus(id) {
    return request.post(`/api/users/${id}/toggle-status`)
  },
  
  // 获取用户统计
  getUserStats() {
    return request.get('/api/users/stats')
  },
  
  // 根据教师获取学生列表
  getStudentsByTeacher(teacherId) {
    return request.get(`/api/users/teacher/${teacherId}/students`)
  },
  
  // 获取学生统计
  getStudentStats() {
    return request.get('/api/users/student-stats')
  },
  getStudentStatsByTeacher(teacherId) {
    return request.get(`/api/users/teacher/${teacherId}/student-stats`)
  }
}

// 课题相关API
export const topicApi = {
  // 获取课题列表
  getTopicList(params) {
    return request.get('/api/topics', { params })
  },
  
  // 获取课题详情
  getTopicById(id) {
    return request.get(`/api/topics/${id}`)
  },
  
  // 创建课题
  createTopic(topicData) {
    return request.post('/api/topics', topicData)
  },
  
  // 更新课题
  updateTopic(id, topicData) {
    return request.put(`/api/topics/${id}`, topicData)
  },
  
  // 删除课题
  deleteTopic(id) {
    return request.delete(`/api/topics/${id}`)
  },
  
  // 根据教师获取课题
  getTopicsByTeacher(teacherId) {
    return request.get(`/api/topics/teacher/${teacherId}`)
  },
  
  // 根据专业获取课题
  getTopicsByMajor(major) {
    return request.get(`/api/topics/major/${major}`)
  },
  
  // 根据难度获取课题
  getTopicsByDifficulty(difficulty) {
    return request.get(`/api/topics/difficulty/${difficulty}`)
  },
  
  // 搜索课题
  searchTopics(keyword) {
    return request.get('/api/topics/search', { params: { keyword } })
  },
  
  // 获取热门课题
  getPopularTopics(limit = 10) {
    return request.get('/api/topics/popular', { params: { limit } })
  },
  
  // 获取课题统计
  getTopicStats() {
    return request.get('/api/topics/stats')
  },
  
  // 获取指定教师的课题统计
  getTopicStatsByTeacher(teacherId) {
    return request.get(`/api/topics/teacher/${teacherId}/stats`)
  }
}

// 选题相关API
export const selectionApi = {
  // 获取选题列表
  getSelectionList(params) {
    return request.get('/api/selections', { params })
  },
  
  // 获取选题详情
  getSelectionById(id) {
    return request.get(`/api/selections/${id}`)
  },
  
  // 选择课题
  selectTopic(selectionData) {
    return request.post('/api/selections', selectionData)
  },
  
  // 取消选择
  cancelSelection(id) {
    return request.delete(`/api/selections/${id}`)
  },
  
  // 审核选题
  reviewSelection(id, reviewData) {
    return request.post(`/api/selections/${id}/review`, reviewData)
  },
  
  // 学生确认申请
  confirmSelection(id) {
    return request.post(`/api/selections/${id}/confirm`)
  },
  
  // 更新进度
  updateProgress(id, progressData) {
    return request.put(`/api/selections/${id}/progress`, progressData)
  },
  
  // 根据学生获取选题
  getSelectionsByStudent(studentId) {
    return request.get(`/api/selections/student/${studentId}`)
  },
  
  // 根据教师获取选题
  getSelectionsByTeacher(teacherId) {
    return request.get(`/api/selections/teacher/${teacherId}`)
  },
  
  // 根据课题获取选题
  getSelectionsByTopic(topicId) {
    return request.get(`/api/selections/topic/${topicId}`)
  },
  
  // 获取选题统计
  getSelectionStats() {
    return request.get('/api/selections/stats')
  }
}

// 文档相关API
export const documentApi = {
  // 上传文档
  uploadDocument(file, selectionId, topicId) {
    const formData = new FormData()
    formData.append('file', file)
    if (selectionId) formData.append('selectionId', selectionId)
    if (topicId) formData.append('topicId', topicId)
    
    return request.post('/api/documents/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  
  // 下载文档
  downloadDocument(id) {
    return request.get(`/api/documents/download/${id}`, {
      responseType: 'blob'
    })
  },
  
  // 删除文档
  deleteDocument(id) {
    return request.delete(`/api/documents/${id}`)
  },
  
  // 审核文档
  reviewDocument(id, reviewData) {
    return request.post(`/api/documents/${id}/review`, reviewData)
  },
  
  // 根据选题获取文档
  getDocumentsBySelection(selectionId) {
    return request.get(`/api/documents/selection/${selectionId}`)
  },
  
  // 根据课题获取文档
  getDocumentsByTopic(topicId) {
    return request.get(`/api/documents/topic/${topicId}`)
  },
  
  // 根据上传者获取文档
  getDocumentsByUploader(uploaderId) {
    return request.get(`/api/documents/uploader/${uploaderId}`)
  },
  
  // 获取文档详情
  getDocumentById(id) {
    return request.get(`/api/documents/${id}`)
  },
  
  // 获取文档统计
  getDocumentStats() {
    return request.get('/api/documents/stats')
  }
}

// 进度相关API
export const progressApi = {
  // 更新进度
  updateProgress(progressData) {
    return request.post('/api/progress/update', progressData)
  },
  
  // 添加里程碑
  addMilestone(milestoneData) {
    return request.post('/api/progress/milestone', milestoneData)
  },
  
  // 更新里程碑状态
  updateMilestoneStatus(updateData) {
    return request.put('/api/progress/milestone/status', updateData)
  },
  
  // 根据选题获取进度
  getProgressBySelection(selectionId) {
    return request.get(`/api/progress/selection/${selectionId}`)
  },
  
  // 根据学生获取进度
  getProgressByStudent(studentId) {
    return request.get(`/api/progress/student/${studentId}`)
  },
  
  // 根据课题获取进度
  getProgressByTopic(topicId) {
    return request.get(`/api/progress/topic/${topicId}`)
  },
  
  // 获取最新进度
  getLatestProgress(selectionId) {
    return request.get(`/api/progress/latest/${selectionId}`)
  },
  
  // 获取进度统计
  getProgressStats() {
    return request.get('/api/progress/stats')
  }
}

// 认证相关API
export const authApi = {
  // 登录
  login(loginData) {
    return request.post('/api/auth/login', loginData)
  },
  
  // 注册
  register(registerData) {
    return request.post('/api/auth/register', registerData)
  },
  
  // 获取用户信息
  getUserInfo() {
    return request.get('/api/auth/userinfo')
  },
  
  // 更新用户信息
  updateUserInfo(userData) {
    return request.put('/api/auth/userinfo', userData)
  },
  
  // 登出
  logout() {
    return request.post('/api/auth/logout')
  },
  
  // 修改密码
  changePassword(passwordData) {
    return request.post('/api/auth/change-password', passwordData)
  }
}

// 统计相关API
export const statsApi = {
  // 获取系统总览统计
  getOverviewStats() {
    return request.get('/api/stats/overview')
  },
  
  // 获取用户统计
  getUserStats() {
    return request.get('/api/stats/users')
  },
  
  // 获取课题统计
  getTopicStats() {
    return request.get('/api/stats/topics')
  },
  
  // 获取选题统计
  getSelectionStats() {
    return request.get('/api/stats/selections')
  },
  
  // 获取文档统计
  getDocumentStats() {
    return request.get('/api/stats/documents')
  },
  
  // 获取进度统计
  getProgressStats() {
    return request.get('/api/stats/progress')
  },
  
  // 获取用户增长趋势
  getUserGrowthTrend(period = 'month') {
    return request.get('/api/stats/user-growth-trend', { params: { period } })
  },
  
  // 获取课题难度分布
  getTopicDifficultyDistribution() {
    return request.get('/api/stats/topic-difficulty-distribution')
  },
  
  // 获取教师课题统计
  getTeacherTopicStats(teacherId) {
    return request.get(`/api/topics/teacher/${teacherId}/stats`)
  },
  
  // 获取教师学生统计
  getTeacherStudentStats(teacherId) {
    return request.get(`/api/users/teacher/${teacherId}/student-stats`)
  },
  
  // 获取教师选题列表
  getTeacherSelections(teacherId) {
    return request.get(`/api/selections/teacher/${teacherId}`)
  }
}
