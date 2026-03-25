<template>
  <div class="ai-assistant-container">
    <el-card class="assistant-card">
      <template #header>
        <div class="card-header">
          <span>
            <el-icon><ChatDotRound /></el-icon>
            智能助学
          </span>
          <div class="header-buttons">
            <el-button 
              type="success" 
              size="small" 
              @click="showHistoryDrawer = true"
              plain
            >
              历史会话
            </el-button>
            <el-button 
              type="primary" 
              size="small" 
              :icon="Refresh"
              @click="newChat"
            >
              新对话
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="clearChat"
              :icon="Delete"
            >
              清空对话
            </el-button>
          </div>
        </div>
      </template>

      <el-drawer
        v-model="showHistoryDrawer"
        title="历史会话"
        direction="rtl"
        size="320px"
      >
        <div class="history-panel">
          <div class="history-actions">
            <el-button type="primary" size="small" :loading="loadingSessions" @click="createSession('新对话')">新建会话</el-button>
            <el-button size="small" @click="loadSessions" :loading="loadingSessions">刷新</el-button>
          </div>
          <el-empty v-if="!sessions.length" description="暂无会话" />
          <el-scrollbar v-else style="height: 100%">
            <div 
              v-for="item in sessions" 
              :key="item.session_id || item.sessionId" 
              class="history-item"
              @click="switchSession(item)"
            >
              <div class="history-title">{{ item.session_title || item.sessionTitle || '新对话' }}</div>
              <div class="history-time">{{ item.last_time || '' }}</div>
            </div>
          </el-scrollbar>
        </div>
      </el-drawer>

      <div class="chat-container">
        <div class="messages-list" ref="messagesList">
          <div 
            v-for="(message, index) in messages" 
            :key="index" 
            :class="['message-item', message.role]"
          >
            <div class="message-avatar">
              <el-icon v-if="message.role === 'user'"><User /></el-icon>
              <el-icon v-else><ChatDotRound /></el-icon>
            </div>
            <div class="message-content">
              <!-- 图片展示 -->
              <div v-if="message.images && message.images.length > 0" class="message-images">
                <img 
                  v-for="(img, idx) in message.images" 
                  :key="idx" 
                  :src="img.url" 
                  :alt="img.name"
                  class="message-image"
                />
              </div>
              <div class="message-text" v-html="formatMessage(message.content)"></div>
              <div class="message-time">{{ message.time }}</div>
            </div>
          </div>
          
          <div v-if="loading" class="message-item assistant">
            <div class="message-avatar">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="message-content">
              <div class="typing-indicator">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>

        <div class="example-questions">
          <el-button 
            type="success" 
            plain 
            size="small"
            @click="insertExample('我有哪些课程？')"
          >
            我有哪些课程？
          </el-button>
          <el-button 
            type="success" 
            plain 
            size="small"
            @click="insertExample('我的作业提交情况如何？')"
          >
            我的作业提交情况如何？
          </el-button>
        </div>

        <div class="input-area">
          <!-- 图片预览区域 -->
          <div v-if="uploadedImages.length > 0" class="image-preview-container">
            <div 
              v-for="(image, index) in uploadedImages" 
              :key="index" 
              class="image-preview-item"
            >
              <img :src="image.url" :alt="image.name" class="preview-image" />
              <el-icon class="remove-icon" @click="removeImage(index)"><Close /></el-icon>
              <span class="image-name">{{ image.name }}</span>
            </div>
          </div>
          
          <div class="input-row">
            <el-upload
              action="#"
              :show-file-list="false"
              :before-upload="handleFileUpload"
              accept="image/*"
              multiple
            >
              <el-button 
                class="upload-button"
                :icon="Paperclip"
                title="上传图片"
                circle
              />
            </el-upload>
            
            <el-input
              v-model="userInput"
              type="textarea"
              :rows="3"
              placeholder="请输入您的问题，例如：我有哪些课程？我的作业提交情况如何？也可以上传图片让我帮您分析！"
              @keydown.enter.ctrl="sendMessage"
              :disabled="loading"
              class="message-input"
            ></el-input>
            
            <el-button 
              type="primary" 
              @click="sendMessage"
              :loading="loading"
              :icon="Promotion"
              class="send-button"
              circle
              title="发送 (Ctrl+Enter)"
            />
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, User, Delete, Promotion, Refresh, Paperclip, Close } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { marked } from 'marked'

