<template>
  <div class="progress-chain-admin">
    <div class="page-header">
      <h2 class="page-title">进度链路</h2>
      <p class="page-desc">配置毕业论文进度节点顺序；选题确认后将自动套用<strong>默认链路</strong>。学生端仅可查看，由指导教师在学生管理中推进。</p>
    </div>

    <el-card shadow="never" class="toolbar-card">
      <el-button type="primary" size="small" icon="el-icon-plus" @click="openChainDialog()">新建链路</el-button>
    </el-card>

    <el-table :data="chains" v-loading="loading" border stripe highlight-current-row @current-change="onChainSelect">
      <el-table-column type="index" label="#" width="50" align="center" />
      <el-table-column prop="name" label="链路名称" min-width="160" />
      <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
      <el-table-column label="默认" width="90" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.isDefault === 1" type="success" size="small">默认</el-tag>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template slot-scope="scope">
          <el-button type="text" size="small" @click="openChainDialog(scope.row)">编辑</el-button>
          <el-button type="text" size="small" @click="handleSetDefault(scope.row)" :disabled="scope.row.isDefault === 1">设为默认</el-button>
          <el-button type="text" size="small" style="color:#F56C6C" @click="handleDeleteChain(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-card v-if="currentChain" class="nodes-card" shadow="never">
      <div slot="header" class="nodes-header">
        <span>节点列表 — {{ currentChain.name }}</span>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="openNodeDialog()">添加节点</el-button>
      </div>
      <el-table :data="nodes" v-loading="nodesLoading" border size="small">
        <el-table-column type="index" label="序" width="55" align="center" />
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
        <el-table-column prop="title" label="节点标题" min-width="160" />
        <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="140" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="openNodeDialog(scope.row)">编辑</el-button>
            <el-button type="text" size="small" style="color:#F56C6C" @click="handleDeleteNode(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="chainForm.id ? '编辑链路' : '新建链路'" :visible.sync="chainDialogVisible" width="480px" @close="resetChainForm">
      <el-form :model="chainForm" label-width="88px">
        <el-form-item label="名称" required>
          <el-input v-model="chainForm.name" maxlength="100" show-word-limit placeholder="如：默认毕业论文进度" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="chainForm.remark" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="chainDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="chainSaving" @click="saveChain">保存</el-button>
      </span>
    </el-dialog>

    <el-dialog :title="nodeForm.id ? '编辑节点' : '添加节点'" :visible.sync="nodeDialogVisible" width="520px" @close="resetNodeForm">
      <el-form :model="nodeForm" label-width="88px">
        <el-form-item label="标题" required>
          <el-input v-model="nodeForm.title" maxlength="200" placeholder="节点名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="nodeForm.sortOrder" :min="1" :max="999" controls-position="right" />
          <span class="hint">数字越小越靠前；留空则自动排在末尾</span>
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="nodeForm.description" type="textarea" :rows="3" maxlength="2000" show-word-limit />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="nodeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="nodeSaving" @click="saveNode">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { progressChainApi } from '@/api'

