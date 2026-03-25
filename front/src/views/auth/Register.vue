<template>
  <div class="register-container">
    <div class="register-box">
      <h2 class="title">用户注册</h2>
      <el-form :model="registerForm" :rules="rules" ref="registerFormRef" class="register-form">
        <el-form-item prop="username">
          <el-input 
            v-model="registerForm.username" 
            placeholder="请输入用户名" 
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="registerForm.password" 
            type="password" 
            placeholder="请输入密码" 
            prefix-icon="Lock"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input 
            v-model="registerForm.confirmPassword" 
            type="password" 
            placeholder="请确认密码" 
            prefix-icon="Lock"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="realName">
          <el-input 
            v-model="registerForm.realName" 
            placeholder="请输入真实姓名" 
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="email">
          <el-input 
            v-model="registerForm.email" 
            placeholder="请输入邮箱" 
            prefix-icon="Message"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input 
            v-model="registerForm.phone" 
            placeholder="请输入手机号" 
            prefix-icon="Phone"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="role">
          <el-select v-model="registerForm.role" placeholder="请选择角色" size="large" style="width: 100%">
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="registerForm.role === 'student'" prop="studentId">
          <el-input 
            v-model="registerForm.studentId" 
            placeholder="请输入学号" 
            prefix-icon="Postcard"
            size="large"
          />
        </el-form-item>
        <el-form-item v-if="registerForm.role === 'teacher'" prop="teacherId">
          <el-input 
            v-model="registerForm.teacherId" 
            placeholder="请输入工号" 
            prefix-icon="Postcard"
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            style="width: 100%" 
            @click="handleRegister"
            :loading="loading"
          >
            注册
          </el-button>
        </el-form-item>
        <div class="links">
          <router-link to="/login">已有账号，去登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/user'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  email: '',
  phone: '',
  role: 'student',
  studentId: '',
  teacherId: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  studentId: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  teacherId: [{ required: true, message: '请输入工号', trigger: 'blur' }]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await register(registerForm)
        ElMessage.success({ message: '注册成功，请登录', duration: 3000 })
        router.push('/login')
      } catch (error) {
        console.error('注册失败：', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: var(--bg-primary);
  padding: 20px 0;
  position: relative;
  overflow: hidden;
}

.register-container::before {
  content: '';
  position: absolute;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(122, 162, 247, 0.15), transparent);
  border-radius: 50%;
  top: -200px;
  right: -200px;
  animation: pulse 8s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.15; }
  50% { transform: scale(1.1); opacity: 0.25; }
}

.register-box {
  width: 480px;
  padding: 48px;
  background: var(--bg-float);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border);
  position: relative;
  z-index: 1;
}

.title {
  text-align: center;
  margin-bottom: 36px;
  color: var(--text-primary);
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.register-form {
  margin-top: 24px;
}

.links {
  text-align: right;
  margin-top: 16px;
}

.links a {
  color: var(--accent-cyan);
  text-decoration: none;
  font-weight: 500;
  font-size: 15px;
  transition: color 0.2s;
}

.links a:hover {
  color: var(--primary);
  text-decoration: underline;
}

/* Element Plus 覆盖 */
.register-box ::v-deep .el-input__wrapper {
  background: var(--bg-secondary) !important;
  border: 1px solid var(--border) !important;
  transition: all 0.25s ease;
}

.register-box ::v-deep .el-input__wrapper:hover {
  border-color: var(--primary) !important;
}

.register-box ::v-deep .el-input__wrapper.is-focus {
  border-color: var(--accent-cyan) !important;
  box-shadow: 0 0 0 3px rgba(125, 207, 255, 0.1) !important;
}

.register-box ::v-deep .el-input__inner {
  color: var(--text-primary) !important;
  font-size: 15px;
}

.register-box ::v-deep .el-input__inner::placeholder {
  color: var(--text-primary);
}

.register-box ::v-deep .el-select .el-input__wrapper {
  background: var(--bg-secondary) !important;
}

.register-box ::v-deep .el-button--primary {
  width: 100%;
  height: 46px;
  border-radius: var(--radius-md);
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, var(--primary), var(--accent-cyan)) !important;
  border: none !important;
  transition: all 0.3s ease;
}

.register-box ::v-deep .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(122, 162, 247, 0.4);
}

.register-box ::v-deep .el-form-item__error {
  color: var(--accent-red);
  font-size: 14px;
  font-weight: 500;
}

.register-box ::v-deep .el-form-item__label {
  color: var(--text-primary);
  font-weight: 500;
  font-size: 15px;
}

/* 修复下拉菜单白色背景 */
.register-box ::v-deep .el-select-dropdown {
  background-color: var(--bg-float) !important;
  border-color: var(--border) !important;
  box-shadow: var(--shadow-md);
}

.register-box ::v-deep .el-select-dropdown__item {
  background-color: var(--bg-float) !important;
  color: var(--text-primary) !important;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.2s;
}

.register-box ::v-deep .el-select-dropdown__item:hover {
  background-color: var(--bg-highlight) !important;
}

.register-box ::v-deep .el-select-dropdown__item.is-selected {
  background-color: var(--bg-highlight) !important;
  color: var(--accent-cyan) !important;
  font-weight: 600;
}
</style>