const messages = ref([])
const sessions = ref([])
const currentSessionId = ref('')
const currentSessionTitle = ref('新对话')
const showHistoryDrawer = ref(false)
const userInput = ref('')
const loading = ref(false)
const loadingSessions = ref(false)
const messagesList = ref(null)
const uploadedImages = ref([])  // 存储上传的图片

const defaultWelcome = () => ({
  role: 'assistant',
  content: '您好！我是您的智能学习助手。您可以向我提问关于课程、作业、考试和成绩的任何问题。我还支持图片识别，您可以上传图片让我帮您分析！',
  time: new Date().toLocaleTimeString()
})

const loadSessions = async () => {
  loadingSessions.value = true
  try {
    const userData = JSON.parse(localStorage.getItem('user') || '{}')
    const userId = userData.id || 1
    const data = await request.get('/ai-assistant/sessions', {
      params: { userId, role: 'student' }
    })
    if (data.success) {
      sessions.value = data.sessions || []
      if (sessions.value.length > 0 && !currentSessionId.value) {
        currentSessionId.value = sessions.value[0].session_id || sessions.value[0].sessionId
        currentSessionTitle.value = sessions.value[0].session_title || sessions.value[0].sessionTitle || '新对话'
      }
    }
  } catch (e) {
    console.error('加载会话失败', e)
    ElMessage.error('加载历史会话失败')
  } finally {
    loadingSessions.value = false
  }
}

const createSession = async (title = '新对话') => {
  try {
    const userData = JSON.parse(localStorage.getItem('user') || '{}')
    const userId = userData.id || 1
    const data = await request.post('/ai-assistant/sessions', {
      userId,
      role: 'student',
      title
    })
    if (data.success) {
      currentSessionId.value = data.session_id || data.sessionId
      currentSessionTitle.value = data.session_title || data.sessionTitle || title
      await loadSessions()
      await loadHistory(currentSessionId.value)
    } else {
      throw new Error(data.error || '创建会话失败')
    }
  } catch (e) {
    console.error('创建会话失败', e)
    ElMessage.error('创建新对话失败')
  }
}

const loadHistory = async (sessionId) => {
  if (!sessionId) return
  try {
    const userData = JSON.parse(localStorage.getItem('user') || '{}')
    const userId = userData.id || 1
    const data = await request.get('/ai-assistant/history', {
      params: { userId, role: 'student', sessionId }
    })
    if (data.success) {
      messages.value = (data.messages || []).map(m => ({
        role: m.role,
        content: m.content,
        time: new Date().toLocaleTimeString()
      }))
      if (messages.value.length === 0) {
        messages.value = [defaultWelcome()]
      }
      await nextTick()
      scrollToBottom()
    }
  } catch (e) {
    console.error('加载历史失败', e)
    messages.value = [defaultWelcome()]
  }
}

const switchSession = async (session) => {
  currentSessionId.value = session.session_id || session.sessionId
  currentSessionTitle.value = session.session_title || session.sessionTitle || '新对话'
  showHistoryDrawer.value = false
  await loadHistory(currentSessionId.value)
}

const insertExample = (example) => {
  userInput.value = example
}

