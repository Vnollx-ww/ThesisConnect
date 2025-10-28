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
        <el-col :span="8" v-for="topic in topics" :key="topic.id">
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
                  <span>{{ topic.teacherName || topic.teacher }}</span>
                </div>
                <div class="meta-item">
                  <i class="el-icon-collection-tag"></i>
                  <span>{{ topic.major }}</span>
                </div>
                <div class="meta-item">
                  <i class="el-icon-time"></i>
                  <span>{{ formatDate(topic.deadline) }}</span>
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
                {{ topic.selectedCount >= topic.maxStudents ? '已满员' : '申请选题' }}
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
          <p><strong>指导教师：</strong>{{ selectedTopic.teacherName || selectedTopic.teacher }}</p>
          <p><strong>专业要求：</strong>{{ selectedTopic.major }}</p>
          <p><strong>难度等级：</strong>{{ getDifficultyText(selectedTopic.difficulty) }}</p>
          <p><strong>截止时间：</strong>{{ formatDate(selectedTopic.deadline) }}</p>
          <p><strong>已选人数：</strong>{{ selectedTopic.selectedCount }}/{{ selectedTopic.maxStudents }}</p>
        </div>
        <div class="detail-content">
          <h4>课题描述</h4>
          <p>{{ selectedTopic.description }}</p>
          
          <h4>技术要求</h4>
          <ul>
            <li v-for="requirement in getRequirementsArray(selectedTopic.requirements)" :key="requirement">
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
          申请此课题
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { topicApi } from '@/api'
import { getCurrentUserId } from '@/utils/user'

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
      loading: false,
      
      // 真实数据
      topics: []
    }
  },
  async mounted() {
    await this.loadTopics()
  },
  computed: {
    // 筛选逻辑已移到后端处理，不再需要前端computed筛选
  },
  methods: {
    // 加载课题列表
    async loadTopics() {
      try {
        this.loading = true
        const params = {
          page: this.currentPage,
          size: this.pageSize,
          status: 'active'
        }
        
        // 添加筛选参数
        if (this.selectedMajor) {
          const majorMap = {
            'cs': '计算机科学与技术',
            'se': '软件工程',
            'ne': '网络工程',
            'is': '信息安全'
          };
          params.major = majorMap[this.selectedMajor];
        }
        
        if (this.selectedDifficulty) {
          params.difficulty = this.selectedDifficulty;
        }
        
        if (this.searchKeyword) {
          params.keyword = this.searchKeyword;
        }
        
        const response = await topicApi.getTopicList(params)
        if (response.code === 200) {
          this.topics = response.data.records || response.data || []
          this.totalTopics = response.data.total || 0
        } else {
          this.$message.error(response.message || '获取课题列表失败')
        }
      } catch (error) {
        console.error('加载课题列表失败:', error)
        this.$message.error('加载课题列表失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    
    handleSearch() {
      this.currentPage = 1;
      this.loadTopics();
    },
    
    handleFilter() {
      this.currentPage = 1;
      this.loadTopics();
    },
    
    resetFilter() {
      this.searchKeyword = '';
      this.selectedMajor = '';
      this.selectedDifficulty = '';
      this.currentPage = 1;
      this.loadTopics();
    },
    
    handleSizeChange(val) {
      this.pageSize = val;
      this.currentPage = 1;
      this.loadTopics()
    },
    
    handleCurrentChange(val) {
      this.currentPage = val;
      this.loadTopics()
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
    
    async selectTopic() {
      if (this.selectedTopic) {
        this.$confirm('确定要申请这个课题吗？提交后需要等待老师审核。', '确认申请', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(async () => {
          try {
            // 获取当前用户ID
            const userId = await getCurrentUserId()
            if (!userId) {
              this.$message.error('请先登录')
              return
            }
            
            const selectionData = {
              topicId: this.selectedTopic.id,
              studentId: userId
            }
            
            const response = await this.$api.selectionApi.selectTopic(selectionData)
            if (response.code === 200) {
              this.detailDialogVisible = false;
              this.$message.success('申请成功！请等待老师审核。');
              // 重新加载课题列表
              await this.loadTopics()
            } else {
              this.$message.error(response.message || '申请失败')
            }
          } catch (error) {
            console.error('选题失败:', error)
            // 显示后端返回的具体错误信息
            this.$message.error(error.message || '选题失败，请稍后重试')
          }
        });
      }
    },
    
    handleClose(done) {
      this.selectedTopic = null;
      done();
    },
    
    formatDate(dateStr) {
      if (!dateStr) return '-'
      // 处理 ISO 格式 2025-10-26T16:00:00 或普通格式 2025-10-26 16:00:00
      return dateStr.split('T')[0].split(' ')[0]
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

.topics-list .el-col {
  display: flex;
  align-items: stretch;
}

.topic-card {
  width: 100%;
  height: 100%;
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
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
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.topic-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.topic-description {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  margin: 0 0 10px 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.topic-meta {
  margin-bottom: 10px;
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

.meta-item span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}

.topic-stats {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0;
  margin-top: auto;
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
  margin-top: 10px;
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
