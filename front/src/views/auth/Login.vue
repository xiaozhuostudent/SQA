<template>
  <div class="login-container" style="position:relative; min-height:100vh;">
    <!-- 背景SVG -->
    <div class="bg-overlay">
      <svg class="background-logo" viewBox="0 0 1200 800" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <linearGradient id="primaryGradient" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" style="stop-color:#4facfe;stop-opacity:0.15" />
            <stop offset="100%" style="stop-color:#00b4db;stop-opacity:0.15" />
          </linearGradient>
        </defs>
        
        <circle cx="200" cy="150" r="120" fill="url(#primaryGradient)" opacity="0.4"/>
        <circle cx="950" cy="600" r="180" fill="url(#primaryGradient)" opacity="0.3"/>
        <circle cx="1050" cy="200" r="90" fill="url(#primaryGradient)" opacity="0.3"/>
      </svg>
    </div>
    
    <!-- 主要内容区 -->
    <div class="login-content">
      <!-- 左侧产品介绍 -->
      <div class="product-intro">
        <div class="product-logo">
          <svg viewBox="0 0 120 120" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <linearGradient id="logoGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color:#4facfe"/>
                <stop offset="100%" style="stop-color:#00b4db"/>
              </linearGradient>
              <filter id="glow">
                <feGaussianBlur stdDeviation="2" result="coloredBlur"/>
                <feMerge>
                  <feMergeNode in="coloredBlur"/>
                  <feMergeNode in="SourceGraphic"/>
                </feMerge>
              </filter>
            </defs>
            <!-- 外圈光晕 -->
            <circle cx="60" cy="60" r="48" fill="none" stroke="url(#logoGradient)" stroke-width="1.5" opacity="0.3"/>
            <!-- 主书本形状 -->
            <path d="M35 35 L35 85 C35 85 40 80 60 80 C80 80 85 85 85 85 L85 35 C85 35 80 40 60 40 C40 40 35 35 35 35 Z" 
                  fill="url(#logoGradient)" opacity="0.9" filter="url(#glow)"/>
            <!-- 书页线条 -->
            <line x1="60" y1="40" x2="60" y2="80" stroke="white" stroke-width="2" opacity="0.6"/>
            <path d="M45 50 L55 50" stroke="white" stroke-width="1.5" opacity="0.5"/>
            <path d="M45 58 L55 58" stroke="white" stroke-width="1.5" opacity="0.5"/>
            <path d="M45 66 L55 66" stroke="white" stroke-width="1.5" opacity="0.5"/>
            <path d="M65 50 L75 50" stroke="white" stroke-width="1.5" opacity="0.5"/>
            <path d="M65 58 L75 58" stroke="white" stroke-width="1.5" opacity="0.5"/>
            <path d="M65 66 L75 66" stroke="white" stroke-width="1.5" opacity="0.5"/>
            <!-- 智能符号 -->
            <circle cx="60" cy="30" r="8" fill="#00f2fe" opacity="0.8"/>
            <path d="M57 30 L59 32 L63 28" stroke="white" stroke-width="2" fill="none" stroke-linecap="round"/>
          </svg>
        </div>
        
        <h1 class="product-name">
          <span class="name-ya">Ya</span><span class="name-li">Li</span>
        </h1>
        <p class="product-slogan">基于大模型的全场景教学系统</p>
        
        <div class="product-features">
          <div class="feature-item">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M4 19.5C4 18.837 4.26339 18.2011 4.73223 17.7322C5.20107 17.2634 5.83696 17 6.5 17H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M6.5 2H20V22H6.5C5.83696 22 5.20107 21.7366 4.73223 21.2678C4.26339 20.7989 4 20.163 4 19.5V4.5C4 3.83696 4.26339 3.20107 4.73223 2.73223C5.20107 2.26339 5.83696 2 6.5 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M8 7H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M8 11H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="feature-text">
              <h3>智能课程管理</h3>
              <p>AI辅助课程设计与规划，多班级智能排课系统，教学资源一键分发，课程数据实时同步</p>
            </div>
          </div>
          
          <div class="feature-item">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M16 4H18C18.5304 4 19.0391 4.21071 19.4142 4.58579C19.7893 4.96086 20 5.46957 20 6V20C20 20.5304 19.7893 21.0391 19.4142 21.4142C19.0391 21.7893 18.5304 22 18 22H6C5.46957 22 4.96086 21.7893 4.58579 21.4142C4.21071 21.0391 4 20.5304 4 20V6C4 5.46957 4.21071 4.96086 4.58579 4.58579C4.96086 4.21071 5.46957 4 6 4H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M15 2H9C8.44772 2 8 2.44772 8 3V5C8 5.55228 8.44772 6 9 6H15C15.5523 6 16 5.55228 16 5V3C16 2.44772 15.5523 2 15 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M9 12L11 14L15 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="feature-text">
              <h3>AI智能批改</h3>
              <p>大模型驱动的自动评分系统，智能代码检测与分析，个性化学习建议生成，作业质量深度评估</p>
            </div>
          </div>
          
          <div class="feature-item">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M3 3V21H21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M18 9L13 14L9 10L3 16" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="18" cy="9" r="1.5" fill="currentColor"/>
                <circle cx="13" cy="14" r="1.5" fill="currentColor"/>
                <circle cx="9" cy="10" r="1.5" fill="currentColor"/>
              </svg>
            </div>
            <div class="feature-text">
              <h3>智能数据分析</h3>
              <p>AI驱动的学习行为分析，多维度成绩预测模型，个性化学情报告生成，智能预警与干预推荐</p>
            </div>
          </div>
          
          <div class="feature-item">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M21 15C21 15.5304 20.7893 16.0391 20.4142 16.4142C20.0391 16.7893 19.5304 17 19 17H7L3 21V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M8 9H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M8 13H13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="feature-text">
              <h3>AI智能助手</h3>
              <p>24小时智能答疑系统，知识图谱构建，个性化学习路径规划，智能内容推荐引擎</p>
            </div>
          </div>
          
          <div class="feature-item">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="2" y="3" width="20" height="14" rx="2" stroke="currentColor" stroke-width="2"/>
                <path d="M8 21H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M12 17V21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <circle cx="7" cy="9" r="1.5" fill="currentColor"/>
                <circle cx="12" cy="9" r="1.5" fill="currentColor"/>
                <circle cx="17" cy="9" r="1.5" fill="currentColor"/>
              </svg>
            </div>
            <div class="feature-text">
              <h3>实时互动课堂</h3>
              <p>高清低延迟视频直播，AI实时语音转文字，智能互动白板系统，自动生成课堂笔记</p>
            </div>
          </div>
          
          <div class="feature-item">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M9 11L12 14L22 4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M21 12V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H16" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="feature-text">
              <h3>智能考试系统</h3>
              <p>AI智能组卷与难度评估，自动阅卷与成绩分析，知识点掌握度评测，个性化补充练习推荐</p>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧登录框 -->
      <div class="login-box glass-card">
        <div class="logo-container">
          <div class="logo-icon">
            <svg viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
              <defs>
                <linearGradient id="miniGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" style="stop-color:#4facfe"/>
                  <stop offset="100%" style="stop-color:#00b4db"/>
                </linearGradient>
              </defs>
              <!-- 书本形状 -->
              <path d="M18 18 L18 46 C18 46 20 44 32 44 C44 44 46 46 46 46 L46 18 C46 18 44 20 32 20 C20 20 18 18 18 18 Z" 
                    fill="url(#miniGradient)" opacity="0.95"/>
              <!-- 中线和页面 -->
              <line x1="32" y1="20" x2="32" y2="44" stroke="white" stroke-width="1.5" opacity="0.7"/>
              <line x1="24" y1="26" x2="28" y2="26" stroke="white" stroke-width="1" opacity="0.6"/>
              <line x1="24" y1="30" x2="28" y2="30" stroke="white" stroke-width="1" opacity="0.6"/>
              <line x1="36" y1="26" x2="40" y2="26" stroke="white" stroke-width="1" opacity="0.6"/>
              <line x1="36" y1="30" x2="40" y2="30" stroke="white" stroke-width="1" opacity="0.6"/>
              <!-- 智能标记 -->
              <circle cx="32" cy="14" r="4" fill="#00f2fe" opacity="0.9"/>
              <path d="M30.5 14 L31.5 15 L33.5 13" stroke="white" stroke-width="1" fill="none"/>
            </svg>
          </div>
          <h2 class="title">基于大模型的全场景教学系统</h2>
        </div>
      
      <!-- 登录方式切换 -->
      <div class="login-tabs">
        <el-radio-group v-model="loginType" size="large" style="width: 100%; margin-bottom: 24px;">
          <el-radio-button label="password" style="width: 50%;">
            <span style="font-size: 15px;">密码登录</span>
          </el-radio-button>
          <el-radio-button label="code" style="width: 50%;">
            <span style="font-size: 15px;">验证码登录</span>
          </el-radio-button>
        </el-radio-group>
      </div>
      
      <!-- 密码登录 -->
      <el-form 
        v-if="loginType === 'password'" 
        :model="loginForm" 
        :rules="passwordRules" 
        ref="loginFormRef" 
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="请输入用户名" 
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="请输入密码" 
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button 
            class="btn-primary-cool" 
            size="large" 
            @click="handleLogin"
            :loading="loading"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 验证码登录 -->
      <el-form 
        v-else 
        :model="codeForm" 
        :rules="codeRules" 
        ref="codeFormRef" 
        class="login-form"
      >
        <el-form-item prop="receiver">
          <el-input 
            v-model="codeForm.receiver" 
            placeholder="请输入邮箱" 
            prefix-icon="Message"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="code">
          <el-input 
            v-model="codeForm.code" 
            placeholder="请输入验证码" 
            prefix-icon="Key"
            size="large"
            @keyup.enter="handleCodeLogin"
            class="code-input-wrapper"
          >
            <template #append>
              <el-button 
                @click="sendCode" 
                :disabled="countdown > 0"
                :loading="sendingCode"
                class="send-code-btn"
              >
                {{ countdown > 0 ? `${countdown}秒后重试` : '发送验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item prop="role">
          <el-select v-model="codeForm.role" placeholder="请选择角色" size="large" style="width: 100%">
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button 
            class="btn-primary-cool" 
            size="large" 
            @click="handleCodeLogin"
            :loading="loading"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref(null)
const codeFormRef = ref(null)
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)

// 登录方式
const loginType = ref('password')
const loginOptions = [
  { label: '密码登录', value: 'password' },
  { label: '验证码登录', value: 'code' }
]

// 密码登录表单
const loginForm = reactive({
  username: '',
  password: ''
})

const passwordRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 验证码登录表单
const codeForm = reactive({
  receiver: '',
  code: '',
  type: 'email', // email 或 sms
  role: 'student'
})

const validateReceiver = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入邮箱'))
  } else {
    // 支持邮箱和手机号格式验证
    const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    const phoneReg = /^1[3-9]\d{9}$/
    if (!emailReg.test(value) && !phoneReg.test(value)) {
      callback(new Error('请输入正确的邮箱或手机号'))
    } else {
      callback()
    }
  }
}

