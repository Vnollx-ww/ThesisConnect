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
              <i class="el-icon-check"></i>
            </div>
            <div class="stat-content">
              <h3>{{ stats.activeTopics }}</h3>
              <p>进行中课题</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon">
              <i class="el-icon-success"></i>
            </div>
            <div class="stat-content">
              <h3>{{ stats.completedTopics }}</h3>
              <p>已完成课题</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon">
              <i class="el-icon-warning"></i>
            </div>
            <div class="stat-content">
              <h3>{{ stats.pausedTopics }}</h3>
              <p>已暂停课题</p>
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
          <el-button type="primary" @click="addTopic">
            <i class="el-icon-plus"></i>
            添加课题
          </el-button>
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
          v-loading="loading"
          @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="title" label="课题名称" min-width="200">
            <template slot-scope="scope">
              <el-button type="text" @click="viewTopicDetail(scope.row)">
                {{ scope.row.title }}
              </el-button>
            </template>
          </el-table-column>
          <el-table-column prop="teacherName" label="指导教师" width="120"></el-table-column>
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
              {{ scope.row.selectedCount || 0 }}/{{ scope.row.maxStudents }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.status)" size="small">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="deadline" label="截止时间" width="120">
            <template slot-scope="scope">
              {{ scope.row.deadline ? scope.row.deadline.split('T')[0] : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="viewCount" label="浏览量" width="80">
            <template slot-scope="scope">
              {{ scope.row.viewCount || 0 }}
            </template>
          </el-table-column>
          <el-table-column prop="rating" label="评分" width="100">
            <template slot-scope="scope">
              <el-rate :value="scope.row.rating || 0" disabled show-score></el-rate>
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
                  <span>{{ selectedTopic.teacherName }}</span>
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
                  <span>{{ selectedTopic.deadline ? selectedTopic.deadline.split('T')[0] : '-' }}</span>
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
              <li v-for="requirement in getRequirementsArray(selectedTopic.requirements)" :key="requirement">
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
    
    <!-- 添加课题对话框 -->
    <el-dialog
      title="添加课题"
      :visible.sync="addDialogVisible"
      width="600px">
      <el-form :model="addForm" :rules="addRules" ref="addForm" label-width="100px">
        <el-form-item label="课题名称" prop="title">
          <el-input v-model="addForm.title" placeholder="请输入课题名称"></el-input>
        </el-form-item>
        <el-form-item label="课题描述" prop="description">
          <el-input v-model="addForm.description" type="textarea" :rows="3" placeholder="请输入课题描述"></el-input>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="专业要求" prop="major">
              <el-input v-model="addForm.major" placeholder="请输入专业要求"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="难度等级" prop="difficulty">
              <el-select v-model="addForm.difficulty" placeholder="请选择难度等级">
                <el-option label="简单" value="easy"></el-option>
                <el-option label="中等" value="medium"></el-option>
                <el-option label="困难" value="hard"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="最大人数" prop="maxStudents">
          <el-input-number v-model="addForm.maxStudents" :min="1" :max="10"></el-input-number>
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker v-model="addForm.deadline" type="date" placeholder="选择截止时间" style="width: 100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="技术要求">
          <el-input v-model="addForm.requirements" type="textarea" :rows="2" placeholder="请输入技术要求"></el-input>
        </el-form-item>
        <el-form-item label="预期成果">
          <el-input v-model="addForm.expectedOutcome" type="textarea" :rows="2" placeholder="请输入预期成果"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdd">确定</el-button>
      </span>
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
            <el-form-item label="状态">
              <el-select v-model="editForm.status">
                <el-option label="进行中" value="active"></el-option>
                <el-option label="已完成" value="completed"></el-option>
                <el-option label="已暂停" value="paused"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="截止时间">
          <el-date-picker v-model="editForm.deadline" type="date" style="width: 100%"></el-date-picker>
        </el-form-item>
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
import { topicApi, userApi } from '@/api/index'

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
      addDialogVisible: false,
      selectedTopic: null,
      selectedTopics: [],
      loading: false,
      
      editForm: {
        id: null,
        title: '',
        description: '',
        major: '',
        difficulty: '',
        maxStudents: 2,
        deadline: '',
        requirements: '',
        expectedOutcome: '',
        status: 'active'
      },
      
      addForm: {
        title: '',
        description: '',
        major: '',
        difficulty: '',
        maxStudents: 2,
        deadline: '',
        requirements: '',
        expectedOutcome: ''
      },
      
      addRules: {
        title: [
          { required: true, message: '请输入课题标题', trigger: 'blur' }
        ],
        description: [
          { required: true, message: '请输入课题描述', trigger: 'blur' }
        ],
        major: [
          { required: true, message: '请输入专业要求', trigger: 'blur' }
        ],
        difficulty: [
          { required: true, message: '请选择难度等级', trigger: 'change' }
        ],
        maxStudents: [
          { required: true, message: '请输入最大学生数', trigger: 'blur' }
        ]
      },
      
      stats: {
        totalTopics: 0,
        activeTopics: 0,
        completedTopics: 0,
        pausedTopics: 0
      },
      
      teachers: [],
      topics: []
    }
  },
  computed: {
    filteredTopics() {
      return this.topics;
    }
  },
  mounted() {
    this.loadTopicStats();
    this.loadTeachers();
    this.loadTopics();
  },
  methods: {
    // 加载课题统计信息
    async loadTopicStats() {
      try {
        const response = await topicApi.getTopicStats();
        if (response.code === 200) {
          this.stats = response.data;
        }
      } catch (error) {
        console.error('加载课题统计失败:', error);
        this.$message.error('加载课题统计失败');
      }
    },

    // 加载教师列表
    async loadTeachers() {
      try {
        const response = await userApi.getUserList({ role: 'teacher', size: 1000 });
        if (response.code === 200) {
          this.teachers = response.data.records;
        }
      } catch (error) {
        console.error('加载教师列表失败:', error);
      }
    },

    // 加载课题列表
    async loadTopics() {
      this.loading = true;
      try {
        const params = {
          page: this.currentPage,
          size: this.pageSize,
          major: this.difficultyFilter || undefined,
          difficulty: this.difficultyFilter || undefined,
          status: this.statusFilter || undefined,
          keyword: this.searchKeyword || undefined
        };
        
        const response = await topicApi.getTopicList(params);
        if (response.code === 200) {
          this.topics = response.data.records;
          this.totalTopics = response.data.total;
        }
      } catch (error) {
        console.error('加载课题列表失败:', error);
        this.$message.error('加载课题列表失败');
      } finally {
        this.loading = false;
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
      this.difficultyFilter = '';
      this.statusFilter = '';
      this.teacherFilter = '';
      this.currentPage = 1;
      this.loadTopics();
    },
    
    handleSizeChange(val) {
      this.pageSize = val;
      this.currentPage = 1;
      this.loadTopics();
    },
    
    handleCurrentChange(val) {
      this.currentPage = val;
      this.loadTopics();
    },
    
    handleSelectionChange(selection) {
      this.selectedTopics = selection;
    },
    
    viewTopicDetail(topic) {
      this.selectedTopic = topic;
      this.detailDialogVisible = true;
    },
    
    editTopic(topic) {
      this.editForm = { 
        id: topic.id,
        title: topic.title,
        description: topic.description,
        major: topic.major,
        difficulty: topic.difficulty,
        maxStudents: topic.maxStudents,
        deadline: topic.deadline,
        requirements: topic.requirements,
        expectedOutcome: topic.expectedOutcome,
        status: topic.status
      };
      this.editDialogVisible = true;
    },
    
    addTopic() {
      this.addForm = {
        title: '',
        description: '',
        major: '',
        difficulty: '',
        maxStudents: 2,
        deadline: '',
        requirements: '',
        expectedOutcome: ''
      };
      this.addDialogVisible = true;
    },
    
    async submitEdit() {
      try {
        const response = await topicApi.updateTopic(this.editForm.id, this.editForm);
        if (response.code === 200) {
        this.$message.success('课题信息更新成功！');
      this.editDialogVisible = false;
          this.loadTopics();
        } else {
          this.$message.error(response.message || '课题信息更新失败');
        }
      } catch (error) {
        console.error('更新课题信息失败:', error);
        // 检查是否是后端返回的错误信息
        if (error.response && error.response.data && error.response.data.message) {
          this.$message.error(error.response.data.message);
        } else {
          this.$message.error('更新失败，请重试');
        }
      }
    },
    
    async submitAdd() {
      this.$refs.addForm.validate(async (valid) => {
        if (valid) {
          try {
            const response = await topicApi.createTopic(this.addForm);
            if (response.code === 200) {
              this.$message.success('课题添加成功！');
              this.addDialogVisible = false;
              this.loadTopics();
              this.loadTopicStats();
            } else {
              this.$message.error(response.message || '课题添加失败');
            }
          } catch (error) {
            console.error('添加课题失败:', error);
            // 检查是否是后端返回的错误信息
            if (error.response && error.response.data && error.response.data.message) {
              this.$message.error(error.response.data.message);
            } else {
              this.$message.error('添加失败，请重试');
            }
          }
        }
      });
    },
    
    viewStudents(topic) {
      this.$message.info(`查看课题"${topic.title}"的学生列表`);
    },
    
    async toggleTopicStatus(topic) {
      const action = topic.status === 'active' ? '暂停' : '启用';
      this.$confirm(`确定要${action}该课题吗？`, `确认${action}`, {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const updateData = {
            ...topic,
            status: topic.status === 'active' ? 'paused' : 'active'
          };
          const response = await topicApi.updateTopic(topic.id, updateData);
          if (response.code === 200) {
        this.$message.success(`课题"${topic.title}"已${action}`);
            this.loadTopics();
            this.loadTopicStats();
          } else {
            this.$message.error(response.message || '状态修改失败');
          }
        } catch (error) {
          console.error('修改课题状态失败:', error);
          this.$message.error('状态修改失败');
        }
      });
    },
    
    async deleteTopic(topic) {
      this.$confirm('确定要删除这个课题吗？', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await topicApi.deleteTopic(topic.id);
          if (response.code === 200) {
          this.$message.success('课题删除成功！');
            this.loadTopics();
            this.loadTopicStats();
          } else {
            this.$message.error(response.message || '课题删除失败');
          }
        } catch (error) {
          console.error('删除课题失败:', error);
          // 检查是否是后端返回的错误信息
          if (error.response && error.response.data && error.response.data.message) {
            this.$message.error(error.response.data.message);
          } else {
            this.$message.error('删除失败');
          }
        }
      });
    },
    
    async batchOperation() {
      if (this.selectedTopics.length === 0) {
        this.$message.warning('请先选择要操作的课题');
        return;
      }
      
      this.$confirm(`确定要删除选中的 ${this.selectedTopics.length} 个课题吗？`, '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const deletePromises = this.selectedTopics.map(topic => 
            topicApi.deleteTopic(topic.id)
          );
          await Promise.all(deletePromises);
          this.$message.success('批量删除课题成功');
          this.loadTopics();
          this.loadTopicStats();
        } catch (error) {
          console.error('批量删除课题失败:', error);
          this.$message.error('批量删除失败');
        }
      });
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