const handleFileUpload = (file) => {
  // 检查文件类型
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.warning('目前仅支持上传图片文件（jpg、png、gif等）')
    return false
  }

  // 检查文件大小（限制为10MB）
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.warning('图片大小不能超过 10MB!')
    return false
  }

  // 读取文件并转换为base64
  const reader = new FileReader()
  reader.onload = (e) => {
    const imageData = {
      name: file.name,
      url: e.target.result,  // base64格式
      file: file
    }
    uploadedImages.value.push(imageData)
    ElMessage.success(`图片 ${file.name} 上传成功！`)
  }
  reader.readAsDataURL(file)
  
  return false  // 阻止自动上传
}

const removeImage = (index) => {
  uploadedImages.value.splice(index, 1)
  ElMessage.info('图片已移除')
}

const sendMessage = async () => {
  if (!userInput.value.trim() && uploadedImages.value.length === 0) {
    ElMessage.warning('请输入问题或上传图片')
    return
  }
  if (!currentSessionId.value) {
    await createSession()
  }
  if (loading.value) return

  const userMessage = userInput.value.trim()
  const images = [...uploadedImages.value]
  
  // 添加用户消息（包含图片）
  messages.value.push({
    role: 'user',
    content: userMessage || '【图片】',
    images: images,
    time: new Date().toLocaleTimeString()
  })

  userInput.value = ''
  uploadedImages.value = []
  loading.value = true

  await nextTick()
  scrollToBottom()

  try {
    const userData = JSON.parse(localStorage.getItem('user') || '{}')
    const userId = userData.id || 1

    const history = messages.value
      .filter(msg => msg.role !== 'system')
      .map(msg => ({ 
        role: msg.role, 
        content: msg.content,
        images: msg.images || []
      }))

    const data = await request.post('/ai-assistant/chat', {
      message: userMessage,
      role: 'student',
      userId,
      sessionId: currentSessionId.value,
      sessionTitle: currentSessionTitle.value,
      history: history.slice(0, -1),
      images: images.map(img => img.url)  // 发送base64图片数据
    }, {
      timeout: 30000  // AI助手请求超时时间设为30秒
    })

    if (data.success) {
      messages.value.push({
        role: 'assistant',
        content: data.response,
        time: new Date().toLocaleTimeString()
      })
      if (!currentSessionId.value && data.sessionId) {
        currentSessionId.value = data.sessionId
      }
    } else {
      throw new Error(data.error || '请求失败')
    }
  } catch (error) {
    console.error('Error:', error)
    messages.value.push({
      role: 'assistant',
      content: '抱歉，处理您的请求时出现错误，请稍后再试。',
      time: new Date().toLocaleTimeString()
    })
  } finally {
    loading.value = false
    await nextTick()
    scrollToBottom()
  }
}

const clearChat = () => {
  messages.value = [defaultWelcome()]
  ElMessage.success('已清空当前会话')
}

const newChat = async () => {
  await createSession('新对话')
  messages.value = [defaultWelcome()]
}

const formatMessage = (content) => {
  try {
    return marked(content)
  } catch (e) {
    return content.replace(/\n/g, '<br>')
  }
}

const scrollToBottom = () => {
  if (messagesList.value) {
    messagesList.value.scrollTop = messagesList.value.scrollHeight
  }
}

onMounted(async () => {
  await loadSessions()
  if (!currentSessionId.value) {
    await createSession('新对话')
  }
  await loadHistory(currentSessionId.value)
  await nextTick()
  scrollToBottom()
})
</script>

<style scoped>
.ai-assistant-container {
  padding: 10px;
  height: 100vh;
  background: #1a1b26;
}


.assistant-card {
  height: 100%;
  display: flex;
  background: #24283b;
  border: none;
  flex-direction: column;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  overflow: hidden;
}

:deep(.el-card__header) {
  background: #1a1b26;
  border-bottom: 1px solid #414868;
  padding: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 20px;
  font-weight: 600;
  color: #c0caf5;
}

