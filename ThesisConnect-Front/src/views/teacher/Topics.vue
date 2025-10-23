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
        <el-table :data="filteredTopics" style="width: 100%">
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
          <el-table-column prop="deadline" label="截止时间" width="120"></el-table-column>
          <el-table-column prop="viewCount" label="浏览量" width="80"></el-table-column>
          <el-table-column prop="rating" label="评分" width="80">
            <template slot-scope="scope">
              <el-rate v-model="scope.row.rating" disabled show-score></el-rate>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template slot-scope="scope">
              <el-button type="text" size="small" @click="editTopic(scope.row)">编辑</el-button>
              <el-button type="text" size="small" @click="viewStudents(scope.row)">学生</el-button>
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
      width="600px"
      :before-close="handleClose">
      <el-form :model="topicForm" :rules="topicRules" ref="topicForm" label-width="100px">
        <el-form-item label="课题名称" prop="title">
          <el-input v-model="topicForm.title" placeholder="请输入课题名称"></el-input>
        </el-form-item>
        
        <el-form-item label="课题描述" prop="description">
          <el-input 
            v-model="topicForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请详细描述课题内容和要求">
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
        <el-button type="primary" @click="submitTopic">确定</el-button>
      </span>
    </el-dialog>
    
    <!-- 课题详情对话框 -->
    <el-dialog
      title="课题详情"
      :visible.sync="detailDialogVisible"
      width="700px">
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
          <div class="content-section">
            <h4>课题描述</h4>
            <p>{{ selectedTopic.description }}</p>
          </div>
          
          <div class="content-section">
            <h4>技术要求</h4>
            <ul>
              <li v-for="requirement in getRequirementsArray(selectedTopic.requirements)" :key="requirement">
                {{ requirement }}
              </li>
            </ul>
          </div>
          
          <div class="content-section">
            <h4>预期成果</h4>
            <p>{{ selectedTopic.expectedOutcome }}</p>
          </div>
          
          <div class="content-section">
            <h4>学生信息</h4>
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
            </el-table>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'TeacherTopics',
  data() {
    return {
      statusFilter: '',
      topicDialogVisible: false,
      detailDialogVisible: false,
      dialogTitle: '发布新课题',
      selectedTopic: null,
      
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
        totalTopics: 8,
        totalStudents: 12,
        avgRating: 4.5,
        totalViews: 456
      },
      
      topics: [
        {
          id: 1,
          title: '基于深度学习的图像识别系统',
          description: '设计并实现一个基于深度学习的图像识别系统，能够识别多种物体和场景。',
          major: '计算机科学与技术',
          difficulty: 'hard',
          maxStudents: 3,
          selectedCount: 2,
          status: 'active',
          deadline: '2024-03-15',
          viewCount: 156,
          rating: 4.5,
          requirements: ['熟悉Python编程', '了解深度学习框架', '有图像处理基础'],
          expectedOutcome: '完成一个可用的图像识别系统，并撰写相关技术文档。',
          students: [
            { name: '张三', studentId: '2021001001', phone: '13800138000', email: 'zhangsan@example.com', progress: 35 },
            { name: '李四', studentId: '2021001002', phone: '13800138001', email: 'lisi@example.com', progress: 20 }
          ]
        },
        {
          id: 2,
          title: '校园二手交易平台设计与实现',
          description: '开发一个校园内的二手物品交易平台，支持用户发布、搜索、交易等功能。',
          major: '软件工程',
          difficulty: 'medium',
          maxStudents: 2,
          selectedCount: 1,
          status: 'active',
          deadline: '2024-03-20',
          viewCount: 89,
          rating: 4.2,
          requirements: ['熟悉Web开发', '了解数据库设计', '有前端开发经验'],
          expectedOutcome: '完成一个功能完整的Web应用系统。',
          students: [
            { name: '王五', studentId: '2021001003', phone: '13800138002', email: 'wangwu@example.com', progress: 15 }
          ]
        },
        {
          id: 3,
          title: '智能家居控制系统',
          description: '设计一个智能家居控制系统，能够远程控制家中的各种设备。',
          major: '网络工程',
          difficulty: 'medium',
          maxStudents: 2,
          selectedCount: 0,
          status: 'active',
          deadline: '2024-03-25',
          viewCount: 67,
          rating: 0,
          requirements: ['熟悉物联网技术', '了解嵌入式开发', '有硬件基础'],
          expectedOutcome: '完成一个可演示的智能家居控制系统原型。',
          students: []
        }
      ]
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
  methods: {
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
    
    submitTopic() {
      this.$refs.topicForm.validate((valid) => {
        if (valid) {
          if (this.dialogTitle === '发布新课题') {
            // 添加新课题
            const newTopic = {
              ...this.topicForm,
              id: Date.now(),
              selectedCount: 0,
              status: 'active',
              viewCount: 0,
              rating: 0,
              students: []
            };
            this.topics.unshift(newTopic);
            this.stats.totalTopics++;
            this.$message.success('课题发布成功！');
          } else {
            // 编辑课题
            const index = this.topics.findIndex(topic => topic.id === this.topicForm.id);
            if (index > -1) {
              this.topics.splice(index, 1, this.topicForm);
              this.$message.success('课题更新成功！');
            }
          }
          this.topicDialogVisible = false;
        }
      });
    },
    
    deleteTopic(topic) {
      this.$confirm('确定要删除这个课题吗？', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const index = this.topics.indexOf(topic);
        if (index > -1) {
          this.topics.splice(index, 1);
          this.stats.totalTopics--;
          this.stats.totalStudents -= topic.selectedCount;
          this.$message.success('课题删除成功！');
        }
      });
    },
    
    viewTopicDetail(topic) {
      this.selectedTopic = topic;
      this.detailDialogVisible = true;
    },
    
    viewStudents(topic) {
      this.$message.info(`查看课题"${topic.title}"的学生列表`);
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
    
    handleClose(done) {
      this.$refs.topicForm.resetFields();
      done();
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
  margin-top: 10px;
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

.topic-detail {
  max-height: 500px;
  overflow-y: auto;
}

.detail-header {
  margin-bottom: 20px;
}

.detail-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 10px 0;
}

.detail-meta {
  display: flex;
  gap: 10px;
}

.content-section {
  margin-bottom: 20px;
}

.content-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 10px 0;
}

.content-section p {
  color: #606266;
  line-height: 1.6;
  margin: 0;
}

.content-section ul {
  margin: 0;
  padding-left: 20px;
}

.content-section li {
  color: #606266;
  margin-bottom: 5px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .add-topic-btn {
    margin-top: 15px;
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
</style>
