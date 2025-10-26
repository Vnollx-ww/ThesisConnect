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
            <v-chart 
              :option="trendOption" 
              style="height: 300px;"
              v-if="trendData.dates && trendData.dates.length > 0"
            />
            <div v-else style="height: 300px; display: flex; align-items: center; justify-content: center; color: #999;">
              暂无数据
            </div>
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
            <v-chart 
              :option="progressOption" 
              style="height: 300px;"
              v-if="progressDataArray && progressDataArray.length > 0"
            />
            <div v-else style="height: 300px; display: flex; align-items: center; justify-content: center; color: #999;">
              暂无数据
            </div>
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
            <el-table :data="topicStats" style="width: 100%" v-loading="loading">
            <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
            <el-table-column prop="title" label="课题名称" min-width="250" show-overflow-tooltip></el-table-column>
            <el-table-column prop="difficulty" label="难度" width="100" align="center">
              <template slot-scope="scope">
                <el-tag :type="getDifficultyType(scope.row.difficulty)" size="small">
                  {{ getDifficultyText(scope.row.difficulty) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="selectedCount" label="选择人数" width="110" align="center"></el-table-column>
            <el-table-column prop="maxStudents" label="最大人数" width="110" align="center"></el-table-column>
            <el-table-column prop="completionRate" label="完成率" width="110" align="center">
              <template slot-scope="scope">
                {{ scope.row.completionRate }}%
              </template>
            </el-table-column>
            <el-table-column prop="avgProgress" label="平均进度" width="110" align="center">
              <template slot-scope="scope">
                {{ scope.row.avgProgress }}%
              </template>
            </el-table-column>
            <el-table-column prop="avgRating" label="平均评分" width="110" align="center">
              <template slot-scope="scope">
                {{ scope.row.avgRating ? scope.row.avgRating.toFixed(1) : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="viewCount" label="浏览量" width="110" align="center"></el-table-column>
            <el-table-column prop="deadline" label="截止时间" width="120" align="center">
              <template slot-scope="scope">
                {{ scope.row.deadline || '-' }}
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <!-- 学生统计 -->
        <el-tab-pane label="学生统计" name="students">
          <el-table :data="studentStats" style="width: 100%" v-loading="loading">
            <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
            <el-table-column prop="name" label="学生姓名" width="120" align="center"></el-table-column>
            <el-table-column prop="studentId" label="学号" width="150" align="center"></el-table-column>
            <el-table-column prop="topicTitle" label="课题" min-width="220" show-overflow-tooltip></el-table-column>
            <el-table-column prop="progress" label="进度" width="110" align="center">
              <template slot-scope="scope">
                {{ scope.row.progress }}%
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="110" align="center">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.status)" size="small">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="selectedTime" label="选择时间" width="130" align="center"></el-table-column>
            <el-table-column prop="lastUpdate" label="最后更新" width="130" align="center"></el-table-column>
            <el-table-column prop="documentsCount" label="文档数" width="110" align="center"></el-table-column>
          </el-table>
        </el-tab-pane>
        
        <!-- 时间统计 -->
        <el-tab-pane label="时间统计" name="time">
          <el-table :data="timeStats" style="width: 100%" v-loading="loading">
            <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
            <el-table-column prop="date" label="日期" width="160" align="center"></el-table-column>
            <el-table-column prop="newTopics" label="新增课题" width="140" align="center"></el-table-column>
            <el-table-column prop="newSelections" label="新增选择" width="140" align="center"></el-table-column>
            <el-table-column prop="progressUpdates" label="进度更新" width="140" align="center"></el-table-column>
            <el-table-column prop="documentUploads" label="文档上传" width="140" align="center"></el-table-column>
            <el-table-column prop="totalViews" label="总浏览量" width="140" align="center"></el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
import { topicApi, selectionApi, statsApi } from '@/api'
import { getCurrentUser } from '@/utils/user'

export default {
  name: 'TeacherReports',
  data() {
    return {
      teacherId: null,
      dateRange: [],
      selectedTopic: '',
      trendPeriod: 'month',
      activeTab: 'topics',
      loading: false,
      
      metrics: {
        totalTopics: 0,
        totalStudents: 0,
        avgRating: 0,
        completionRate: 0
      },
      
      topics: [],
      fullTopics: [], // 完整的课题列表，包含所有字段
      selections: [], // 选题列表
      
      topicStats: [],
      studentStats: [],
      timeStats: [],
      
      // 图表数据
      trendData: {
        dates: [],
        values: []
      },
      progressDataArray: []
    }
  },
  computed: {
    trendOption() {
      return {
        title: {
          text: '课题选择趋势',
          left: 'center',
          textStyle: {
            fontSize: 16
          }
        },
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['选择人数'],
          bottom: 10
        },
        xAxis: {
          type: 'category',
          data: this.trendData.dates,
          axisLabel: {
            interval: 0,
            rotate: 45
          }
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '选择人数',
            type: 'line',
            smooth: true,
            data: this.trendData.values,
            itemStyle: {
              color: '#667eea'
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  { offset: 0, color: 'rgba(102, 126, 234, 0.3)' },
                  { offset: 1, color: 'rgba(102, 126, 234, 0)' }
                ]
              }
            }
          }
        ],
        grid: {
          left: '10%',
          right: '10%',
          bottom: '20%'
        }
      }
    },
    
    progressOption() {
      return {
        title: {
          text: '学生进度分布',
          left: 'center',
          textStyle: {
            fontSize: 16
          }
        },
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          bottom: 10
        },
        series: [
          {
            name: '学生人数',
            type: 'pie',
            radius: '50%',
            data: this.progressDataArray,
            label: {
              show: true,
              formatter: '{b}: {c}'
            },
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      }
    }
  },
  async mounted() {
    await this.loadTeacherData()
  },
  methods: {
    async loadTeacherData() {
      try {
        this.loading = true
        
        // 获取当前用户信息
        const currentUser = await getCurrentUser()
        if (!currentUser || !currentUser.id) {
          this.$message.error('获取用户信息失败')
          return
        }
        
        this.teacherId = currentUser.id
        
        // 获取教师课题统计
        await this.loadTopicStats()
        
        // 获取教师学生统计
        await this.loadStudentStats()
        
        // 获取教师课题列表
        await this.loadTopics()
        
        // 获取教师选题列表
        await this.loadSelections()
        
        // 更新metrics中的totalStudents（基于选题列表）
        this.updateMetrics()
        
      } catch (error) {
        console.error('加载数据失败:', error)
        this.$message.error('加载数据失败')
      } finally {
        this.loading = false
      }
    },
    
    async loadTopicStats() {
      try {
        const response = await statsApi.getTeacherTopicStats(this.teacherId)
        if (response.code === 200 && response.data) {
          const data = response.data
          this.metrics = {
            totalTopics: data.totalTopics || 0,
            totalStudents: 0, // 将在 loadSelections 后更新
            avgRating: data.avgRating || 0,
            completionRate: data.completedTopics && data.totalTopics > 0 
              ? Math.round((data.completedTopics / data.totalTopics) * 100) 
              : 0
          }
        }
      } catch (error) {
        console.error('获取课题统计失败:', error)
      }
    },
    
    updateMetrics() {
      // 使用选题列表的长度来更新 totalStudents
      this.metrics.totalStudents = this.selections.length
    },
    
    async loadStudentStats() {
      try {
        const response = await statsApi.getTeacherStudentStats(this.teacherId)
        if (response.code === 200 && response.data) {
          // 这个接口返回统计信息，不需要处理学生列表
          // 学生列表从selections中获取
        }
      } catch (error) {
        console.error('获取学生统计失败:', error)
      }
    },
    
    async loadTopics() {
      try {
        const response = await topicApi.getTopicsByTeacher(this.teacherId)
        if (response.code === 200 && response.data) {
          this.fullTopics = response.data
          this.topics = response.data.map(topic => ({
            id: topic.id,
            title: topic.title
          }))
        }
      } catch (error) {
        console.error('获取课题列表失败:', error)
      }
    },
    
    async loadSelections() {
      try {
        const response = await statsApi.getTeacherSelections(this.teacherId)
        if (response.code === 200 && response.data) {
          this.selections = response.data
          
          // 转换选题数据为topicStats
          this.processTopicStats()
          this.processStudentStats()
          
          // 更新图表数据
          this.updateChartData()
        }
      } catch (error) {
        console.error('获取选题列表失败:', error)
      }
    },
    
    processTopicStats() {
      // 将selections转换为topicStats
      const topicMap = new Map()
      
      // 首先为所有该教师的课题创建stats
      this.fullTopics.forEach(topic => {
        topicMap.set(topic.id, {
          title: topic.title,
          difficulty: topic.difficulty || 'medium',
          selectedCount: topic.selectedCount || 0,
          maxStudents: topic.maxStudents || 0,
          completionRate: 0,
          avgProgress: 0,
          avgRating: topic.rating || 0,
          viewCount: topic.viewCount || 0,
          deadline: this.formatDate(topic.deadline),
          topicId: topic.id
        })
      })
      
      // 然后计算每个课题的平均进度
      topicMap.forEach((stats, topicId) => {
        const selectionsForTopic = this.selections.filter(s => s.topicId === topicId && s.progress !== undefined && s.progress !== null)
        if (selectionsForTopic.length > 0) {
          const totalProgress = selectionsForTopic.reduce((sum, s) => sum + (s.progress || 0), 0)
          stats.avgProgress = Math.round(totalProgress / selectionsForTopic.length)
        }
      })
      
      this.topicStats = Array.from(topicMap.values())
    },
    
    formatDate(dateStr) {
      if (!dateStr) return ''
      // 处理 ISO 格式 2025-10-23T10:49:58 或普通格式 2025-10-23 10:49:58
      return dateStr.split('T')[0].split(' ')[0]
    },
    
    processStudentStats() {
      // 将selections转换为studentStats
      this.studentStats = this.selections.map(selection => ({
        name: selection.studentName || '未知',
        studentId: selection.studentNumber || '',
        topicTitle: selection.topicTitle || this.topics.find(t => t.id === selection.topicId)?.title || '未知',
        progress: selection.progress || 0,
        status: selection.status || 'pending',
        selectedTime: this.formatDate(selection.createTime || selection.selectionTime),
        lastUpdate: this.formatDate(selection.updateTime),
        documentsCount: 0 // 暂时设为0，需要单独获取
      }))
    },
    
    getDifficultyFromTopic(topicId) {
      const topic = this.fullTopics.find(t => t.id === topicId)
      return topic?.difficulty || 'medium'
    },
    
    handleDateChange() {
      this.refreshData()
    },
    
    handleTopicChange() {
      this.refreshData()
    },
    
    async refreshData() {
      this.$message.success('数据已刷新')
      await this.loadTeacherData()
    },
    
    exportReport() {
      this.$message.info('报表导出功能开发中...')
    },
    
    exportDetailedData() {
      this.$message.info('详细数据导出功能开发中...')
    },
    
    setTrendPeriod(period) {
      this.trendPeriod = period
      this.updateChartData()
    },
    
    updateChartData() {
      // 更新趋势数据
      this.updateTrendData()
      
      // 更新进度分布数据
      this.updateProgressData()
    },
    
    updateTrendData() {
      const dates = []
      const values = []
      
      if (this.selections.length === 0) {
        // 如果没有数据，生成模拟数据
        const now = new Date()
        for (let i = 6; i >= 0; i--) {
          const date = new Date(now)
          date.setDate(date.getDate() - i)
          dates.push(date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' }))
          values.push(Math.floor(Math.random() * 5))
        }
      } else {
        // 统计每月的选择人数（按月统计更直观）
        const monthMap = new Map()
        this.selections.forEach(selection => {
          if (selection.createTime) {
            const date = new Date(selection.createTime)
            const year = date.getFullYear()
            const month = date.getMonth() + 1
            // 使用 "年-月" 格式，如 "2024-10"
            const monthKey = `${year}-${String(month).padStart(2, '0')}`
            monthMap.set(monthKey, (monthMap.get(monthKey) || 0) + 1)
          }
        })
        
        // 按时间排序
        const sortedMonths = Array.from(monthMap.entries()).sort((a, b) => a[0].localeCompare(b[0]))
        
        sortedMonths.forEach(([monthKey, count]) => {
          // 将 "2024-10" 格式化为 "2024年10月"
          const [year, month] = monthKey.split('-')
          dates.push(`${year}年${parseInt(month)}月`)
          values.push(count)
        })
      }
      
      this.trendData = { dates, values }
    },
    
    updateProgressData() {
      // 统计不同进度区间的学生数
      const progressRanges = [
        { name: '0-20%', min: 0, max: 20, value: 0 },
        { name: '21-40%', min: 21, max: 40, value: 0 },
        { name: '41-60%', min: 41, max: 60, value: 0 },
        { name: '61-80%', min: 61, max: 80, value: 0 },
        { name: '81-100%', min: 81, max: 100, value: 0 }
      ]
      
      this.selections.forEach(selection => {
        if (selection.progress !== undefined && selection.progress !== null) {
          progressRanges.forEach(range => {
            if (selection.progress >= range.min && selection.progress <= range.max) {
              range.value++
            }
          })
        }
      })
      
      // 过滤掉数值为0的区间
      this.progressDataArray = progressRanges.filter(range => range.value > 0).map(range => ({
        value: range.value,
        name: range.name
      }))
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
        'pending': 'warning',
        'approved': 'success',
        'active': 'success',
        'rejected': 'danger',
        'completed': 'info',
        'paused': 'warning'
      };
      return typeMap[status] || 'info';
    },
    
    getStatusText(status) {
      const textMap = {
        'pending': '待审核',
        'approved': '已通过',
        'active': '进行中',
        'rejected': '已拒绝',
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