.card-header span {
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-header .el-icon {
  font-size: 24px;
}

.header-buttons {
  display: flex;
  gap: 10px;
}

.header-buttons .el-button {
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.header-buttons .el-button--success {
  background: rgba(158, 206, 106, 0.1);
  border: 1px solid #9ece6a;
  color: #9ece6a;
}

.header-buttons .el-button--primary {
  background: rgba(122, 162, 247, 0.1);
  border: 1px solid #7aa2f7;
  color: #7aa2f7;
}

.header-buttons .el-button--danger {
  background: rgba(247, 118, 142, 0.1);
  border: 1px solid #f7768e;
  color: #f7768e;
}

.header-buttons .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(122, 162, 247, 0.3);
}

.header-buttons .el-button--success:hover {
  background: #9ece6a;
  color: #1a1b26;
}

.header-buttons .el-button--primary:hover {
  background: #7aa2f7;
  color: #1a1b26;
}

.header-buttons .el-button--danger:hover {
  background: #f7768e;
  color: #1a1b26;
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 180px);
  padding: 24px;
  background: #24283b;
}

.messages-list {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: #1a1b26;
  border-radius: 16px;
  margin-bottom: 20px;
  border: 2px solid #414868;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.3);
}

.message-item {
  display: flex;
  margin-bottom: 24px;
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.message-item.user .message-avatar {
  background: linear-gradient(135deg, #7aa2f7 0%, #7dcfff 100%);
  color: #1a1b26;
  margin-left: 16px;
}

.message-item.assistant .message-avatar {
  background: linear-gradient(135deg, #9ece6a 0%, #73daca 100%);
  color: #1a1b26;
  margin-right: 16px;
}

.message-content {
  max-width: 75%;
  display: flex;
  flex-direction: column;
}

.message-text {
  padding: 16px 20px;
  border-radius: 16px;
  line-height: 1.8;
  word-wrap: break-word;
  white-space: pre-wrap;
  font-size: 15px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.message-item.user .message-text {
  background: linear-gradient(135deg, #7aa2f7 0%, #7dcfff 100%);
  color: #1a1b26;
  border-bottom-right-radius: 4px;
}

.message-item.assistant .message-text {
  background: #24283b;
  color: #c0caf5;
  border: 2px solid #414868;
  border-bottom-left-radius: 4px;
}

.message-time {
  font-size: 12px;
  color: #565f89;
  margin-top: 6px;
  padding: 0 6px;
  opacity: 0.8;
}

.message-item.user .message-time {
  text-align: right;
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 16px 20px;
}

.typing-indicator span {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, #7aa2f7 0%, #7dcfff 100%);
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.5;
  }
  30% {
    transform: translateY(-12px);
    opacity: 1;
  }
}

.example-questions {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.example-questions .el-button {
  border-radius: 20px;
  border: 2px solid #9ece6a;
  color: #9ece6a;
  font-weight: 500;
  transition: all 0.3s ease;
  background: #1a1b26;
}

.example-questions .el-button:hover {
  background: linear-gradient(135deg, #9ece6a 0%, #73daca 100%);
  color: #1a1b26;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(158, 206, 106, 0.3);
}

.input-area {
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: #1a1b26;
  padding: 16px;
  border-radius: 16px;
  border: 2px solid #414868;
}

.input-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 图片预览容器 */
.image-preview-container {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  width: 100%;
  padding: 12px;
  background: #24283b;
  border-radius: 12px;
  border: 2px dashed #7aa2f7;
}

.image-preview-item {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid #414868;
  transition: all 0.3s ease;
}

.image-preview-item:hover {
  border-color: #7aa2f7;
  box-shadow: 0 4px 12px rgba(122, 162, 247, 0.3);
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-icon {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  background: rgba(247, 118, 142, 0.9);
  color: #1a1b26;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s ease;
}

.remove-icon:hover {
  background: #f7768e;
  transform: scale(1.2);
}

.image-name {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(26, 27, 38, 0.9);
  color: #c0caf5;
  padding: 4px;
  font-size: 10px;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 消息中的图片样式 */
.message-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.message-image {
  max-width: 200px;
  max-height: 200px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid #414868;
}

.message-image:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(122, 162, 247, 0.3);
}

.message-input {
  flex: 1;
}

.input-row :deep(.el-textarea__inner) {
  border-radius: 12px;
  border: 2px solid #414868;
  padding: 12px 16px;
  font-size: 14px;
  transition: all 0.3s ease;
  resize: none;
  background: #24283b;
  color: #c0caf5;
}

.input-row :deep(.el-textarea__inner):focus {
  border-color: #7aa2f7;
  box-shadow: 0 0 0 3px rgba(122, 162, 247, 0.2);
}

.upload-button {
  width: 46px;
  height: 46px;
  background: linear-gradient(135deg, #9ece6a 0%, #73daca 100%);
  border: none;
  color: #1a1b26;
  font-size: 20px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(158, 206, 106, 0.3);
  flex-shrink: 0;
}

.upload-button:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(158, 206, 106, 0.4);
  background: linear-gradient(135deg, #73daca 0%, #9ece6a 100%);
}

.upload-button:active {
  transform: scale(0.95);
}

.send-button {
  width: 46px;
  height: 46px;
  background: linear-gradient(135deg, #7aa2f7 0%, #7dcfff 100%);
  border: none;
  color: #1a1b26;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(122, 162, 247, 0.3);
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.send-button:hover {
  background: linear-gradient(135deg, #7dcfff 0%, #7aa2f7 100%);
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(122, 162, 247, 0.4);
}

.send-button:active {
  transform: scale(0.95);
}

.tips {
  margin-top: 16px;
}

.tips :deep(.el-alert) {
  border-radius: 12px;
  border: 2px solid #414868;
  background: #24283b;
  color: #c0caf5;
}

/* 滚动条样式 */
.messages-list::-webkit-scrollbar {
  width: 8px;
}

.messages-list::-webkit-scrollbar-track {
  background: #1a1b26;
  border-radius: 4px;
}

.messages-list::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #7aa2f7 0%, #7dcfff 100%);
  border-radius: 4px;
}

.messages-list::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #7dcfff 0%, #7aa2f7 100%);
}

/* 历史会话抽屉样式 */
.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  padding: 16px;
  background: #24283b;
  border: 2px solid #414868;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.history-item:hover {
  background: #343b58;
  border-color: #7aa2f7;
  transform: translateX(-4px);
  box-shadow: 0 4px 12px rgba(122, 162, 247, 0.3);
}

.history-title {
  font-weight: 600;
  color: #c0caf5;
  margin-bottom: 8px;
  font-size: 15px;
}

.history-role {
  margin-bottom: 8px;
}

.history-content {
  color: #9aa5ce;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-time {
  font-size: 12px;
  color: #565f89;
  text-align: right;
}

.history-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
  padding: 8px;
  background: #1a1b26;
}

.history-actions {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.history-actions .el-button {
  flex: 1;
  border-radius: 8px;
  font-weight: 500;
  background: #24283b;
  border: 1px solid #414868;
  color: #c0caf5;
}

.history-actions .el-button:hover {
  background: #343b58;
  border-color: #7aa2f7;
}

/* 抽屉样式优化 */
:deep(.el-drawer) {
  border-radius: 16px 0 0 16px;
  background: #1a1b26;
}

:deep(.el-drawer__header) {
  background: #1a1b26;
  color: #c0caf5;
  padding: 20px;
  margin-bottom: 0;
  border-radius: 16px 0 0 0;
  border-bottom: 1px solid #414868;
}

:deep(.el-drawer__title) {
  color: #c0caf5;
  font-weight: 600;
  font-size: 18px;
}

:deep(.el-drawer__close-btn) {
  color: #c0caf5;
  font-size: 20px;
}
</style>
