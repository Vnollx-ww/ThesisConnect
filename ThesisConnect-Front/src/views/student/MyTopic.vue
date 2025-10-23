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
        <el-button type="primary" size="small" @click="editProgress">更新进度</el-button>
      </div>
      
      <div class="topic-info-card">
        <div class="topic-basic-info">
          <h4>{{ myTopic.title }}</h4>
          <div class="info-row">
            <span class="label">指导教师：</span>
            <span class="value">{{ myTopic.teacher }}</span>
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
            <span class="value">{{ myTopic.selectedTime }}</span>
          </div>
          <div class="info-row">
            <span class="label">截止时间：</span>
            <span class="value">{{ myTopic.deadline }}</span>
          </div>
        </div>
        
        <div class="topic-description">
          <h5>课题描述</h5>
          <p>{{ myTopic.description }}</p>
        </div>
        
        <div class="topic-requirements">
          <h5>技术要求</h5>
          <ul>
            <li v-for="requirement in myTopic.requirements" :key="requirement">
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
              :timestamp="milestone.date"
              :type="milestone.status === 'completed' ? 'success' : milestone.status === 'current' ? 'primary' : 'info'">
              <div class="milestone-content">
                <h4>{{ milestone.title }}</h4>
                <p>{{ milestone.description }}</p>
                <el-tag 
                  :type="milestone.status === 'completed' ? 'success' : milestone.status === 'current' ? 'primary' : 'info'"
                  size="small">
                  {{ getStatusText(milestone.status) }}
                </el-tag>
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
export default {
  name: 'MyTopic',
  data() {
    return {
      progressDialogVisible: false,
      progressForm: {
        percentage: 0,
        description: '',
        problems: ''
      },
      
      // 模拟数据
      myTopic: {
        id: 1,
        title: '基于深度学习的图像识别系统',
        description: '设计并实现一个基于深度学习的图像识别系统，能够识别多种物体和场景。',
        teacher: '李教授',
        major: '计算机科学与技术',
        difficulty: 'hard',
        selectedTime: '2024-01-15',
        deadline: '2024-03-15',
        requirements: ['熟悉Python编程', '了解深度学习框架', '有图像处理基础'],
        progress: 35
      },
      
      milestones: [
        {
          title: '需求分析',
          description: '完成系统需求分析和功能设计',
          date: '2024-01-20',
          status: 'completed'
        },
        {
          title: '技术调研',
          description: '调研相关技术和算法',
          date: '2024-02-01',
          status: 'completed'
        },
        {
          title: '系统设计',
          description: '完成系统架构设计和数据库设计',
          date: '2024-02-15',
          status: 'current'
        },
        {
          title: '核心功能开发',
          description: '实现图像识别核心功能',
          date: '2024-03-01',
          status: 'pending'
        },
        {
          title: '系统测试',
          description: '完成系统测试和优化',
          date: '2024-03-10',
          status: 'pending'
        },
        {
          title: '论文撰写',
          description: '完成毕业论文撰写',
          date: '2024-03-15',
          status: 'pending'
        }
      ],
      
      documents: [
        {
          name: '需求分析文档.docx',
          type: '需求文档',
          size: '2.3MB',
          uploadTime: '2024-01-20',
          status: 'approved'
        },
        {
          name: '系统设计文档.pdf',
          type: '设计文档',
          size: '5.1MB',
          uploadTime: '2024-02-15',
          status: 'pending'
        },
        {
          name: '技术调研报告.docx',
          type: '调研报告',
          size: '3.2MB',
          uploadTime: '2024-02-01',
          status: 'approved'
        }
      ]
    }
  },
  methods: {
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
    
    submitProgress() {
      this.myTopic.progress = this.progressForm.percentage;
      this.progressDialogVisible = false;
      this.$message.success('进度更新成功！');
    },
    
    uploadDocument() {
      this.$message.info('文档上传功能开发中...');
    },
    
    downloadDocument(document) {
      this.$message.info(`正在下载 ${document.name}`);
    },
    
    deleteDocument(document) {
      this.$confirm('确定要删除这个文档吗？', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const index = this.documents.indexOf(document);
        if (index > -1) {
          this.documents.splice(index, 1);
          this.$message.success('文档删除成功！');
        }
      });
    },
    
    goToTopics() {
      this.$router.push('/layout/student/topics');
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

.milestone-content h4 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 5px 0;
}

.milestone-content p {
  color: #606266;
  margin: 0 0 8px 0;
  font-size: 14px;
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
