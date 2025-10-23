<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '240px'" class="sidebar">
      <div class="logo">
        <div class="logo-icon">🎓</div>
        <span v-show="!isCollapse" class="logo-text">选题系统</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="true"
        router
        class="sidebar-menu">
        
        <!-- 学生菜单 -->
        <template v-if="userRole === 'student'">
          <el-menu-item index="/student/topics">
            <i class="el-icon-document"></i>
            <span slot="title">选题列表</span>
          </el-menu-item>
          <el-menu-item index="/student/my-topic">
            <i class="el-icon-star-on"></i>
            <span slot="title">我的选题</span>
          </el-menu-item>
          <el-menu-item index="/student/profile">
            <i class="el-icon-user"></i>
            <span slot="title">个人信息</span>
          </el-menu-item>
        </template>
        
        <!-- 教师菜单 -->
        <template v-if="userRole === 'teacher'">
          <el-menu-item index="/teacher/topics">
            <i class="el-icon-document"></i>
            <span slot="title">课题管理</span>
          </el-menu-item>
          <el-menu-item index="/teacher/students">
            <i class="el-icon-user"></i>
            <span slot="title">学生管理</span>
          </el-menu-item>
          <el-menu-item index="/teacher/reports">
            <i class="el-icon-data-line"></i>
            <span slot="title">统计报表</span>
          </el-menu-item>
          <el-menu-item index="/teacher/profile">
            <i class="el-icon-setting"></i>
            <span slot="title">个人设置</span>
          </el-menu-item>
        </template>
        
        <!-- 管理员菜单 -->
        <template v-if="userRole === 'admin'">
          <el-menu-item index="/admin/dashboard">
            <i class="el-icon-data-board"></i>
            <span slot="title">数据概览</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <i class="el-icon-user"></i>
            <span slot="title">用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/topics">
            <i class="el-icon-document"></i>
            <span slot="title">课题管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/system">
            <i class="el-icon-setting"></i>
            <span slot="title">系统设置</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    
    <!-- 主内容区 -->
    <el-container class="main-container">
      <!-- 顶部栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-button 
            type="text" 
            @click="toggleCollapse"
            class="collapse-btn">
            <i :class="isCollapse ? 'el-icon-s-unfold' : 'el-icon-s-fold'"></i>
          </el-button>
          
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item 
              v-for="item in breadcrumbList" 
              :key="item.path"
              :to="item.path">
              {{ item.name }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <img src="@/assets/vnollx.jpg" alt="头像" class="avatar">
              <span class="username">{{ username }}</span>
              <i class="el-icon-arrow-down"></i>
            </div>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="profile">个人资料</el-dropdown-item>
              <el-dropdown-item command="settings">系统设置</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 内容区域 -->
      <el-main class="main-content">
        <router-view></router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script>
import { authApi } from '@/api/index.js'

export default {
  name: 'Layout',
  data() {
    return {
      isCollapse: false,
      userRole: this.getUserRoleFromRoute(),
      username: '用户',
      breadcrumbList: []
    }
  },
  async mounted() {
    await this.loadUserInfo();
  },
  computed: {
    activeMenu() {
      return this.$route.path;
    }
  },
  watch: {
    $route: {
      handler(route) {
        this.updateBreadcrumb(route);
        this.userRole = this.getUserRoleFromRoute();
      },
      immediate: true
    }
  },
  methods: {
    getUserRoleFromRoute() {
      const path = this.$route.path;
      if (path.startsWith('/student')) {
        return 'student';
      } else if (path.startsWith('/teacher')) {
        return 'teacher';
      } else if (path.startsWith('/admin')) {
        return 'admin';
      }
      // 兼容旧路由
      if (path.includes('/student/')) {
        return 'student';
      } else if (path.includes('/teacher/')) {
        return 'teacher';
      } else if (path.includes('/admin/')) {
        return 'admin';
      }
      // 如果路径是 /layout 或 /layout/，根据用户信息判断角色
      if (path === '/layout' || path === '/layout/') {
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
        return userInfo.role || 'admin'; // 默认管理员
      }
      return 'admin'; // 默认管理员
    },
    
    async loadUserInfo() {
      try {
        const response = await authApi.getUserInfo();
        if (response.code === 200 && response.data) {
          this.username = response.data.realName || response.data.username || '用户';
        }
      } catch (error) {
        console.error('获取用户信息失败:', error);
        this.username = '用户';
      }
    },
    
    toggleCollapse() {
      this.isCollapse = !this.isCollapse;
    },
    
    updateBreadcrumb(route) {
      const matched = route.matched.filter(item => item.name);
      this.breadcrumbList = matched.map(item => ({
        name: item.name,
        path: item.path
      }));
    },
    
    handleCommand(command) {
      switch (command) {
        case 'profile':
          this.$message.info('个人资料功能开发中...');
          break;
        case 'settings':
          this.$message.info('系统设置功能开发中...');
          break;
        case 'logout':
          this.$confirm('确定要退出登录吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            // 清除用户信息
            localStorage.removeItem('token');
            localStorage.removeItem('userInfo');
            this.$router.push('/login');
            this.$message.success('已退出登录');
          });
          break;
      }
    }
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  display: flex;
}

.sidebar {
  background: #ffffff;
  border-right: 1px solid #e6e6e6;
  transition: width 0.3s ease;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  color: #2c3e50;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 1px solid #e6e6e6;
}

.logo-icon {
  font-size: 32px;
  margin-right: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  white-space: nowrap;
}

.sidebar-menu {
  border: none;
  background: #ffffff;
}

.sidebar-menu >>> .el-menu-item {
  color: #606266;
  height: 50px;
  line-height: 50px;
}

.sidebar-menu >>> .el-menu-item:hover {
  background: #f5f7fa;
  color: #409eff;
}

.sidebar-menu >>> .el-menu-item.is-active {
  background: #ecf5ff;
  color: #409eff;
  border-right: 3px solid #409eff;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.header {
  background: white;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
}

.collapse-btn {
  font-size: 18px;
  margin-right: 20px;
  color: #606266;
}

.collapse-btn:hover {
  color: #409eff;
}

.breadcrumb {
  font-size: 14px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background 0.3s ease;
}

.user-info:hover {
  background: #f5f7fa;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: 8px;
}

.username {
  margin-right: 8px;
  font-size: 14px;
  color: #606266;
}

.main-content {
  background: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    height: 100vh;
    z-index: 1000;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
  }
  
  .sidebar.show {
    transform: translateX(0);
  }
  
  .main-container {
    margin-left: 0;
  }
  
  .header {
    padding: 0 15px;
  }
  
  .main-content {
    padding: 15px;
  }
}
</style>
