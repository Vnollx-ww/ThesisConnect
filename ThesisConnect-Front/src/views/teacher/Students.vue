<template>
  <div class="teacher-students">
    <div class="page-header">
      <h2 class="page-title">学生管理</h2>
      <p class="page-desc">管理选择您课题的学生，跟踪他们的学习进度</p>
    </div>
    
    <!-- 学生统计概览 -->
    <div class="stats-overview">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon">
              <i class="el-icon-user"></i>
            </div>
            <div class="stat-content">
              <h3>{{ stats.totalStudents }}</h3>
              <p>总学生数</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon">
              <i class="el-icon-time"></i>
            </div>
            <div class="stat-content">
              <h3>{{ stats.activeStudents }}</h3>
              <p>活跃学生</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon">
              <i class="el-icon-check"></i>
            </div>
            <div class="stat-content">
              <h3>{{ stats.completedStudents }}</h3>
              <p>已完成</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon">
              <i class="el-icon-data-line"></i>
            </div>
            <div class="stat-content">
              <h3>{{ stats.avgProgress }}%</h3>
              <p>平均进度</p>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
    
    <!-- 筛选和搜索 -->
    <div class="filter-section">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索学生姓名或学号"
            prefix-icon="el-icon-search"
            @input="handleSearch">
          </el-input>
        </el-col>
        <el-col :span="6">
          <el-select v-model="statusFilter" placeholder="状态筛选" @change="handleFilter">
            <el-option label="全部状态" value=""></el-option>
            <el-option label="进行中" value="active"></el-option>
            <el-option label="已完成" value="completed"></el-option>
            <el-option label="已暂停" value="paused"></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="topicFilter" placeholder="课题筛选" @change="handleFilter">
            <el-option label="全部课题" value=""></el-option>
            <el-option 
              v-for="topic in topics" 
              :key="topic.id" 
              :label="topic.title" 
              :value="topic.id">
            </el-option>
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button @click="resetFilter">重置筛选</el-button>
        </el-col>
      </el-row>
    </div>
    
    <!-- 学生列表 -->
    <div class="students-section">
      <div class="section-header">
        <h3>学生列表</h3>
        <el-button type="primary" @click="exportStudents">
          <i class="el-icon-download"></i>
          导出数据
        </el-button>
      </div>
      
      <div class="students-table">
        <el-table :data="filteredStudents" style="width: 100%">
          <el-table-column prop="name" label="姓名" width="100"></el-table-column>
          <el-table-column prop="studentId" label="学号" width="120"></el-table-column>
          <el-table-column prop="topicTitle" label="课题" width="200">
            <template slot-scope="scope">
              <el-button type="text" @click="viewTopic(scope.row)">
                {{ scope.row.topicTitle }}
              </el-button>
            </template>
          </el-table-column>
          <el-table-column prop="progress" label="进度" width="120">
            <template slot-scope="scope">
              <el-progress :percentage="scope.row.progress" :stroke-width="6"></el-progress>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.status)" size="small">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="lastUpdate" label="最后更新" width="120"></el-table-column>
          <el-table-column prop="phone" label="联系电话" width="120"></el-table-column>
          <el-table-column prop="email" label="邮箱" width="150"></el-table-column>
          <el-table-column label="操作" width="280">
            <template slot-scope="scope">
              <el-button type="text" size="small" @click="viewStudentDetail(scope.row)">详情</el-button>
              <el-button type="text" size="small" @click="updateProgress(scope.row)">更新进度</el-button>
              <el-button type="text" size="small" @click="sendMessage(scope.row)">发送消息</el-button>
              <el-button type="text" size="small" @click="evaluateStudent(scope.row)">评价</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    
    <!-- 学生详情对话框 -->
    <el-dialog
      title="学生详情"
      :visible.sync="studentDialogVisible"
      width="700px">
      <div v-if="selectedStudent" class="student-detail">
        <div class="student-header">
          <div class="student-basic">
            <el-avatar :size="60" :src="selectedStudent.avatar">
              <i class="el-icon-user-solid"></i>
            </el-avatar>
            <div class="student-info">
              <h3>{{ selectedStudent.name }}</h3>
              <p>学号：{{ selectedStudent.studentId }}</p>
              <p>专业：{{ selectedStudent.major }}</p>
            </div>
          </div>
          <div class="student-stats">
            <div class="stat-item">
              <span class="stat-number">{{ selectedStudent.progress }}%</span>
              <span class="stat-label">完成进度</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ selectedStudent.gpa }}</span>
              <span class="stat-label">GPA</span>
            </div>
          </div>
        </div>
        
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="basic">
            <div class="info-content">
              <el-row :gutter="20">
                <el-col :span="12">
                  <div class="info-item">
                    <label>联系电话：</label>
                    <span>{{ selectedStudent.phone }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="info-item">
                    <label>邮箱：</label>
                    <span>{{ selectedStudent.email }}</span>
                  </div>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <div class="info-item">
                    <label>选择时间：</label>
                    <span>{{ selectedStudent.selectedTime }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="info-item">
                    <label>最后更新：</label>
                    <span>{{ selectedStudent.lastUpdate }}</span>
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="学习进度" name="progress">
            <div class="progress-content">
              <div class="progress-overview">
                <el-progress 
                  :percentage="selectedStudent.progress" 
                  :color="getProgressColor(selectedStudent.progress)"
                  :stroke-width="8">
                </el-progress>
              </div>
              
              <div class="progress-timeline">
                <el-timeline>
                  <el-timeline-item
                    v-for="(milestone, index) in selectedStudent.milestones"
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
          </el-tab-pane>
          
          <el-tab-pane label="文档记录" name="documents">
            <div class="documents-content">
              <el-table :data="selectedStudent.documents" style="width: 100%">
                <el-table-column prop="name" label="文档名称" min-width="200"></el-table-column>
                <el-table-column prop="type" label="文档类型" width="120"></el-table-column>
                <el-table-column prop="uploadTime" label="上传时间" width="150"></el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template slot-scope="scope">
                    <el-tag :type="scope.row.status === 'approved' ? 'success' : scope.row.status === 'pending' ? 'warning' : 'danger'" size="small">
                      {{ getDocumentStatusText(scope.row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="100" fixed="right">
                  <template slot-scope="scope">
                    <el-button type="text" size="small" @click="downloadDocument(scope.row)">下载</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
    
    <!-- 发送消息对话框 -->
    <el-dialog
      title="发送消息"
      :visible.sync="messageDialogVisible"
      width="500px">
      <el-form :model="messageForm" label-width="80px">
        <el-form-item label="收件人">
          <el-input v-model="messageForm.receiver" disabled></el-input>
        </el-form-item>
        <el-form-item label="消息内容">
          <el-input
            v-model="messageForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入消息内容">
          </el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="messageDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="sendMessageSubmit">发送</el-button>
      </span>
    </el-dialog>
    
    <!-- 学生评价对话框 -->
    <el-dialog
      title="学生评价"
      :visible.sync="evaluateDialogVisible"
      width="500px">
      <el-form :model="evaluateForm" label-width="100px">
        <el-form-item label="学生姓名">
          <el-input v-model="evaluateForm.studentName" disabled></el-input>
        </el-form-item>
        <el-form-item label="学习态度">
          <el-rate v-model="evaluateForm.attitude"></el-rate>
        </el-form-item>
        <el-form-item label="技术能力">
          <el-rate v-model="evaluateForm.ability"></el-rate>
        </el-form-item>
        <el-form-item label="创新思维">
          <el-rate v-model="evaluateForm.innovation"></el-rate>
        </el-form-item>
        <el-form-item label="综合评价">
          <el-input
            v-model="evaluateForm.comment"
            type="textarea"
            :rows="3"
            placeholder="请输入综合评价">
          </el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="evaluateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEvaluation">提交</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'TeacherStudents',
  data() {
    return {
      searchKeyword: '',
      statusFilter: '',
      topicFilter: '',
      studentDialogVisible: false,
      messageDialogVisible: false,
      evaluateDialogVisible: false,
      activeTab: 'basic',
      selectedStudent: null,
      
      messageForm: {
        receiver: '',
        content: ''
      },
      
      evaluateForm: {
        studentName: '',
        attitude: 0,
        ability: 0,
        innovation: 0,
        comment: ''
      },
      
      stats: {
        totalStudents: 12,
        activeStudents: 8,
        completedStudents: 2,
        avgProgress: 65
      },
      
      topics: [
        { id: 1, title: '基于深度学习的图像识别系统' },
        { id: 2, title: '校园二手交易平台设计与实现' },
        { id: 3, title: '智能家居控制系统' }
      ],
      
      students: [
        {
          id: 1,
          name: '张三',
          studentId: '2021001001',
          topicId: 1,
          topicTitle: '基于深度学习的图像识别系统',
          progress: 35,
          status: 'active',
          lastUpdate: '2024-02-15',
          phone: '13800138000',
          email: 'zhangsan@example.com',
          major: '计算机科学与技术',
          gpa: '3.8',
          selectedTime: '2024-01-15',
          avatar: '',
          milestones: [
            { title: '需求分析', description: '完成系统需求分析', date: '2024-01-20', status: 'completed' },
            { title: '技术调研', description: '调研相关技术', date: '2024-02-01', status: 'completed' },
            { title: '系统设计', description: '完成系统设计', date: '2024-02-15', status: 'current' }
          ],
          documents: [
            { name: '需求分析文档.docx', type: '需求文档', uploadTime: '2024-01-20', status: 'approved' },
            { name: '技术调研报告.docx', type: '调研报告', uploadTime: '2024-02-01', status: 'approved' }
          ]
        },
        {
          id: 2,
          name: '李四',
          studentId: '2021001002',
          topicId: 1,
          topicTitle: '基于深度学习的图像识别系统',
          progress: 20,
          status: 'active',
          lastUpdate: '2024-02-10',
          phone: '13800138001',
          email: 'lisi@example.com',
          major: '计算机科学与技术',
          gpa: '3.6',
          selectedTime: '2024-01-20',
          avatar: '',
          milestones: [
            { title: '需求分析', description: '完成系统需求分析', date: '2024-01-25', status: 'completed' },
            { title: '技术调研', description: '调研相关技术', date: '2024-02-10', status: 'current' }
          ],
          documents: [
            { name: '需求分析文档.docx', type: '需求文档', uploadTime: '2024-01-25', status: 'pending' }
          ]
        },
        {
          id: 3,
          name: '王五',
          studentId: '2021001003',
          topicId: 2,
          topicTitle: '校园二手交易平台设计与实现',
          progress: 15,
          status: 'active',
          lastUpdate: '2024-02-12',
          phone: '13800138002',
          email: 'wangwu@example.com',
          major: '软件工程',
          gpa: '3.5',
          selectedTime: '2024-01-25',
          avatar: '',
          milestones: [
            { title: '需求分析', description: '完成系统需求分析', date: '2024-02-12', status: 'current' }
          ],
          documents: []
        }
      ]
    }
  },
  computed: {
    filteredStudents() {
      let filtered = this.students;
      
      // 搜索过滤
      if (this.searchKeyword) {
        filtered = filtered.filter(student => 
          student.name.toLowerCase().includes(this.searchKeyword.toLowerCase()) ||
          student.studentId.toLowerCase().includes(this.searchKeyword.toLowerCase())
        );
      }
      
      // 状态过滤
      if (this.statusFilter) {
        filtered = filtered.filter(student => student.status === this.statusFilter);
      }
      
      // 课题过滤
      if (this.topicFilter) {
        filtered = filtered.filter(student => student.topicId === this.topicFilter);
      }
      
      return filtered;
    }
  },
  methods: {
    handleSearch() {
      // 搜索逻辑已在computed中处理
    },
    
    handleFilter() {
      // 筛选逻辑已在computed中处理
    },
    
    resetFilter() {
      this.searchKeyword = '';
      this.statusFilter = '';
      this.topicFilter = '';
    },
    
    viewStudentDetail(student) {
      this.selectedStudent = student;
      this.studentDialogVisible = true;
    },
    
    sendMessage(student) {
      this.messageForm.receiver = student.name;
      this.messageForm.content = '';
      this.messageDialogVisible = true;
    },
    
    sendMessageSubmit() {
      this.messageDialogVisible = false;
      this.$message.success('消息发送成功！');
    },
    
    evaluateStudent(student) {
      this.evaluateForm.studentName = student.name;
      this.evaluateForm.attitude = 0;
      this.evaluateForm.ability = 0;
      this.evaluateForm.innovation = 0;
      this.evaluateForm.comment = '';
      this.evaluateDialogVisible = true;
    },
    
    submitEvaluation() {
      this.evaluateDialogVisible = false;
      this.$message.success('评价提交成功！');
    },
    
    viewTopic(student) {
      this.$message.info(`查看课题"${student.topicTitle}"的详情`);
    },
    
    downloadDocument(document) {
      this.$message.info(`正在下载 ${document.name}`);
    },
    
    exportStudents() {
      this.$message.info('学生数据导出功能开发中...');
    },
    
    getStatusType(status) {
      const typeMap = {
        'active': 'success',
        'completed': 'info',
        'paused': 'warning'
      };
      return typeMap[status] || 'info';
    },
    
    getStatusText(status) {
      const textMap = {
        'active': '进行中',
        'completed': '已完成',
        'paused': '已暂停'
      };
      return textMap[status] || '未知';
    },
    
    getDocumentStatusText(status) {
      const statusMap = {
        'approved': '已通过',
        'pending': '待审核',
        'rejected': '已拒绝'
      };
      return statusMap[status] || '未知';
    },
    
    getProgressColor(progress) {
      if (progress >= 80) return '#67c23a';
      if (progress >= 60) return '#e6a23c';
      if (progress >= 40) return '#f56c6c';
      return '#909399';
    }
  }
}
</script>

<style scoped>
.teacher-students {
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

.stats-overview {
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-icon i {
  font-size: 24px;
  color: white;
}

.stat-content h3 {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 5px 0;
}

.stat-content p {
  color: #7f8c8d;
  margin: 0;
  font-size: 14px;
}

.filter-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.students-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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

.student-detail {
  max-height: 500px;
  overflow-y: auto;
}

.student-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.student-basic {
  display: flex;
  align-items: center;
}

.student-info {
  margin-left: 15px;
}

.student-info h3 {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 5px 0;
}

.student-info p {
  color: #7f8c8d;
  margin: 0 0 3px 0;
  font-size: 14px;
}

.student-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
}

.stat-label {
  font-size: 12px;
  color: #7f8c8d;
}

.info-content {
  padding: 20px 0;
}

.info-item {
  margin-bottom: 15px;
}

.info-item label {
  font-weight: 500;
  color: #606266;
  margin-right: 10px;
}

.info-item span {
  color: #2c3e50;
}

.progress-content {
  padding: 20px 0;
}

.progress-overview {
  margin-bottom: 30px;
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

.documents-content {
  padding: 20px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .filter-section .el-row {
    flex-direction: column;
  }
  
  .filter-section .el-col {
    margin-bottom: 10px;
  }
  
  .stats-overview .el-row {
    flex-direction: column;
  }
  
  .stats-overview .el-col {
    margin-bottom: 15px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .student-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .student-basic {
    margin-bottom: 15px;
  }
  
  .student-stats {
    align-self: stretch;
    justify-content: space-around;
  }
}
</style>
