<template>
  <div class="student-topics">
    <div class="page-header">
      <h2 class="page-title">选题列表</h2>
      <p class="page-desc">浏览所有可选的毕业论文题目，选择您感兴趣的课题</p>
    </div>
    
    <!-- 搜索和筛选 -->
    <div class="filter-section">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索课题名称或关键词"
            prefix-icon="el-icon-search"
            @input="handleSearch">
          </el-input>
        </el-col>
        <el-col :span="6">
          <el-select v-model="selectedMajor" placeholder="选择专业" @change="handleFilter">
            <el-option label="全部专业" value=""></el-option>
            <el-option label="计算机科学与技术" value="cs"></el-option>
            <el-option label="软件工程" value="se"></el-option>
            <el-option label="网络工程" value="ne"></el-option>
            <el-option label="信息安全" value="is"></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="selectedDifficulty" placeholder="选择难度" @change="handleFilter">
            <el-option label="全部难度" value=""></el-option>
            <el-option label="简单" value="easy"></el-option>
            <el-option label="中等" value="medium"></el-option>
            <el-option label="困难" value="hard"></el-option>
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="resetFilter">重置筛选</el-button>
        </el-col>
      </el-row>
    </div>
    
    <!-- 选题列表 -->
    <div class="topics-list">
      <el-row :gutter="20">
        <el-col :span="8" v-for="topic in filteredTopics" :key="topic.id">
          <div class="topic-card">
            <div class="topic-header">
              <h3 class="topic-title">{{ topic.title }}</h3>
              <el-tag 
                :type="getDifficultyType(topic.difficulty)" 
                size="small">
                {{ getDifficultyText(topic.difficulty) }}
              </el-tag>
            </div>
            
            <div class="topic-content">
              <p class="topic-description">{{ topic.description }}</p>
              
              <div class="topic-meta">
                <div class="meta-item">
                  <i class="el-icon-user"></i>
                  <span>{{ topic.teacher }}</span>
                </div>
                <div class="meta-item">
                  <i class="el-icon-collection-tag"></i>
                  <span>{{ topic.major }}</span>
                </div>
                <div class="meta-item">
                  <i class="el-icon-time"></i>
                  <span>{{ topic.deadline }}</span>
                </div>
              </div>
              
              <div class="topic-stats">
                <span class="stat-item">
                  <i class="el-icon-star-on"></i>
                  {{ topic.selectedCount }}/{{ topic.maxStudents }} 人
                </span>
                <span class="stat-item">
                  <i class="el-icon-view"></i>
                  {{ topic.viewCount }} 次浏览
                </span>
              </div>
            </div>
            
            <div class="topic-actions">
              <el-button 
                type="primary" 
                size="small" 
                @click="viewTopicDetail(topic)"
                :disabled="topic.selectedCount >= topic.maxStudents">
                {{ topic.selectedCount >= topic.maxStudents ? '已满员' : '选择课题' }}
              </el-button>
              <el-button 
                size="small" 
                @click="viewTopicDetail(topic)">
                查看详情
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
    
    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[6, 12, 18, 24]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalTopics">
      </el-pagination>
    </div>
    
    <!-- 课题详情对话框 -->
    <el-dialog
      title="课题详情"
      :visible.sync="detailDialogVisible"
      width="600px"
      :before-close="handleClose">
      <div v-if="selectedTopic" class="topic-detail">
        <h3>{{ selectedTopic.title }}</h3>
        <div class="detail-meta">
          <p><strong>指导教师：</strong>{{ selectedTopic.teacher }}</p>
          <p><strong>专业要求：</strong>{{ selectedTopic.major }}</p>
          <p><strong>难度等级：</strong>{{ getDifficultyText(selectedTopic.difficulty) }}</p>
          <p><strong>截止时间：</strong>{{ selectedTopic.deadline }}</p>
          <p><strong>已选人数：</strong>{{ selectedTopic.selectedCount }}/{{ selectedTopic.maxStudents }}</p>
        </div>
        <div class="detail-content">
          <h4>课题描述</h4>
          <p>{{ selectedTopic.description }}</p>
          
          <h4>技术要求</h4>
          <ul>
            <li v-for="requirement in selectedTopic.requirements" :key="requirement">
              {{ requirement }}
            </li>
          </ul>
          
          <h4>预期成果</h4>
          <p>{{ selectedTopic.expectedOutcome }}</p>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="selectTopic"
          :disabled="selectedTopic && selectedTopic.selectedCount >= selectedTopic.maxStudents">
          选择此课题
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'StudentTopics',
  data() {
    return {
      searchKeyword: '',
      selectedMajor: '',
      selectedDifficulty: '',
      currentPage: 1,
      pageSize: 6,
      totalTopics: 0,
      detailDialogVisible: false,
      selectedTopic: null,
      
      // 模拟数据
      topics: [
        {
          id: 1,
          title: '基于深度学习的图像识别系统',
          description: '设计并实现一个基于深度学习的图像识别系统，能够识别多种物体和场景。',
          teacher: '李教授',
          major: '计算机科学与技术',
          difficulty: 'hard',
          deadline: '2024-03-15',
          selectedCount: 2,
          maxStudents: 3,
          viewCount: 156,
          requirements: ['熟悉Python编程', '了解深度学习框架', '有图像处理基础'],
          expectedOutcome: '完成一个可用的图像识别系统，并撰写相关技术文档。'
        },
        {
          id: 2,
          title: '校园二手交易平台设计与实现',
          description: '开发一个校园内的二手物品交易平台，支持用户发布、搜索、交易等功能。',
          teacher: '王老师',
          major: '软件工程',
          difficulty: 'medium',
          deadline: '2024-03-20',
          selectedCount: 1,
          maxStudents: 2,
          viewCount: 89,
          requirements: ['熟悉Web开发', '了解数据库设计', '有前端开发经验'],
          expectedOutcome: '完成一个功能完整的Web应用系统。'
        },
        {
          id: 3,
          title: '智能家居控制系统',
          description: '设计一个智能家居控制系统，能够远程控制家中的各种设备。',
          teacher: '张教授',
          major: '网络工程',
          difficulty: 'medium',
          deadline: '2024-03-25',
          selectedCount: 0,
          maxStudents: 2,
          viewCount: 67,
          requirements: ['熟悉物联网技术', '了解嵌入式开发', '有硬件基础'],
          expectedOutcome: '完成一个可演示的智能家居控制系统原型。'
        },
        {
          id: 4,
          title: '基于区块链的数据安全存储系统',
          description: '利用区块链技术设计一个安全的数据存储系统，确保数据的完整性和不可篡改性。',
          teacher: '刘老师',
          major: '信息安全',
          difficulty: 'hard',
          deadline: '2024-03-30',
          selectedCount: 1,
          maxStudents: 2,
          viewCount: 134,
          requirements: ['了解区块链原理', '熟悉密码学', '有分布式系统基础'],
          expectedOutcome: '完成一个基于区块链的数据存储系统原型。'
        },
        {
          id: 5,
          title: '学生成绩管理系统',
          description: '开发一个学生成绩管理系统，支持成绩录入、查询、统计等功能。',
          teacher: '陈老师',
          major: '软件工程',
          difficulty: 'easy',
          deadline: '2024-04-01',
          selectedCount: 3,
          maxStudents: 4,
          viewCount: 45,
          requirements: ['熟悉数据库操作', '了解Web开发', '有系统设计能力'],
          expectedOutcome: '完成一个功能完整的管理系统。'
        },
        {
          id: 6,
          title: '移动端健康管理应用',
          description: '开发一个移动端健康管理应用，帮助用户记录和管理健康数据。',
          teacher: '赵教授',
          major: '计算机科学与技术',
          difficulty: 'medium',
          deadline: '2024-04-05',
          selectedCount: 2,
          maxStudents: 3,
          viewCount: 78,
          requirements: ['熟悉移动开发', '了解健康数据管理', '有UI设计能力'],
          expectedOutcome: '完成一个可用的移动应用。'
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
      
      // 专业过滤
      if (this.selectedMajor) {
        const majorMap = {
          'cs': '计算机科学与技术',
          'se': '软件工程',
          'ne': '网络工程',
          'is': '信息安全'
        };
        filtered = filtered.filter(topic => topic.major === majorMap[this.selectedMajor]);
      }
      
      // 难度过滤
      if (this.selectedDifficulty) {
        filtered = filtered.filter(topic => topic.difficulty === this.selectedDifficulty);
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
      this.selectedMajor = '';
      this.selectedDifficulty = '';
      this.currentPage = 1;
    },
    
    handleSizeChange(val) {
      this.pageSize = val;
      this.currentPage = 1;
    },
    
    handleCurrentChange(val) {
      this.currentPage = val;
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
    
    viewTopicDetail(topic) {
      this.selectedTopic = topic;
      this.detailDialogVisible = true;
    },
    
    selectTopic() {
      if (this.selectedTopic) {
        this.$confirm('确定要选择这个课题吗？', '确认选择', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          // 更新选题状态
          this.selectedTopic.selectedCount++;
          this.detailDialogVisible = false;
          this.$message.success('选题成功！');
        });
      }
    },
    
    handleClose(done) {
      this.selectedTopic = null;
      done();
    }
  }
}
</script>

<style scoped>
.student-topics {
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

.filter-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.topics-list {
  margin-bottom: 30px;
}

.topic-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.topic-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.topic-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.topic-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
  flex: 1;
  margin-right: 10px;
}

.topic-content {
  flex: 1;
}

.topic-description {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  margin: 0 0 15px 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.topic-meta {
  margin-bottom: 15px;
}

.meta-item {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
  font-size: 13px;
  color: #909399;
}

.meta-item i {
  margin-right: 5px;
  width: 14px;
}

.topic-stats {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  font-size: 13px;
  color: #909399;
}

.stat-item {
  display: flex;
  align-items: center;
}

.stat-item i {
  margin-right: 3px;
}

.topic-actions {
  display: flex;
  gap: 10px;
}

.topic-actions .el-button {
  flex: 1;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.topic-detail h3 {
  color: #2c3e50;
  margin: 0 0 15px 0;
}

.detail-meta {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 6px;
  margin-bottom: 20px;
}

.detail-meta p {
  margin: 5px 0;
  font-size: 14px;
}

.detail-content h4 {
  color: #2c3e50;
  margin: 15px 0 8px 0;
  font-size: 16px;
}

.detail-content p {
  color: #606266;
  line-height: 1.6;
  margin: 0 0 10px 0;
}

.detail-content ul {
  margin: 0;
  padding-left: 20px;
}

.detail-content li {
  color: #606266;
  margin-bottom: 5px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .filter-section .el-row {
    flex-direction: column;
  }
  
  .filter-section .el-col {
    margin-bottom: 10px;
  }
  
  .topics-list .el-row {
    flex-direction: column;
  }
  
  .topic-actions {
    flex-direction: column;
  }
}
</style>
