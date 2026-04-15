<template>
  <div class="page admin-logs">
    <div class="page-header">
      <h2 class="page-title">操作日志</h2>
      <p class="page-desc">系统记录的关键操作（登录、登出、审核、删除用户等）</p>
    </div>
    <el-table v-loading="loading" :data="rows" stripe style="width: 100%">
      <el-table-column prop="createTime" label="时间" width="180" />
      <el-table-column prop="username" label="用户" width="120" />
      <el-table-column prop="operation" label="操作" width="140" />
      <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
      <el-table-column prop="method" label="请求" min-width="160" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP" width="140" />
    </el-table>
    <div class="pager">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :total="total"
        :page-size="size"
        :current-page.sync="page"
        @current-change="load"
      />
    </div>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'AdminSystemLogs',
  data() {
    return {
      loading: false,
      rows: [],
      total: 0,
      page: 1,
      size: 10
    }
  },
  mounted() {
    this.load()
  },
  methods: {
    async load(p) {
      if (p) this.page = p
      this.loading = true
      try {
        const res = await request({
          url: '/api/admin/system-logs',
          method: 'get',
          params: { page: this.page, size: this.size }
        })
        const data = res.data || {}
        this.rows = data.records || []
        this.total = data.total || 0
      } catch (e) {
        this.$message.error((e && e.message) || '加载失败')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.admin-logs {
  max-width: 1200px;
  margin: 0 auto;
}
.page-header {
  margin-bottom: 20px;
}
.page-title {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 8px 0;
}
.page-desc {
  color: #909399;
  margin: 0;
  font-size: 14px;
}
.pager {
  margin-top: 16px;
  text-align: right;
}
</style>
