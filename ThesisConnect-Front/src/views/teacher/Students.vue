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
            <el-option label="待审核" value="pending"></el-option>
            <el-option label="待学生确认" value="approved"></el-option>
            <el-option label="已确认" value="confirmed"></el-option>
            <el-option label="进行中" value="active"></el-option>
            <el-option label="已完成" value="completed"></el-option>
            <el-option label="已暂停" value="paused"></el-option>
            <el-option label="已拒绝" value="rejected"></el-option>
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
          <el-table-column prop="real_name" label="姓名" width="100"></el-table-column>
          <el-table-column prop="student_id" label="学号" width="120"></el-table-column>
          <el-table-column prop="topic_title" label="课题" width="200">
            <template slot-scope="scope">
              <el-button type="text" @click="viewTopic(scope.row)">
                {{ scope.row.topic_title }}
              </el-button>
            </template>
          </el-table-column>
          <el-table-column prop="progress" label="进度" width="120">
            <template slot-scope="scope">
              <el-progress :percentage="scope.row.progress || 0" :stroke-width="6"></el-progress>
            </template>
          </el-table-column>
          <el-table-column prop="selection_status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getSelectionStatusType(scope.row.selection_status)" size="small">
                {{ getSelectionStatusText(scope.row.selection_status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="selection_time" label="选题时间" width="120">
            <template slot-scope="scope">
              {{ formatDate(scope.row.selection_time) }}
            </template>
          </el-table-column>
          <el-table-column prop="phone" label="联系电话" width="120"></el-table-column>
          <el-table-column prop="email" label="邮箱" width="150"></el-table-column>
          <el-table-column label="操作" width="170">
            <template slot-scope="scope">
              <el-button 
                v-if="scope.row.selection_status === 'pending'" 
                type="text" 
                size="small" 
                style="color: #67C23A; margin-right: 5px;"
                @click="reviewSelection(scope.row, 'approved')">
                通过
              </el-button>
              <el-button 
                v-if="scope.row.selection_status === 'pending'" 
                type="text" 
                size="small" 
                style="color: #F56C6C; margin-right: 5px;"
                @click="reviewSelection(scope.row, 'rejected')">
                拒绝
              </el-button>
              <el-button type="text" size="small" @click="viewStudentDetail(scope.row)" style="margin-right: 5px;">详情</el-button>
              <el-button type="text" size="small" @click="sendMessage(scope.row)" style="margin-right: 5px;">消息</el-button>
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
              <h3>{{ selectedStudent.real_name }}</h3>
              <p>学号：{{ selectedStudent.student_id }}</p>
              <p>专业：{{ selectedStudent.department }}</p>
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
                    <label>申请状态：</label>
                    <el-tag 
                      :type="getSelectionStatusType(selectedStudent.selection_status)" 
                      size="small">
                      {{ getSelectionStatusText(selectedStudent.selection_status) }}
                    </el-tag>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="info-item">
                    <label>选择时间：</label>
                    <span>{{ formatDate(selectedStudent.selection_time) }}</span>
                  </div>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <div class="info-item">
                    <label>最后更新：</label>
                    <span>{{ formatDate(selectedStudent.update_time) }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="info-item">
                    <label>进度：</label>
                    <el-progress 
                      :percentage="selectedStudent.progress || 0" 
                      :color="getProgressColor(selectedStudent.progress || 0)"
                      :stroke-width="6">
                    </el-progress>
                  </div>
                </el-col>
              </el-row>
              <el-row v-if="selectedStudent.selection_status === 'approved'" :gutter="20">
                <el-col :span="24">
                  <el-alert
                    title="待学生确认"
                    type="warning"
                    :closable="false"
                    show-icon>
                    <template slot="title">
                      <span>该学生的申请已通过审核，等待学生确认选题</span>
                    </template>
                  </el-alert>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="课题进度" name="progress">
            <div class="progress-content">
              <div class="progress-overview">
                <el-progress 
                  :percentage="selectedStudent.progress || 0" 
                  :color="getProgressColor(selectedStudent.progress || 0)"
                  :stroke-width="8">
                </el-progress>
                <p style="text-align: center; margin-top: 10px; color: #666;">
                  整体进度：{{ selectedStudent.progress || 0 }}%
                </p>
              </div>
              
              <!-- 进度时间线 -->
              <div class="progress-timeline" v-if="studentProgressList && studentProgressList.length > 0">
                <h4>进度记录</h4>
                <el-timeline>
                  <el-timeline-item
                    v-for="(progress, index) in studentProgressList"
                    :key="index"
                    :timestamp="formatDateTime(progress.createTime)"
                    :type="progress.milestoneStatus === 'completed' ? 'success' : progress.milestoneStatus === 'current' ? 'primary' : 'info'">
                    <div class="milestone-content">
                      <div class="milestone-header">
                        <h4>{{ progress.milestoneTitle || '进度更新' }}</h4>
                        <div class="milestone-status">
                          <el-tag 
                            :type="progress.milestoneStatus === 'completed' ? 'success' : progress.milestoneStatus === 'current' ? 'primary' : 'info'"
                            size="small">
                            {{ getMilestoneStatusText(progress.milestoneStatus) }}
                          </el-tag>
                        </div>
                      </div>
                      <p class="milestone-description">{{ progress.description || progress.milestoneDescription || '暂无描述' }}</p>
                      <div class="milestone-footer">
                        <span class="progress-text">进度: {{ progress.percentage }}%</span>
                        <span class="progress-time">{{ formatDateTime(progress.createTime) }}</span>
                      </div>
                      
                      <!-- 报告文档与审核状态 -->
                      <div class="review-section" style="margin-top: 10px; padding-top: 10px; border-top: 1px dashed #eee;">
                        <div v-if="progress.reportUrl" style="margin-bottom: 5px;">
                          <a :href="progress.reportUrl" target="_blank" style="color: #409EFF; text-decoration: none; display: flex; align-items: center;">
                            <i class="el-icon-document" style="margin-right: 5px;"></i> 查看报告文档
                          </a>
                        </div>
                        
                        <div class="audit-status" style="display: flex; align-items: center; justify-content: space-between; margin-top: 5px;">
                          <div>
                            <span style="color: #909399; font-size: 12px; margin-right: 5px;">审核状态:</span>
                            <el-tag 
                              size="mini" 
                              effect="dark"
                              :type="progress.status === 'approved' ? 'success' : progress.status === 'rejected' ? 'danger' : 'warning'">
                              {{ progress.status === 'approved' ? '审核通过' : progress.status === 'rejected' ? '审核未通过' : '待审核' }}
                            </el-tag>
                          </div>
                          
                          <div v-if="progress.status === 'pending' || !progress.status" class="audit-actions">
                            <el-button type="text" size="mini" style="color: #67C23A" @click="handleReview(progress, 'approved')">通过</el-button>
                            <el-button type="text" size="mini" style="color: #F56C6C" @click="handleReview(progress, 'rejected')">拒绝</el-button>
                          </div>
                        </div>
                        
                        <div v-if="progress.status === 'rejected'" class="reject-reason" style="margin-top: 5px; background-color: #FEF0F0; padding: 5px 10px; border-radius: 4px;">
                          <span style="color: #F56C6C; font-size: 12px;">拒绝原因: {{ progress.rejectReason }}</span>
                        </div>
                      </div>
                      
                      <div class="problems-section" v-if="progress.problems">
                        <h5>遇到的问题</h5>
                        <p>{{ progress.problems }}</p>
                      </div>
                    </div>
                  </el-timeline-item>
                </el-timeline>
              </div>
              
              <!-- 无进度记录时的提示 -->
              <div v-else class="no-progress">
                <i class="el-icon-time"></i>
                <p>暂无进度记录</p>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="课题信息" name="topic">
            <div class="topic-info">
              <div class="info-item">
                <label>课题名称：</label>
                <span>{{ selectedStudent.topic_title }}</span>
              </div>
              <div class="info-item">
                <label>课题描述：</label>
                <span>{{ selectedStudent.topic_description || '暂无描述' }}</span>
              </div>
              <div class="info-item">
                <label>难度等级：</label>
                <span>{{ getDifficultyText(selectedStudent.difficulty) }}</span>
              </div>
              <div class="info-item">
                <label>专业要求：</label>
                <span>{{ selectedStudent.topic_major || '不限专业' }}</span>
              </div>
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
    
    <!-- 课题详情对话框 -->
    <el-dialog
      title="课题详情"
      :visible.sync="detailDialogVisible"
      width="800px">
      <div v-if="selectedTopic" class="topic-detail">
        <div class="detail-header">
          <h3>{{ selectedTopic.title }}</h3>
          <div class="detail-meta">
            <el-tag :type="getDifficultyType(selectedTopic.difficulty)" size="small">
              {{ getDifficultyText(selectedTopic.difficulty) }}
            </el-tag>
            <el-tag :type="getStatusType(selectedTopic.status)" size="small">
              {{ getStatusText(selectedTopic.status) }}
            </el-tag>
          </div>
        </div>
        
        <div class="detail-content">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本信息" name="basic">
              <div class="info-grid">
                <div class="info-item">
                  <label>课题名称:</label>
                  <span>{{ selectedTopic.title }}</span>
                </div>
                <div class="info-item">
                  <label>专业要求:</label>
                  <span>{{ selectedTopic.major }}</span>
                </div>
                <div class="info-item">
                  <label>难度等级:</label>
                  <span>{{ getDifficultyText(selectedTopic.difficulty) }}</span>
                </div>
                <div class="info-item">
                  <label>最大人数:</label>
                  <span>{{ selectedTopic.maxStudents }}</span>
                </div>
                <div class="info-item">
                  <label>已选人数:</label>
                  <span>{{ selectedTopic.selectedCount }}</span>
                </div>
                <div class="info-item">
                  <label>截止时间:</label>
                  <span>{{ selectedTopic.deadline }}</span>
                </div>
                <div class="info-item">
                  <label>浏览量:</label>
                  <span>{{ selectedTopic.viewCount }}</span>
                </div>
                <div class="info-item">
                  <label>评分:</label>
                  <span>{{ selectedTopic.rating }}</span>
                </div>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="课题描述" name="description">
              <div class="description-content">
                <h4>课题描述</h4>
                <p>{{ selectedTopic.description }}</p>
                
                <h4>技术要求</h4>
                <div class="requirements">
                  <p v-for="requirement in getRequirementsArray(selectedTopic.requirements)" :key="requirement">
                    • {{ requirement }}
                  </p>
                </div>
                
                <h4>预期成果</h4>
                <p>{{ selectedTopic.expectedOutcome }}</p>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <!-- 进度审核拒绝原因对话框 -->
    <el-dialog
      title="拒绝进度更新"
      :visible.sync="reviewDialogVisible"
      width="400px">
      <el-form :model="reviewForm">
        <el-form-item label="拒绝原因">
          <el-input
            v-model="reviewForm.rejectReason"
            type="textarea"
            :rows="3"
            placeholder="请输入拒绝原因，以便学生修改">
          </el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submitReview">确认拒绝</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { userApi, progressApi, selectionApi } from '@/api'

export default {
  name: 'TeacherStudents',
  data() {
    return {
      loading: false,
      searchKeyword: '',
      statusFilter: '',
      topicFilter: '',
      studentDialogVisible: false,
      messageDialogVisible: false,
      evaluateDialogVisible: false,
      detailDialogVisible: false,
      activeTab: 'basic',
      selectedStudent: null,
      selectedTopic: null,
      studentProgressList: [],
      
      // 进度审核相关
      reviewDialogVisible: false,
      currentReviewProgress: null,
      reviewForm: {
        rejectReason: ''
      },
      
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
        totalStudents: 0,
        activeStudents: 0,
        completedStudents: 0,
        avgProgress: 0
      },
      
      topics: [
        { id: 1, title: '基于深度学习的图像识别系统' },
        { id: 2, title: '校园二手交易平台设计与实现' },
        { id: 3, title: '智能家居控制系统' }
      ],
      
      students: []
    }
  },
  computed: {
    filteredStudents() {
      let filtered = this.students;
      
      // 搜索过滤
      if (this.searchKeyword) {
        filtered = filtered.filter(student => 
          student.real_name && student.real_name.toLowerCase().includes(this.searchKeyword.toLowerCase()) ||
          student.student_id && student.student_id.toLowerCase().includes(this.searchKeyword.toLowerCase()) ||
          student.topic_title && student.topic_title.toLowerCase().includes(this.searchKeyword.toLowerCase())
        );
      }
      
      // 状态过滤
      if (this.statusFilter) {
        filtered = filtered.filter(student => student.selection_status === this.statusFilter);
      }
      
      // 课题过滤
      if (this.topicFilter) {
        filtered = filtered.filter(student => student.topic_id === this.topicFilter);
      }
      
      return filtered;
    }
  },
  mounted() {
    this.loadStudents();
    this.loadStats();
  },
  methods: {
    getCurrentTeacherId() {
      const userInfo = JSON.parse(localStorage.getItem('user') || '{}');
      if (!userInfo || !userInfo.id) {
        this.$message.error('用户信息获取失败，请重新登录');
        this.$router.push('/login');
        return null;
      }
      return userInfo.id;
    },
    
    async loadStudents() {
      try {
        this.loading = true;
        const teacherId = this.getCurrentTeacherId();
        if (!teacherId) {
          return;
        }
        const response = await userApi.getStudentsByTeacher(teacherId);
        if (response.code === 200) {
          this.students = response.data || [];
        } else {
          this.$message.error(response.message || '获取学生列表失败');
        }
      } catch (error) {
        console.error('加载学生列表失败:', error);
        this.$message.error('加载学生列表失败，请稍后重试');
      } finally {
        this.loading = false;
      }
    },
    
    async loadStats() {
      try {
        const teacherId = this.getCurrentTeacherId();
        if (!teacherId) {
          return;
        }
        const response = await userApi.getStudentStatsByTeacher(teacherId);
        if (response.code === 200) {
          this.stats = response.data;
        }
      } catch (error) {
        console.error('加载统计数据失败:', error);
      }
    },
    
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
      this.activeTab = 'basic';
      // 加载学生进度数据
      this.loadStudentProgress(student.id);
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
    
    async reviewSelection(student, status) {
      const action = status === 'approved' ? '通过' : '拒绝';
      
      this.$confirm(`确定要${action}该学生的选题申请吗？`, `确认${action}`, {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 找到该学生对应的selection记录
          const selectionId = student.selection_id;
          
          if (!selectionId) {
            this.$message.error('无法获取选题记录ID');
            return;
          }
          
          const reviewData = {
            status: status
          };
          
          const response = await selectionApi.reviewSelection(selectionId, reviewData);
          if (response.code === 200) {
            this.$message.success(`${action}成功！`);
            // 重新加载学生列表
            await this.loadStudents();
            await this.loadStats();
          } else {
            this.$message.error(response.message || `${action}失败`);
          }
        } catch (error) {
          console.error(`${action}申请失败:`, error);
          this.$message.error(`${action}失败，请稍后重试`);
        }
      }).catch(() => {
        // 用户取消
      });
    },
    
    viewTopic(student) {
      // 直接弹窗显示课题详情
      this.selectedTopic = {
        id: student.topic_id,
        title: student.topic_title,
        description: student.topic_description || '暂无描述',
        major: student.major || '不限专业',
        difficulty: student.difficulty || 'medium',
        maxStudents: student.max_students || 2,
        deadline: student.deadline || '暂无截止时间',
        requirements: student.requirements || '暂无要求',
        expectedOutcome: student.expected_outcome || '暂无预期成果',
        viewCount: student.view_count || 0,
        rating: student.rating || '暂无评分',
        status: student.topic_status || 'active',
        selectedCount: student.selected_count || 0
      };
      this.activeTab = 'basic';
      this.detailDialogVisible = true;
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
    
    getSelectionStatusType(status) {
      const statusMap = {
        'pending': 'warning',
        'approved': 'warning',
        'confirmed': 'success',
        'active': 'success',
        'rejected': 'danger',
        'completed': 'info',
        'paused': 'info'
      };
      return statusMap[status] || 'info';
    },
    
    getSelectionStatusText(status) {
      const statusMap = {
        'pending': '待审核',
        'approved': '待学生确认',
        'confirmed': '已确认',
        'active': '进行中',
        'rejected': '已拒绝',
        'completed': '已完成',
        'paused': '已暂停'
      };
      return statusMap[status] || '未知';
    },
    
    formatDate(dateString) {
      if (!dateString) return '-';
      const date = new Date(dateString);
      return date.toLocaleDateString('zh-CN');
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
    
    getRequirementsArray(requirements) {
      if (!requirements) return [];
      if (typeof requirements === 'string') {
        return requirements.split('\n').filter(req => req.trim());
      }
      return requirements;
    },
    
    getProgressColor(progress) {
      if (progress >= 80) return '#67C23A';
      if (progress >= 60) return '#E6A23C';
      if (progress >= 40) return '#F56C6C';
      return '#909399';
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
    },
    
    // 加载学生进度数据
    async loadStudentProgress(studentId) {
      try {
        const response = await progressApi.getProgressByStudent(studentId);
        if (response.code === 200) {
          this.studentProgressList = response.data || [];
          // 按时间倒序排列，最新的在前面
          this.studentProgressList.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));
        } else {
          this.studentProgressList = [];
          console.error('获取学生进度失败:', response.message);
        }
      } catch (error) {
        console.error('加载学生进度失败:', error);
        this.studentProgressList = [];
      }
    },
    
    // 进度审核
    handleReview(progress, status) {
      if (status === 'rejected') {
        this.currentReviewProgress = progress;
        this.reviewForm.rejectReason = '';
        this.reviewDialogVisible = true;
      } else {
        this.$confirm('确定要通过这条进度更新吗？', '确认通过', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'success'
        }).then(async () => {
          try {
            const reviewData = {
              id: progress.id,
              status: 'approved',
              rejectReason: ''
            };
            const response = await progressApi.reviewProgress(reviewData);
            if (response.code === 200) {
              this.$message.success('审核已通过');
              // 刷新进度列表
              if (this.selectedStudent) {
                await this.loadStudentProgress(this.selectedStudent.id);
              }
            } else {
              this.$message.error(response.message || '操作失败');
            }
          } catch (error) {
            console.error('审核失败:', error);
            this.$message.error('审核失败，请稍后重试');
          }
        });
      }
    },

    async submitReview() {
      if (!this.reviewForm.rejectReason.trim()) {
        this.$message.warning('请输入拒绝原因');
        return;
      }
      
      try {
        const reviewData = {
          id: this.currentReviewProgress.id,
          status: 'rejected',
          rejectReason: this.reviewForm.rejectReason
        };
        const response = await progressApi.reviewProgress(reviewData);
        if (response.code === 200) {
          this.$message.success('已拒绝该进度更新');
          this.reviewDialogVisible = false;
          // 刷新进度列表
          if (this.selectedStudent) {
            await this.loadStudentProgress(this.selectedStudent.id);
          }
        } else {
          this.$message.error(response.message || '操作失败');
        }
      } catch (error) {
        console.error('审核失败:', error);
        this.$message.error('审核失败，请稍后重试');
      }
    },

    // 获取里程碑状态文本
    getMilestoneStatusText(status) {
      const statusMap = {
        'completed': '已完成',
        'current': '进行中',
        'pending': '待开始'
      };
      return statusMap[status] || '未知';
    },
    
    // 格式化时间显示
    formatDateTime(dateTime) {
      if (!dateTime) return '';
      // 将 ISO 8601 格式转换为更友好的显示格式
      return dateTime.replace('T', ' ').replace(/\.\d{3}Z?$/, '');
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

.progress-timeline {
  margin-top: 20px;
}

.progress-timeline h4 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 15px 0;
}

.milestone-content {
  background: #f8f9fa;
  border-radius: 6px;
  padding: 15px;
  margin-bottom: 10px;
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

.milestone-status {
  margin-left: 10px;
}

.milestone-description {
  color: #606266;
  margin: 0 0 8px 0;
  font-size: 14px;
  line-height: 1.5;
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
  font-weight: 500;
}

.progress-time {
  font-size: 12px;
  color: #999;
}

.problems-section {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #e4e7ed;
}

.problems-section h5 {
  font-size: 14px;
  font-weight: 600;
  color: #f56c6c;
  margin: 0 0 5px 0;
}

.problems-section p {
  color: #606266;
  margin: 0;
  font-size: 13px;
  line-height: 1.4;
}

.no-progress {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.no-progress i {
  font-size: 48px;
  margin-bottom: 15px;
  display: block;
}

.no-progress p {
  margin: 0;
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

/* 课题详情对话框样式 */
.topic-detail {
  padding: 20px 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.detail-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.detail-meta {
  display: flex;
  gap: 10px;
}

.detail-content {
  margin-top: 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-item label {
  font-weight: 600;
  color: #2c3e50;
  margin-right: 10px;
  min-width: 80px;
}

.info-item span {
  color: #7f8c8d;
}

.description-content h4 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 20px 0 10px 0;
}

.description-content h4:first-child {
  margin-top: 0;
}

.description-content p {
  color: #7f8c8d;
  line-height: 1.6;
  margin: 10px 0;
}

.requirements p {
  margin: 5px 0;
  padding-left: 10px;
}
</style>
