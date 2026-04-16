<template>
  <div class="notifications-page">
    <div class="page-header">
      <h2 class="page-title">消息中心</h2>
      <p class="page-desc">站内通知与待办提醒（与邮件并行）</p>
    </div>

    <el-card v-if="pendingSummary && typeof pendingSummary === 'object' && Object.keys(pendingSummary).length" class="summary-card" shadow="never">
      <div slot="header" class="clearfix">
        <span>待办</span>
      </div>
      <el-row :gutter="16">
        <el-col v-if="pendingSummary.pendingReviews != null" :span="8">
          <div class="stat"><span class="label">待审核选题</span><span class="num">{{ pendingSummary.pendingReviews }}</span></div>
        </el-col>
        <el-col v-if="pendingSummary.pendingConfirm != null" :span="8">
          <div class="stat"><span class="label">待确认选题</span><span class="num">{{ pendingSummary.pendingConfirm }}</span></div>
        </el-col>
        <el-col v-if="pendingSummary.unreadNotifications != null" :span="8">
          <div class="stat"><span class="label">未读通知</span><span class="num">{{ pendingSummary.unreadNotifications }}</span></div>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="list-card" shadow="never">
      <div slot="header" class="toolbar">
        <el-radio-group v-model="readFilter" size="small" @change="onReadFilterChange">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button :label="0">未读</el-radio-button>
          <el-radio-button :label="1">已读</el-radio-button>
        </el-radio-group>
        <el-button type="primary" size="small" plain @click="markAllRead" :disabled="unreadCount === 0">全部标为已读</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" style="width: 100%">
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column label="类型" width="160">
          <template slot-scope="scope">{{ bizTypeLabel(scope.row.bizType) }}</template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip />
        <el-table-column label="时间" width="170">
          <template slot-scope="scope">{{ formatTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template slot-scope="scope">
            <el-tag :type="scope.row.readFlag === 1 ? 'info' : 'success'" size="mini">
              {{ scope.row.readFlag === 1 ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="scope">
            <el-button v-if="scope.row.readFlag !== 1" type="text" size="small" @click="markOne(scope.row)">标为已读</el-button>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page.sync="page"
          @current-change="loadList"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { notificationApi } from '@/api'

export default {
  name: 'NotificationsCenter',
  data() {
    return {
      loading: false,
      rows: [],
      total: 0,
      page: 1,
      pageSize: 10,
      readFilter: 'all',
      pendingSummary: {},
      unreadCount: 0
    }
  },
  mounted() {
    this.loadSummary()
    this.loadUnreadCount()
    this.loadList()
  },
  methods: {
    async loadSummary() {
      try {
        const res = await notificationApi.getPendingSummary()
        if (res.code === 200 && res.data) {
          this.pendingSummary = res.data
        }
      } catch (e) {
        console.warn(e)
      }
    },
    async loadUnreadCount() {
      try {
        const res = await notificationApi.getUnreadCount()
        if (res.code === 200 && res.data) {
          this.unreadCount = res.data.count || 0
        }
      } catch (e) {
        console.warn(e)
      }
    },
    onReadFilterChange() {
      this.page = 1
      this.loadList()
    },
    async loadList() {
      this.loading = true
      try {
        const params = { page: this.page, size: this.pageSize }
        if (this.readFilter !== 'all') {
          params.readFlag = this.readFilter
        }
        const res = await notificationApi.getList(params)
        if (res.code === 200 && res.data) {
          this.rows = res.data.records || []
          this.total = res.data.total || 0
        }
      } catch (e) {
        this.$message.error(e.message || '加载失败')
      } finally {
        this.loading = false
      }
    },
    async markOne(row) {
      try {
        const res = await notificationApi.markRead(row.id)
        if (res.code === 200) {
          this.$message.success('已标记')
          await this.loadList()
          await this.loadSummary()
          await this.loadUnreadCount()
          this.$root.$emit('notifications-updated')
        }
      } catch (e) {
        this.$message.error(e.message || '操作失败')
      }
    },
    async markAllRead() {
      try {
        const res = await notificationApi.markAllRead()
        if (res.code === 200) {
          this.$message.success('已全部标为已读')
          await this.loadList()
          await this.loadSummary()
          await this.loadUnreadCount()
          this.$root.$emit('notifications-updated')
        }
      } catch (e) {
        this.$message.error(e.message || '操作失败')
      }
    },
    formatTime(t) {
      if (!t) return '-'
      return String(t).replace('T', ' ').slice(0, 19)
    },
    bizTypeLabel(code) {
      if (!code) return '—'
      const map = {
        SELECTION_SUBMITTED: '选题申请',
        SELECTION_APPROVED: '选题已通过',
        SELECTION_REJECTED: '选题未通过',
        PROGRESS_NODE_REVIEWED: '阶段材料审核'
      }
      return map[code] || code
    }
  }
}
</script>

<style scoped>
.notifications-page {
  max-width: 1100px;
  margin: 0 auto;
}
.page-header {
  margin-bottom: 16px;
}
.page-title {
  margin: 0 0 8px;
  font-size: 22px;
}
.page-desc {
  margin: 0;
  color: #909399;
  font-size: 14px;
}
.summary-card {
  margin-bottom: 16px;
}
.stat {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.stat .label {
  color: #909399;
  font-size: 13px;
}
.stat .num {
  font-size: 22px;
  font-weight: 600;
  color: #409eff;
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.list-card {
  margin-bottom: 24px;
}
.pagination-wrap {
  margin-top: 16px;
  text-align: right;
}
.muted {
  color: #c0c4cc;
}
</style>
