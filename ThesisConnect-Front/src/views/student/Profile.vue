<template>
  <div class="student-profile">
    <div class="page-header">
      <h2 class="page-title">个人信息</h2>
      <p class="page-desc">管理您的个人资料和账户设置</p>
    </div>

    <el-row :gutter="20">
      <!-- 左侧：个人信息卡片 -->
      <el-col :span="8">
        <div class="profile-card">
          <div class="profile-header">
            <div class="avatar-section">
              <el-upload
                class="avatar-uploader"
                action="#"
                :show-file-list="false"
                :http-request="uploadAvatar"
                :before-upload="beforeAvatarUpload">
                <div class="avatar-wrapper">
                  <el-avatar :size="80" :src="userInfo.avatar">
                    <i class="el-icon-user-solid"></i>
                  </el-avatar>
                  <div class="avatar-mask">
                    <i class="el-icon-camera"></i>
                    <span>更换</span>
                  </div>
                </div>
              </el-upload>
              <div class="change-avatar-text">点击头像更换</div>
            </div>
            <div class="user-basic-info">
              <h3>{{ userInfo.name }}</h3>
              <p class="user-id">学号：{{ userInfo.studentId }}</p>
              <p class="user-major">{{ userInfo.major }}</p>
            </div>
          </div>

          <div class="profile-stats">
            <div class="stat-item">
              <span class="stat-number">{{ userInfo.gpa }}</span>
              <span class="stat-label">GPA</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ userInfo.credits }}</span>
              <span class="stat-label">学分</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ userInfo.rank }}</span>
              <span class="stat-label">排名</span>
            </div>
          </div>
        </div>

        <!-- 快捷操作 -->
        <div class="quick-actions">
          <h4>快捷操作</h4>
          <el-button @click="editProfile" class="action-btn">
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
                    <el-form-item label="学号">
                      <el-input v-model="userInfo.studentId" disabled></el-input>
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
                    <el-form-item label="专业">
                      <el-input v-model="userInfo.major" disabled></el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="班级">
                      <el-input v-model="userInfo.class" :disabled="!isEditing"></el-input>
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-form-item label="联系电话">
                  <el-input v-model="userInfo.phone" :disabled="!isEditing"></el-input>
                </el-form-item>

                <el-form-item label="邮箱">
                  <el-input v-model="userInfo.email" :disabled="!isEditing"></el-input>
                </el-form-item>

                <el-form-item label="家庭地址">
                  <el-input v-model="userInfo.address" type="textarea" :disabled="!isEditing"></el-input>
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
                    <el-form-item label="入学年份">
                      <el-input v-model="academicInfo.enrollmentYear" disabled></el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="预计毕业">
                      <el-input v-model="academicInfo.graduationYear" disabled></el-input>
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="当前GPA">
                      <el-input v-model="academicInfo.gpa" disabled></el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="已修学分">
                      <el-input v-model="academicInfo.credits" disabled></el-input>
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="专业排名">
                      <el-input v-model="academicInfo.rank" disabled></el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="奖学金">
                      <el-input v-model="academicInfo.scholarship" :disabled="!isEditing"></el-input>
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-form-item label="获奖情况">
                  <el-input
                    v-model="academicInfo.awards"
                    type="textarea"
                    :rows="3"
                    :disabled="!isEditing">
                  </el-input>
                </el-form-item>

                <el-form-item label="研究兴趣">
                  <el-input
                    v-model="academicInfo.interests"
                    type="textarea"
                    :rows="3"
                    :disabled="!isEditing">
                  </el-input>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>

          <!-- 账户设置 -->
          <el-tab-pane label="账户设置" name="account">
            <div class="info-section">
              <div class="setting-item">
                <div class="setting-content">
                  <h4>修改密码</h4>
                  <p>定期修改密码可以保护您的账户安全</p>
                </div>
                <el-button @click="changePassword">修改</el-button>
              </div>

              <div class="setting-item">
                <div class="setting-content">
                  <h4>邮箱验证</h4>
                  <p>当前邮箱：{{ userInfo.email }}</p>
                </div>
                <el-button @click="verifyEmail">验证</el-button>
              </div>

              <div class="setting-item">
                <div class="setting-content">
                  <h4>手机验证</h4>
                  <p>当前手机：{{ userInfo.phone }}</p>
                </div>
                <el-button @click="verifyPhone">验证</el-button>
              </div>

              <div class="setting-item">
                <div class="setting-content">
                  <h4>数据导出</h4>
                  <p>导出您的个人数据和学习记录</p>
                </div>
                <el-button @click="exportData">导出</el-button>
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
import { authApi, userApi, fileApi } from '@/api'

