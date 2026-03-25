<template>
  <div class="profile-page">
    <el-alert
      v-if="userInfo.role && userInfo.role !== 'teacher'"
      class="role-alert"
      title="角色错误"
      :description="`当前账号是${userInfo.role === 'student' ? '学生' : '管理员'}账号,请访问对应端查看个人信息`"
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
            <el-tag type="warning">教师</el-tag>
          </div>
          <p class="hero-subtitle">
            {{ userInfo.department || '未设置院系' }} · {{ titleMap[userInfo.title] || '未设置职称' }}
          </p>
          <div class="hero-meta">
            <div class="meta-item">
              <span class="meta-label">工号</span>
              <span class="meta-value">{{ userInfo.teacherNumber || '——' }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">研究方向</span>
              <span class="meta-value">{{ userInfo.researchField || '未填写' }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">联系方式</span>
              <span class="meta-value">{{ userInfo.phone || '未填写' }}</span>
            </div>
          </div>
        </div>
        <div class="hero-actions">
          <el-button type="primary" @click="updateInfo">保存修改</el-button>
          <el-button plain @click="changePasswordVisible = true">修改密码</el-button>
        </div>
      </section>

      <el-row :gutter="20" class="info-grid">
        <el-col :xs="24" :md="16">
          <el-card class="info-card">
            <div class="card-title">基础资料</div>
            <el-form :model="userInfo" label-position="top" class="profile-form">
              <div class="form-section-title">身份信息</div>
              <el-row :gutter="16">
                <el-col :xs="24" :md="12">
                  <el-form-item label="用户名">
                    <el-input v-model="userInfo.username" disabled />
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :md="12">
                  <el-form-item label="邮箱">
                    <el-input v-model="userInfo.email" type="email" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="16">
                <el-col :xs="24" :md="12">
                  <el-form-item label="手机号">
                    <el-input v-model="userInfo.phone" />
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :md="12">
                  <el-form-item label="性别">
                    <el-radio-group v-model="userInfo.gender" class="inline-radio">
                      <el-radio value="male">男</el-radio>
                      <el-radio value="female">女</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
              </el-row>

              <div class="form-section-title">教学信息</div>
              <el-row :gutter="16">
                <el-col :xs="24" :md="12">
                  <el-form-item label="所属院系">
                    <el-input v-model="userInfo.department" />
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :md="12">
                  <el-form-item label="职称">
                    <el-select v-model="userInfo.title" placeholder="请选择职称" style="width: 100%">
                      <el-option label="助教" value="assistant" />
                      <el-option label="讲师" value="lecturer" />
                      <el-option label="副教授" value="associate_professor" />
                      <el-option label="教授" value="professor" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="研究方向">
                <el-input v-model="userInfo.researchField" type="textarea" :rows="3" placeholder="请输入研究方向" />
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>

        <el-col :xs="24" :md="8">
          <el-card class="stats-card">
            <div class="card-title">教学摘要</div>
            <div class="stats-grid">
              <div class="stat-item">
                <span class="stat-label">最近更新</span>
                <span class="stat-value">{{ lastUpdatedText }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">院系</span>
                <span class="stat-value">{{ userInfo.department || '未填写' }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">当前职称</span>
                <span class="stat-value">{{ titleMap[userInfo.title] || '未填写' }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">联系邮箱</span>
                <span class="stat-value">{{ userInfo.email || '未填写' }}</span>
              </div>
            </div>
            <div class="tips-box">
              <p>保持研究方向与联系方式最新，可让学生与教务及时获取到您的课程信息与科研合作意向。</p>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="changePasswordVisible" title="修改密码" width="500px">
      <el-form :model="passwordForm" label-width="100px">
        <el-form-item label="原密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="changePasswordVisible = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword">确定</el-button>
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

const userInfo = ref({
  id: null,
  username: '',
  role: '',
  teacherNumber: '',
  realName: '',
  gender: 'male',
  email: '',
  phone: '',
  department: '',
  title: '',
  researchField: '',
  updatedAt: '',
  avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
})

const passwordForm = ref({
  userId: null,
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const titleMap = {
  assistant: '助教',
  lecturer: '讲师',
  associate_professor: '副教授',
  professor: '教授'
}

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
    
    console.log('🔍 [教师端Profile] localStorage中的userInfo:', storedUserInfo)
    
    if (!storedUserInfo.id) {
      ElMessage.error('用户信息不存在，请重新登录')
      setTimeout(() => {
        window.location.href = '/login'
      }, 1500)
      return
    }
    
    const res = await request.get(`/user/info/${storedUserInfo.id}`)
    
    console.log('🔍 [教师端Profile] 后端返回的数据:', res.data)
    
    if (res.code === 200 && res.data) {
      // 优先使用localStorage中的role,因为登录时已经确定
      const actualRole = storedUserInfo.role || res.data.role || 'teacher'
      console.log('🔍 [教师端Profile] 最终使用的role:', actualRole)
      
      userInfo.value = {
        id: res.data.id,
        username: res.data.username || '',
        role: actualRole,
        teacherNumber: res.data.teacherNumber || '',
        realName: res.data.realName || '',
        gender: res.data.gender || 'male',
        email: res.data.email || '',
        phone: res.data.phone || '',
        department: res.data.department || '',
        title: res.data.title || '',
        researchField: res.data.researchField || '',
        updatedAt: res.data.updatedAt || Date.now(),
        avatar: res.data.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
      }
    } else {
      ElMessage.error(res.message || '加载用户信息失败')
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('加载用户信息失败,请重试')
  }
}

// 更新用户信息
const updateInfo = async () => {
  try {
    const updateData = {
      id: userInfo.value.id,
      realName: userInfo.value.realName,
      gender: userInfo.value.gender,
      email: userInfo.value.email,
      phone: userInfo.value.phone,
      department: userInfo.value.department,
      title: userInfo.value.title,
      researchField: userInfo.value.researchField
    }
    
    const res = await request.put('/user/update', updateData)
    
    if (res.code === 200) {
      ElMessage.success('信息更新成功')
      await loadUserInfo()
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (error) {
    console.error('更新失败:', error)
    ElMessage.error('更新失败: ' + (error.message || ''))
  }
}

// 修改密码
const handleChangePassword = async () => {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    ElMessage.error('请填写完整的密码信息')
    return
  }
  
  if (passwordForm.value.newPassword.length < 6) {
    ElMessage.error('新密码长度至少6位')
    return
  }

  try {
    const storedUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const res = await request.post('/user/change-password', {
      userId: storedUserInfo.id,
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    
    if (res.code === 200) {
      ElMessage.success('密码修改成功,请重新登录')
      changePasswordVisible.value = false
      passwordForm.value = {
        userId: null,
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
      // 清空登录状态
      setTimeout(() => {
        userStore.logout()
      }, 1500)
    } else {
      ElMessage.error(res.message || '密码修改失败')
    }
  } catch (error) {
    console.error('密码修改失败:', error)
    ElMessage.error('密码修改失败: ' + (error.message || ''))
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
  background: linear-gradient(135deg, rgba(255, 159, 67, 0.1), rgba(255, 94, 98, 0.25));
  border: 1px solid var(--border);
  border-radius: 18px;
  padding: 24px;
  box-shadow: 0 12px 30px rgba(57, 25, 5, 0.25);
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

.form-section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.04em;
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
  background: rgba(255, 159, 67, 0.12);
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
