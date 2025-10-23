<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h1 class="system-title">用户注册</h1>
        <p class="system-subtitle">User Registration</p>
      </div>
      
      <div class="register-form">
        <el-form :model="registerForm" :rules="rules" ref="registerForm" label-width="0">
          <el-form-item prop="role">
            <el-select v-model="registerForm.role" placeholder="请选择用户类型" class="role-select">
              <el-option label="学生" value="student"></el-option>
              <el-option label="教师" value="teacher"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item prop="username">
            <el-input 
              v-model="registerForm.username" 
              placeholder="请输入用户名"
              prefix-icon="el-icon-user"
              class="register-input">
            </el-input>
          </el-form-item>
          
          <el-form-item prop="realName">
            <el-input 
              v-model="registerForm.realName" 
              placeholder="请输入真实姓名"
              prefix-icon="el-icon-user-solid"
              class="register-input">
            </el-input>
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="registerForm.password" 
              type="password" 
              placeholder="请输入密码"
              prefix-icon="el-icon-lock"
              class="register-input">
            </el-input>
          </el-form-item>
          
          <el-form-item prop="confirmPassword">
            <el-input 
              v-model="registerForm.confirmPassword" 
              type="password" 
              placeholder="请确认密码"
              prefix-icon="el-icon-lock"
              class="register-input">
            </el-input>
          </el-form-item>
          
          <el-form-item prop="email">
            <el-input 
              v-model="registerForm.email" 
              placeholder="请输入邮箱"
              prefix-icon="el-icon-message"
              class="register-input">
            </el-input>
          </el-form-item>
          
          <el-form-item prop="phone">
            <el-input 
              v-model="registerForm.phone" 
              placeholder="请输入手机号"
              prefix-icon="el-icon-phone"
              class="register-input">
            </el-input>
          </el-form-item>
          
          <el-form-item prop="department">
            <el-input 
              v-model="registerForm.department" 
              placeholder="请输入院系"
              prefix-icon="el-icon-office-building"
              class="register-input">
            </el-input>
          </el-form-item>
          
          <el-form-item prop="studentId" v-if="registerForm.role === 'student'">
            <el-input 
              v-model="registerForm.studentId" 
              placeholder="请输入学号"
              prefix-icon="el-icon-postcard"
              class="register-input">
            </el-input>
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              class="register-btn" 
              :loading="loading"
              @click="handleRegister">
              {{ loading ? '注册中...' : '注册' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <div class="register-footer">
        <p class="login-link">
          已有账号？<a href="#" @click="goToLogin">立即登录</a>
        </p>
        <p class="copyright">© 2024 毕业论文选题系统 版权所有</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Register',
  data() {
    return {
      loading: false,
      registerForm: {
        role: '',
        username: '',
        realName: '',
        password: '',
        confirmPassword: '',
        email: '',
        phone: '',
        department: '',
        studentId: ''
      },
      rules: {
        role: [
          { required: true, message: '请选择用户类型', trigger: 'change' }
        ],
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
          { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
        ],
        realName: [
          { required: true, message: '请输入真实姓名', trigger: 'blur' },
          { min: 2, max: 10, message: '姓名长度在 2 到 10 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认密码', trigger: 'blur' },
          { validator: this.validateConfirmPassword, trigger: 'blur' }
        ],
        email: [
          { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
        ],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
        ],
        department: [
          { required: true, message: '请输入院系', trigger: 'blur' }
        ],
        studentId: [
          { required: true, message: '请输入学号', trigger: 'blur' },
          { pattern: /^\d{8,12}$/, message: '学号应为8-12位数字', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    validateConfirmPassword(rule, value, callback) {
      if (value === '') {
        callback(new Error('请确认密码'));
      } else if (value !== this.registerForm.password) {
        callback(new Error('两次输入的密码不一致'));
      } else {
        callback();
      }
    },
    
    handleRegister() {
      this.$refs.registerForm.validate((valid) => {
        if (valid) {
          this.loading = true;
          
          // 调用后端注册API
          this.$http.post('/api/auth/register', this.registerForm)
            .then(response => {
              this.loading = false;
              
              if (response.code === 200) {
                this.$message.success('注册成功！');
                // 注册成功后跳转到登录页面
                this.$router.push('/login');
              } else {
                this.$message.error(response.message || '注册失败');
              }
            })
            .catch(error => {
              this.loading = false;
              this.$message.error(error.message || '注册失败，请检查网络连接');
            });
        }
      });
    },
    
    goToLogin() {
      this.$router.push('/login');
    }
  }
}
</script>

<style scoped>
.register-container {
  height: 100vh;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  padding: 20px 0;
}

.register-box {
  width: 450px;
  background: #ffffff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 10;
  max-height: 90vh;
  overflow-y: auto;
}

.register-header {
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

.register-form {
  margin-bottom: 20px;
}

.role-select,
.register-input {
  width: 100%;
}

.register-input {
  height: 40px;
}

.register-input >>> .el-input__inner {
  height: 40px;
  border-radius: 8px;
  border: 1px solid #e1e8ed;
  font-size: 14px;
  transition: all 0.3s ease;
}

.register-input >>> .el-input__inner:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.role-select >>> .el-input__inner {
  height: 40px;
  border-radius: 8px;
  border: 1px solid #e1e8ed;
}

.register-btn {
  width: 100%;
  height: 45px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  background: #409eff;
  border: none;
  transition: all 0.3s ease;
}

.register-btn:hover {
  background: #66b1ff;
}

.register-footer {
  text-align: center;
}

.login-link {
  margin-bottom: 10px;
}

.login-link a {
  color: #409eff;
  text-decoration: none;
  font-weight: 500;
}

.login-link a:hover {
  text-decoration: underline;
}

.copyright {
  font-size: 12px;
  color: #95a5a6;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .register-box {
    width: 90%;
    padding: 30px 20px;
  }
  
  .system-title {
    font-size: 20px;
  }
}
</style>