const codeRules = {
  receiver: [{ required: true, validator: validateReceiver, trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

// 密码登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      
      try {
        const response = await request({
          url: '/auth/login',
          method: 'post',
          data: loginForm
        })
        
        const data = response.data || response
        
        if (data.code === 200 || data.success !== false) {
          const result = data.data || data
          userStore.setToken(result.token)
          userStore.setUserInfo(result.userInfo || result.user)
          
          ElMessage.success('登录成功')
          
          // 根据用户信息中的角色跳转
          const userInfo = result.userInfo || result.user
          const roleRoutes = {
            student: '/student/dashboard',
            teacher: '/teacher/dashboard',
            admin: '/admin/dashboard'
          }
          
          router.push(roleRoutes[userInfo.role])
        } else {
          ElMessage.error(data.message || '登录失败')
        }
      } catch (error) {
        console.error('登录失败:', error)
        // 显示详细的错误信息
        if (error.response) {
          // 服务器返回错误响应
          const errorData = error.response.data
          if (errorData && errorData.message) {
            ElMessage.error(errorData.message)
          } else if (error.response.status === 401) {
            ElMessage.error('用户名或密码错误')
          } else if (error.response.status === 500) {
            ElMessage.error('服务器错误，请稍后重试')
          } else {
            ElMessage.error('登录失败，请检查用户名和密码')
          }
        } else if (error.request) {
          // 请求已发出但没有收到响应
          ElMessage.error('网络连接失败，请检查网络设置')
        } else {
          // 其他错误
          ElMessage.error('登录失败，请稍后重试')
        }
      } finally {
        loading.value = false
      }
    }
  })
}

