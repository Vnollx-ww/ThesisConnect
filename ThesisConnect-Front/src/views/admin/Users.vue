<template>
  <div class="admin-users">
    <div class="page-header">
      <h2 class="page-title">用户管理</h2>
      <p class="page-desc">管理系统中的所有用户，包括学生、教师和管理员</p>
      <el-button type="primary" @click="addUser" class="add-user-btn">
        <i class="el-icon-plus"></i>
        添加用户
      </el-button>
    </div>
    
    <!-- 用户统计概览 -->
    <div class="stats-overview">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon">
              <i class="el-icon-user"></i>
            </div>
            <div class="stat-content">
              <h3>{{ stats.totalUsers }}</h3>
              <p>总用户数</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon">
              <i class="el-icon-user-solid"></i>
            </div>
            <div class="stat-content">
              <h3>{{ stats.students }}</h3>
              <p>学生用户</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon">
              <i class="el-icon-s-custom"></i>
            </div>
            <div class="stat-content">
              <h3>{{ stats.teachers }}</h3>
              <p>教师用户</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon">
              <i class="el-icon-s-platform"></i>
            </div>
            <div class="stat-content">
              <h3>{{ stats.admins }}</h3>
              <p>管理员</p>
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
            placeholder="搜索用户名或ID"
            prefix-icon="el-icon-search"
            @input="handleSearch">
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="roleFilter" placeholder="角色筛选" @change="handleFilter">
            <el-option label="全部角色" value=""></el-option>
            <el-option label="学生" value="student"></el-option>
            <el-option label="教师" value="teacher"></el-option>
            <el-option label="管理员" value="admin"></el-option>
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="statusFilter" placeholder="状态筛选" @change="handleFilter">
            <el-option label="全部状态" value=""></el-option>
            <el-option label="正常" value="1"></el-option>
            <el-option label="禁用" value="0"></el-option>
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button @click="resetFilter">重置筛选</el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="batchOperation">
            <i class="el-icon-s-operation"></i>
            批量操作
          </el-button>
        </el-col>
      </el-row>
    </div>
    
    <!-- 用户列表 -->
    <div class="users-section">
      <div class="section-header">
        <h3>用户列表</h3>
        <div class="header-actions">
          <el-button @click="exportUsers">
            <i class="el-icon-download"></i>
            导出用户
          </el-button>
          <el-button @click="importUsers">
            <i class="el-icon-upload"></i>
            导入用户
          </el-button>
        </div>
      </div>
      
      <div class="users-table">
        <el-table 
          :data="filteredUsers" 
          style="width: 100%"
          v-loading="loading"
          @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="avatar" label="头像" width="80">
            <template slot-scope="scope">
              <el-avatar :size="40" :src="scope.row.avatar">
                <i class="el-icon-user-solid"></i>
              </el-avatar>
            </template>
          </el-table-column>
          <el-table-column prop="realName" label="姓名" width="100"></el-table-column>
          <el-table-column prop="username" label="用户名" width="120"></el-table-column>
          <el-table-column prop="studentId" label="学号/工号" width="120"></el-table-column>
          <el-table-column prop="role" label="角色" width="100">
            <template slot-scope="scope">
              <el-tag :type="getRoleType(scope.row.role)" size="small">
                {{ getRoleText(scope.row.role) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="department" label="部门/专业" min-width="150"></el-table-column>
          <el-table-column prop="email" label="邮箱" min-width="180"></el-table-column>
          <el-table-column prop="phone" label="电话" width="120"></el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.status)" size="small">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="lastLoginTime" label="最后登录" width="120">
            <template slot-scope="scope">
              {{ scope.row.lastLoginTime ? scope.row.lastLoginTime.split('T')[0] : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="注册时间" width="120">
            <template slot-scope="scope">
              {{ scope.row.createTime ? scope.row.createTime.split('T')[0] : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template slot-scope="scope">
              <el-button type="text" size="small" @click="viewUserDetail(scope.row)">详情</el-button>
              <el-button type="text" size="small" @click="editUser(scope.row)">编辑</el-button>
              <el-button type="text" size="small" @click="resetPassword(scope.row)">重置密码</el-button>
              <el-button type="text" size="small" @click="toggleUserStatus(scope.row)">
                {{ scope.row.status === 1 ? '禁用' : '启用' }}
              </el-button>
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
          :total="totalUsers">
        </el-pagination>
      </div>
    </div>
    
    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="userDialogVisible"
      width="600px"
      :before-close="handleClose">
      <el-form :model="userForm" :rules="userRules" ref="userForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="userForm.username" placeholder="请输入用户名"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="userForm.realName" placeholder="请输入真实姓名"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学号/工号" prop="studentId">
              <el-input v-model="userForm.studentId" placeholder="请输入学号或工号"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门/专业" prop="department">
              <el-input v-model="userForm.department" placeholder="请输入部门或专业"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="角色" prop="role">
              <el-select v-model="userForm.role" placeholder="选择角色">
                <el-option label="学生" value="student"></el-option>
                <el-option label="教师" value="teacher"></el-option>
                <el-option label="管理员" value="admin"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="userForm.status" placeholder="选择状态">
                <el-option label="正常" :value="1"></el-option>
                <el-option label="禁用" :value="0"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="userForm.email" placeholder="请输入邮箱"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电话" prop="phone">
              <el-input v-model="userForm.phone" placeholder="请输入电话"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        
        <el-form-item label="密码" prop="password" v-if="dialogTitle === '添加用户'">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUser">确定</el-button>
      </span>
    </el-dialog>
    
    <!-- 用户详情对话框 -->
    <el-dialog
      title="用户详情"
      :visible.sync="detailDialogVisible"
      width="700px">
      <div v-if="selectedUser" class="user-detail">
        <div class="user-header">
          <div class="user-basic">
            <el-avatar :size="80" :src="selectedUser.avatar">
              <i class="el-icon-user-solid"></i>
            </el-avatar>
            <div class="user-info">
              <h3>{{ selectedUser.realName }}</h3>
              <p>用户名：{{ selectedUser.username }}</p>
              <p>学号/工号：{{ selectedUser.studentId }}</p>
              <p>角色：{{ getRoleText(selectedUser.role) }}</p>
            </div>
          </div>
          <div class="user-status">
            <el-tag :type="getStatusType(selectedUser.status)" size="medium">
              {{ getStatusText(selectedUser.status) }}
            </el-tag>
          </div>
        </div>
        
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="basic">
            <div class="info-content">
              <el-row :gutter="20">
                <el-col :span="12">
                  <div class="info-item">
                    <label>邮箱：</label>
                    <span>{{ selectedUser.email }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="info-item">
                    <label>电话：</label>
                    <span>{{ selectedUser.phone }}</span>
                  </div>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <div class="info-item">
                    <label>部门/专业：</label>
                    <span>{{ selectedUser.department }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="info-item">
                    <label>注册时间：</label>
                    <span>{{ selectedUser.createTime }}</span>
                  </div>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <div class="info-item">
                    <label>最后登录：</label>
                    <span>{{ selectedUser.lastLoginTime ? selectedUser.lastLoginTime.split('T')[0] : '-' }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="info-item">
                    <label>登录次数：</label>
                    <span>{{ selectedUser.loginCount || 0 }}</span>
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="活动记录" name="activities">
            <div class="activities-content">
              <el-timeline>
                <el-timeline-item
                  v-for="(activity, index) in selectedUser.activities"
                  :key="index"
                  :timestamp="activity.time">
                  <div class="activity-content">
                    <h4>{{ activity.title }}</h4>
                    <p>{{ activity.description }}</p>
                  </div>
                </el-timeline-item>
              </el-timeline>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { userApi } from '@/api/index'

export default {
  name: 'AdminUsers',
  data() {
    return {
      searchKeyword: '',
      roleFilter: '',
      statusFilter: '',
      currentPage: 1,
      pageSize: 20,
      totalUsers: 0,
      userDialogVisible: false,
      detailDialogVisible: false,
      dialogTitle: '添加用户',
      selectedUser: null,
      activeTab: 'basic',
      selectedUsers: [],
      loading: false,
      
      userForm: {
        username: '',
        realName: '',
        role: '',
        status: 1,
        email: '',
        phone: '',
        department: '',
        studentId: '',
        password: ''
      },
      
      userRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        realName: [
          { required: true, message: '请输入真实姓名', trigger: 'blur' }
        ],
        role: [
          { required: true, message: '请选择角色', trigger: 'change' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入电话', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
        ]
      },
      
      stats: {
        totalUsers: 0,
        students: 0,
        teachers: 0,
        admins: 0
      },
      
      users: []
    }
  },
  computed: {
    filteredUsers() {
      return this.users;
    }
  },
  mounted() {
    this.loadUserStats();
    this.loadUsers();
  },
  methods: {
    // 加载用户统计信息
    async loadUserStats() {
      try {
        const response = await userApi.getUserStats();
        if (response.code === 200) {
          this.stats = response.data;
        }
      } catch (error) {
        console.error('加载用户统计失败:', error);
        this.$message.error('加载用户统计失败');
      }
    },

    // 加载用户列表
    async loadUsers() {
      this.loading = true;
      try {
        const params = {
          page: this.currentPage,
          size: this.pageSize,
          role: this.roleFilter || undefined,
          status: this.statusFilter || undefined,
          keyword: this.searchKeyword || undefined
        };
        
        const response = await userApi.getUserList(params);
        if (response.code === 200) {
          this.users = response.data.records;
          this.totalUsers = response.data.total;
        }
      } catch (error) {
        console.error('加载用户列表失败:', error);
        this.$message.error('加载用户列表失败');
      } finally {
        this.loading = false;
      }
    },

    handleSearch() {
      this.currentPage = 1;
      this.loadUsers();
    },
    
    handleFilter() {
      this.currentPage = 1;
      this.loadUsers();
    },
    
    resetFilter() {
      this.searchKeyword = '';
      this.roleFilter = '';
      this.statusFilter = '';
      this.currentPage = 1;
      this.loadUsers();
    },
    
    handleSizeChange(val) {
      this.pageSize = val;
      this.currentPage = 1;
      this.loadUsers();
    },
    
    handleCurrentChange(val) {
      this.currentPage = val;
      this.loadUsers();
    },
    
    handleSelectionChange(selection) {
      this.selectedUsers = selection;
    },
    
    addUser() {
      this.dialogTitle = '添加用户';
      this.userForm = {
        username: '',
        realName: '',
        role: '',
        status: 1,
        email: '',
        phone: '',
        department: '',
        studentId: '',
        password: ''
      };
      this.userDialogVisible = true;
    },
    
    editUser(user) {
      this.dialogTitle = '编辑用户';
      this.userForm = { 
        id: user.id,
        username: user.username,
        realName: user.realName,
        role: user.role,
        status: user.status,
        email: user.email,
        phone: user.phone,
        department: user.department,
        studentId: user.studentId,
        password: ''
      };
      this.userDialogVisible = true;
    },
    
    async submitUser() {
      this.$refs.userForm.validate(async (valid) => {
        if (valid) {
          try {
          if (this.dialogTitle === '添加用户') {
            // 添加新用户
              const response = await userApi.createUser(this.userForm);
              if (response.code === 200) {
            this.$message.success('用户添加成功！');
                this.userDialogVisible = false;
                this.loadUsers();
                this.loadUserStats();
              } else {
                this.$message.error(response.message || '用户添加失败');
              }
          } else {
            // 编辑用户
              const response = await userApi.updateUser(this.userForm.id, this.userForm);
              if (response.code === 200) {
              this.$message.success('用户信息更新成功！');
                this.userDialogVisible = false;
                this.loadUsers();
              } else {
                this.$message.error(response.message || '用户信息更新失败');
            }
          }
          } catch (error) {
            console.error('提交用户信息失败:', error);
            this.$message.error('操作失败，请重试');
          }
        }
      });
    },
    
    viewUserDetail(user) {
      this.selectedUser = user;
      this.detailDialogVisible = true;
    },
    
    async resetPassword(user) {
      this.$confirm('确定要重置该用户的密码吗？', '确认重置', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await userApi.resetPassword(user.id, { newPassword: '123456' });
          if (response.code === 200) {
            this.$message.success(`用户 ${user.realName} 的密码已重置为默认密码 123456`);
          } else {
            this.$message.error(response.message || '密码重置失败');
          }
        } catch (error) {
          console.error('重置密码失败:', error);
          this.$message.error('密码重置失败');
        }
      });
    },
    
    async toggleUserStatus(user) {
      const action = user.status === 1 ? '禁用' : '启用';
      this.$confirm(`确定要${action}该用户吗？`, `确认${action}`, {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await userApi.toggleUserStatus(user.id);
          if (response.code === 200) {
            this.$message.success(`用户 ${user.realName} 已${action}`);
            this.loadUsers();
            this.loadUserStats();
          } else {
            this.$message.error(response.message || '状态修改失败');
          }
        } catch (error) {
          console.error('修改用户状态失败:', error);
          this.$message.error('状态修改失败');
        }
      });
    },
    
    async batchOperation() {
      if (this.selectedUsers.length === 0) {
        this.$message.warning('请先选择要操作的用户');
        return;
      }
      
      this.$confirm(`确定要删除选中的 ${this.selectedUsers.length} 个用户吗？`, '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const userIds = this.selectedUsers.map(user => user.id);
          const response = await userApi.batchDeleteUsers(userIds);
          if (response.code === 200) {
            this.$message.success('批量删除用户成功');
            this.loadUsers();
            this.loadUserStats();
          } else {
            this.$message.error(response.message || '批量删除失败');
          }
        } catch (error) {
          console.error('批量删除用户失败:', error);
          this.$message.error('批量删除失败');
        }
      });
    },
    
    exportUsers() {
      this.$message.info('用户导出功能开发中...');
    },
    
    importUsers() {
      this.$message.info('用户导入功能开发中...');
    },
    
    getRoleType(role) {
      const typeMap = {
        'student': 'primary',
        'teacher': 'success',
        'admin': 'warning'
      };
      return typeMap[role] || 'info';
    },
    
    getRoleText(role) {
      const textMap = {
        'student': '学生',
        'teacher': '教师',
        'admin': '管理员'
      };
      return textMap[role] || '未知';
    },
    
    getStatusType(status) {
      const typeMap = {
        1: 'success',
        0: 'danger'
      };
      return typeMap[status] || 'info';
    },
    
    getStatusText(status) {
      const textMap = {
        1: '正常',
        0: '禁用'
      };
      return textMap[status] || '未知';
    },
    
    handleClose(done) {
      this.$refs.userForm.resetFields();
      done();
    }
  }
}
</script>

<style scoped>
.admin-users {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
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

.add-user-btn {
  margin-top: 10px;
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

.users-section {
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

.user-detail {
  max-height: 500px;
  overflow-y: auto;
}

.user-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.user-basic {
  display: flex;
  align-items: center;
}

.user-info {
  margin-left: 15px;
}

.user-info h3 {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 5px 0;
}

.user-info p {
  color: #7f8c8d;
  margin: 0 0 3px 0;
  font-size: 14px;
}

.info-content {
  padding: 20px 0;
}

.info-item {
  margin-bottom: 15px;
}

.info-item label {
  font-weight: 500;
  color: #606266;
  margin-right: 10px;
}

.info-item span {
  color: #2c3e50;
}

.activities-content {
  padding: 20px 0;
}

.activity-content h4 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 5px 0;
}

.activity-content p {
  color: #606266;
  margin: 0;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .add-user-btn {
    margin-top: 15px;
  }
  
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
  
  .user-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .user-basic {
    margin-bottom: 15px;
  }
}
</style>
