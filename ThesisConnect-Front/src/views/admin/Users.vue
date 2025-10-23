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
            <el-option label="正常" value="active"></el-option>
            <el-option label="禁用" value="disabled"></el-option>
            <el-option label="待审核" value="pending"></el-option>
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
          @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="avatar" label="头像" width="80">
            <template slot-scope="scope">
              <el-avatar :size="40" :src="scope.row.avatar">
                <i class="el-icon-user-solid"></i>
              </el-avatar>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="姓名" width="100"></el-table-column>
          <el-table-column prop="userId" label="用户ID" width="120"></el-table-column>
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
          <el-table-column prop="lastLogin" label="最后登录" width="120"></el-table-column>
          <el-table-column prop="createTime" label="注册时间" width="120"></el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template slot-scope="scope">
              <el-button type="text" size="small" @click="viewUserDetail(scope.row)">详情</el-button>
              <el-button type="text" size="small" @click="editUser(scope.row)">编辑</el-button>
              <el-button type="text" size="small" @click="resetPassword(scope.row)">重置密码</el-button>
              <el-button type="text" size="small" @click="toggleUserStatus(scope.row)">
                {{ scope.row.status === 'active' ? '禁用' : '启用' }}
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
            <el-form-item label="姓名" prop="name">
              <el-input v-model="userForm.name" placeholder="请输入姓名"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户ID" prop="userId">
              <el-input v-model="userForm.userId" placeholder="请输入用户ID"></el-input>
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
                <el-option label="正常" value="active"></el-option>
                <el-option label="禁用" value="disabled"></el-option>
                <el-option label="待审核" value="pending"></el-option>
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
        
        <el-form-item label="部门/专业" prop="department">
          <el-input v-model="userForm.department" placeholder="请输入部门或专业"></el-input>
        </el-form-item>
        
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
              <h3>{{ selectedUser.name }}</h3>
              <p>ID：{{ selectedUser.userId }}</p>
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
                    <span>{{ selectedUser.lastLogin }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="info-item">
                    <label>登录次数：</label>
                    <span>{{ selectedUser.loginCount }}</span>
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
      
      userForm: {
        name: '',
        userId: '',
        role: '',
        status: 'active',
        email: '',
        phone: '',
        department: '',
        password: ''
      },
      
      userRules: {
        name: [
          { required: true, message: '请输入姓名', trigger: 'blur' }
        ],
        userId: [
          { required: true, message: '请输入用户ID', trigger: 'blur' }
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
        totalUsers: 1256,
        students: 1100,
        teachers: 150,
        admins: 6
      },
      
      users: [
        {
          id: 1,
          name: '张三',
          userId: '2021001001',
          role: 'student',
          department: '计算机科学与技术',
          email: 'zhangsan@example.com',
          phone: '13800138000',
          status: 'active',
          lastLogin: '2024-02-15',
          createTime: '2024-01-15',
          loginCount: 45,
          avatar: '',
          activities: [
            { title: '登录系统', description: '用户登录系统', time: '2024-02-15 14:30' },
            { title: '选择课题', description: '选择了课题"基于深度学习的图像识别系统"', time: '2024-02-15 13:45' },
            { title: '上传文档', description: '上传了需求分析文档', time: '2024-02-15 12:20' }
          ]
        },
        {
          id: 2,
          name: '李教授',
          userId: 'T2021001',
          role: 'teacher',
          department: '计算机科学与技术学院',
          email: 'li.professor@university.edu.cn',
          phone: '13800138001',
          status: 'active',
          lastLogin: '2024-02-15',
          createTime: '2024-01-10',
          loginCount: 78,
          avatar: '',
          activities: [
            { title: '登录系统', description: '用户登录系统', time: '2024-02-15 14:30' },
            { title: '发布课题', description: '发布了新课题"基于深度学习的图像识别系统"', time: '2024-02-15 13:45' },
            { title: '审核文档', description: '审核了学生提交的文档', time: '2024-02-15 12:20' }
          ]
        },
        {
          id: 3,
          name: '王五',
          userId: '2021001003',
          role: 'student',
          department: '软件工程',
          email: 'wangwu@example.com',
          phone: '13800138002',
          status: 'pending',
          lastLogin: '2024-02-14',
          createTime: '2024-02-14',
          loginCount: 3,
          avatar: '',
          activities: [
            { title: '注册账户', description: '用户注册了账户', time: '2024-02-14 16:30' },
            { title: '登录系统', description: '用户登录系统', time: '2024-02-14 16:35' }
          ]
        }
      ]
    }
  },
  computed: {
    filteredUsers() {
      let filtered = this.users;
      
      // 搜索过滤
      if (this.searchKeyword) {
        filtered = filtered.filter(user => 
          user.name.toLowerCase().includes(this.searchKeyword.toLowerCase()) ||
          user.userId.toLowerCase().includes(this.searchKeyword.toLowerCase())
        );
      }
      
      // 角色过滤
      if (this.roleFilter) {
        filtered = filtered.filter(user => user.role === this.roleFilter);
      }
      
      // 状态过滤
      if (this.statusFilter) {
        filtered = filtered.filter(user => user.status === this.statusFilter);
      }
      
      this.totalUsers = filtered.length;
      
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
      this.roleFilter = '';
      this.statusFilter = '';
      this.currentPage = 1;
    },
    
    handleSizeChange(val) {
      this.pageSize = val;
      this.currentPage = 1;
    },
    
    handleCurrentChange(val) {
      this.currentPage = val;
    },
    
    handleSelectionChange(selection) {
      this.selectedUsers = selection;
    },
    
    addUser() {
      this.dialogTitle = '添加用户';
      this.userForm = {
        name: '',
        userId: '',
        role: '',
        status: 'active',
        email: '',
        phone: '',
        department: '',
        password: ''
      };
      this.userDialogVisible = true;
    },
    
    editUser(user) {
      this.dialogTitle = '编辑用户';
      this.userForm = { ...user };
      this.userDialogVisible = true;
    },
    
    submitUser() {
      this.$refs.userForm.validate((valid) => {
        if (valid) {
          if (this.dialogTitle === '添加用户') {
            // 添加新用户
            const newUser = {
              ...this.userForm,
              id: Date.now(),
              lastLogin: '-',
              createTime: new Date().toISOString().split('T')[0],
              loginCount: 0,
              avatar: '',
              activities: []
            };
            this.users.unshift(newUser);
            this.stats.totalUsers++;
            this.$message.success('用户添加成功！');
          } else {
            // 编辑用户
            const index = this.users.findIndex(user => user.id === this.userForm.id);
            if (index > -1) {
              this.users.splice(index, 1, this.userForm);
              this.$message.success('用户信息更新成功！');
            }
          }
          this.userDialogVisible = false;
        }
      });
    },
    
    viewUserDetail(user) {
      this.selectedUser = user;
      this.detailDialogVisible = true;
    },
    
    resetPassword(user) {
      this.$confirm('确定要重置该用户的密码吗？', '确认重置', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$message.success(`用户 ${user.name} 的密码已重置为默认密码`);
      });
    },
    
    toggleUserStatus(user) {
      const action = user.status === 'active' ? '禁用' : '启用';
      this.$confirm(`确定要${action}该用户吗？`, `确认${action}`, {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        user.status = user.status === 'active' ? 'disabled' : 'active';
        this.$message.success(`用户 ${user.name} 已${action}`);
      });
    },
    
    batchOperation() {
      if (this.selectedUsers.length === 0) {
        this.$message.warning('请先选择要操作的用户');
        return;
      }
      this.$message.info('批量操作功能开发中...');
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
        'active': 'success',
        'disabled': 'danger',
        'pending': 'warning'
      };
      return typeMap[status] || 'info';
    },
    
    getStatusText(status) {
      const textMap = {
        'active': '正常',
        'disabled': '禁用',
        'pending': '待审核'
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
