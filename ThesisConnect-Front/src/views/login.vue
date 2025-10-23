<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1 class="system-title">毕业论文选题双向选择系统</h1>
        <p class="system-subtitle">Graduation Thesis Topic Selection System</p>
      </div>
      
      <div class="login-form">
        <el-form :model="loginForm" :rules="rules" ref="loginForm" label-width="0">
          <el-form-item prop="role">
            <el-select v-model="loginForm.role" placeholder="请选择登录身份" class="role-select">
              <el-option label="学生" value="student"></el-option>
              <el-option label="教师" value="teacher"></el-option>
              <el-option label="管理员" value="admin"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item prop="username">
            <el-input 
              v-model="loginForm.username" 
              placeholder="请输入用户名"
              prefix-icon="el-icon-user"
              class="login-input">
            </el-input>
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="请输入密码"
              prefix-icon="el-icon-lock"
              class="login-input"
              @keyup.enter.native="handleLogin">
            </el-input>
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              class="login-btn" 
              :loading="loading"
              @click="handleLogin">
              {{ loading ? '登录中...' : '登录' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <div class="login-footer">
        <p class="register-link">
          还没有账号？<a href="#" @click="goToRegister">立即注册</a>
        </p>
        <p class="copyright">© 2024 毕业论文选题系统 版权所有</p>
      </div>
    </div>
    
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      loading: false,
      loginForm: {
        role: '',
        username: '',
        password: ''
      },
      rules: {
        role: [
          { required: true, message: '请选择登录身份', trigger: 'change' }
        ],
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate((valid) => {
        if (valid) {
          this.loading = true;
          
          // 调用后端登录API
          this.$http.post('/api/auth/login', this.loginForm)
            .then(response => {
              this.loading = false;
              
              if (response.code === 200) {
                // 保存token和用户信息
                localStorage.setItem('token', response.data.token);
                localStorage.setItem('user', JSON.stringify(response.data.user));
                localStorage.setItem('role', response.data.role);
                
                // 根据角色跳转到不同页面
                if (this.loginForm.role === 'student') {
                  this.$router.push('/student/topics');
                } else if (this.loginForm.role === 'teacher') {
                  this.$router.push('/teacher/topics');
                } else if (this.loginForm.role === 'admin') {
                  this.$router.push('/admin/dashboard');
                }
                
                this.$message.success('登录成功！');
              } else {
                this.$message.error(response.message || '登录失败');
              }
            })
            .catch(error => {
              this.loading = false;
              this.$message.error(error.message || '登录失败，请检查网络连接');
            });
        }
      });
    },
    
    goToRegister() {
      this.$router.push('/register');
    }
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.login-box {
  width: 400px;
  background: #ffffff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 10;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.system-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
  letter-spacing: 1px;
}

.system-subtitle {
  font-size: 14px;
  color: #7f8c8d;
  margin: 0;
  font-style: italic;
}

.login-form {
  margin-bottom: 20px;
}

.role-select,
.login-input {
  width: 100%;
}

.login-input {
  height: 45px;
}

.login-input >>> .el-input__inner {
  height: 45px;
  border-radius: 8px;
  border: 1px solid #e1e8ed;
  font-size: 14px;
  transition: all 0.3s ease;
}

.login-input >>> .el-input__inner:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.role-select >>> .el-input__inner {
  height: 45px;
  border-radius: 8px;
  border: 1px solid #e1e8ed;
}

.login-btn {
  width: 100%;
  height: 45px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  background: #409eff;
  border: none;
  transition: all 0.3s ease;
}

.login-btn:hover {
  background: #66b1ff;
}

.login-footer {
  text-align: center;
}

.register-link {
  margin-bottom: 10px;
}

.register-link a {
  color: #409eff;
  text-decoration: none;
  font-weight: 500;
}

.register-link a:hover {
  text-decoration: underline;
}

.copyright {
  font-size: 12px;
  color: #95a5a6;
  margin: 0;
}


/* 响应式设计 */
@media (max-width: 480px) {
  .login-box {
    width: 90%;
    padding: 30px 20px;
  }
  
  .system-title {
    font-size: 20px;
  }
}
</style>