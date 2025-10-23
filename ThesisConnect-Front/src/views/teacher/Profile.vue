<template>
  <div class="teacher-profile">
    <div class="page-header">
      <h2 class="page-title">个人设置</h2>
      <p class="page-desc">管理您的个人信息和系统偏好设置</p>
    </div>
    
    <el-row :gutter="20">
      <!-- 左侧：个人信息卡片 -->
      <el-col :span="8">
        <div class="profile-card">
          <div class="profile-header">
            <div class="avatar-section">
              <el-avatar :size="80" :src="userInfo.avatar">
                <i class="el-icon-user-solid"></i>
              </el-avatar>
              <el-button type="text" @click="changeAvatar" class="change-avatar-btn">
                更换头像
              </el-button>
            </div>
            <div class="user-basic-info">
              <h3>{{ userInfo.name }}</h3>
              <p class="user-title">{{ userInfo.title }}</p>
              <p class="user-department">{{ userInfo.department }}</p>
            </div>
          </div>
          
          <div class="profile-stats">
            <div class="stat-item">
              <span class="stat-number">{{ userInfo.topicsCount }}</span>
              <span class="stat-label">发布课题</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ userInfo.studentsCount }}</span>
              <span class="stat-label">指导学生</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ userInfo.rating }}</span>
              <span class="stat-label">平均评分</span>
            </div>
          </div>
        </div>
        
        <!-- 快捷操作 -->
        <div class="quick-actions">
          <h4>快捷操作</h4>
          <el-button type="primary" @click="editProfile" class="action-btn">
            <i class="el-icon-edit"></i>
            编辑资料
          </el-button>
          <el-button @click="changePassword" class="action-btn">
            <i class="el-icon-lock"></i>
            修改密码
          </el-button>
          <el-button @click="exportData" class="action-btn">
            <i class="el-icon-download"></i>
            导出数据
          </el-button>
        </div>
      </el-col>
      
      <!-- 右侧：详细信息 -->
      <el-col :span="16">
        <el-tabs v-model="activeTab" type="card">
          <!-- 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <div class="info-section">
              <el-form :model="userInfo" label-width="100px" class="info-form">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="姓名">
                      <el-input v-model="userInfo.name" :disabled="!isEditing"></el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="工号">
                      <el-input v-model="userInfo.teacherId" disabled></el-input>
                    </el-form-item>
                  </el-col>
                </el-row>
                
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="性别">
                      <el-select v-model="userInfo.gender" :disabled="!isEditing">
                        <el-option label="男" value="male"></el-option>
                        <el-option label="女" value="female"></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="出生日期">
                      <el-date-picker
                        v-model="userInfo.birthday"
                        type="date"
                        placeholder="选择日期"
                        :disabled="!isEditing">
                      </el-date-picker>
                    </el-form-item>
                  </el-col>
                </el-row>
                
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="职称">
                      <el-input v-model="userInfo.title" :disabled="!isEditing"></el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="所属学院">
                      <el-input v-model="userInfo.department" :disabled="!isEditing"></el-input>
                    </el-form-item>
                  </el-col>
                </el-row>
                
                <el-form-item label="联系电话">
                  <el-input v-model="userInfo.phone" :disabled="!isEditing"></el-input>
                </el-form-item>
                
                <el-form-item label="邮箱">
                  <el-input v-model="userInfo.email" :disabled="!isEditing"></el-input>
                </el-form-item>
                
                <el-form-item label="办公地址">
                  <el-input v-model="userInfo.office" :disabled="!isEditing"></el-input>
                </el-form-item>
                
                <el-form-item label="个人简介">
                  <el-input v-model="userInfo.bio" type="textarea" :disabled="!isEditing"></el-input>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>
          
          <!-- 学术信息 -->
          <el-tab-pane label="学术信息" name="academic">
            <div class="info-section">
              <el-form :model="academicInfo" label-width="120px" class="info-form">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="学历">
                      <el-select v-model="academicInfo.education" :disabled="!isEditing">
                        <el-option label="本科" value="bachelor"></el-option>
                        <el-option label="硕士" value="master"></el-option>
                        <el-option label="博士" value="phd"></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="毕业院校">
                      <el-input v-model="academicInfo.school" :disabled="!isEditing"></el-input>
                    </el-form-item>
                  </el-col>
                </el-row>
                
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="专业方向">
                      <el-input v-model="academicInfo.major" :disabled="!isEditing"></el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="入职时间">
                      <el-date-picker
                        v-model="academicInfo.hireDate"
                        type="date"
                        placeholder="选择日期"
                        :disabled="!isEditing">
                      </el-date-picker>
                    </el-form-item>
                  </el-col>
                </el-row>
                
                <el-form-item label="研究领域">
                  <el-input 
                    v-model="academicInfo.researchAreas" 
                    type="textarea" 
                    :rows="3"
                    :disabled="!isEditing">
                  </el-input>
                </el-form-item>
                
                <el-form-item label="主要成果">
                  <el-input 
                    v-model="academicInfo.achievements" 
                    type="textarea" 
                    :rows="3"
                    :disabled="!isEditing">
                  </el-input>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>
          
          <!-- 系统设置 -->
          <el-tab-pane label="系统设置" name="settings">
            <div class="info-section">
              <div class="setting-item">
                <div class="setting-content">
                  <h4>邮件通知</h4>
                  <p>接收系统邮件通知和提醒</p>
                </div>
                <el-switch v-model="settings.emailNotification"></el-switch>
              </div>
              
              <div class="setting-item">
                <div class="setting-content">
                  <h4>短信通知</h4>
                  <p>接收重要事件的短信通知</p>
                </div>
                <el-switch v-model="settings.smsNotification"></el-switch>
              </div>
              
              <div class="setting-item">
                <div class="setting-content">
                  <h4>自动审核</h4>
                  <p>自动审核学生提交的文档</p>
                </div>
                <el-switch v-model="settings.autoReview"></el-switch>
              </div>
              
              <div class="setting-item">
                <div class="setting-content">
                  <h4>课题推荐</h4>
                  <p>根据学生兴趣推荐相关课题</p>
                </div>
                <el-switch v-model="settings.topicRecommendation"></el-switch>
              </div>
              
              <div class="setting-item">
                <div class="setting-content">
                  <h4>数据导出</h4>
                  <p>允许导出个人数据和学习记录</p>
                </div>
                <el-switch v-model="settings.dataExport"></el-switch>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
        
        <!-- 操作按钮 -->
        <div class="form-actions" v-if="isEditing">
          <el-button @click="cancelEdit">取消</el-button>
          <el-button type="primary" @click="saveProfile">保存</el-button>
        </div>
      </el-col>
    </el-row>
    
    <!-- 修改密码对话框 -->
    <el-dialog
      title="修改密码"
      :visible.sync="passwordDialogVisible"
      width="400px">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordForm" label-width="100px">
        <el-form-item label="当前密码" prop="currentPassword">
          <el-input v-model="passwordForm.currentPassword" type="password"></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPassword">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { authApi, userApi } from '@/api'

