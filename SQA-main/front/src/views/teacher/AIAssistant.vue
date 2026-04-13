<template>
  <div class="ai-assistant-container">
    <el-card class="assistant-card">
      <template #header>
        <div class="card-header">
          <span>
            <el-icon><ChatDotRound /></el-icon>
            智能助教
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
            @click="insertExample('我的课程中有多少学生？')"
          >
            我的课程中有多少学生？
          </el-button>
          <el-button 
            type="success" 
            plain 
            size="small"
            @click="insertExample('作业提交情况如何？')"
          >
            作业提交情况如何？
          </el-button>
        </div>

        <div class="input-area">
          <el-upload
            action="#"
            :show-file-list="false"
            :before-upload="handleFileUpload"
            accept=".pdf,.doc,.docx,.txt,.jpg,.jpeg,.png"
          >
            <el-button 
              class="upload-button"
              :icon="Paperclip"
              title="上传文件"
            >
              <span class="upload-text"></span>
            </el-button>
          </el-upload>
          <el-input
            v-model="userInput"
            type="textarea"
            :rows="3"
            placeholder="请输入您的问题，例如：我的课程中有多少学生？作业提交情况如何？"
            @keydown.enter.ctrl="sendMessage"
            :disabled="loading"
          ></el-input>
          <el-button 
            type="primary" 
            @click="sendMessage"
            :loading="loading"
            :icon="Promotion"
            class="send-button"
          >
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, User, Delete, Promotion, Refresh, Paperclip } from '@element-plus/icons-vue'
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

const defaultWelcome = () => ({
  role: 'assistant',
  content: '您好！我是您的智能教学助手。我可以帮助您管理课程、作业、考试和学生信息。',
  time: new Date().toLocaleTimeString()
})