// 发送验证码
const sendCode = async () => {
  if (!codeForm.receiver) {
    ElMessage.warning('请输入邮箱')
    return
  }
  
  // 验证邮箱格式
  const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailReg.test(codeForm.receiver)) {
    ElMessage.error('邮箱格式不正确')
    return
  }
  
  sendingCode.value = true
  
  try {
    const response = await request({
      url: '/auth/send-code',
      method: 'post',
      data: {
        receiver: codeForm.receiver,
        type: codeForm.type
      }
    })
    
    const data = response.data || response
    
    if (data.code === 200 || data.success !== false) {
      ElMessage.success(data.message || '验证码已发送')
      
      // 开始倒计时
      countdown.value = 60
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    } else {
      ElMessage.error(data.message || '发送失败')
    }
  } catch (error) {
    console.error('发送验证码失败:', error)
  } finally {
    sendingCode.value = false
  }
}

// 验证码登录
const handleCodeLogin = async () => {
  if (!codeFormRef.value) return
  
  await codeFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      
      try {
        const response = await request({
          url: '/auth/login/code',
          method: 'post',
          data: codeForm
        })
        
        const data = response.data || response
        
        if (data.code === 200 || data.success !== false) {
          const result = data.data || data
          userStore.setToken(result.token)
          userStore.setUserInfo(result.userInfo || result.user)
          
          ElMessage.success('登录成功')
          
          // 根据角色跳转
          const roleRoutes = {
            student: '/student/dashboard',
            teacher: '/teacher/dashboard',
            admin: '/admin/dashboard'
          }
          
          router.push(roleRoutes[codeForm.role])
        } else {
          ElMessage.error(data.message || '登录失败')
        }
      } catch (error) {
        console.error('登录失败:', error)
        // 显示详细的错误信息
        if (error.response) {
          // 服务器返回错误响应
          const errorData = error.response.data
          if (errorData && errorData.message) {
            ElMessage.error(errorData.message)
          } else if (error.response.status === 401) {
            ElMessage.error('验证码错误或已过期')
          } else if (error.response.status === 404) {
            ElMessage.error('用户不存在')
          } else if (error.response.status === 500) {
            ElMessage.error('服务器错误，请稍后重试')
          } else {
            ElMessage.error('登录失败，请检查验证码是否正确')
          }
        } else if (error.request) {
          // 请求已发出但没有收到响应
          ElMessage.error('网络连接失败，请检查网络设置')
        } else {
          // 其他错误
          ElMessage.error('登录失败，请稍后重试')
        }
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1d2e 0%, #16213e 50%, #1a1d2e 100%);
  position: relative;
  overflow: hidden;
  padding: 20px 40px;
}

.bg-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  opacity: 0.8;
}