export default {
  name: 'TeacherProfile',
  data() {
    return {
      activeTab: 'basic',
      isEditing: false,
      passwordDialogVisible: false,
      
      passwordForm: {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      
      passwordRules: {
        currentPassword: [
          { required: true, message: '请输入当前密码', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          { validator: this.validateConfirmPassword, trigger: 'blur' }
        ]
      },
      
      settings: {
        emailNotification: true,
        smsNotification: false,
        autoReview: false,
        topicRecommendation: true,
        dataExport: true
      },
      
      // 用户基本信息
      userInfo: {
        name: '',
        teacherId: '',
        gender: '',
        birthday: '',
        title: '',
        department: '',
        phone: '',
        email: '',
        office: '',
        bio: '',
        avatar: '',
        topicsCount: 0,
        studentsCount: 0,
        rating: 0
      },
      
      // 学术信息
      academicInfo: {
        education: '',
        school: '',
        major: '',
        hireDate: '',
        researchAreas: '',
        achievements: ''
      }
    }
  },
  async mounted() {
    await this.loadUserInfo()
  },
  methods: {
    // 加载用户信息
    async loadUserInfo() {
      try {
        const response = await authApi.getUserInfo()
        if (response.code === 200 && response.data) {
          const userData = response.data
          // 更新用户基本信息
          this.userInfo = {
            name: userData.realName || userData.username || '',
            teacherId: userData.studentId || '',
            gender: userData.gender || '',
            birthday: userData.birthday || '',
            title: userData.title || '',
            department: userData.department || '',
            phone: userData.phone || '',
            email: userData.email || '',
            office: userData.address || '',
            bio: userData.bio || '',
            avatar: userData.avatar || '',
            topicsCount: userData.topicsCount || 0,
            studentsCount: userData.studentsCount || 0,
            rating: userData.rating || 0
          }
          
          // 更新学术信息
          this.academicInfo = {
            education: userData.education || '',
            school: userData.school || '',
            major: userData.major || '',
            hireDate: userData.hireDate || '',
            researchAreas: userData.researchAreas || '',
            achievements: userData.achievements || ''
          }
        } else {
          this.$message.error('获取用户信息失败')
        }
      } catch (error) {
        console.error('加载用户信息失败:', error)
        this.$message.error('加载用户信息失败，请稍后重试')
      }
    },
    validateConfirmPassword(rule, value, callback) {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入密码不一致'));
      } else {
        callback();
      }
    },
    
    editProfile() {
      this.isEditing = true;
    },
    
    cancelEdit() {
      this.isEditing = false;
      this.$message.info('已取消编辑');
    },
    
    async saveProfile() {
      try {
        // 准备更新数据
        const updateData = {
          realName: this.userInfo.name,
          gender: this.userInfo.gender,
          birthday: this.formatBirthday(this.userInfo.birthday),
          phone: this.userInfo.phone,
          email: this.userInfo.email,
          department: this.userInfo.department,
          title: this.userInfo.title,
          office: this.userInfo.office,
          bio: this.userInfo.bio
        };
        
        const response = await authApi.updateUserInfo(updateData);
        if (response.code === 200) {
          this.isEditing = false;
          this.$message.success('个人信息保存成功！');
          
          // 重新加载用户信息以确保数据同步
          await this.loadUserInfo();
        } else {
          this.$message.error(response.message || '保存个人信息失败');
        }
      } catch (error) {
        console.error('保存个人信息失败:', error);
        this.$message.error('保存个人信息失败，请稍后重试');
      }
    },
    
    // 格式化生日日期
    formatBirthday(birthday) {
      if (!birthday) return '';
      
      // 确保birthday是字符串类型
      const birthdayStr = String(birthday);
      
      // 如果是ISO格式的日期时间字符串，只提取日期部分
      if (birthdayStr.includes('T') && !birthdayStr.includes('GMT')) {
        return birthdayStr.split('T')[0];
      }
      
      // 如果是Date对象的字符串表示，转换为YYYY-MM-DD格式
      if (birthdayStr.includes('GMT') || birthdayStr.includes('GM')) {
        try {
          const date = new Date(birthdayStr);
          if (!isNaN(date.getTime())) {
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            return `${year}-${month}-${day}`;
          }
        } catch (e) {
          console.warn('日期解析失败:', e);
        }
      }
      
      // 如果已经是YYYY-MM-DD格式，直接返回
      if (/^\d{4}-\d{2}-\d{2}$/.test(birthdayStr)) {
        return birthdayStr;
      }
      
      // 其他情况，尝试解析为日期
      try {
        const date = new Date(birthdayStr);
        if (!isNaN(date.getTime())) {
          const year = date.getFullYear();
          const month = String(date.getMonth() + 1).padStart(2, '0');
          const day = String(date.getDate()).padStart(2, '0');
          return `${year}-${month}-${day}`;
        }
      } catch (e) {
        console.warn('日期解析失败:', e);
      }
      
      // 如果无法解析，返回空字符串
      return '';
    },
    
    changeAvatar() {
      this.$message.info('头像上传功能开发中...');
    },
    
    changePassword() {
      this.passwordForm = {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      };
      this.passwordDialogVisible = true;
    },
    
    submitPassword() {
      this.$refs.passwordForm.validate((valid) => {
        if (valid) {
          this.passwordDialogVisible = false;
          this.$message.success('密码修改成功！');
        }
      });
    },
    
    exportData() {
      this.$message.info('数据导出功能开发中...');
    }
  }
}
</script>

<style scoped>
.teacher-profile {
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

.profile-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.profile-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.avatar-section {
  text-align: center;
  margin-right: 20px;
}

.change-avatar-btn {
  margin-top: 8px;
  font-size: 12px;
}

.user-basic-info h3 {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 5px 0;
}

.user-title,
.user-department {
  color: #7f8c8d;
  margin: 0 0 3px 0;
  font-size: 14px;
}

.profile-stats {
  display: flex;
  justify-content: space-around;
  border-top: 1px solid #f0f0f0;
  padding-top: 15px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
}

.stat-label {
  font-size: 12px;
  color: #7f8c8d;
}

.quick-actions {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.quick-actions h4 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 15px 0;
}

.action-btn {
  width: 100%;
  margin-bottom: 10px;
  justify-content: flex-start;
}

.action-btn i {
  margin-right: 8px;
}

.info-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.info-form {
  max-width: 600px;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-content h4 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 5px 0;
}

.setting-content p {
  color: #7f8c8d;
  margin: 0;
  font-size: 14px;
}

.form-actions {
  margin-top: 20px;
  text-align: right;
}

.form-actions .el-button {
  margin-left: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    text-align: center;
  }
  
  .avatar-section {
    margin-right: 0;
    margin-bottom: 15px;
  }
  
  .profile-stats {
    flex-direction: column;
    gap: 10px;
  }
  
  .setting-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .setting-item .el-switch {
    margin-top: 10px;
  }
}
</style>
