<template>
  <div class="my-topic">
    <div class="page-header">
      <h2 class="page-title">我的选题</h2>
      <p class="page-desc">查看您已选择的课题信息和管理状态</p>
    </div>
    
    <!-- 选题状态概览 -->
    <div class="status-overview">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="status-card">
            <div class="status-icon">
              <i class="el-icon-document"></i>
            </div>
            <div class="status-content">
              <h3>{{ myTopic ? 1 : 0 }}</h3>
              <p>已选课题</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="status-card">
            <div class="status-icon">
              <i class="el-icon-time"></i>
            </div>
            <div class="status-content">
              <h3>{{ getDaysLeft() }}</h3>
              <p>剩余天数</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="status-card">
            <div class="status-icon">
              <i class="el-icon-star-on"></i>
            </div>
            <div class="status-content">
              <h3>{{ getProgress() }}%</h3>
              <p>完成进度</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="status-card">
            <div class="status-icon">
              <i class="el-icon-medal"></i>
            </div>
            <div class="status-content">
              <h3>{{ getGrade() }}</h3>
              <p>预期成绩</p>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
    
    <!-- 选题详情 -->
    <div v-if="myTopic" class="topic-detail-section">
      <div class="section-header">
        <h3>选题详情</h3>
      </div>
      
      <div class="topic-info-card">
        <div class="topic-basic-info">
          <h4>{{ myTopic.title }}</h4>
          <div class="info-row">
            <span class="label">指导教师：</span>
            <span class="value">{{ myTopic.teacherName || myTopic.teacher }}</span>
          </div>
          <div class="info-row">
            <span class="label">专业要求：</span>
            <span class="value">{{ myTopic.major }}</span>
          </div>
          <div class="info-row">
            <span class="label">难度等级：</span>
            <el-tag :type="getDifficultyType(myTopic.difficulty)" size="small">
              {{ getDifficultyText(myTopic.difficulty) }}
            </el-tag>
          </div>
          <div class="info-row">
            <span class="label">选择时间：</span>
            <span class="value">{{ formatDateTime(myTopic.selectedTime) }}</span>
          </div>
          <div class="info-row">
            <span class="label">截止时间：</span>
            <span class="value">{{ formatDateTime(myTopic.deadline) }}</span>
          </div>
        </div>
        
        <div class="topic-description">
          <h5>课题描述</h5>
          <p>{{ myTopic.description }}</p>
        </div>
        
        <div class="topic-requirements">
          <h5>技术要求</h5>
          <ul>
            <li v-for="requirement in getRequirementsArray(myTopic.requirements)" :key="requirement">
              {{ requirement }}
            </li>
          </ul>
        </div>
      </div>
    </div>
    
    <!-- 进度管理 -->
    <div v-if="myTopic" class="progress-section">
      <div class="section-header">
        <h3>进度管理</h3>
        <el-button type="primary" size="small" @click="editProgress">更新进度</el-button>
      </div>
      
      <div class="progress-card">
        <div class="progress-overview">
          <el-progress 
            :percentage="getProgress()" 
            :color="getProgressColor()"
            :stroke-width="8">
          </el-progress>
          <p class="progress-text">整体进度：{{ getProgress() }}%</p>
        </div>
        
        <div class="progress-timeline">
          <el-timeline>
            <el-timeline-item
              v-for="(milestone, index) in milestones"
              :key="index"
              :timestamp="formatDateTime(milestone.date)"
              :type="milestone.status === 'completed' ? 'success' : milestone.status === 'current' ? 'primary' : 'info'">
              <div class="milestone-content">
                <div class="milestone-header">
                  <h4>{{ milestone.title }}</h4>
                  <div class="milestone-actions">
                    <el-select 
                      v-model="milestone.status" 
                      size="mini" 
                      @change="updateMilestoneStatus(milestone)"
                      style="width: 80px;">
                      <el-option label="待开始" value="pending"></el-option>
                      <el-option label="进行中" value="current"></el-option>
                      <el-option label="已完成" value="completed"></el-option>
                    </el-select>
                  </div>
                </div>
                <p>{{ milestone.description }}</p>
                <div class="milestone-footer">
                  <el-tag 
                    :type="milestone.status === 'completed' ? 'success' : milestone.status === 'current' ? 'primary' : 'info'"
                    size="small">
                    {{ getStatusText(milestone.status) }}
                  </el-tag>
                  <span class="progress-text">进度: {{ milestone.percentage }}%</span>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </div>
    
    <!-- 文档管理 -->
    <div v-if="myTopic" class="documents-section">
      <div class="section-header">
        <h3>文档管理</h3>
        <el-button type="primary" size="small" @click="uploadDocument">上传文档</el-button>
      </div>
      
      <div class="documents-card">
        <el-table :data="documents" style="width: 100%">
          <el-table-column prop="name" label="文档名称" min-width="200"></el-table-column>
          <el-table-column prop="type" label="文档类型" width="120"></el-table-column>
          <el-table-column prop="size" label="文件大小" width="100"></el-table-column>
          <el-table-column prop="uploadTime" label="上传时间" width="150"></el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === 'approved' ? 'success' : scope.row.status === 'pending' ? 'warning' : 'danger'" size="small">
                {{ getDocumentStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template slot-scope="scope">
              <el-button type="text" size="small" @click="downloadDocument(scope.row)">下载</el-button>
              <el-button type="text" size="small" @click="deleteDocument(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    
    <!-- 无选题状态 -->
    <div v-else class="no-topic">
      <div class="no-topic-content">
        <i class="el-icon-document-remove"></i>
        <h3>您还没有选择课题</h3>
        <p>请前往选题列表选择您感兴趣的课题</p>
        <el-button type="primary" @click="goToTopics">去选题</el-button>
      </div>
    </div>
    
    <!-- 进度更新对话框 -->
    <el-dialog
      title="更新进度"
      :visible.sync="progressDialogVisible"
      width="500px">
      <el-form :model="progressForm" label-width="100px">
        <el-form-item label="里程碑标题">
          <el-input
            v-model="progressForm.milestoneTitle"
            placeholder="请输入里程碑标题，如：需求分析、系统设计、编码实现等">
          </el-input>
        </el-form-item>
        <el-form-item label="里程碑状态">
          <el-select v-model="progressForm.milestoneStatus" placeholder="请选择状态">
            <el-option label="进行中" value="current"></el-option>
            <el-option label="已完成" value="completed"></el-option>
            <el-option label="待开始" value="pending"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="当前进度">
          <el-slider v-model="progressForm.percentage" :min="0" :max="100"></el-slider>
        </el-form-item>
        <el-form-item label="进度说明">
          <el-input
            v-model="progressForm.description"
            type="textarea"
            :rows="3"
            placeholder="请描述当前完成的工作内容">
          </el-input>
        </el-form-item>
        <el-form-item label="遇到的问题">
          <el-input
            v-model="progressForm.problems"
            type="textarea"
            :rows="2"
            placeholder="请描述遇到的问题（可选）">
          </el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="progressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProgress">提交</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { selectionApi, progressApi, documentApi, topicApi } from '@/api'
import { getCurrentUserId } from '@/utils/user'

export default {
  name: 'MyTopic',
  data() {
    return {
      loading: false,
      progressDialogVisible: false,
      progressForm: {
        milestoneTitle: '',
        milestoneStatus: 'current',
        percentage: 0,
        description: '',
        problems: ''
      },
      
      // 真实数据
      myTopic: null,
      milestones: [],
      documents: []
    }
  },
  async mounted() {
    await this.loadMyTopicData()
  },
  methods: {
    // 加载我的选题数据
    async loadMyTopicData() {
      try {
        this.loading = true
        
        // 获取当前用户ID
        const userId = await getCurrentUserId()
        if (!userId) {
          this.$message.error('请先登录')
          this.$router.push('/login')
          return
        }
        
        // 获取学生的选题信息
        const selectionResponse = await selectionApi.getSelectionsByStudent(userId)
        if (selectionResponse.code === 200 && selectionResponse.data && selectionResponse.data.length > 0) {
          // 取第一个选题（假设学生只能选一个课题）
          const selection = selectionResponse.data[0]
          
          // 根据topicId获取课题详情
          try {
            const topicResponse = await topicApi.getTopicById(selection.topicId)
            if (topicResponse.code === 200 && topicResponse.data) {
              // 合并选题信息和课题详情
              this.myTopic = {
                ...topicResponse.data,
                // 选题相关信息
                selectionId: selection.id,
                selectedTime: selection.selectionTime,
                status: selection.status,
                progress: selection.progress || 0,
                progressDescription: selection.progressDescription,
                problems: selection.problems,
                finalGrade: selection.finalGrade,
                studentId: selection.studentId,
                studentName: selection.studentName,
                studentNumber: selection.studentNumber,
                // 确保教师信息正确显示
                teacherName: selection.teacherName || topicResponse.data.teacherName
              }
              
              // 获取进度信息
              await this.loadProgressData()
              
              // 获取文档信息
              await this.loadDocumentsData()
            } else {
              this.$message.error('获取课题详情失败')
              this.myTopic = null
            }
          } catch (error) {
            console.error('获取课题详情失败:', error)
            this.$message.error('获取课题详情失败')
            this.myTopic = null
          }
        } else {
          this.myTopic = null
          this.milestones = []
          this.documents = []
        }
      } catch (error) {
        console.error('加载我的选题数据失败:', error)
        
        // 检查是否是权限问题
        if (error.message && (error.message.includes('权限') || error.message.includes('登录'))) {
          this.$message.error('登录已过期，请重新登录')
          this.$router.push('/login')
        } else {
          this.$message.error('加载数据失败，请稍后重试')
        }
      } finally {
        this.loading = false
      }
    },
    
    // 加载进度数据
    async loadProgressData() {
      if (!this.myTopic || !this.myTopic.selectionId) return
      
      try {
        const response = await progressApi.getProgressBySelection(this.myTopic.selectionId)
        if (response.code === 200) {
          // 处理进度数据，过滤出里程碑记录
          const progressData = response.data || []
          this.milestones = progressData
            .map(item => ({
              id: item.id,
              title: item.milestoneTitle || '进度更新', // 为没有标题的记录提供默认标题
              description: item.milestoneDescription || item.description || '',
              status: item.milestoneStatus || 'pending',
              date: item.milestoneDate || item.createTime,
              percentage: item.percentage
            }))
            .sort((a, b) => new Date(a.date) - new Date(b.date)) // 按时间排序
        }
      } catch (error) {
        console.error('加载进度数据失败:', error)
      }
    },
    
    // 加载文档数据
    async loadDocumentsData() {
      if (!this.myTopic || !this.myTopic.selectionId) return
      
      try {
        const response = await documentApi.getDocumentsBySelection(this.myTopic.selectionId)
        if (response.code === 200) {
          this.documents = response.data || []
        }
      } catch (error) {
        console.error('加载文档数据失败:', error)
      }
    },
    
    getDaysLeft() {
      if (!this.myTopic) return 0;
      const deadline = new Date(this.myTopic.deadline);
      const today = new Date();
      const diffTime = deadline - today;
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      return diffDays > 0 ? diffDays : 0;
    },
    
    getProgress() {
      return this.myTopic ? this.myTopic.progress : 0;
    },
    
    getGrade() {
      const progress = this.getProgress();
      if (progress >= 90) return 'A+';
      if (progress >= 80) return 'A';
      if (progress >= 70) return 'B+';
      if (progress >= 60) return 'B';
      return 'C';
    },
    
    getDifficultyType(difficulty) {
      const typeMap = {
        'easy': 'success',
        'medium': 'warning',
        'hard': 'danger'
      };
      return typeMap[difficulty] || 'info';
    },
    
    getDifficultyText(difficulty) {
      const textMap = {
        'easy': '简单',
        'medium': '中等',
        'hard': '困难'
      };
      return textMap[difficulty] || '未知';
    },
    
    getProgressColor() {
      const progress = this.getProgress();
      if (progress >= 80) return '#67c23a';
      if (progress >= 60) return '#e6a23c';
      if (progress >= 40) return '#f56c6c';
      return '#909399';
    },
    
    getStatusText(status) {
      const statusMap = {
        'completed': '已完成',
        'current': '进行中',
        'pending': '待开始'
      };
      return statusMap[status] || '未知';
    },
    
    // 更新里程碑状态
    async updateMilestoneStatus(milestone) {
      try {
        const updateData = {
          id: milestone.id,
          milestoneStatus: milestone.status
        }
        
        const response = await progressApi.updateMilestoneStatus(updateData)
        if (response.code === 200) {
          this.$message.success('里程碑状态更新成功！')
          // 重新加载进度数据
          await this.loadProgressData()
        } else {
          this.$message.error(response.message || '状态更新失败')
          // 恢复原状态
          await this.loadProgressData()
        }
      } catch (error) {
        console.error('更新里程碑状态失败:', error)
        this.$message.error('状态更新失败，请稍后重试')
        // 恢复原状态
        await this.loadProgressData()
      }
    },
    
    // 格式化时间显示，去掉T字符
    formatDateTime(dateTime) {
      if (!dateTime) return ''
      // 将 ISO 8601 格式转换为更友好的显示格式
      return dateTime.replace('T', ' ').replace(/\.\d{3}Z?$/, '')
    },
    
    getDocumentStatusText(status) {
      const statusMap = {
        'approved': '已通过',
        'pending': '待审核',
        'rejected': '已拒绝'
      };
      return statusMap[status] || '未知';
    },
    
    editProgress() {
      this.progressForm.percentage = this.myTopic.progress;
      this.progressForm.description = '';
      this.progressForm.problems = '';
      this.progressDialogVisible = true;
    },
    
    async submitProgress() {
      try {
        const progressData = {
          selectionId: this.myTopic.selectionId,
          milestoneTitle: this.progressForm.milestoneTitle,
          milestoneStatus: this.progressForm.milestoneStatus,
          percentage: this.progressForm.percentage,
          description: this.progressForm.description,
          problems: this.progressForm.problems
        }
        
        const response = await progressApi.updateProgress(progressData)
        if (response.code === 200) {
          // 更新本地数据
          this.myTopic.progress = this.progressForm.percentage
          this.progressDialogVisible = false
          this.$message.success('进度更新成功！')
          
          // 重新加载选题数据和进度数据
          await this.loadMyTopicData()
          await this.loadProgressData()
        } else {
          this.$message.error(response.message || '进度更新失败')
        }
      } catch (error) {
        console.error('更新进度失败:', error)
        this.$message.error('进度更新失败，请稍后重试')
      }
    },
    
    uploadDocument() {
      this.$message.info('文档上传功能开发中...');
    },
    
    async downloadDocument(document) {
      try {
        const response = await documentApi.downloadDocument(document.id)
        // 处理文件下载
        const blob = new Blob([response])
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = document.name
        link.click()
        window.URL.revokeObjectURL(url)
        this.$message.success('文档下载成功！')
      } catch (error) {
        console.error('下载文档失败:', error)
        this.$message.error('下载文档失败，请稍后重试')
      }
    },
    
    async deleteDocument(document) {
      this.$confirm('确定要删除这个文档吗？', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await documentApi.deleteDocument(document.id)
          if (response.code === 200) {
            // 从本地列表中移除
            const index = this.documents.indexOf(document)
        if (index > -1) {
              this.documents.splice(index, 1)
            }
            this.$message.success('文档删除成功！')
          } else {
            this.$message.error(response.message || '文档删除失败')
          }
        } catch (error) {
          console.error('删除文档失败:', error)
          this.$message.error('删除文档失败，请稍后重试')
        }
      });
    },
    
    goToTopics() {
      this.$router.push('/layout/student/topics');
    },
    
    // 处理技术要求格式
    getRequirementsArray(requirements) {
      if (!requirements) return []
      
      // 如果是数组，直接返回
      if (Array.isArray(requirements)) {
        return requirements
      }
      
      // 如果是字符串，按逗号或换行符分割
      if (typeof requirements === 'string') {
        // 处理可能的编码问题，去除特殊字符
        let cleanString = requirements.replace(/[\u200B-\u200D\uFEFF]/g, '') // 去除零宽字符
        cleanString = cleanString.replace(/\s+/g, ' ') // 将多个空格替换为单个空格
        
        return cleanString
          .split(/[,，\n\r]/) // 按逗号、中文逗号、换行符分割
          .map(req => req.trim()) // 去除前后空格
          .filter(req => req.length > 0) // 过滤空字符串
      }
      
      return []
    }
  }
}
</script>