.background-logo {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.login-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1700px;
  width: 100%;
  gap: 100px;
  position: relative;
  z-index: 1;
  height: 100%;
  padding-left: 40px;
}

/* 左侧产品介绍 */
.product-intro {
  flex: 1.2;
  color: white;
  max-width: 900px;
}

.product-logo {
  width: 110px;
  height: 110px;
  margin-bottom: 28px;
  opacity: 0.95;
}

.product-logo svg {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 8px 24px rgba(79, 172, 254, 0.3));
}

.product-name {
  font-size: 82px;
  font-weight: 900;
  margin: 0 0 16px 0;
  letter-spacing: 5px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  font-family: 'Arial', 'Helvetica', sans-serif;
}

.name-ya {
  background: linear-gradient(135deg, #4facfe 0%, #00d4ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 0 30px rgba(79, 172, 254, 0.5);
  position: relative;
}

.name-li {
  background: linear-gradient(135deg, #00f2fe 0%, #4facfe 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 0 30px rgba(0, 242, 254, 0.5);
  position: relative;
}

.product-slogan {
  font-size: 26px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0 0 40px 0;
  font-weight: 500;
  letter-spacing: 2px;
}

.product-features {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18px 20px;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
  padding: 20px;
  background: rgba(79, 172, 254, 0.08);
  border-radius: 14px;
  border: 1px solid rgba(79, 172, 254, 0.2);
  transition: all 0.3s ease;
}

.feature-item:hover {
  background: rgba(79, 172, 254, 0.14);
  border-color: rgba(79, 172, 254, 0.35);
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(79, 172, 254, 0.2);
}

.feature-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #4facfe;
  background: rgba(79, 172, 254, 0.15);
  border-radius: 12px;
  margin-bottom: 4px;
}

.feature-icon svg {
  width: 28px;
  height: 28px;
}

.feature-text h3 {
  margin: 0 0 8px 0;
  font-size: 19px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 0.5px;
}

.feature-text p {
  margin: 0;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.75);
  line-height: 1.65;
}

