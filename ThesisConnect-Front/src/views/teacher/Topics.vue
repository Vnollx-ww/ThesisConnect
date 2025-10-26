<template>
  <div class="teacher-topics">
    <div class="page-header">
      <div class="page-header-content">
      <h2 class="page-title">课题管理</h2>
      <p class="page-desc">管理您发布的毕业论文课题，查看学生选择情况</p>
      </div>
      <el-button type="primary" @click="addTopic" class="add-topic-btn">
        <i class="el-icon-plus"></i>
        发布新课题
      </el-button>
    </div>
    
    <!-- 统计概览 -->
    <div class="stats-overview">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon">
              <i class="el-icon-document"></i>
            </div>
            <div class="stat-content">
              <h3>{{ stats.totalTopics }}</h3>
              <p>总课题数</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon">
              <i class="el-icon-user"></i>
            </div>
            <div class="stat-content">
              <h3>{{ stats.totalStudents }}</h3>
              <p>已选学生</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon">
              <i class="el-icon-star-on"></i>
            </div>
            <div class="stat-content">
              <h3>{{ stats.avgRating }}</h3>
              <p>平均评分</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon">
              <i class="el-icon-view"></i>
            </div>
            <div class="stat-content">
              <h3>{{ stats.totalViews }}</h3>
              <p>总浏览量</p>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
    
    <!-- 课题列表 -->
    <div class="topics-section">
      <div class="section-header">
        <h3>我的课题</h3>
        <div class="filter-controls">
          <el-select v-model="statusFilter" placeholder="状态筛选" @change="handleFilter">
            <el-option label="全部状态" value=""></el-option>
            <el-option label="进行中" value="active"></el-option>
            <el-option label="已完成" value="completed"></el-option>
            <el-option label="已暂停" value="paused"></el-option>
          </el-select>
        </div>
      </div>
      
      <div class="topics-table">
        <el-table :data="filteredTopics" style="width: 100%" v-loading="loading">
          <el-table-column prop="title" label="课题名称" width="200">
            <template slot-scope="scope">
              <el-button type="text" @click="viewTopicDetail(scope.row)">
                {{ scope.row.title }}
              </el-button>
            </template>
          </el-table-column>
          <el-table-column prop="difficulty" label="难度" width="100">
            <template slot-scope="scope">
              <el-tag :type="getDifficultyType(scope.row.difficulty)" size="small">
                {{ getDifficultyText(scope.row.difficulty) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="selectedCount" label="已选人数" width="100">
            <template slot-scope="scope">
              {{ scope.row.selectedCount }}/{{ scope.row.maxStudents }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.status)" size="small">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="deadline" label="截止时间" width="180">
            <template slot-scope="scope">
              {{ formatDate(scope.row.deadline) }}
            </template>
          </el-table-column>
          <el-table-column prop="viewCount" label="浏览量" width="100"></el-table-column>
          <el-table-column prop="rating" label="评分" width="100"></el-table-column>
          <el-table-column label="操作" width="180">
            <template slot-scope="scope">
              <el-button type="text" size="small" @click="viewTopicDetail(scope.row)">详情</el-button>
              <el-button type="text" size="small" @click="editTopic(scope.row)">编辑</el-button>
              <el-button type="text" size="small" @click="toggleTopicStatus(scope.row)">
                {{ scope.row.status === 'active' ? '暂停' : '启用' }}
              </el-button>
              <el-button type="text" size="small" @click="deleteTopic(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    
    <!-- 发布/编辑课题对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="topicDialogVisible"
      width="600px">
      <el-form :model="topicForm" :rules="topicRules" ref="topicForm" label-width="100px">
        <el-form-item label="课题名称" prop="title">
          <el-input v-model="topicForm.title" placeholder="请输入课题名称"></el-input>
        </el-form-item>
        
        <el-form-item label="课题描述" prop="description">
          <el-input 
            v-model="topicForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请详细描述课题内容">
          </el-input>
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="专业要求" prop="major">
              <el-select v-model="topicForm.major" placeholder="选择专业">
                <el-option label="计算机科学与技术" value="计算机科学与技术"></el-option>
                <el-option label="软件工程" value="软件工程"></el-option>
                <el-option label="网络工程" value="网络工程"></el-option>
                <el-option label="信息安全" value="信息安全"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="难度等级" prop="difficulty">
              <el-select v-model="topicForm.difficulty" placeholder="选择难度">
                <el-option label="简单" value="easy"></el-option>
                <el-option label="中等" value="medium"></el-option>
                <el-option label="困难" value="hard"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最大人数" prop="maxStudents">
              <el-input-number v-model="topicForm.maxStudents" :min="1" :max="5"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        
            <el-form-item label="截止时间" prop="deadline">
              <el-date-picker
                v-model="topicForm.deadline"
                type="date"
            placeholder="选择截止时间"
            style="width: 100%;">
              </el-date-picker>
            </el-form-item>
        
        <el-form-item label="技术要求">
          <el-input 
            v-model="topicForm.requirements" 
            type="textarea" 
            :rows="2"
            placeholder="请输入技术要求，每行一个">
          </el-input>
        </el-form-item>
        
        <el-form-item label="预期成果">
          <el-input 
            v-model="topicForm.expectedOutcome" 
            type="textarea" 
            :rows="2"
            placeholder="请描述预期成果">
          </el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="topicDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTopic" :loading="loading">确定</el-button>
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
                  <span>{{ formatDate(selectedTopic.deadline) }}</span>
                </div>
                <div class="info-item">
                  <label>浏览量:</label>
                  <span>{{ selectedTopic.viewCount }}</span>
                </div>
                <div class="info-item">
                  <label>评分:</label>
                  <span>{{ selectedTopic.rating || '暂无评分' }}</span>
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
          
            <el-tab-pane label="选择学生" name="students">
              <div class="students-list">
                <div v-if="selectedTopic.students && selectedTopic.students.length > 0">
            <el-table :data="selectedTopic.students" style="width: 100%">
              <el-table-column prop="name" label="姓名" width="100"></el-table-column>
              <el-table-column prop="studentId" label="学号" width="120"></el-table-column>
              <el-table-column prop="phone" label="联系电话" width="120"></el-table-column>
              <el-table-column prop="email" label="邮箱" width="150"></el-table-column>
              <el-table-column prop="progress" label="进度" width="100">
                <template slot-scope="scope">
                  <el-progress :percentage="scope.row.progress" :stroke-width="6"></el-progress>
                </template>
              </el-table-column>
                    <el-table-column label="操作" width="120">
                      <template slot-scope="scope">
                        <el-button type="text" size="small" @click="viewStudentProgress(scope.row)">查看进度</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
                <div v-else class="no-students">
                  <p>暂无学生选择此课题</p>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { topicApi } from '@/api'

export default {
  name: 'TeacherTopics',
  data() {
    return {
      loading: false,
      statusFilter: '',
      topicDialogVisible: false,
      detailDialogVisible: false,
      dialogTitle: '发布新课题',
      selectedTopic: null,
      activeTab: 'basic',
      
      topicForm: {
        title: '',
        description: '',
        major: '',
        difficulty: '',
        maxStudents: 2,
        deadline: '',
        requirements: '',
        expectedOutcome: ''
      },
      
      topicRules: {
        title: [
          { required: true, message: '请输入课题名称', trigger: 'blur' }
        ],
        description: [
          { required: true, message: '请输入课题描述', trigger: 'blur' }
        ],
        major: [
          { required: true, message: '请选择专业', trigger: 'change' }
        ],
        difficulty: [
          { required: true, message: '请选择难度', trigger: 'change' }
        ],
        maxStudents: [
          { required: true, message: '请设置最大人数', trigger: 'blur' }
        ],
        deadline: [
          { required: true, message: '请选择截止时间', trigger: 'change' }
        ]
      },
      
      stats: {
        totalTopics: 0,
        totalStudents: 0,
        avgRating: 0,
        totalViews: 0
      },
      
      topics: []
    }
  },
  computed: {
    filteredTopics() {
      if (this.statusFilter) {
        return this.topics.filter(topic => topic.status === this.statusFilter);
      }
      return this.topics;
    }
  },
  async mounted() {
    await this.loadTopics();
    await this.loadStats();
    
    // 检查是否有来自学生管理页面的课题详情查看请求
    this.checkTopicDetailRequest();
  },
  methods: {
    // 加载课题数据
    async loadTopics() {
      try {
        this.loading = true;
        const teacherId = this.getCurrentTeacherId();
        if (!teacherId) {
          return;
        }
        const response = await topicApi.getTopicsByTeacher(teacherId);
        if (response.code === 200) {
          this.topics = response.data || [];
        } else {
          this.$message.error(response.message || '获取课题列表失败');
        }
      } catch (error) {
        console.error('加载课题列表失败:', error);
        this.$message.error('加载课题列表失败，请稍后重试');
      } finally {
        this.loading = false;
      }
    },
    
    // 加载统计数据
    async loadStats() {
      try {
        const teacherId = this.getCurrentTeacherId();
        if (!teacherId) {
          return;
        }
        const response = await topicApi.getTopicStatsByTeacher(teacherId);
        if (response.code === 200) {
          this.stats = response.data;
        }
      } catch (error) {
        console.error('加载统计数据失败:', error);
      }
    },
    
    // 检查课题详情查看请求
    checkTopicDetailRequest() {
      const highlightTopic = this.$route.query.highlightTopic;
      const topicTitle = this.$route.query.topicTitle;
      
      if (highlightTopic && topicTitle) {
        // 查找对应的课题
        const topic = this.topics.find(t => t.id == highlightTopic);
        if (topic) {
          // 延迟执行，确保数据已加载
          this.$nextTick(() => {
            this.viewTopicDetail(topic);
            // 清除URL参数
            this.$router.replace({ path: '/teacher/topics' });
          });
        } else {
          this.$message.warning(`未找到课题"${topicTitle}"`);
        }
      }
    },
    
    // 获取当前教师ID
    getCurrentTeacherId() {
      const userInfo = JSON.parse(localStorage.getItem('user') || '{}');
      if (!userInfo || !userInfo.id) {
        this.$message.error('用户信息获取失败，请重新登录');
        this.$router.push('/login');
        return null;
      }
      return userInfo.id;
    },
    
    addTopic() {
      this.dialogTitle = '发布新课题';
      this.topicForm = {
        title: '',
        description: '',
        major: '',
        difficulty: '',
        maxStudents: 2,
        deadline: '',
        requirements: '',
        expectedOutcome: ''
      };
      this.topicDialogVisible = true;
    },
    
    editTopic(topic) {
      this.dialogTitle = '编辑课题';
      this.topicForm = { ...topic };
      this.topicDialogVisible = true;
    },
    
    async submitTopic() {
      this.$refs.topicForm.validate(async (valid) => {
        if (valid) {
          try {
            this.loading = true;
            let response;
            
          if (this.dialogTitle === '发布新课题') {
              // 发布新课题
              response = await topicApi.createTopic(this.topicForm);
            } else {
              // 更新课题
              response = await topicApi.updateTopic(this.topicForm.id, this.topicForm);
            }
            
            if (response.code === 200) {
              this.topicDialogVisible = false;
              this.$message.success(this.dialogTitle === '发布新课题' ? '课题发布成功！' : '课题更新成功！');
              // 重新加载数据
              await this.loadTopics();
              await this.loadStats();
          } else {
              this.$message.error(response.message || '操作失败');
            }
          } catch (error) {
            console.error('提交课题失败:', error);
            this.$message.error('操作失败，请稍后重试');
          } finally {
            this.loading = false;
          }
        }
      });
    },
    
    async deleteTopic(topic) {
      try {
        await this.$confirm('确定要删除这个课题吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
        });
        
        const response = await topicApi.deleteTopic(topic.id);
        if (response.code === 200) {
          this.$message.success('删除成功！');
          await this.loadTopics();
          await this.loadStats();
        } else {
          this.$message.error(response.message || '删除失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除课题失败:', error);
          this.$message.error('删除失败，请稍后重试');
        }
      }
    },
    
    async toggleTopicStatus(topic) {
      try {
        const newStatus = topic.status === 'active' ? 'paused' : 'active';
        const action = newStatus === 'active' ? '启用' : '暂停';
        
        await this.$confirm(`确定要${action}这个课题吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        
        const response = await topicApi.updateTopic(topic.id, { ...topic, status: newStatus });
        if (response.code === 200) {
          this.$message.success(`${action}成功！`);
          await this.loadTopics();
          await this.loadStats();
        } else {
          this.$message.error(response.message || `${action}失败`);
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('切换课题状态失败:', error);
          this.$message.error('操作失败，请稍后重试');
        }
      }
    },
    
    viewTopicDetail(topic) {
      this.selectedTopic = topic;
      this.activeTab = 'basic';
      this.detailDialogVisible = true;
    },
    
    getRequirementsArray(requirements) {
      if (!requirements) return [];
      if (typeof requirements === 'string') {
        return requirements.split('\n').filter(req => req.trim());
      }
      return requirements;
    },
    
    viewStudentProgress(student) {
      this.$message.info(`查看学生 ${student.name} 的进度详情功能开发中...`);
    },
    
    handleFilter() {
      // 筛选逻辑已在computed中处理
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
    
    formatDate(dateStr) {
      if (!dateStr) return '-';
      // 处理 ISO 格式 2025-10-23T10:49:58 或普通格式 2025-10-23 10:49:58
      return dateStr.split('T')[0].split(' ')[0];
    }
  }
}
</script>

<style scoped>
.teacher-topics {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
}

.page-header-content {
  flex: 1;
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

.add-topic-btn {
  margin-left: 20px;
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

.topics-section {
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

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .add-topic-btn {
    margin-top: 15px;
    margin-left: 0;
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
  
  .filter-controls {
    margin-top: 10px;
  }
}

/* 隐藏表格水平滚动条 */
.topics-table {
  overflow-x: hidden;
}

.topics-table .el-table {
  overflow-x: hidden;
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

.students-list {
  max-height: 400px;
  overflow-y: auto;
}

.no-students {
  text-align: center;
  padding: 40px;
  color: #999;
}

.no-students p {
  margin: 0;
  font-size: 14px;
}
</style>