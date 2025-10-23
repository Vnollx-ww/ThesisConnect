<template>
  <div class="admin-topics">
    <div class="page-header">
      <h2 class="page-title">课题管理</h2>
      <p class="page-desc">管理系统中的所有课题，监控课题发布和选择情况</p>
    </div>
    
    <!-- 课题统计概览 -->
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
              <i class="el-icon-star-on"></i>
            </div>
            <div class="stat-content">
              <h3>{{ stats.selectedTopics }}</h3>
              <p>已选课题</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon">
              <i class="el-icon-user"></i>
            </div>
            <div class="stat-content">
              <h3>{{ stats.totalSelections }}</h3>
              <p>选题总数</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon">
              <i class="el-icon-data-line"></i>
            </div>
            <div class="stat-content">
              <h3>{{ stats.avgRating }}</h3>
              <p>平均评分</p>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
    
    <!-- 筛选和搜索 -->
    <div class="filter-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索课题名称或关键词"
            prefix-icon="el-icon-search"
            @input="handleSearch">
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="difficultyFilter" placeholder="难度筛选" @change="handleFilter">
            <el-option label="全部难度" value=""></el-option>
            <el-option label="简单" value="easy"></el-option>
            <el-option label="中等" value="medium"></el-option>
            <el-option label="困难" value="hard"></el-option>
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="statusFilter" placeholder="状态筛选" @change="handleFilter">
            <el-option label="全部状态" value=""></el-option>
            <el-option label="进行中" value="active"></el-option>
            <el-option label="已完成" value="completed"></el-option>
            <el-option label="已暂停" value="paused"></el-option>
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="teacherFilter" placeholder="教师筛选" @change="handleFilter">
            <el-option label="全部教师" value=""></el-option>
            <el-option 
              v-for="teacher in teachers" 
              :key="teacher.id" 
              :label="teacher.name" 
              :value="teacher.id">
            </el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button @click="resetFilter">重置筛选</el-button>
        </el-col>
      </el-row>
    </div>
    
    <!-- 课题列表 -->
    <div class="topics-section">
      <div class="section-header">
        <h3>课题列表</h3>
        <div class="header-actions">
          <el-button @click="exportTopics">
            <i class="el-icon-download"></i>
            导出课题
          </el-button>
          <el-button type="primary" @click="batchOperation">
            <i class="el-icon-s-operation"></i>
            批量操作
          </el-button>
        </div>
      </div>
      
      <div class="topics-table">
        <el-table 
          :data="filteredTopics" 
          style="width: 100%"
          @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="title" label="课题名称" min-width="200">
            <template slot-scope="scope">
              <el-button type="text" @click="viewTopicDetail(scope.row)">
                {{ scope.row.title }}
              </el-button>
            </template>
          </el-table-column>
          <el-table-column prop="teacher" label="指导教师" width="120"></el-table-column>
          <el-table-column prop="major" label="专业要求" min-width="150"></el-table-column>
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
          <el-table-column prop="rating" label="评分" width="100">
            <template slot-scope="scope">
              <el-rate v-model="scope.row.rating" disabled show-score></el-rate>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template slot-scope="scope">
              <el-button type="text" size="small" @click="viewStudents(scope.row)">学生</el-button>
              <el-button type="text" size="small" @click="editTopic(scope.row)">编辑</el-button>
              <el-button type="text" size="small" @click="toggleTopicStatus(scope.row)">
                {{ scope.row.status === 'active' ? '暂停' : '启用' }}
              </el-button>
              <el-button type="text" size="small" @click="deleteTopic(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalTopics">
        </el-pagination>
      </div>
    </div>
    
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
          <div class="content-section">
            <h4>基本信息</h4>
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="info-item">
                  <label>指导教师：</label>
                  <span>{{ selectedTopic.teacher }}</span>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="info-item">
                  <label>专业要求：</label>
                  <span>{{ selectedTopic.major }}</span>
                </div>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="info-item">
                  <label>最大人数：</label>
                  <span>{{ selectedTopic.maxStudents }}</span>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="info-item">
                  <label>截止时间：</label>
                  <span>{{ selectedTopic.deadline }}</span>
                </div>
              </el-col>
            </el-row>
          </div>
          
          <div class="content-section">
            <h4>课题描述</h4>
            <p>{{ selectedTopic.description }}</p>
          </div>
          
          <div class="content-section">
            <h4>技术要求</h4>
            <ul>
              <li v-for="requirement in selectedTopic.requirements" :key="requirement">
                {{ requirement }}
              </li>
            </ul>
          </div>
          
          <div class="content-section">
            <h4>选择学生</h4>
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
    
    <!-- 编辑课题对话框 -->
    <el-dialog
      title="编辑课题"
      :visible.sync="editDialogVisible"
      width="600px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="课题名称">
          <el-input v-model="editForm.title"></el-input>
        </el-form-item>
        <el-form-item label="课题描述">
          <el-input v-model="editForm.description" type="textarea" :rows="3"></el-input>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="专业要求">
              <el-input v-model="editForm.major"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="难度等级">
              <el-select v-model="editForm.difficulty">
                <el-option label="简单" value="easy"></el-option>
                <el-option label="中等" value="medium"></el-option>
                <el-option label="困难" value="hard"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最大人数">
              <el-input-number v-model="editForm.maxStudents" :min="1" :max="10"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="截止时间">
              <el-date-picker v-model="editForm.deadline" type="date"></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="技术要求">
          <el-input v-model="editForm.requirements" type="textarea" :rows="2"></el-input>
        </el-form-item>
        <el-form-item label="预期成果">
          <el-input v-model="editForm.expectedOutcome" type="textarea" :rows="2"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'AdminTopics',
  data() {
    return {
      searchKeyword: '',
      difficultyFilter: '',
      statusFilter: '',
      teacherFilter: '',
      currentPage: 1,
      pageSize: 20,
      totalTopics: 0,
      detailDialogVisible: false,
      editDialogVisible: false,
      selectedTopic: null,
      selectedTopics: [],
      
      editForm: {
        title: '',
        description: '',
        major: '',
        difficulty: '',
        maxStudents: 2,
        deadline: '',
        requirements: '',
        expectedOutcome: ''
      },
      
      stats: {
        totalTopics: 89,
        selectedTopics: 67,
        totalSelections: 134,
        avgRating: 4.2
      },
      
      teachers: [
        { id: 1, name: '李教授' },
        { id: 2, name: '王老师' },
        { id: 3, name: '张教授' },
        { id: 4, name: '刘老师' }
      ],
      
      topics: [
        {
          id: 1,
          title: '基于深度学习的图像识别系统',
          description: '设计并实现一个基于深度学习的图像识别系统，能够识别多种物体和场景。',
          teacher: '李教授',
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
          teacher: '王老师',
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
          teacher: '张教授',
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
      let filtered = this.topics;
      
      // 搜索过滤
      if (this.searchKeyword) {
        filtered = filtered.filter(topic => 
          topic.title.toLowerCase().includes(this.searchKeyword.toLowerCase()) ||
          topic.description.toLowerCase().includes(this.searchKeyword.toLowerCase())
        );
      }
      
      // 难度过滤
      if (this.difficultyFilter) {
        filtered = filtered.filter(topic => topic.difficulty === this.difficultyFilter);
      }
      
      // 状态过滤
      if (this.statusFilter) {
        filtered = filtered.filter(topic => topic.status === this.statusFilter);
      }
      
      // 教师过滤
      if (this.teacherFilter) {
        const teacher = this.teachers.find(t => t.id === this.teacherFilter);
        if (teacher) {
          filtered = filtered.filter(topic => topic.teacher === teacher.name);
        }
      }
      
      this.totalTopics = filtered.length;
      
      // 分页
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return filtered.slice(start, end);
    }
  },
  methods: {
    handleSearch() {
      this.currentPage = 1;
    },
    
    handleFilter() {
      this.currentPage = 1;
    },
    
    resetFilter() {
      this.searchKeyword = '';
      this.difficultyFilter = '';
      this.statusFilter = '';
      this.teacherFilter = '';
      this.currentPage = 1;
    },
    
    handleSizeChange(val) {
      this.pageSize = val;
      this.currentPage = 1;
    },
    
    handleCurrentChange(val) {
      this.currentPage = val;
    },
    
    handleSelectionChange(selection) {
      this.selectedTopics = selection;
    },
    
    viewTopicDetail(topic) {
      this.selectedTopic = topic;
      this.detailDialogVisible = true;
    },
    
    editTopic(topic) {
      this.editForm = { ...topic };
      this.editDialogVisible = true;
    },
    
    submitEdit() {
      const index = this.topics.findIndex(topic => topic.id === this.editForm.id);
      if (index > -1) {
        this.topics.splice(index, 1, this.editForm);
        this.$message.success('课题信息更新成功！');
      }
      this.editDialogVisible = false;
    },
    
    viewStudents(topic) {
      this.$message.info(`查看课题"${topic.title}"的学生列表`);
    },
    
    toggleTopicStatus(topic) {
      const action = topic.status === 'active' ? '暂停' : '启用';
      this.$confirm(`确定要${action}该课题吗？`, `确认${action}`, {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        topic.status = topic.status === 'active' ? 'paused' : 'active';
        this.$message.success(`课题"${topic.title}"已${action}`);
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
          this.$message.success('课题删除成功！');
        }
      });
    },
    
    batchOperation() {
      if (this.selectedTopics.length === 0) {
        this.$message.warning('请先选择要操作的课题');
        return;
      }
      this.$message.info('批量操作功能开发中...');
    },
    
    exportTopics() {
      this.$message.info('课题导出功能开发中...');
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
    }
  }
}
</script>

<style scoped>
.admin-topics {
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

.header-actions {
  display: flex;
  gap: 10px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
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

.info-item {
  margin-bottom: 10px;
}

.info-item label {
  font-weight: 500;
  color: #606266;
  margin-right: 10px;
}

.info-item span {
  color: #2c3e50;
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
  
  .header-actions {
    margin-top: 10px;
  }
}
</style>
