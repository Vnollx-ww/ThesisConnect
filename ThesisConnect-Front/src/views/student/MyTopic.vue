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
    
    <!-- 我的申请列表 -->
    <div v-if="applications && applications.length > 0 && !myTopic" class="applications-section">
      <div class="section-header">
        <h3>
          <i class="el-icon-document-add"></i>
          我的申请
          <el-tag size="small" :type="getPendingCount() > 0 ? 'warning' : 'success'">
            {{ applications.length }} 个
          </el-tag>
        </h3>
      </div>
      
      <div class="applications-list">
        <div v-for="app in applications" :key="app.id" class="application-card" :class="getApplicationCardClass(app.status)">
          <div class="application-header">
            <div class="application-title-section">
              <div class="application-icon">
                <i class="el-icon-folder-opened"></i>
              </div>
              <div class="application-title-content">
                <h4>{{ app.topicTitle }}</h4>
                <div class="application-meta">
                  <el-tag 
                    :type="getApplicationStatusType(app.status)" 
                    size="mini"
                    effect="plain">
                    {{ getApplicationStatusText(app.status) }}
                  </el-tag>
                  <span class="teacher-name">
                    <i class="el-icon-user"></i>
                    {{ app.teacherName }}
                  </span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="application-body">
            <div class="application-info-grid">
              <div class="info-item">
                <i class="el-icon-time"></i>
                <div class="info-text">
                  <div class="info-label">申请时间</div>
                  <div class="info-value">{{ formatDate(app.createTime) }}</div>
                </div>
              </div>
              <div v-if="app.status === 'approved'" class="info-item">
                <i class="el-icon-check"></i>
                <div class="info-text">
                  <div class="info-label">审核通过时间</div>
                  <div class="info-value">{{ formatDate(app.updateTime) }}</div>
                </div>
              </div>
            </div>
          </div>
          
          <div v-if="app.status === 'approved'" class="application-footer">
            <div class="approval-notice">
              <i class="el-icon-info"></i>
              <span>老师已审核通过，请确认是否选择此课题</span>
            </div>
            <div class="application-actions">
              <el-button 
                type="success" 
                icon="el-icon-check"
                @click="confirmApplication(app)">
                确认选题
              </el-button>
              <el-button 
                icon="el-icon-close"
                @click="cancelApplication(app)">
                拒绝申请
              </el-button>
            </div>
          </div>
          
          <div v-else-if="app.status === 'pending'" class="application-footer">
            <div class="application-actions" style="width: 100%; justify-content: flex-end;">
              <el-button 
                type="danger" 
                size="small"
                icon="el-icon-delete"
                @click="withdrawApplication(app)">
                撤销申请
              </el-button>
            </div>
          </div>
          
          <div v-else-if="app.status === 'rejected'" class="application-footer">
            <div class="application-actions" style="width: 100%; justify-content: flex-end;">
              <el-button 
                type="info" 
                size="small"
                icon="el-icon-delete"
                @click="deleteApplication(app)">
                删除记录
              </el-button>
            </div>
          </div>
        </div>
      </div>
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
      <!-- 结题评价展示 -->
      <div v-if="myTopic.status === 'completed' && myTopic.finalGrade" class="completion-card" style="margin-bottom: 20px; background: #f0f9eb; border: 1px solid #e1f3d8; padding: 20px; border-radius: 4px;">
        <div style="display: flex; align-items: center; margin-bottom: 15px;">
          <i class="el-icon-s-claim" style="color: #67C23A; font-size: 24px; margin-right: 10px;"></i>
          <h3 style="margin: 0; color: #67C23A;">恭喜！您的课题已结题</h3>
        </div>
        <div class="grade-info" style="display: flex; margin-bottom: 15px;">
          <div style="margin-right: 30px;">
            <span style="color: #606266; font-weight: bold;">最终成绩：</span>
            <span style="font-size: 20px; color: #F56C6C; font-weight: bold;">{{ myTopic.finalGrade }}</span>
          </div>
        </div>
        <div v-if="myTopic.teacherEvaluation" class="evaluation-info">
          <div style="color: #606266; font-weight: bold; margin-bottom: 5px;">教师评价：</div>
          <div style="color: #606266; line-height: 1.6; background: #fff; padding: 10px; border-radius: 4px;">
            {{ myTopic.teacherEvaluation }}
          </div>
        </div>
      </div>

      <div class="section-header">
        <h3>进度管理</h3>
        <el-tooltip :content="progressTooltip" placement="top" :disabled="!isUpdateDisabled">
          <span style="display: inline-block;">
            <el-button type="primary" size="small" @click="editProgress()" :disabled="isUpdateDisabled">更新进度</el-button>
          </span>
        </el-tooltip>
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
                  <h4 style="display: flex; align-items: center;">
                    {{ milestone.title }}
                    <el-tag size="mini" type="info" style="margin-left: 10px; font-weight: normal;">进度: {{ milestone.percentage }}%</el-tag>
                  </h4>
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
                  <div style="margin-top: 5px;">
                    <el-tag 
                      size="mini" 
                      effect="dark"
                      :type="milestone.auditStatus === 'approved' ? 'success' : milestone.auditStatus === 'rejected' ? 'danger' : 'warning'">
                      {{ milestone.auditStatus === 'approved' ? '审核通过' : milestone.auditStatus === 'rejected' ? '审核未通过' : '待审核' }}
                    </el-tag>
                    <span v-if="milestone.reportUrl" style="margin-left: 10px;">
                      <a :href="milestone.reportUrl" target="_blank" style="color: #409EFF; text-decoration: none;">
                        <i class="el-icon-document"></i> 查看报告
                      </a>
                    </span>
                  </div>
                  <div v-if="milestone.auditStatus === 'rejected'" style="margin-top: 5px; color: #F56C6C; font-size: 12px;">
                    <div>原因: {{ milestone.rejectReason }}</div>
                    <el-button type="text" size="mini" @click="editProgress(milestone)" v-if="myTopic.status !== 'completed'">修改并重新提交</el-button>
                  </div>
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
          <el-table-column prop="originalName" label="文档名称" min-width="200"></el-table-column>
          <el-table-column prop="type" label="文档类型" width="120"></el-table-column>
          <el-table-column label="文件大小" width="120">
            <template slot-scope="scope">
              {{ (scope.row.size / 1024).toFixed(2) }} KB
            </template>
          </el-table-column>
          <el-table-column label="上传时间" width="180">
            <template slot-scope="scope">
              {{ formatDateTime(scope.row.createTime) }}
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
    <div v-else-if="!applications || applications.length === 0" class="no-topic">
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
        <el-form-item label="报告文档">
          <el-upload
            action="#"
            :http-request="handleUpload"
            :file-list="fileList"
            :limit="1"
            :on-remove="handleRemove">
            <el-button size="small" type="primary">点击上传</el-button>
            <div slot="tip" class="el-upload__tip">只能上传docx/pdf文件，每个进度需单独上传</div>
          </el-upload>
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
    <!-- 文档上传对话框 -->
    <el-dialog
      title="上传文档"
      :visible.sync="documentDialogVisible"
      width="400px">
      <div class="upload-container">
        <el-upload
          class="upload-demo"
          drag
          action="#"
          :http-request="handleDocumentUpload"
          :file-list="documentFileList"
          :on-remove="handleDocumentRemove"
          multiple>
          <i class="el-icon-upload"></i>
          <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
          <div class="el-upload__tip" slot="tip">支持各类文档格式，大小不超过50MB</div>
        </el-upload>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="documentDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { selectionApi, progressApi, documentApi, topicApi, fileApi } from '@/api'
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
        problems: '',
        reportUrl: ''
      },
      fileList: [],
      documentDialogVisible: false,
      documentFileList: [],
      
      // 真实数据
      myTopic: null,
      milestones: [],
      documents: [],
      applications: [] // 我的申请列表
    }
  },
  computed: {
    // 检查是否有待审核的进度
    hasPendingReview() {
      if (!this.milestones || this.milestones.length === 0) return false;
      // 只要有一个状态是pending，就认为有待审核的
      return this.milestones.some(m => m.auditStatus === 'pending');
    },
    isUpdateDisabled() {
      return this.hasPendingReview || (this.myTopic && this.myTopic.status === 'completed');
    },
    progressTooltip() {
      if (this.myTopic && this.myTopic.status === 'completed') {
        return '课题已结题，不能更新进度';
      }
      if (this.hasPendingReview) {
        return '当前有待审核的进度，请等待审核完成后再提交';
      }
      return '';
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
          // 分离已确认的选题（status为confirmed或active）和待处理的申请
          const confirmedSelections = selectionResponse.data.filter(s => 
            s.status === 'confirmed' || s.status === 'active' || s.status === 'completed'
          )
          const pendingApplications = selectionResponse.data.filter(s => 
            s.status === 'pending' || s.status === 'approved' || s.status === 'rejected'
          )
          
          this.applications = pendingApplications
          
          // 如果有已确认的选题，取第一个
          if (confirmedSelections.length > 0) {
            const selection = confirmedSelections[0]
            
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
                  teacherEvaluation: selection.teacherEvaluation,
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
        } else {
          this.myTopic = null
          this.milestones = []
          this.documents = []
          this.applications = []
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
              problems: item.problems,
              status: item.milestoneStatus || 'pending',
              auditStatus: item.status || 'pending',
              rejectReason: item.rejectReason,
              reportUrl: item.reportUrl,
              date: item.milestoneDate || item.createTime,
              percentage: item.percentage
            }))
            .sort((a, b) => new Date(a.date) - new Date(b.date)) // 按时间排序
        }
      } catch (error) {
        console.error('加载进度数据失败:', error)
      }
    },

    // 文件上传处理
    async handleUpload(options) {
      try {
        const { file } = options
        // 检查文件类型
        const isDoc = file.type === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' || 
                      file.type === 'application/pdf' || 
                      file.name.endsWith('.docx') || 
                      file.name.endsWith('.pdf')
        
        if (!isDoc) {
          this.$message.error('只能上传 docx 或 pdf 格式的文件!')
          return
        }

        const response = await fileApi.uploadFile(file)
        if (response.code === 200) {
          this.progressForm.reportUrl = response.data
          this.$message.success('文件上传成功')
        } else {
          this.$message.error('文件上传失败')
          this.fileList = []
        }
      } catch (error) {
        console.error('文件上传出错:', error)
        this.$message.error('文件上传出错')
        this.fileList = []
      }
    },

    handleRemove() {
      this.progressForm.reportUrl = ''
      this.fileList = []
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
    
    // 格式化日期显示（只显示日期，不显示时间）
    formatDate(dateStr) {
      if (!dateStr) return '-'
      // 处理 ISO 格式 2025-10-23T10:49:58 或普通格式 2025-10-23 10:49:58
      return dateStr.split('T')[0].split(' ')[0]
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
    
    editProgress(milestone) {
      if (milestone && milestone.id) {
        this.progressForm.milestoneTitle = milestone.title;
        this.progressForm.milestoneStatus = milestone.status;
        this.progressForm.percentage = milestone.percentage;
        this.progressForm.description = milestone.description;
        this.progressForm.problems = milestone.problems;
        this.progressForm.reportUrl = ''; // 重置文件，要求重新上传
        this.fileList = [];
      } else {
        this.progressForm.percentage = this.myTopic.progress;
        this.progressForm.description = '';
        this.progressForm.problems = '';
        this.progressForm.reportUrl = '';
        this.fileList = [];
      }
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
          problems: this.progressForm.problems,
          reportUrl: this.progressForm.reportUrl
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
      this.documentDialogVisible = true;
      this.documentFileList = [];
    },
    
    async handleDocumentUpload(options) {
      try {
        const { file } = options;
        
        if (!this.myTopic) {
          this.$message.error('无法获取选题信息');
          options.onError();
          return;
        }
        
        // 限制文件大小 (例如 50MB)
        const isLt50M = file.size / 1024 / 1024 < 50;
        if (!isLt50M) {
          this.$message.error('上传文件大小不能超过 50MB!');
          options.onError();
          return;
        }

        // 调用上传API
        const response = await documentApi.uploadDocument(
          file, 
          this.myTopic.selectionId, 
          this.myTopic.id // topicId
        );
        
        if (response.code === 200) {
          this.$message.success('文档上传成功');
          // 刷新文档列表
          await this.loadDocumentsData();
          options.onSuccess(response.data);
        } else {
          this.$message.error(response.message || '上传失败');
          options.onError();
        }
      } catch (error) {
        console.error('上传出错:', error);
        this.$message.error('上传出错');
        options.onError();
      }
    },
    
    handleDocumentRemove(file, fileList) {
      this.documentFileList = fileList;
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
      this.$router.push('/student/topics');
    },
    
    // 获取申请状态文本
    getApplicationStatusText(status) {
      const statusMap = {
        'pending': '待审核',
        'approved': '审核通过',
        'rejected': '已拒绝',
        'confirmed': '已确认'
      };
      return statusMap[status] || '未知';
    },
    
    // 获取申请状态类型
    getApplicationStatusType(status) {
      const typeMap = {
        'pending': 'warning',
        'approved': 'success',
        'rejected': 'danger',
        'confirmed': 'success'
      };
      return typeMap[status] || 'info';
    },
    
    // 获取申请卡片样式类
    getApplicationCardClass(status) {
      if (status === 'approved') return 'application-approved'
      if (status === 'rejected') return 'application-rejected'
      return 'application-pending'
    },
    
    // 获取待审核数量
    getPendingCount() {
      return this.applications.filter(app => app.status === 'pending').length
    },
    
    // 确认申请（学生确认选题）
    async confirmApplication(app) {
      this.$confirm('确定要确认选择这个课题吗？确认后选题将正式开始。', '确认选题', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 调用后端接口确认申请
          const response = await selectionApi.confirmSelection(app.id)
          if (response.code === 200) {
            this.$message.success('选题确认成功！')
            // 重新加载数据
            await this.loadMyTopicData()
          } else {
            this.$message.error(response.message || '确认失败')
          }
        } catch (error) {
          console.error('确认申请失败:', error)
          this.$message.error('确认失败，请稍后重试')
        }
      })
    },
    
    // 拒绝申请（学生拒绝选题）
    async cancelApplication(app) {
      this.$confirm('确定要拒绝这个选题吗？拒绝后将无法再选择。', '确认拒绝', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 调用后端接口，删除或取消该申请
          const response = await selectionApi.cancelSelection(app.id)
          if (response.code === 200) {
            this.$message.success('已拒绝该选题')
            // 重新加载数据
            await this.loadMyTopicData()
          } else {
            this.$message.error(response.message || '操作失败')
          }
        } catch (error) {
          console.error('拒绝申请失败:', error)
          this.$message.error('操作失败，请稍后重试')
        }
      })
    },
    
    // 撤销申请（学生撤销待审核的申请）
    async withdrawApplication(app) {
      this.$confirm('确定要撤销这个申请吗？', '确认撤销', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 调用后端接口，删除该申请
          const response = await selectionApi.cancelSelection(app.id)
          if (response.code === 200) {
            this.$message.success('申请已撤销')
            // 重新加载数据
            await this.loadMyTopicData()
          } else {
            this.$message.error(response.message || '操作失败')
          }
        } catch (error) {
          console.error('撤销申请失败:', error)
          this.$message.error('操作失败，请稍后重试')
        }
      })
    },
    
    // 删除记录（删除已拒绝的申请记录）
    async deleteApplication(app) {
      this.$confirm('确定要删除这条记录吗？', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 调用后端接口，删除该记录
          // 注意：后端cancelSelection接口目前只允许删除pending状态，需要修改后端支持删除rejected状态
          // 或者前端不调用后端，只是隐藏显示？不，应该调用后端彻底删除。
          // 我们需要让后端cancelSelection支持删除rejected状态，或者增加一个新的delete接口
          // 暂时复用cancelSelection，但需要后端配合修改
          const response = await selectionApi.cancelSelection(app.id)
          if (response.code === 200) {
            this.$message.success('记录已删除')
            // 重新加载数据
            await this.loadMyTopicData()
          } else {
            this.$message.error(response.message || '操作失败')
          }
        } catch (error) {
          console.error('删除记录失败:', error)
          this.$message.error('操作失败，请稍后重试')
        }
      })
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

