<template>
  <div class="admin-dashboard">
    <div class="page-header">
      <h2 class="page-title">数据概览</h2>
      <p class="page-desc">系统整体运行情况和关键指标监控</p>
    </div>
    
    <!-- 关键指标 -->
    <div class="key-metrics">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="metric-card">
            <div class="metric-icon">
              <i class="el-icon-user"></i>
            </div>
            <div class="metric-content">
              <h3>{{ metrics.totalUsers }}</h3>
              <p>总用户数</p>
              <span class="metric-change positive">+{{ metrics.userGrowth }}%</span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="metric-card">
            <div class="metric-icon">
              <i class="el-icon-document"></i>
            </div>
            <div class="metric-content">
              <h3>{{ metrics.totalTopics }}</h3>
              <p>发布课题数</p>
              <span class="metric-change positive">+{{ metrics.topicGrowth }}%</span>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="metric-card">
            <div class="metric-icon">
              <i class="el-icon-star-on"></i>
            </div>
            <div class="metric-content">
              <h3>{{ metrics.totalSelections }}</h3>
              <p>选题总数</p>
              <span class="metric-change positive">+{{ metrics.selectionGrowth }}%</span>
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
              <span class="metric-change positive">+{{ metrics.completionGrowth }}%</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
    
    <!-- 图表区域 -->
    <el-row :gutter="20">
      <!-- 用户增长趋势 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <h3>用户增长趋势</h3>
            <el-button-group>
              <el-button size="small" :type="userTrendPeriod === 'week' ? 'primary' : ''" @click="setUserTrendPeriod('week')">周</el-button>
              <el-button size="small" :type="userTrendPeriod === 'month' ? 'primary' : ''" @click="setUserTrendPeriod('month')">月</el-button>
              <el-button size="small" :type="userTrendPeriod === 'year' ? 'primary' : ''" @click="setUserTrendPeriod('year')">年</el-button>
            </el-button-group>
          </div>
          <div class="chart-content">
            <div id="userTrendChart" style="height: 300px;"></div>
          </div>
        </div>
      </el-col>
      
      <!-- 课题分布 -->
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <h3>课题难度分布</h3>
          </div>
          <div class="chart-content">
            <div id="topicDistributionChart" style="height: 300px;"></div>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 系统状态 -->
    <div class="system-status">
      <div class="section-header">
        <h3>系统状态</h3>
        <el-button type="primary" size="small" @click="refreshSystemStatus">刷新状态</el-button>
      </div>
      
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="status-card">
            <div class="status-header">
              <h4>服务器状态</h4>
              <el-tag :type="systemStatus.server === 'normal' ? 'success' : 'danger'" size="small">
                {{ systemStatus.server === 'normal' ? '正常' : '异常' }}
              </el-tag>
            </div>
            <div class="status-content">
              <div class="status-item">
                <span>CPU使用率：</span>
                <span>{{ systemStatus.cpu }}%</span>
              </div>
              <div class="status-item">
                <span>内存使用率：</span>
                <span>{{ systemStatus.memory }}%</span>
              </div>
              <div class="status-item">
                <span>磁盘使用率：</span>
                <span>{{ systemStatus.disk }}%</span>
              </div>
            </div>
          </div>
        </el-col>
        
        <el-col :span="8">
          <div class="status-card">
            <div class="status-header">
              <h4>数据库状态</h4>
              <el-tag :type="systemStatus.database === 'normal' ? 'success' : 'danger'" size="small">
                {{ systemStatus.database === 'normal' ? '正常' : '异常' }}
              </el-tag>
            </div>
            <div class="status-content">
              <div class="status-item">
                <span>连接数：</span>
                <span>{{ systemStatus.dbConnections }}</span>
              </div>
              <div class="status-item">
                <span>查询响应时间：</span>
                <span>{{ systemStatus.queryTime }}ms</span>
              </div>
              <div class="status-item">
                <span>存储空间：</span>
                <span>{{ systemStatus.dbStorage }}</span>
              </div>
            </div>
          </div>
        </el-col>
        
        <el-col :span="8">
          <div class="status-card">
            <div class="status-header">
              <h4>网络状态</h4>
              <el-tag :type="systemStatus.network === 'normal' ? 'success' : 'danger'" size="small">
                {{ systemStatus.network === 'normal' ? '正常' : '异常' }}
              </el-tag>
            </div>
            <div class="status-content">
              <div class="status-item">
                <span>在线用户：</span>
                <span>{{ systemStatus.onlineUsers }}</span>
              </div>
              <div class="status-item">
                <span>今日访问量：</span>
                <span>{{ systemStatus.dailyVisits }}</span>
              </div>
              <div class="status-item">
                <span>平均响应时间：</span>
                <span>{{ systemStatus.responseTime }}ms</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
    
    <!-- 最近活动 -->
    <div class="recent-activities">
      <div class="section-header">
        <h3>最近活动</h3>
        <el-button type="primary" size="small" @click="viewAllActivities">查看全部</el-button>
      </div>
      
      <div class="activities-list">
        <el-timeline>
          <el-timeline-item
            v-for="(activity, index) in recentActivities"
            :key="index"
            :timestamp="activity.time"
            :type="getActivityType(activity.type)">
            <div class="activity-content">
              <h4>{{ activity.title }}</h4>
              <p>{{ activity.description }}</p>
              <el-tag :type="getActivityType(activity.type)" size="small">
                {{ getActivityTypeText(activity.type) }}
              </el-tag>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>
    
    <!-- 快速操作 -->
    <div class="quick-actions">
      <div class="section-header">
        <h3>快速操作</h3>
      </div>
      
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="action-card" @click="goToUserManagement">
            <div class="action-icon">
              <i class="el-icon-user"></i>
            </div>
            <div class="action-content">
              <h4>用户管理</h4>
              <p>管理系统用户</p>
            </div>
          </div>
        </el-col>
        
        <el-col :span="6">
          <div class="action-card" @click="goToTopicManagement">
            <div class="action-icon">
              <i class="el-icon-document"></i>
            </div>
            <div class="action-content">
              <h4>课题管理</h4>
              <p>管理所有课题</p>
            </div>
          </div>
        </el-col>
        
        <el-col :span="6">
          <div class="action-card" @click="goToSystemSettings">
            <div class="action-icon">
              <i class="el-icon-setting"></i>
            </div>
            <div class="action-content">
              <h4>系统设置</h4>
              <p>配置系统参数</p>
            </div>
          </div>
        </el-col>
        
        <el-col :span="6">
          <div class="action-card" @click="exportSystemData">
            <div class="action-icon">
              <i class="el-icon-download"></i>
            </div>
            <div class="action-content">
              <h4>数据导出</h4>
              <p>导出系统数据</p>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { statsApi } from '@/api'