export default {
  name: 'StudentProfile',
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

      // 用户基本信息
      userInfo: {
        name: '',
        studentId: '',
        gender: '',
        birthday: '',
        major: '',
        class: '',
        phone: '',
        email: '',
        address: '',
        avatar: '',
        gpa: '',
        credits: '',
        rank: ''
      },

      // 学术信息
      academicInfo: {
        enrollmentYear: '',
        graduationYear: '',
        gpa: '',
        credits: '',
        rank: '',
        scholarship: '',
        awards: '',
        interests: ''
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
            studentId: userData.studentId || '',
            gender: userData.gender || '',
            birthday: userData.birthday || '',
            major: userData.major || '',
            class: userData.className || '',
            phone: userData.phone || '',
            email: userData.email || '',
            address: userData.address || '',
            avatar: userData.avatar || '',
            gpa: userData.gpa || '',
            credits: userData.credits || '',
            rank: userData.rank || ''
          }

          // 更新学术信息
          this.academicInfo = {
            enrollmentYear: userData.enrollmentYear || '',
            graduationYear: userData.graduationYear || '',
            gpa: userData.gpa || '',
            credits: userData.credits || '',
            rank: userData.rank || '',
            scholarship: userData.scholarship || '',
            awards: userData.awards || '',
            interests: userData.interests || ''
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
          address: this.userInfo.address,
          className: this.userInfo.class
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

    async uploadAvatar(params) {
      try {
        const file = params.file
        const response = await fileApi.uploadFile(file)

        if (response.code === 200) {
          const avatarUrl = response.data
          this.userInfo.avatar = avatarUrl

          // 立即更新用户信息中的头像
          const updateData = {
            avatar: avatarUrl
          }

          const updateResponse = await authApi.updateUserInfo(updateData)
          if (updateResponse.code === 200) {
            this.$message.success('头像更换成功')
          } else {
            this.$message.warning('头像上传成功但保存失败')
          }
        } else {
          this.$message.error(response.message || '头像上传失败')
        }
      } catch (error) {
        console.error('头像上传异常:', error)
        this.$message.error('头像上传失败，请稍后重试')
      }
    },

    beforeAvatarUpload(file) {
      const isJPG = file.type === 'image/jpeg';
      const isPNG = file.type === 'image/png';
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isJPG && !isPNG) {
        this.$message.error('上传头像图片只能是 JPG 或 PNG 格式!');
        return false;
      }
      if (!isLt2M) {
        this.$message.error('上传头像图片大小不能超过 2MB!');
        return false;
      }
      return true;
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

    verifyEmail() {
      this.$message.info('邮箱验证功能开发中...');
    },

    verifyPhone() {
      this.$message.info('手机验证功能开发中...');
    },

    exportData() {
      this.$message.info('数据导出功能开发中...');
    }
  }
}
</script>

<style scoped>
.student-profile {
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

.avatar-wrapper {
  position: relative;
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;
  display: inline-block;
}

.avatar-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-wrapper:hover .avatar-mask {
  opacity: 1;
}

.avatar-mask i {
  font-size: 20px;
  margin-bottom: 5px;
}

.avatar-mask span {
  font-size: 12px;
}

.change-avatar-text {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
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

.user-id,
.user-major {
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

  .setting-item .el-button {
    margin-top: 10px;
  }
}
</style>