/* 右侧登录框 */
.login-box {
  width: 480px;
  height: 500px;
  flex-shrink: 0;
  padding: 32px 30px;
  border-radius: 20px;
  background: rgba(26, 29, 46, 0.85);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
  position: relative;
  margin-right: 20px;
}

.logo-container {
  text-align: center;
  margin-bottom: 24px;
}

.logo-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 10px;
  background: linear-gradient(135deg, #4facfe 0%, #00b4db 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(79, 172, 254, 0.3);
}

.logo-icon svg {
  width: 32px;
  height: 32px;
}

.title {
  text-align: center;
  margin: 0;
  color: #ffffff;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.5px;
  background: linear-gradient(135deg, #ffffff 0%, #a8e6ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.4;
}

.login-form { 
  height: 250px ;
  margin-top: 22px; 
}

/* Element Plus Tokyo Night 覆盖 */
.login-box ::v-deep .el-input__wrapper { 
  background: rgba(22, 33, 62, 0.6);
  border: 1px solid rgba(79, 172, 254, 0.2);
  box-shadow: none;
  border-radius: 10px;
  padding: 1px 11px;
  transition: all 0.3s ease;
}

.login-box ::v-deep .el-input__wrapper:hover { 
  border-color: rgba(79, 172, 254, 0.3);
  background: rgba(22, 33, 62, 0.7);
}

.login-box ::v-deep .el-input__wrapper.is-focus { 
  border-color: #4facfe;
  background: rgba(22, 33, 62, 0.8);
  box-shadow: none;
}

.login-box ::v-deep .el-input__inner { 
  color: #ffffff;
  font-size: 14px;
  line-height: 20px;
}

.login-box ::v-deep .el-input__inner::placeholder { 
  color: rgba(255, 255, 255, 0.4);
}

.login-box ::v-deep .el-input__prefix {
  color: rgba(79, 172, 254, 0.8);
}

.login-box ::v-deep .el-select .el-input__wrapper { 
  background: rgba(22, 33, 62, 0.6);
}

.login-box ::v-deep .el-button { 
  width: 100%;
  height: 40px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, #4facfe 0%, #00b4db 100%);
  border: none !important;
  outline: none !important;
  color: white;
  box-shadow: 0 4px 16px rgba(79, 172, 254, 0.35);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.login-box ::v-deep .el-button:hover {
  background: linear-gradient(135deg, #5fbdff 0%, #00c5e6 100%);
  box-shadow: 0 6px 24px rgba(79, 172, 254, 0.45);
  transform: translateY(-2px);
}

.login-box ::v-deep .el-button:active {
  transform: translateY(0px);
  box-shadow: 0 2px 12px rgba(79, 172, 254, 0.35);
}

.login-box ::v-deep .el-form-item {
  margin-bottom: 15px;
}

/* 修复下拉菜单白色背景 */
.login-box ::v-deep .el-select-dropdown { 
  background-color: rgba(22, 33, 62, 0.95) !important;
  border-color: rgba(79, 172, 254, 0.3) !important;
  backdrop-filter: blur(10px);
  border-radius: 10px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
}

.login-box ::v-deep .el-select-dropdown__item { 
  background-color: transparent !important;
  color: #ffffff !important;
  font-size: 15px;
}

.login-box ::v-deep .el-select-dropdown__item:hover { 
  background-color: rgba(79, 172, 254, 0.15) !important;
}

.login-box ::v-deep .el-select-dropdown__item.is-selected { 
  background: linear-gradient(90deg, rgba(79, 172, 254, 0.3) 0%, rgba(0, 180, 219, 0.3) 100%) !important;
  color: #4facfe !important;
  font-weight: 600;
}

/* 登录方式切换样式 */
.login-tabs {
  margin-bottom: 24px;
}

.login-box ::v-deep .el-radio-group {
  display: flex;
  width: 100%;
  background: rgba(22, 33, 62, 0.4);
  border-radius: 12px;
  padding: 4px;
  border: 1px solid rgba(79, 172, 254, 0.15);
}

.login-box ::v-deep .el-radio-button {
  flex: 1;
}

.login-box ::v-deep .el-radio-button__inner {
  width: 100%;
  background: transparent;
  border: none !important;
  outline: none !important;
  color: rgba(255, 255, 255, 0.6);
  font-weight: 500;
  border-radius: 8px;
  padding: 10px 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: none;
}

.login-box ::v-deep .el-radio-button__original-radio:checked + .el-radio-button__inner {
  background: linear-gradient(135deg, #4facfe 0%, #00b4db 100%);
  color: #ffffff;
  font-weight: 600;
  box-shadow: 0 4px 16px rgba(79, 172, 254, 0.5);
  border: none !important;
}

.login-box ::v-deep .el-radio-button:first-child .el-radio-button__inner {
  border-radius: 8px;
  border: none !important;
}

.login-box ::v-deep .el-radio-button:last-child .el-radio-button__inner {
  border-radius: 8px;
  border: none !important;
}

.login-box ::v-deep .el-radio-button__inner:hover {
  color: #ffffff;
  background: rgba(79, 172, 254, 0.2);
  transform: scale(1.02);
}

/* 去掉所有可能的白边 */
.login-box ::v-deep .el-radio-button {
  border: none !important;
  box-shadow: none !important;
}

.login-box ::v-deep .el-button--large {
  border: none !important;
}

/* 分段控制器样式 */
.login-box ::v-deep .el-segmented { 
  background: rgba(22, 33, 62, 0.6);
  border: 1px solid rgba(79, 172, 254, 0.2);
  border-radius: 10px;
  padding: 4px;
}

.login-box ::v-deep .el-segmented__item { 
  color: rgba(255, 255, 255, 0.6);
  font-weight: 500;
  border-radius: 8px;
}

.login-box ::v-deep .el-segmented__item.is-selected { 
  background: linear-gradient(135deg, #4facfe 0%, #00b4db 100%);
  color: #ffffff;
  font-weight: 600;
  box-shadow: none;
}

/* 验证码输入框样式 */
.login-box ::v-deep .el-input-group__append { 
  background: rgba(22, 33, 62, 0.4);
  border: none !important;
  outline: none !important;
  border-radius: 0 10px 10px 0;
  padding: 0;
  overflow: hidden;
  min-width: 130px;
  box-shadow: none !important;
}

.login-box ::v-deep .el-input-group__append .el-button { 
  height: 100%;
  min-width: 130px;
  white-space: nowrap;
  padding: 0 18px;
  background: linear-gradient(135deg, rgba(79, 172, 254, 0.15) 0%, rgba(0, 180, 219, 0.15) 100%);
  color: #4facfe;
  font-weight: 600;
  font-size: 14px;
  border: none !important;
  outline: none !important;
  box-shadow: none !important;
  transition: all 0.3s ease;
}

.login-box ::v-deep .el-input-group__append .el-button:hover { 
  color: #5fbdff;
  background: linear-gradient(135deg, rgba(79, 172, 254, 0.25) 0%, rgba(0, 180, 219, 0.25) 100%);
  box-shadow: none;
  transform: scale(1.02);
}

.login-box ::v-deep .el-input-group__append .el-button.is-disabled { 
  color: rgba(255, 255, 255, 0.3);
  background: rgba(22, 33, 62, 0.3);
  cursor: not-allowed;
}

.login-box ::v-deep .el-input-group .el-input__wrapper { 
  border-right: none !important;
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
  margin-right: 0 !important;
}

.login-box ::v-deep .el-input-group__append { 
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
  border: none !important;
  outline: none !important;
  margin-left: 0 !important;
}

.login-box ::v-deep .el-input-group {
  display: flex;
  border: none !important;
  box-shadow: none !important;
}

</style>