export default {
  name: 'AdminDashboard',
  data() {
    return {
      userTrendPeriod: 'month',
      loading: false,
      
      metrics: {
        totalUsers: 0,
        userGrowth: 0,
        totalTopics: 0,
        topicGrowth: 0,
        totalSelections: 0,
        selectionGrowth: 0,
        completionRate: 0,
        completionGrowth: 0
      },
      
      systemStatus: {
        server: 'normal',
        cpu: 45,
        memory: 62,
        disk: 38,
        database: 'normal',
        dbConnections: 156,
        queryTime: 12,
        dbStorage: '2.3GB',
        network: 'normal',
        onlineUsers: 89,
        dailyVisits: 1234,
        responseTime: 85
      },
      
      recentActivities: [
        {
          title: '新用户注册',
          description: '学生张三注册了账户',
          time: '2024-02-15 14:30',
          type: 'user'
        },
        {
          title: '课题发布',
          description: '李教授发布了新课题"基于AI的智能推荐系统"',
          time: '2024-02-15 13:45',
          type: 'topic'
        },
        {
          title: '选题成功',
          description: '王五选择了课题"校园二手交易平台"',
          time: '2024-02-15 12:20',
          type: 'selection'
        },
        {
          title: '系统维护',
          description: '系统进行了定期维护，运行正常',
          time: '2024-02-15 10:00',
          type: 'system'
        },
        {
          title: '文档上传',
          description: '学生李四上传了需求分析文档',
          time: '2024-02-15 09:15',
          type: 'document'
        }
      ]
    }
  },
  async mounted() {
    await this.loadDashboardData();
    this.initCharts();
  },
  methods: {
    // 加载仪表板数据
    async loadDashboardData() {
      try {
        this.loading = true
        const response = await statsApi.getOverviewStats()
        if (response.code === 200) {
          this.metrics = response.data
        } else {
          this.$message.error(response.message || '获取统计数据失败')
        }
      } catch (error) {
        console.error('加载仪表板数据失败:', error)
        this.$message.error('加载仪表板数据失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    
    setUserTrendPeriod(period) {
      this.userTrendPeriod = period;
      this.initCharts();
    },
    
    initCharts() {
      // 这里应该使用 ECharts 或其他图表库
      // 由于项目中没有安装图表库，这里只是模拟
      this.$nextTick(() => {
        console.log('初始化图表');
      });
    },
    
    refreshSystemStatus() {
      this.$message.success('系统状态已刷新');
    },
    
    viewAllActivities() {
      this.$message.info('查看全部活动功能开发中...');
    },
    
    goToUserManagement() {
      this.$router.push('/layout/admin/users');
    },
    
    goToTopicManagement() {
      this.$router.push('/layout/admin/topics');
    },
    
    goToSystemSettings() {
      this.$router.push('/layout/admin/system');
    },
    
    exportSystemData() {
      this.$message.info('数据导出功能开发中...');
    },
    
    getActivityType(type) {
      const typeMap = {
        'user': 'primary',
        'topic': 'success',
        'selection': 'warning',
        'system': 'info',
        'document': 'primary'
      };
      return typeMap[type] || 'info';
    },
    
    getActivityTypeText(type) {
      const textMap = {
        'user': '用户',
        'topic': '课题',
        'selection': '选题',
        'system': '系统',
        'document': '文档'
      };
      return textMap[type] || '未知';
    }
  }
}
</script>

<style scoped>
.admin-dashboard {
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

.system-status,
.recent-activities,
.quick-actions {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
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

.status-card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 15px;
  border: 1px solid #e9ecef;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.status-header h4 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.status-content {
  font-size: 14px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.status-item span:first-child {
  color: #606266;
}

.status-item span:last-child {
  color: #2c3e50;
  font-weight: 500;
}

.activity-content h4 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 5px 0;
}

.activity-content p {
  color: #606266;
  margin: 0 0 8px 0;
  font-size: 14px;
}

.action-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e9ecef;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  border-color: #409eff;
}

.action-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.action-icon i {
  font-size: 20px;
  color: white;
}

.action-content h4 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 5px 0;
}

.action-content p {
  color: #7f8c8d;
  margin: 0;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
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
  
  .system-status .el-row {
    flex-direction: column;
  }
  
  .system-status .el-col {
    margin-bottom: 15px;
  }
  
  .quick-actions .el-row {
    flex-direction: column;
  }
  
  .quick-actions .el-col {
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