<style scoped>
.my-topic {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 30px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.page-desc {
  color: #7f8c8d;
  margin: 0;
  font-size: 14px;
}

.status-overview {
  margin-bottom: 30px;
}

.status-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
}

.status-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.status-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.status-icon i {
  font-size: 24px;
  color: white;
}

.status-content h3 {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 5px 0;
}

.status-content p {
  color: #7f8c8d;
  margin: 0;
  font-size: 14px;
}

.topic-detail-section,
.progress-section,
.documents-section {
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.topic-info-card,
.progress-card,
.documents-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.topic-basic-info h4 {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 15px 0;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.info-row .label {
  font-weight: 500;
  color: #606266;
  width: 100px;
  flex-shrink: 0;
}

.info-row .value {
  color: #2c3e50;
}

.topic-description,
.topic-requirements {
  margin-top: 20px;
}

.topic-description h5,
.topic-requirements h5 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 10px 0;
}

.topic-description p {
  color: #606266;
  line-height: 1.6;
  margin: 0;
}

.topic-requirements ul {
  margin: 0;
  padding-left: 20px;
}

.topic-requirements li {
  color: #606266;
  margin-bottom: 5px;
}

.progress-overview {
  margin-bottom: 30px;
}

.progress-text {
  text-align: center;
  margin-top: 10px;
  color: #606266;
  font-size: 14px;
}

.milestone-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.milestone-header h4 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
  flex: 1;
}

.milestone-actions {
  margin-left: 10px;
}

.milestone-content p {
  color: #606266;
  margin: 0 0 8px 0;
  font-size: 14px;
}

.milestone-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.progress-text {
  font-size: 12px;
  color: #999;
}

.no-topic {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.no-topic-content {
  text-align: center;
}

.no-topic-content i {
  font-size: 64px;
  color: #c0c4cc;
  margin-bottom: 20px;
}

.no-topic-content h3 {
  font-size: 20px;
  color: #606266;
  margin: 0 0 10px 0;
}

.no-topic-content p {
  color: #909399;
  margin: 0 0 20px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .status-overview .el-row {
    flex-direction: column;
  }
  
  .status-overview .el-col {
    margin-bottom: 15px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .section-header .el-button {
    margin-top: 10px;
  }
}
</style>