.applications-section,
.topic-detail-section,
.progress-section,
.documents-section {
  margin-bottom: 30px;
}

.applications-list {
  display: grid;
  gap: 20px;
}

.application-card {
  background: white;
  border-radius: 12px;
  border: 2px solid #f0f0f0;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.application-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: #e4e7ed;
  transition: all 0.3s ease;
}

.application-approved::before {
  background: linear-gradient(to bottom, #67C23A, #85ce61);
  animation: pulse 2s infinite;
}

.application-pending::before {
  background: linear-gradient(to bottom, #E6A23C, #f0a020);
}

.application-rejected::before {
  background: linear-gradient(to bottom, #F56C6C, #f78989);
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.application-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: #409EFF;
}

.application-header {
  margin-bottom: 20px;
}

.application-title-section {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.application-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.application-icon i {
  font-size: 24px;
  color: white;
}

.application-title-content {
  flex: 1;
}

.application-title-content h4 {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 12px 0;
}

.application-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.teacher-name {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
  font-size: 14px;
}

.application-body {
  margin-bottom: 20px;
}

.application-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.info-item i {
  font-size: 18px;
  color: #409EFF;
}

.info-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.info-value {
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
}

.application-footer {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 2px solid #f0f0f0;
}

.approval-notice {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #ecf5ff;
  border-left: 4px solid #409EFF;
  border-radius: 6px;
  margin-bottom: 16px;
  color: #606266;
  font-size: 14px;
}

.approval-notice i {
  color: #409EFF;
  font-size: 16px;
}

.application-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
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
