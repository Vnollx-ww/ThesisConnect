// 用户相关API
export const userApi = {
  // 获取用户列表
  getUserList(params) {
    return this.$http.get('/api/users', { params })
  },
  
  // 获取用户详情
  getUserById(id) {
    return this.$http.get(`/api/users/${id}`)
  },
  
  // 创建用户
  createUser(userData) {
    return this.$http.post('/api/users', userData)
  },
  
  // 更新用户
  updateUser(id, userData) {
    return this.$http.put(`/api/users/${id}`, userData)
  },
  
  // 删除用户
  deleteUser(id) {
    return this.$http.delete(`/api/users/${id}`)
  },
  
  // 批量删除用户
  batchDeleteUsers(userIds) {
    return this.$http.delete('/api/users/batch', { data: userIds })
  },
  
  // 重置密码
  resetPassword(id, passwordData) {
    return this.$http.post(`/api/users/${id}/reset-password`, passwordData)
  },
  
  // 切换用户状态
  toggleUserStatus(id) {
    return this.$http.post(`/api/users/${id}/toggle-status`)
  },
  
  // 获取用户统计
  getUserStats() {
    return this.$http.get('/api/users/stats')
  }
}

// 课题相关API
export const topicApi = {
  // 获取课题列表
  getTopicList(params) {
    return this.$http.get('/api/topics', { params })
  },
  
  // 获取课题详情
  getTopicById(id) {
    return this.$http.get(`/api/topics/${id}`)
  },
  
  // 创建课题
  createTopic(topicData) {
    return this.$http.post('/api/topics', topicData)
  },
  
  // 更新课题
  updateTopic(id, topicData) {
    return this.$http.put(`/api/topics/${id}`, topicData)
  },
  
  // 删除课题
  deleteTopic(id) {
    return this.$http.delete(`/api/topics/${id}`)
  },
  
  // 根据教师获取课题
  getTopicsByTeacher(teacherId) {
    return this.$http.get(`/api/topics/teacher/${teacherId}`)
  },
  
  // 根据专业获取课题
  getTopicsByMajor(major) {
    return this.$http.get(`/api/topics/major/${major}`)
  },
  
  // 根据难度获取课题
  getTopicsByDifficulty(difficulty) {
    return this.$http.get(`/api/topics/difficulty/${difficulty}`)
  },
  
  // 搜索课题
  searchTopics(keyword) {
    return this.$http.get('/api/topics/search', { params: { keyword } })
  },
  
  // 获取热门课题
  getPopularTopics(limit = 10) {
    return this.$http.get('/api/topics/popular', { params: { limit } })
  },
  
  // 获取课题统计
  getTopicStats() {
    return this.$http.get('/api/topics/stats')
  }
}

// 选题相关API
export const selectionApi = {
  // 获取选题列表
  getSelectionList(params) {
    return this.$http.get('/api/selections', { params })
  },
  
  // 获取选题详情
  getSelectionById(id) {
    return this.$http.get(`/api/selections/${id}`)
  },
  
  // 选择课题
  selectTopic(selectionData) {
    return this.$http.post('/api/selections', selectionData)
  },
  
  // 取消选择
  cancelSelection(id) {
    return this.$http.delete(`/api/selections/${id}`)
  },
  
  // 审核选题
  reviewSelection(id, reviewData) {
    return this.$http.post(`/api/selections/${id}/review`, reviewData)
  },
  
  // 更新进度
  updateProgress(id, progressData) {
    return this.$http.put(`/api/selections/${id}/progress`, progressData)
  },
  
  // 根据学生获取选题
  getSelectionsByStudent(studentId) {
    return this.$http.get(`/api/selections/student/${studentId}`)
  },
  
  // 根据教师获取选题
  getSelectionsByTeacher(teacherId) {
    return this.$http.get(`/api/selections/teacher/${teacherId}`)
  },
  
  // 根据课题获取选题
  getSelectionsByTopic(topicId) {
    return this.$http.get(`/api/selections/topic/${topicId}`)
  },
  
  // 获取选题统计
  getSelectionStats() {
    return this.$http.get('/api/selections/stats')
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
    
    return this.$http.post('/api/documents/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  
  // 下载文档
  downloadDocument(id) {
    return this.$http.get(`/api/documents/download/${id}`, {
      responseType: 'blob'
    })
  },
  
  // 删除文档
  deleteDocument(id) {
    return this.$http.delete(`/api/documents/${id}`)
  },
  
  // 审核文档
  reviewDocument(id, reviewData) {
    return this.$http.post(`/api/documents/${id}/review`, reviewData)
  },
  
  // 根据选题获取文档
  getDocumentsBySelection(selectionId) {
    return this.$http.get(`/api/documents/selection/${selectionId}`)
  },
  
  // 根据课题获取文档
  getDocumentsByTopic(topicId) {
    return this.$http.get(`/api/documents/topic/${topicId}`)
  },
  
  // 根据上传者获取文档
  getDocumentsByUploader(uploaderId) {
    return this.$http.get(`/api/documents/uploader/${uploaderId}`)
  },
  
  // 获取文档详情
  getDocumentById(id) {
    return this.$http.get(`/api/documents/${id}`)
  },
  
  // 获取文档统计
  getDocumentStats() {
    return this.$http.get('/api/documents/stats')
  }
}

// 进度相关API
export const progressApi = {
  // 更新进度
  updateProgress(progressData) {
    return this.$http.post('/api/progress/update', progressData)
  },
  
  // 添加里程碑
  addMilestone(milestoneData) {
    return this.$http.post('/api/progress/milestone', milestoneData)
  },
  
  // 根据选题获取进度
  getProgressBySelection(selectionId) {
    return this.$http.get(`/api/progress/selection/${selectionId}`)
  },
  
  // 根据学生获取进度
  getProgressByStudent(studentId) {
    return this.$http.get(`/api/progress/student/${studentId}`)
  },
  
  // 根据课题获取进度
  getProgressByTopic(topicId) {
    return this.$http.get(`/api/progress/topic/${topicId}`)
  },
  
  // 获取最新进度
  getLatestProgress(selectionId) {
    return this.$http.get(`/api/progress/latest/${selectionId}`)
  },
  
  // 获取进度统计
  getProgressStats() {
    return this.$http.get('/api/progress/stats')
  }
}

// 认证相关API
export const authApi = {
  // 登录
  login(loginData) {
    return this.$http.post('/api/auth/login', loginData)
  },
  
  // 注册
  register(registerData) {
    return this.$http.post('/api/auth/register', registerData)
  },
  
  // 获取用户信息
  getUserInfo() {
    return this.$http.get('/api/auth/userinfo')
  },
  
  // 登出
  logout() {
    return this.$http.post('/api/auth/logout')
  },
  
  // 修改密码
  changePassword(passwordData) {
    return this.$http.post('/api/auth/change-password', passwordData)
  }
}

// 统计相关API
export const statsApi = {
  // 获取系统总览统计
  getOverviewStats() {
    return this.$http.get('/api/stats/overview')
  },
  
  // 获取用户统计
  getUserStats() {
    return this.$http.get('/api/stats/users')
  },
  
  // 获取课题统计
  getTopicStats() {
    return this.$http.get('/api/stats/topics')
  },
  
  // 获取选题统计
  getSelectionStats() {
    return this.$http.get('/api/stats/selections')
  },
  
  // 获取文档统计
  getDocumentStats() {
    return this.$http.get('/api/stats/documents')
  },
  
  // 获取进度统计
  getProgressStats() {
    return this.$http.get('/api/stats/progress')
  }
}
