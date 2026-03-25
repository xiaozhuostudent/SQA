<template>
  <div class="profile-page">
    <el-alert
      v-if="userInfo.role && userInfo.role !== 'admin'"
      class="role-alert"
      title="角色错误"
      :description="`当前账号是${userInfo.role === 'student' ? '学生' : '教师'}账号,请访问对应端查看个人信息`"
      type="error"
      :closable="false"
    />

    <template v-else>
      <section class="profile-hero">
        <div class="hero-avatar">
          <el-avatar :size="110" :src="userInfo.avatar" />
        </div>
        <div class="hero-content">
          <div class="hero-title-group">
            <h2>{{ userInfo.realName || userInfo.username || '未填写姓名' }}</h2>
            <el-tag type="danger">管理员</el-tag>
          </div>
          <p class="hero-subtitle">
            权限等级 {{ userInfo.permissionLevel || 1 }} · {{ userInfo.role === 'admin' ? '全局管理权限' : '跨端账号' }}
          </p>
          <div class="hero-meta">
            <div class="meta-item">
              <span class="meta-label">编号</span>
              <span class="meta-value">{{ userInfo.adminNumber || '——' }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">邮箱</span>
              <span class="meta-value">{{ userInfo.email || '未填写' }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">手机号</span>
              <span class="meta-value">{{ userInfo.phone || '未填写' }}</span>
            </div>
          </div>
        </div>
        <div class="hero-actions">
          <el-button type="primary" @click="updateInfo" :loading="updating">保存修改</el-button>
          <el-button plain @click="changePasswordVisible = true">修改密码</el-button>
        </div>
      </section>

      <el-row :gutter="20" class="info-grid">
        <el-col :xs="24" :md="16">
          <el-card class="info-card">
            <div class="card-title">账户信息</div>
            <el-form :model="userInfo" label-position="top" class="profile-form">
              <el-row :gutter="16">
                <el-col :xs="24" :md="12">
                  <el-form-item label="用户名">
                    <el-input v-model="userInfo.username" disabled />
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :md="12">
                  <el-form-item label="真实姓名" required>
                    <el-input v-model="userInfo.realName" placeholder="请输入真实姓名" maxlength="20" show-word-limit />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="16">
                <el-col :xs="24" :md="12">
                  <el-form-item label="邮箱" required>
                    <el-input v-model="userInfo.email" type="email" placeholder="请输入邮箱地址" maxlength="50" />
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :md="12">
                  <el-form-item label="手机号">
                    <el-input v-model="userInfo.phone" placeholder="请输入11位手机号" maxlength="11" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="16">
                <el-col :xs="24" :md="12">
                  <el-form-item label="性别">
                    <el-radio-group v-model="userInfo.gender" class="inline-radio">
                      <el-radio value="male">男</el-radio>
                      <el-radio value="female">女</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :md="12">
                  <el-form-item label="权限等级" v-if="userInfo.role === 'admin'">
                    <el-rate v-model="userInfo.permissionLevel" disabled show-score />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-card>
        </el-col>

        <el-col :xs="24" :md="8">
          <el-card class="stats-card">
            <div class="card-title">安全速览</div>
            <div class="stats-grid">
              <div class="stat-item">
                <span class="stat-label">最近更新</span>
                <span class="stat-value">{{ lastUpdatedText }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">角色</span>
                <span class="stat-value">{{ roleText }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">权限等级</span>
                <span class="stat-value">Lv.{{ userInfo.permissionLevel || 1 }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">注册邮箱</span>
                <span class="stat-value">{{ userInfo.email || '未填写' }}</span>
              </div>
            </div>
            <div class="tips-box">
              <p>建议定期更新管理员资料，并开启双端验证，确保所有敏感操作均可追溯。</p>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="changePasswordVisible" title="修改密码" width="500px" @close="resetPasswordForm">
      <el-form :model="passwordForm" label-width="100px">
        <el-form-item label="原密码" required>
          <el-input 
            v-model="passwordForm.oldPassword" 
            type="password" 
            show-password
            placeholder="请输入原密码"
            maxlength="50"
          />
        </el-form-item>
        <el-form-item label="新密码" required>
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password" 
            show-password
            placeholder="请输入新密码（至少6位）"
            maxlength="50"
          />
        </el-form-item>
        <el-form-item label="确认密码" required>
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password" 
            show-password
            placeholder="请再次输入新密码"
            maxlength="50"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="changePasswordVisible = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword" :loading="changingPassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const userStore = useUserStore()
const changePasswordVisible = ref(false)
const updating = ref(false)
const changingPassword = ref(false)

const userInfo = ref({
  id: null,
  username: '',
  adminNumber: '',
  permissionLevel: 1,
  realName: '',
  gender: 'male',
  email: '',
  phone: '',
  role: 'admin',
  updatedAt: '',
  avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
})

const passwordForm = ref({
  userId: null,
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const roleText = computed(() => {
  if (userInfo.value.role === 'admin') return '管理员'
  if (userInfo.value.role === 'teacher') return '教师'
  if (userInfo.value.role === 'student') return '学生'
  return '未知角色'
})

const lastUpdatedText = computed(() => {
  if (!userInfo.value.updatedAt) return '尚未同步'
  const date = new Date(userInfo.value.updatedAt)
  if (Number.isNaN(date.getTime())) return '尚未同步'
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
})

// 加载用户信息
const loadUserInfo = async () => {
  try {
    // 从 userStore 或 localStorage 获取 userInfo
    let storedUserInfo = userStore.userInfo
    if (!storedUserInfo || !storedUserInfo.id) {
      storedUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    }
    
    console.log('🔍 [管理员端Profile] localStorage中的userInfo:', storedUserInfo)
    
    if (!storedUserInfo.id) {
      ElMessage.error('用户信息不存在，请重新登录')
      setTimeout(() => {
        window.location.href = '/login'
      }, 1500)
      return
    }
    
    const res = await request.get(`/user/info/${storedUserInfo.id}`)
    
    console.log('🔍 [管理员端Profile] 后端返回的数据:', res.data)
    
    // 兼容不同的响应格式
    const data = res.data || res
    
    if (data) {
      userInfo.value = {
        id: data.id,
        username: data.username || '',
        adminNumber: data.adminNumber || data.studentNumber || data.teacherNumber || '',
        permissionLevel: data.permissionLevel || 5,
        realName: data.realName || '',
        gender: data.gender || 'male',
        email: data.email || '',
        phone: data.phone || '',
        role: data.role || 'admin',
        updatedAt: data.updatedAt || Date.now(),
        avatar: data.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
      }
      
      // 更新本地存储
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      userStore.setUserInfo(userInfo.value)
    } else {
      ElMessage.error('加载用户信息失败')
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('加载用户信息失败：' + (error.response?.data?.message || error.message))
  }
}

// 更新用户信息
const updateInfo = async () => {
  if (updating.value) return
  
  try {
    updating.value = true
    
    // 验证必填字段
    if (!userInfo.value.realName) {
      ElMessage.warning('请输入真实姓名')
      return
    }
    
    if (!userInfo.value.email) {
      ElMessage.warning('请输入邮箱')
      return
    }
    
    // 验证邮箱格式
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(userInfo.value.email)) {
      ElMessage.warning('邮箱格式不正确')
      return
    }
    
    // 验证手机号格式（如果填写了）
    if (userInfo.value.phone) {
      const phoneRegex = /^1[3-9]\d{9}$/
      if (!phoneRegex.test(userInfo.value.phone)) {
        ElMessage.warning('手机号格式不正确')
        return
      }
    }
    
    const updateData = {
      id: userInfo.value.id,
      realName: userInfo.value.realName,
      gender: userInfo.value.gender,
      email: userInfo.value.email,
      phone: userInfo.value.phone
    }
    
    const res = await request.put('/user/update', updateData)
    
    // 兼容不同的响应格式
    const data = res.data || res
    
    if (data.code === 200 || res.code === 200 || data.success !== false) {
      ElMessage.success('信息更新成功')
      // 更新本地存储
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      userStore.setUserInfo(userInfo.value)
      await loadUserInfo()
    } else {
      ElMessage.error(data.message || res.message || '更新失败')
    }
  } catch (error) {
    console.error('更新失败:', error)
    ElMessage.error('更新失败：' + (error.response?.data?.message || error.message))
  } finally {
    updating.value = false
  }
}

// 修改密码
const handleChangePassword = async () => {
  if (changingPassword.value) return
  
  try {
    changingPassword.value = true
    
    // 验证表单
    if (!passwordForm.value.oldPassword) {
      ElMessage.error('请输入原密码')
      return
    }
    
    if (!passwordForm.value.newPassword) {
      ElMessage.error('请输入新密码')
      return
    }
    
    if (!passwordForm.value.confirmPassword) {
      ElMessage.error('请确认新密码')
      return
    }
    
    if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
      ElMessage.error('两次输入的密码不一致')
      return
    }
    
    if (passwordForm.value.newPassword.length < 6) {
      ElMessage.error('新密码长度至少6位')
      return
    }
    
    if (passwordForm.value.oldPassword === passwordForm.value.newPassword) {
      ElMessage.error('新密码不能与原密码相同')
      return
    }

    const storedUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const res = await request.post('/user/change-password', {
      userId: storedUserInfo.id || userInfo.value.id,
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    
    // 兼容不同的响应格式
    const data = res.data || res
    
    if (data.code === 200 || res.code === 200 || data.success !== false) {
      ElMessage.success('密码修改成功，请重新登录')
      changePasswordVisible.value = false
      resetPasswordForm()
      // 清空登录状态
      setTimeout(() => {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        userStore.logout()
        window.location.href = '/login'
      }, 1500)
    } else {
      ElMessage.error(data.message || res.message || '密码修改失败')
    }
  } catch (error) {
    console.error('密码修改失败:', error)
    ElMessage.error('密码修改失败：' + (error.response?.data?.message || error.message))
  } finally {
    changingPassword.value = false
  }
}

// 重置密码表单
const resetPasswordForm = () => {
  passwordForm.value = {
    userId: null,
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 0;
}

.role-alert {
  margin-bottom: 12px;
}

.profile-hero {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 20px;
  background: linear-gradient(135deg, rgba(244, 114, 182, 0.12), rgba(99, 102, 241, 0.28));
  border: 1px solid var(--border);
  border-radius: 18px;
  padding: 24px;
  box-shadow: 0 12px 34px rgba(31, 29, 43, 0.5);
}

.hero-avatar {
  flex: 0 0 auto;
}

.hero-content {
  flex: 1 1 260px;
  min-width: 240px;
}

.hero-title-group {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.hero-title-group h2 {
  margin: 0;
  font-size: 24px;
  color: var(--text-primary);
}

.hero-subtitle {
  margin: 0 0 16px;
  color: var(--text-secondary);
  font-size: 14px;
}

.hero-meta {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
}

.meta-item {
  padding: 12px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(0, 0, 0, 0.15);
}

.meta-label {
  display: block;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 4px;
}

.meta-value {
  font-size: 16px;
  color: #fff;
  font-weight: 600;
}

.hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.info-grid {
  width: 100%;
}

.card-title {
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 16px;
  color: var(--text-primary);
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stats-card {
  height: 100%;
}

.stats-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-item {
  padding: 12px 14px;
  border-radius: 10px;
  border: 1px dashed var(--border);
  background: var(--bg-secondary);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
  text-transform: uppercase;
}

.stat-value {
  font-size: 16px;
  color: var(--text-primary);
  font-weight: 600;
}

.tips-box {
  margin-top: 18px;
  padding: 14px;
  border-radius: 12px;
  background: rgba(99, 102, 241, 0.12);
  color: var(--text-secondary);
  line-height: 1.5;
  font-size: 13px;
}

.inline-radio :deep(.el-radio__label) {
  color: var(--text-primary);
}

.profile-page :deep(.el-card) {
  background: var(--bg-float);
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
}

.profile-page :deep(.el-form-item__label) {
  color: var(--text-primary);
  font-weight: 500;
}

.profile-page :deep(.el-input__wrapper) {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  box-shadow: none;
}

.profile-page :deep(.el-input__inner) {
  color: var(--text-primary);
}

.profile-page :deep(.el-input.is-disabled .el-input__wrapper) {
  background: var(--bg-primary);
}

.profile-page :deep(.el-radio__input.is-checked + .el-radio__label) {
  color: var(--accent-cyan);
}

.profile-page :deep(.el-dialog) {
  background: var(--bg-float);
  border: 1px solid var(--border);
}

.profile-page :deep(.el-dialog__title) {
  color: var(--text-primary);
}

@media (max-width: 768px) {
  .profile-hero {
    padding: 18px;
  }

  .hero-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