const loadSessions = async () => {
  loadingSessions.value = true
  try {
    const userData = JSON.parse(localStorage.getItem('user') || '{}')
    const userId = userData.id || 1
    const data = await request.get('/ai-assistant/sessions', {
      params: { userId, role: 'teacher' }
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
      role: 'teacher',
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
      params: { userId, role: 'teacher', sessionId }
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
  ElMessage.warning('文件上传功能正在开发中，敬请期待！')
  return false
}

const sendMessage = async () => {
  if (!userInput.value.trim()) {
    ElMessage.warning('请输入问题')
    return
  }
  if (!currentSessionId.value) {
    await createSession()
  }
  if (loading.value) return

  const userMessage = userInput.value.trim()
  messages.value.push({
    role: 'user',
    content: userMessage,
    time: new Date().toLocaleTimeString()
  })

  userInput.value = ''
  loading.value = true

  await nextTick()
  scrollToBottom()

  try {
    const userData = JSON.parse(localStorage.getItem('user') || '{}')
    const userId = userData.id || 1

    const history = messages.value
      .filter(msg => msg.role !== 'system')
      .map(msg => ({ role: msg.role, content: msg.content }))

    const data = await request.post('/ai-assistant/chat', {
      message: userMessage,
      role: 'teacher',
      userId,
      sessionId: currentSessionId.value,
      sessionTitle: currentSessionTitle.value,
      history: history.slice(0, -1)
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
  padding: 20px;
  height: calc(100vh - 120px);
}

.assistant-card {
  height: 100%;
  display: flex;
  background: var(--bg-float);
  border: 1px solid var(--border);
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
  color: #ffffff;
}

.card-header span {
  display: flex;
  align-items: center;
  gap: 10px;
  background: linear-gradient(135deg, #7aa2f7 0%, #7dcfff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.card-header .el-icon {
  font-size: 24px;
  color: #7dcfff;
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
  border-color: rgba(158, 206, 106, 0.4);
  color: #9ece6a;
}

.header-buttons .el-button--success:hover {
  background: rgba(158, 206, 106, 0.2);
  border-color: #9ece6a;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(158, 206, 106, 0.3);
}

.header-buttons .el-button--primary {
  background: linear-gradient(135deg, #7aa2f7 0%, #7dcfff 100%);
  border: none;
  box-shadow: 0 2px 8px rgba(122, 162, 247, 0.3);
}

.header-buttons .el-button--primary:hover {
  background: linear-gradient(135deg, #89b4fa 0%, #89ddff 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(125, 207, 255, 0.4);
}

.header-buttons .el-button--danger {
  background: rgba(247, 118, 142, 0.1);
  border-color: rgba(247, 118, 142, 0.4);
  color: #f7768e;
}

.header-buttons .el-button--danger:hover {
  background: rgba(247, 118, 142, 0.2);
  border-color: #f7768e;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(247, 118, 142, 0.3);
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 250px);
}

.messages-list {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: rgba(22, 33, 62, 0.3);
  border-radius: 8px;
  margin-bottom: 20px;
  border: 1px solid rgba(125, 207, 255, 0.15);
}

.messages-list::-webkit-scrollbar {
  width: 8px;
}

.messages-list::-webkit-scrollbar-thumb {
  background: rgba(125, 207, 255, 0.3);
  border-radius: 4px;
}

.message-item {
  display: flex;
  margin-bottom: 24px;
  gap: 14px;
  animation: messageSlide 0.3s ease;
}

@keyframes messageSlide {
  from {
    opacity: 0;
    transform: translateY(10px);
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
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.message-item.user .message-avatar {
  background: linear-gradient(135deg, #7aa2f7 0%, #7dcfff 100%);
  color: white;
}

.message-item.assistant .message-avatar {
  background: linear-gradient(135deg, #9ece6a 0%, #73daca 100%);
  color: white;
}

.message-content {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

.message-text {
  padding: 14px 18px;
  border-radius: 12px;
  line-height: 1.7;
  word-wrap: break-word;
  white-space: pre-wrap;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.message-item.user .message-text {
  background: linear-gradient(135deg, #7aa2f7 0%, #7dcfff 100%);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.message-item.assistant .message-text {
  background: rgba(125, 207, 255, 0.1);
  color: #ffffff;
  border: 1px solid rgba(125, 207, 255, 0.2);
}

.message-time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 6px;
  padding: 0 4px;
}

.message-item.user .message-time {
  text-align: right;
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 12px 16px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(125, 207, 255, 0.6);
  animation: typing 1.4s infinite;
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
  }
  30% {
    transform: translateY(-10px);
  }
}

.example-questions {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
  padding: 16px;
  background: rgba(22, 33, 62, 0.3);
  border-radius: 12px;
  border: 1px solid rgba(125, 207, 255, 0.15);
}

.example-questions .el-button {
  background: rgba(125, 207, 255, 0.1);
  border: 1px solid rgba(125, 207, 255, 0.3);
  color: #7dcfff;
  transition: all 0.3s ease;
  font-weight: 500;
  padding: 8px 16px;
}

.example-questions .el-button:hover {
  background: rgba(125, 207, 255, 0.2);
  border-color: #7dcfff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(125, 207, 255, 0.3);
}

.input-area {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 20px 24px;
  border-top: 1px solid rgba(125, 207, 255, 0.2);
  background: rgba(22, 33, 62, 0.5);
}

.input-area :deep(.el-textarea) {
  flex: 1;
}

.input-area :deep(.el-textarea__inner) {
  background: rgba(22, 33, 62, 0.8);
  border: 1px solid rgba(125, 207, 255, 0.3);
  color: #ffffff;
  box-shadow: none;
}

.upload-button {
  background: rgba(125, 207, 255, 0.1);
  border: 1px solid rgba(125, 207, 255, 0.3);
  color: #7dcfff;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(125, 207, 255, 0.2);
}

.upload-button:hover {
  background: rgba(125, 207, 255, 0.2);
  border-color: #7dcfff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(125, 207, 255, 0.3);
}

.upload-button:active {
  transform: translateY(0);
}

.upload-text {
  margin-left: 6px;
}

.send-button {
  height: 40px;
  background: linear-gradient(135deg, #7aa2f7 0%, #7dcfff 100%);
  border: none;
  color: white;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(122, 162, 247, 0.3);
  transition: all 0.3s ease;
}

.send-button:hover {
  background: linear-gradient(135deg, #89b4fa 0%, #89ddff 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(125, 207, 255, 0.4);
}

.tips {
  margin-top: 16px;
}

.tips :deep(.el-alert) {
  border-radius: 8px;
  background: rgba(125, 207, 255, 0.1);
  border: 1px solid rgba(125, 207, 255, 0.2);
  color: #ffffff;
}

/* 历史会话抽屉样式 */
.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  padding: 16px;
  background: rgba(22, 33, 62, 0.5);
  border: 1px solid rgba(125, 207, 255, 0.15);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.history-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, #7aa2f7 0%, #7dcfff 100%);
  opacity: 0;
  transition: opacity 0.3s;
}

.history-item:hover {
  background: rgba(125, 207, 255, 0.15);
  border-color: #7dcfff;
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(125, 207, 255, 0.2);
}

.history-item:hover::before {
  opacity: 1;
}

.history-role {
  margin-bottom: 8px;
  color: #7dcfff;
  font-weight: 500;
}

.history-content {
  color: var(--text-primary);
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
  color: rgba(125, 207, 255, 0.6);
  display: flex;
  align-items: center;
  gap: 4px;
}

.history-time::before {
  content: '🕐';
  font-size: 14px;
}

.history-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
}

.history-actions {
  display: flex;
  gap: 10px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(125, 207, 255, 0.2);
}

.history-actions .el-button {
  flex: 1;
  border-radius: 8px;
  font-weight: 500;
}

.history-title {
  font-weight: 600;
  margin-bottom: 8px;
  color: #ffffff;
  font-size: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