export default {
  name: 'AdminProgressChain',
  data() {
    return {
      loading: false,
      chains: [],
      currentChain: null,
      nodes: [],
      nodesLoading: false,
      chainDialogVisible: false,
      chainSaving: false,
      chainForm: { id: null, name: '', remark: '' },
      nodeDialogVisible: false,
      nodeSaving: false,
      nodeForm: { id: null, title: '', description: '', sortOrder: null }
    }
  },
  mounted() {
    this.loadChains()
  },
  methods: {
    async loadChains() {
      this.loading = true
      try {
        const res = await progressChainApi.list()
        if (res.code === 200) {
          this.chains = res.data || []
          if (this.currentChain) {
            const id = this.currentChain.id
            this.currentChain = this.chains.find(c => c.id === id) || null
            if (this.currentChain) this.loadNodes(this.currentChain.id)
            else {
              this.nodes = []
            }
          }
        } else {
          this.$message.error(res.message || '加载失败')
        }
      } catch (e) {
        console.error(e)
        this.$message.error('加载失败')
      } finally {
        this.loading = false
      }
    },
    onChainSelect(row) {
      this.currentChain = row
      if (row && row.id) this.loadNodes(row.id)
      else {
        this.nodes = []
      }
    },
    async loadNodes(chainId) {
      this.nodesLoading = true
      try {
        const res = await progressChainApi.listNodes(chainId)
        if (res.code === 200) {
          this.nodes = res.data || []
        }
      } catch (e) {
        console.error(e)
      } finally {
        this.nodesLoading = false
      }
    },
    openChainDialog(row) {
      if (row) {
        this.chainForm = { id: row.id, name: row.name, remark: row.remark || '' }
      } else {
        this.resetChainForm()
      }
      this.chainDialogVisible = true
    },
    resetChainForm() {
      this.chainForm = { id: null, name: '', remark: '' }
    },
    async saveChain() {
      if (!this.chainForm.name || !this.chainForm.name.trim()) {
        this.$message.warning('请填写链路名称')
        return
      }
      this.chainSaving = true
      try {
        let res
        if (this.chainForm.id) {
          res = await progressChainApi.update(this.chainForm.id, {
            name: this.chainForm.name.trim(),
            remark: this.chainForm.remark
          })
        } else {
          res = await progressChainApi.create({
            name: this.chainForm.name.trim(),
            remark: this.chainForm.remark
          })
        }
        if (res.code === 200) {
          this.$message.success('保存成功')
          this.chainDialogVisible = false
          await this.loadChains()
        } else {
          this.$message.error(res.message || '保存失败')
        }
      } catch (e) {
        console.error(e)
        this.$message.error('保存失败')
      } finally {
        this.chainSaving = false
      }
    },
    async handleSetDefault(row) {
      try {
        const res = await progressChainApi.setDefault(row.id)
        if (res.code === 200) {
          this.$message.success('已设为默认')
          await this.loadChains()
        } else {
          this.$message.error(res.message || '操作失败')
        }
      } catch (e) {
        console.error(e)
        this.$message.error('操作失败')
      }
    },
    handleDeleteChain(row) {
      this.$confirm(`确定删除链路「${row.name}」及其节点吗？`, '提示', { type: 'warning' })
        .then(async () => {
          const res = await progressChainApi.remove(row.id)
          if (res.code === 200) {
            this.$message.success('已删除')
            if (this.currentChain && this.currentChain.id === row.id) {
              this.currentChain = null
              this.nodes = []
            }
            await this.loadChains()
          } else {
            this.$message.error(res.message || '删除失败')
          }
        })
        .catch(() => {})
    },
    openNodeDialog(row) {
      if (!this.currentChain) {
        this.$message.warning('请先在上表选中一条链路')
        return
      }
      if (row) {
        this.nodeForm = {
          id: row.id,
          title: row.title,
          description: row.description || '',
          sortOrder: row.sortOrder
        }
      } else {
        this.resetNodeForm()
      }
      this.nodeDialogVisible = true
    },
    resetNodeForm() {
      this.nodeForm = { id: null, title: '', description: '', sortOrder: null }
    },
    async saveNode() {
      if (!this.currentChain) return
      if (!this.nodeForm.title || !this.nodeForm.title.trim()) {
        this.$message.warning('请填写节点标题')
        return
      }
      this.nodeSaving = true
      try {
        const payload = {
          title: this.nodeForm.title.trim(),
          description: this.nodeForm.description,
          sortOrder: this.nodeForm.sortOrder
        }
        let res
        if (this.nodeForm.id) {
          res = await progressChainApi.updateNode(this.nodeForm.id, payload)
        } else {
          res = await progressChainApi.addNode(this.currentChain.id, payload)
        }
        if (res.code === 200) {
          this.$message.success('保存成功')
          this.nodeDialogVisible = false
          await this.loadNodes(this.currentChain.id)
        } else {
          this.$message.error(res.message || '保存失败')
        }
      } catch (e) {
        console.error(e)
        this.$message.error('保存失败')
      } finally {
        this.nodeSaving = false
      }
    },
    handleDeleteNode(row) {
      this.$confirm(`确定删除节点「${row.title}」？`, '提示', { type: 'warning' })
        .then(async () => {
          const res = await progressChainApi.deleteNode(row.id)
          if (res.code === 200) {
            this.$message.success('已删除')
            await this.loadNodes(this.currentChain.id)
          } else {
            this.$message.error(res.message || '删除失败')
          }
        })
        .catch(() => {})
    }
  }
}
</script>

<style scoped>
.progress-chain-admin {
  max-width: 1100px;
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
  line-height: 1.5;
}
.toolbar-card {
  margin-bottom: 16px;
}
.nodes-card {
  margin-top: 20px;
}
.nodes-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.hint {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}
</style>
