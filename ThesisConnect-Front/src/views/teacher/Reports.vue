<template>
  <div class="teacher-reports">
    <div class="page-header">
      <h2 class="page-title">统计报表</h2>
      <p class="page-desc">查看课题选择情况和学生学习统计</p>
    </div>
    
    <!-- 时间筛选 -->
    <div class="time-filter">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="handleDateChange">
          </el-date-picker>
        </el-col>
        <el-col :span="6">
          <el-select v-model="selectedTopic" placeholder="选择课题" @change="handleTopicChange">
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
          <el-button type="primary" @click="refreshData">刷新数据</el-button>
        </el-col>
        <el-col :span="6">
          <el-button @click="exportReport">
            <i class="el-icon-download"></i>
            导出报表
          </el-button>
        </el-col>
      </el-row>
    </div>
    
    <!-- 关键指标 -->
    <div class="key-metrics">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="metric-card">
            <div class="metric-icon">
              <i class="el-icon-document"></i>
            </div>
            <div class="metric-content">
              <h3>{{ metrics.totalTopics }}</h3>
              <p>发布课题数</p>
              <span class="metric-change positive">+12%</span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="metric-card">
            <div class="metric-icon">
              <i class="el-icon-user"></i>
            </div>
            <div class="metric-content">
              <h3>{{ metrics.totalStudents }}</h3>
              <p>选择学生数</p>
              <span class="metric-change positive">+8%</span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="metric-card">
            <div class="metric-icon">
              <i class="el-icon-star-on"></i>
            </div>
            <div class="metric-content">
              <h3>{{ metrics.avgRating }}</h3>
              <p>平均评分</p>
              <span class="metric-change positive">+0.3</span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="metric-card">
            <div class="metric-icon">
              <i class="el-icon-data-line"></i>
            </div>
            <div class="metric-content">
              <h3>{{ metrics.completionRate }}%</h3>
              <p>完成率</p>
              <span class="metric-change positive">+5%</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
    
    <!-- 图表区域 -->
    <el-row :gutter="20">
      <!-- 课题选择趋势 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <h3>课题选择趋势</h3>
            <el-button-group>
              <el-button size="small" :type="trendPeriod === 'week' ? 'primary' : ''" @click="setTrendPeriod('week')">周</el-button>
              <el-button size="small" :type="trendPeriod === 'month' ? 'primary' : ''" @click="setTrendPeriod('month')">月</el-button>
              <el-button size="small" :type="trendPeriod === 'year' ? 'primary' : ''" @click="setTrendPeriod('year')">年</el-button>
            </el-button-group>
          </div>
          <div class="chart-content">
            <div id="trendChart" style="height: 300px;"></div>
          </div>
        </div>
      </el-col>
      
      <!-- 学生进度分布 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <h3>学生进度分布</h3>
          </div>
          <div class="chart-content">
            <div id="progressChart" style="height: 300px;"></div>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 详细统计表格 -->
    <div class="detailed-stats">
      <div class="section-header">
        <h3>详细统计</h3>
        <el-button type="primary" size="small" @click="exportDetailedData">导出详细数据</el-button>
      </div>
      
      <el-tabs v-model="activeTab" type="card">
        <!-- 课题统计 -->
        <el-tab-pane label="课题统计" name="topics">
          <el-table :data="topicStats" style="width: 100%">
            <el-table-column prop="title" label="课题名称" width="200"></el-table-column>
            <el-table-column prop="difficulty" label="难度" width="100">
              <template slot-scope="scope">
                <el-tag :type="getDifficultyType(scope.row.difficulty)" size="small">
                  {{ getDifficultyText(scope.row.difficulty) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="selectedCount" label="选择人数" width="100"></el-table-column>
            <el-table-column prop="maxStudents" label="最大人数" width="100"></el-table-column>
            <el-table-column prop="completionRate" label="完成率" width="100">
              <template slot-scope="scope">
                {{ scope.row.completionRate }}%
              </template>
            </el-table-column>
            <el-table-column prop="avgProgress" label="平均进度" width="100">
              <template slot-scope="scope">
                {{ scope.row.avgProgress }}%
              </template>
            </el-table-column>
            <el-table-column prop="avgRating" label="平均评分" width="100">
              <template slot-scope="scope">
                <el-rate v-model="scope.row.avgRating" disabled show-score></el-rate>
              </template>
            </el-table-column>
            <el-table-column prop="viewCount" label="浏览量" width="100"></el-table-column>
          </el-table>
        </el-tab-pane>
        
        <!-- 学生统计 -->
        <el-tab-pane label="学生统计" name="students">
          <el-table :data="studentStats" style="width: 100%">
            <el-table-column prop="name" label="学生姓名" width="100"></el-table-column>
            <el-table-column prop="studentId" label="学号" width="120"></el-table-column>
            <el-table-column prop="topicTitle" label="课题" width="200"></el-table-column>
            <el-table-column prop="progress" label="进度" width="100">
              <template slot-scope="scope">
                {{ scope.row.progress }}%
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.status)" size="small">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="selectedTime" label="选择时间" width="120"></el-table-column>
            <el-table-column prop="lastUpdate" label="最后更新" width="120"></el-table-column>
            <el-table-column prop="documentsCount" label="文档数" width="100"></el-table-column>
          </el-table>
        </el-tab-pane>
        
        <!-- 时间统计 -->
        <el-tab-pane label="时间统计" name="time">
          <el-table :data="timeStats" style="width: 100%">
            <el-table-column prop="date" label="日期" width="120"></el-table-column>
            <el-table-column prop="newTopics" label="新增课题" width="100"></el-table-column>
            <el-table-column prop="newSelections" label="新增选择" width="100"></el-table-column>
            <el-table-column prop="progressUpdates" label="进度更新" width="100"></el-table-column>
            <el-table-column prop="documentUploads" label="文档上传" width="100"></el-table-column>
            <el-table-column prop="totalViews" label="总浏览量" width="100"></el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TeacherReports',
  data() {
    return {
      dateRange: [],
      selectedTopic: '',
      trendPeriod: 'month',
      activeTab: 'topics',
      
      metrics: {
        totalTopics: 8,
        totalStudents: 12,
        avgRating: 4.5,
        completionRate: 85
      },
      
      topics: [
        { id: 1, title: '基于深度学习的图像识别系统' },
        { id: 2, title: '校园二手交易平台设计与实现' },
        { id: 3, title: '智能家居控制系统' }
      ],
      
      topicStats: [
        {
          title: '基于深度学习的图像识别系统',
          difficulty: 'hard',
          selectedCount: 2,
          maxStudents: 3,
          completionRate: 80,
          avgProgress: 35,
          avgRating: 4.5,
          viewCount: 156
        },
        {
          title: '校园二手交易平台设计与实现',
          difficulty: 'medium',
          selectedCount: 1,
          maxStudents: 2,
          completionRate: 50,
          avgProgress: 15,
          avgRating: 4.2,
          viewCount: 89
        },
        {
          title: '智能家居控制系统',
          difficulty: 'medium',
          selectedCount: 0,
          maxStudents: 2,
          completionRate: 0,
          avgProgress: 0,
          avgRating: 0,
          viewCount: 67
        }
      ],
      
      studentStats: [
        {
          name: '张三',
          studentId: '2021001001',
          topicTitle: '基于深度学习的图像识别系统',
          progress: 35,
          status: 'active',
          selectedTime: '2024-01-15',
          lastUpdate: '2024-02-15',
          documentsCount: 3
        },
        {
          name: '李四',
          studentId: '2021001002',
          topicTitle: '基于深度学习的图像识别系统',
          progress: 20,
          status: 'active',
          selectedTime: '2024-01-20',
          lastUpdate: '2024-02-10',
          documentsCount: 2
        },
        {
          name: '王五',
          studentId: '2021001003',
          topicTitle: '校园二手交易平台设计与实现',
          progress: 15,
          status: 'active',
          selectedTime: '2024-01-25',
          lastUpdate: '2024-02-12',
          documentsCount: 1
        }
      ],
      
      timeStats: [
        { date: '2024-02-15', newTopics: 1, newSelections: 0, progressUpdates: 3, documentUploads: 2, totalViews: 45 },
        { date: '2024-02-14', newTopics: 0, newSelections: 1, progressUpdates: 2, documentUploads: 1, totalViews: 38 },
        { date: '2024-02-13', newTopics: 0, newSelections: 0, progressUpdates: 1, documentUploads: 0, totalViews: 32 },
        { date: '2024-02-12', newTopics: 1, newSelections: 1, progressUpdates: 2, documentUploads: 3, totalViews: 56 },
        { date: '2024-02-11', newTopics: 0, newSelections: 0, progressUpdates: 1, documentUploads: 1, totalViews: 28 }
      ]
    }
  },
  mounted() {
    this.initCharts();
  },
  methods: {
    handleDateChange() {
      this.refreshData();
    },
    
    handleTopicChange() {
      this.refreshData();
    },
    
    refreshData() {
      this.$message.success('数据已刷新');
      this.initCharts();
    },
    
    exportReport() {
      this.$message.info('报表导出功能开发中...');
    },
    
    exportDetailedData() {
      this.$message.info('详细数据导出功能开发中...');
    },
    
    setTrendPeriod(period) {
      this.trendPeriod = period;
      this.initCharts();
    },
    
    initCharts() {
      // 这里应该使用 ECharts 或其他图表库
      // 由于项目中没有安装图表库，这里只是模拟
      this.$nextTick(() => {
        // 模拟图表初始化
        console.log('初始化图表');
      });
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
.teacher-reports {
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

.time-filter {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.key-metrics {
  margin-bottom: 30px;
}

.metric-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.metric-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.metric-icon i {
  font-size: 24px;
  color: white;
}

.metric-content h3 {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 5px 0;
}

.metric-content p {
  color: #7f8c8d;
  margin: 0 0 5px 0;
  font-size: 14px;
}

.metric-change {
  font-size: 12px;
  font-weight: 500;
}

.metric-change.positive {
  color: #67c23a;
}

.metric-change.negative {
  color: #f56c6c;
}

.chart-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.chart-content {
  position: relative;
}

.detailed-stats {
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
  .time-filter .el-row {
    flex-direction: column;
  }
  
  .time-filter .el-col {
    margin-bottom: 10px;
  }
  
  .key-metrics .el-row {
    flex-direction: column;
  }
  
  .key-metrics .el-col {
    margin-bottom: 15px;
  }
  
  .chart-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .chart-header .el-button-group {
    margin-top: 10px;
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
