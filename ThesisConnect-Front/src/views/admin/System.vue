<template>
  <div class="admin-system">
    <div class="page-header">
      <h2 class="page-title">系统设置</h2>
      <p class="page-desc">配置系统参数和全局设置</p>
    </div>
    
    <el-row :gutter="20">
      <!-- 左侧：设置分类 -->
      <el-col :span="6">
        <div class="settings-nav">
          <el-menu
            :default-active="activeMenu"
            @select="handleMenuSelect"
            class="settings-menu">
            <el-menu-item index="basic">
              <i class="el-icon-setting"></i>
              <span slot="title">基本设置</span>
            </el-menu-item>
            <el-menu-item index="topic">
              <i class="el-icon-document"></i>
              <span slot="title">课题设置</span>
            </el-menu-item>
            <el-menu-item index="user">
              <i class="el-icon-user"></i>
              <span slot="title">用户设置</span>
            </el-menu-item>
            <el-menu-item index="notification">
              <i class="el-icon-message"></i>
              <span slot="title">通知设置</span>
            </el-menu-item>
            <el-menu-item index="security">
              <i class="el-icon-lock"></i>
              <span slot="title">安全设置</span>
            </el-menu-item>
            <el-menu-item index="backup">
              <i class="el-icon-download"></i>
              <span slot="title">备份设置</span>
            </el-menu-item>
          </el-menu>
        </div>
      </el-col>
      
      <!-- 右侧：设置内容 -->
      <el-col :span="18">
        <div class="settings-content">
          <!-- 基本设置 -->
          <div v-show="activeMenu === 'basic'" class="setting-panel">
            <h3>基本设置</h3>
            <el-form :model="basicSettings" label-width="120px">
              <el-form-item label="系统名称">
                <el-input v-model="basicSettings.systemName"></el-input>
              </el-form-item>
              <el-form-item label="系统版本">
                <el-input v-model="basicSettings.version" disabled></el-input>
              </el-form-item>
              <el-form-item label="系统描述">
                <el-input v-model="basicSettings.description" type="textarea" :rows="3"></el-input>
              </el-form-item>
              <el-form-item label="管理员邮箱">
                <el-input v-model="basicSettings.adminEmail"></el-input>
              </el-form-item>
              <el-form-item label="系统时区">
                <el-select v-model="basicSettings.timezone">
                  <el-option label="北京时间 (UTC+8)" value="Asia/Shanghai"></el-option>
                  <el-option label="东京时间 (UTC+9)" value="Asia/Tokyo"></el-option>
                  <el-option label="纽约时间 (UTC-5)" value="America/New_York"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="语言设置">
                <el-select v-model="basicSettings.language">
                  <el-option label="简体中文" value="zh-CN"></el-option>
                  <el-option label="English" value="en-US"></el-option>
                </el-select>
              </el-form-item>
            </el-form>
          </div>
          
          <!-- 课题设置 -->
          <div v-show="activeMenu === 'topic'" class="setting-panel">
            <h3>课题设置</h3>
            <el-form :model="topicSettings" label-width="120px">
              <el-form-item label="选题开始时间">
                <el-date-picker
                  v-model="topicSettings.selectionStartTime"
                  type="datetime"
                  placeholder="选择开始时间">
                </el-date-picker>
              </el-form-item>
              <el-form-item label="选题结束时间">
                <el-date-picker
                  v-model="topicSettings.selectionEndTime"
                  type="datetime"
                  placeholder="选择结束时间">
                </el-date-picker>
              </el-form-item>
              <el-form-item label="课题发布审核">
                <el-switch v-model="topicSettings.topicReview"></el-switch>
                <span class="setting-desc">开启后，教师发布的课题需要管理员审核</span>
              </el-form-item>
              <el-form-item label="最大选题数">
                <el-input-number v-model="topicSettings.maxSelections" :min="1" :max="10"></el-input-number>
                <span class="setting-desc">每个学生最多可选择的课题数量</span>
              </el-form-item>
              <el-form-item label="课题难度等级">
                <el-checkbox-group v-model="topicSettings.difficultyLevels">
                  <el-checkbox label="easy">简单</el-checkbox>
                  <el-checkbox label="medium">中等</el-checkbox>
                  <el-checkbox label="hard">困难</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              <el-form-item label="课题分类">
                <el-input v-model="topicSettings.categories" type="textarea" :rows="3" placeholder="每行一个分类"></el-input>
              </el-form-item>
            </el-form>
          </div>
          
          <!-- 用户设置 -->
          <div v-show="activeMenu === 'user'" class="setting-panel">
            <h3>用户设置</h3>
            <el-form :model="userSettings" label-width="120px">
              <el-form-item label="用户注册审核">
                <el-switch v-model="userSettings.registrationReview"></el-switch>
                <span class="setting-desc">开启后，新用户注册需要管理员审核</span>
              </el-form-item>
              <el-form-item label="默认用户状态">
                <el-select v-model="userSettings.defaultStatus">
                  <el-option label="正常" value="active"></el-option>
                  <el-option label="待审核" value="pending"></el-option>
                  <el-option label="禁用" value="disabled"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="密码最小长度">
                <el-input-number v-model="userSettings.minPasswordLength" :min="6" :max="20"></el-input-number>
              </el-form-item>
              <el-form-item label="密码复杂度">
                <el-checkbox-group v-model="userSettings.passwordComplexity">
                  <el-checkbox label="uppercase">包含大写字母</el-checkbox>
                  <el-checkbox label="lowercase">包含小写字母</el-checkbox>
                  <el-checkbox label="number">包含数字</el-checkbox>
                  <el-checkbox label="special">包含特殊字符</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              <el-form-item label="登录失败锁定">
                <el-switch v-model="userSettings.loginLock"></el-switch>
                <span class="setting-desc">开启后，连续登录失败会锁定账户</span>
              </el-form-item>
              <el-form-item label="最大失败次数">
                <el-input-number v-model="userSettings.maxFailures" :min="3" :max="10"></el-input-number>
              </el-form-item>
              <el-form-item label="锁定时间(分钟)">
                <el-input-number v-model="userSettings.lockTime" :min="5" :max="60"></el-input-number>
              </el-form-item>
            </el-form>
          </div>
          
          <!-- 通知设置 -->
          <div v-show="activeMenu === 'notification'" class="setting-panel">
            <h3>通知设置</h3>
            <el-form :model="notificationSettings" label-width="120px">
              <el-form-item label="邮件通知">
                <el-switch v-model="notificationSettings.emailNotification"></el-switch>
              </el-form-item>
              <el-form-item label="SMTP服务器">
                <el-input v-model="notificationSettings.smtpServer"></el-input>
              </el-form-item>
              <el-form-item label="SMTP端口">
                <el-input-number v-model="notificationSettings.smtpPort" :min="1" :max="65535"></el-input-number>
              </el-form-item>
              <el-form-item label="发送邮箱">
                <el-input v-model="notificationSettings.senderEmail"></el-input>
              </el-form-item>
              <el-form-item label="邮箱密码">
                <el-input v-model="notificationSettings.emailPassword" type="password"></el-input>
              </el-form-item>
              <el-form-item label="短信通知">
                <el-switch v-model="notificationSettings.smsNotification"></el-switch>
              </el-form-item>
              <el-form-item label="短信API">
                <el-input v-model="notificationSettings.smsApi"></el-input>
              </el-form-item>
              <el-form-item label="API密钥">
                <el-input v-model="notificationSettings.smsApiKey" type="password"></el-input>
              </el-form-item>
            </el-form>
          </div>
          
          <!-- 安全设置 -->
          <div v-show="activeMenu === 'security'" class="setting-panel">
            <h3>安全设置</h3>
            <el-form :model="securitySettings" label-width="120px">
              <el-form-item label="HTTPS强制">
                <el-switch v-model="securitySettings.forceHttps"></el-switch>
                <span class="setting-desc">强制使用HTTPS协议</span>
              </el-form-item>
              <el-form-item label="会话超时(分钟)">
                <el-input-number v-model="securitySettings.sessionTimeout" :min="10" :max="480"></el-input-number>
              </el-form-item>
              <el-form-item label="IP白名单">
                <el-input v-model="securitySettings.ipWhitelist" type="textarea" :rows="3" placeholder="每行一个IP地址"></el-input>
              </el-form-item>
              <el-form-item label="操作日志">
                <el-switch v-model="securitySettings.operationLog"></el-switch>
                <span class="setting-desc">记录用户操作日志</span>
              </el-form-item>
              <el-form-item label="数据加密">
                <el-switch v-model="securitySettings.dataEncryption"></el-switch>
                <span class="setting-desc">对敏感数据进行加密存储</span>
              </el-form-item>
              <el-form-item label="备份加密">
                <el-switch v-model="securitySettings.backupEncryption"></el-switch>
                <span class="setting-desc">备份文件加密存储</span>
              </el-form-item>
            </el-form>
          </div>
          
          <!-- 备份设置 -->
          <div v-show="activeMenu === 'backup'" class="setting-panel">
            <h3>备份设置</h3>
            <el-form :model="backupSettings" label-width="120px">
              <el-form-item label="自动备份">
                <el-switch v-model="backupSettings.autoBackup"></el-switch>
                <span class="setting-desc">开启自动备份功能</span>
              </el-form-item>
              <el-form-item label="备份频率">
                <el-select v-model="backupSettings.backupFrequency">
                  <el-option label="每日" value="daily"></el-option>
                  <el-option label="每周" value="weekly"></el-option>
                  <el-option label="每月" value="monthly"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="备份时间">
                <el-time-picker v-model="backupSettings.backupTime" placeholder="选择备份时间"></el-time-picker>
              </el-form-item>
              <el-form-item label="保留天数">
                <el-input-number v-model="backupSettings.retentionDays" :min="7" :max="365"></el-input-number>
              </el-form-item>
              <el-form-item label="备份路径">
                <el-input v-model="backupSettings.backupPath"></el-input>
              </el-form-item>
              <el-form-item label="远程备份">
                <el-switch v-model="backupSettings.remoteBackup"></el-switch>
                <span class="setting-desc">备份到远程服务器</span>
              </el-form-item>
              <el-form-item label="FTP服务器">
                <el-input v-model="backupSettings.ftpServer"></el-input>
              </el-form-item>
              <el-form-item label="FTP用户名">
                <el-input v-model="backupSettings.ftpUsername"></el-input>
              </el-form-item>
              <el-form-item label="FTP密码">
                <el-input v-model="backupSettings.ftpPassword" type="password"></el-input>
              </el-form-item>
            </el-form>
          </div>
          
          <!-- 操作按钮 -->
          <div class="setting-actions">
            <el-button @click="resetSettings">重置</el-button>
            <el-button type="primary" @click="saveSettings">保存设置</el-button>
            <el-button @click="testConnection">测试连接</el-button>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'AdminSystem',
  data() {
    return {
      activeMenu: 'basic',
      
      basicSettings: {
        systemName: '毕业论文选题双向选择系统',
        version: 'v1.0.0',
        description: '一个用于管理毕业论文选题的系统，支持学生和教师双向选择。',
        adminEmail: 'admin@university.edu.cn',
        timezone: 'Asia/Shanghai',
        language: 'zh-CN'
      },
      
      topicSettings: {
        selectionStartTime: '2024-02-01 00:00:00',
        selectionEndTime: '2024-03-31 23:59:59',
        topicReview: true,
        maxSelections: 3,
        difficultyLevels: ['easy', 'medium', 'hard'],
        categories: '计算机科学与技术\n软件工程\n网络工程\n信息安全'
      },
      
      userSettings: {
        registrationReview: true,
        defaultStatus: 'pending',
        minPasswordLength: 8,
        passwordComplexity: ['uppercase', 'lowercase', 'number'],
        loginLock: true,
        maxFailures: 5,
        lockTime: 30
      },
      
      notificationSettings: {
        emailNotification: true,
        smtpServer: 'smtp.university.edu.cn',
        smtpPort: 587,
        senderEmail: 'noreply@university.edu.cn',
        emailPassword: '',
        smsNotification: false,
        smsApi: '',
        smsApiKey: ''
      },
      
      securitySettings: {
        forceHttps: true,
        sessionTimeout: 120,
        ipWhitelist: '',
        operationLog: true,
        dataEncryption: true,
        backupEncryption: true
      },
      
      backupSettings: {
        autoBackup: true,
        backupFrequency: 'daily',
        backupTime: '02:00:00',
        retentionDays: 30,
        backupPath: '/backup',
        remoteBackup: false,
        ftpServer: '',
        ftpUsername: '',
        ftpPassword: ''
      }
    }
  },
  methods: {
    handleMenuSelect(index) {
      this.activeMenu = index;
    },
    
    saveSettings() {
      this.$confirm('确定要保存所有设置吗？', '确认保存', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 这里应该调用API保存设置
        this.$message.success('设置保存成功！');
      });
    },
    
    resetSettings() {
      this.$confirm('确定要重置所有设置为默认值吗？', '确认重置', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 重置为默认值
        this.$message.success('设置已重置为默认值！');
      });
    },
    
    testConnection() {
      this.$message.info('正在测试连接...');
      // 模拟测试连接
      setTimeout(() => {
        this.$message.success('连接测试成功！');
      }, 2000);
    }
  }
}
</script>

<style scoped>
.admin-system {
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

.settings-nav {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.settings-menu {
  border: none;
}

.settings-menu >>> .el-menu-item {
  height: 50px;
  line-height: 50px;
}

.settings-menu >>> .el-menu-item:hover {
  background: #f5f7fa;
  color: #409eff;
}

.settings-menu >>> .el-menu-item.is-active {
  background: #409eff;
  color: white;
}

.settings-content {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.setting-panel h3 {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 20px 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #e6e6e6;
}

.setting-desc {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

.setting-actions {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e6e6e6;
  text-align: right;
}

.setting-actions .el-button {
  margin-left: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-system .el-row {
    flex-direction: column;
  }
  
  .admin-system .el-col {
    margin-bottom: 20px;
  }
  
  .settings-nav {
    order: 2;
  }
  
  .settings-content {
    order: 1;
  }
  
  .setting-actions {
    text-align: center;
  }
  
  .setting-actions .el-button {
    margin: 5px;
  }
}
</style>
