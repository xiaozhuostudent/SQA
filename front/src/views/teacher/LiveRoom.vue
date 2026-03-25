<template>
  <div class="live-room-page">
    <div v-loading="loading" class="live-container">
      <div class="live-header">
        <el-button @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <div class="live-info">
          <h2>{{ liveStream?.title }}</h2>
          <el-tag :type="getStatusType(liveStream?.status)">
            {{ getStatusText(liveStream?.status) }}
          </el-tag>
        </div>
        <el-button v-if="liveStream?.status === 'live'" type="danger" @click="endLive">
          结束直播
        </el-button>
      </div>

      <div class="live-body">
        <div class="video-section">
          <div class="video-player">
            <!-- 这里集成视频推流/播放组件 -->
            <div class="video-placeholder">
              <el-icon size="80"><VideoCamera /></el-icon>
              <p>视频播放区域</p>
              <p class="stream-url">推流地址: {{ replaceLocalAddress(liveStream?.streamUrl) }}</p>
              <p class="stream-key">推流密钥: {{ liveStream?.streamKey }}</p>
              <el-button type="primary" @click="copyStreamInfo">复制推流信息</el-button>
            </div>
          </div>
          
          <div class="live-stats">
            <div class="stat-item">
              <el-icon><User /></el-icon>
              <span>{{ liveStream?.viewerCount || 0 }} 人在线</span>
            </div>
            <div class="stat-item">
              <el-icon><View /></el-icon>
              <span>{{ liveStream?.totalViews || 0 }} 累计观看</span>
            </div>
            <div class="stat-item">
              <el-icon><Clock /></el-icon>
              <span>{{ getDuration() }}</span>
            </div>
          </div>
        </div>

        <div class="chat-section">
          <div class="chat-header">
            <h3>聊天互动</h3>
            <el-tag>{{ messages.length }} 条消息</el-tag>
          </div>
          
          <div class="chat-messages" ref="chatMessagesRef">
            <div v-for="(msg, idx) in messages" :key="idx" class="chat-message">
              <div class="message-avatar">
                <el-avatar :size="32">{{ (msg.userName || '匿名').charAt(0) }}</el-avatar>
              </div>
              <div class="message-body">
                <div class="message-header">
                  <span class="message-user" :class="{ teacher: msg.role === 'teacher' }">
                    {{ msg.userName || '匿名用户' }}
                    <el-tag v-if="msg.role === 'teacher'" type="warning" size="small">老师</el-tag>
                  </span>
                  <span class="message-time">{{ formatTime(msg.time) }}</span>
                </div>
                <div class="message-content">{{ msg.content }}</div>
              </div>
            </div>
            <div v-if="messages.length === 0" class="empty-chat">
              暂无消息
            </div>
          </div>
          
          <div class="chat-input">
            <el-input
              v-model="messageInput"
              placeholder="输入消息..."
              @keyup.enter="sendMessage"
            >
              <template #append>
                <el-button @click="sendMessage" :disabled="!messageInput.trim()">
                  发送
                </el-button>
              </template>
            </el-input>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, VideoCamera, User, View, Clock } from '@element-plus/icons-vue'
import * as livestreamApi from '@/api/livestream'
import * as liveChatApi from '@/api/liveChat'
import { useUserStore } from '@/stores/user'
import LiveChatWebSocket from '@/utils/liveChatWebSocket'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const runtimeHost = window.location.hostname || 'localhost'

const bannedWords = [
  // 中文常见粗口
  '傻逼','煞笔','沙比','傻b','sb','死妈','你妈','妈的','他妈的','尼玛','草泥马',
  '操你','操他','操她','我操','操','艹','cao',
  '逼','装逼','牛逼','傻屌','屌','叼你',
  '垃圾','废物','狗屎','狗屁','滚开','滚蛋','去死','找死','有病',

  // 中文变体 / 谐音
  '妈逼','马币','傻x','傻叉','沙雕','智障','脑残',
  'cnm','nmsl','nmb','nm','tm',

  // 英文脏话（常见）
  'fuck','fucking','shit','bullshit','wtf','damn','bitch','asshole',
  'bastard','dick','piss','crap','sex',

  // 网络常见攻击性词
  'low逼','辣鸡','撕逼','黑子','喷子','脑子进水'
]

const containsSensitiveWords = (text) => {
  const normalized = text.toLowerCase()
  return bannedWords.some(word => normalized.includes(word))
}

const loading = ref(false)
const liveStream = ref(null)
const messages = ref([])
const messageInput = ref('')
const chatMessagesRef = ref(null)
const startTime = ref(null)
const durationInterval = ref(null)
let chatWebSocket = null

onMounted(async () => {
  await loadLiveStream()
  startDurationTimer()
  await loadChatHistory()
  // 延迟初始化WebSocket
  setTimeout(() => {
    initWebSocket()
  }, 500)
})

onBeforeUnmount(() => {
  if (durationInterval.value) {
    clearInterval(durationInterval.value)
  }
  disconnectWebSocket()
})

const loadLiveStream = async () => {
  loading.value = true
  try {
    const res = await livestreamApi.getLiveStreamById(route.params.id)
    if (res.code === 200) {
      liveStream.value = res.data
      if (res.data.startTime) {
        startTime.value = new Date(res.data.startTime)
      }
    } else {
      ElMessage.error(res.message || '加载直播信息失败')
    }
  } catch (error) {
    console.error('加载直播信息失败:', error)
    ElMessage.error('加载直播信息失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push({ name: 'TeacherLiveStream' })
}

const endLive = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要结束这场直播吗？',
      '提示',
      { type: 'warning' }
    )
    
    const res = await livestreamApi.endLiveStream(route.params.id)
    if (res.code === 200) {
      ElMessage.success('直播已结束')
      goBack()
    } else {
      ElMessage.error(res.message || '结束直播失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('结束直播失败:', error)
      ElMessage.error('结束直播失败')
    }
  }
}

const copyStreamInfo = () => {
  const info = `推流地址: ${replaceLocalAddress(liveStream.value?.streamUrl)}\n推流密钥: ${liveStream.value?.streamKey}`
  navigator.clipboard.writeText(info).then(() => {
    ElMessage.success('推流信息已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}

const replaceLocalAddress = (value) => {
  if (!value) return ''
  if (!runtimeHost || runtimeHost === 'localhost') return value
  return value.replace(/localhost|127\.0\.0\.1/g, runtimeHost)
}

const sendMessage = () => {
  const content = messageInput.value.trim()
  if (!content) return

  if (containsSensitiveWords(content)) {
    ElMessage.error('请文明发言，消息未发送')
    return
  }
  
  const message = {
    userId: userStore.userInfo.id,
    userName: userStore.userInfo?.name || userStore.userInfo?.nickname || userStore.userInfo?.username || '教师',
    content,
    userRole: 'teacher'
  }
  
  // 通过WebSocket发送消息
  if (chatWebSocket && chatWebSocket.isConnected()) {
    chatWebSocket.sendMessage(message)
    messageInput.value = ''
  } else {
    ElMessage.warning('聊天室未连接，请刷新页面重试')
  }
}

// 初始化WebSocket连接
const initWebSocket = () => {
  chatWebSocket = new LiveChatWebSocket()
  chatWebSocket.connect(route.params.id, handleNewMessage)
}

// 断开WebSocket连接
const disconnectWebSocket = () => {
  if (chatWebSocket) {
    chatWebSocket.disconnect()
    chatWebSocket = null
  }
}

// 处理收到的新消息
const handleNewMessage = (message) => {
  messages.value.push({
    userName: message.userName,
    content: message.content,
    time: message.createdAt || new Date(),
    role: message.userRole
  })
  
  // 滚动到底部
  nextTick(() => {
    if (chatMessagesRef.value) {
      chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
    }
  })
}

// 加载聊天历史记录
const loadChatHistory = async () => {
  try {
    const res = await liveChatApi.getChatMessages(route.params.id)
    if (res.code === 200 && res.data) {
      messages.value = res.data.map(msg => ({
        userName: msg.userName,
        content: msg.content,
        time: msg.createdAt,
        role: msg.userRole
      }))
      
      // 滚动到底部
      nextTick(() => {
        if (chatMessagesRef.value) {
          chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
        }
      })
    }
  } catch (error) {
    console.error('加载聊天历史失败:', error)
  }
}

const formatTime = (time) => {
  return new Date(time).toLocaleTimeString('zh-CN')
}

const startDurationTimer = () => {
  durationInterval.value = setInterval(() => {
    // 触发重新渲染时长
  }, 1000)
}

const getDuration = () => {
  if (!startTime.value) return '00:00:00'
  
  const now = new Date()
  const diff = Math.floor((now - startTime.value) / 1000)
  
  const hours = Math.floor(diff / 3600)
  const minutes = Math.floor((diff % 3600) / 60)
  const seconds = diff % 60
  
  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
}

const getStatusType = (status) => {
  const types = {
    scheduled: 'info',
    live: 'success',
    ended: 'info'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    scheduled: '预定中',
    live: '直播中',
    ended: '已结束'
  }
  return texts[status] || status
}
</script>

<style scoped>
.live-room-page {
  height: 100vh;
  background: #1a1b26;
}

.live-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.live-header {
  background: #16161e;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  border-bottom: 1px solid #2a2b37;
}

.live-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.live-info h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #c0caf5;
}

.live-body {
  flex: 1;
  display: flex;
  gap: 16px;
  padding: 16px;
  overflow: hidden;
}

.video-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.video-player {
  flex: 1;
  background: #16161e;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
  border: 1px solid #2a2b37;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.video-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #7dcfff;
  gap: 16px;
  padding: 40px;
}

.stream-url,
.stream-key {
  font-size: 13px;
  color: #9aa5ce;
  font-family: 'JetBrains Mono', monospace;
  background: #1a1b26;
  padding: 8px 16px;
  border-radius: 6px;
  border: 1px solid #2a2b37;
}

.live-stats {
  background: #16161e;
  padding: 20px;
  border-radius: 12px;
  display: flex;
  gap: 32px;
  border: 1px solid #2a2b37;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  color: #c0caf5;
  font-weight: 500;
}

.chat-section {
  width: 380px;
  background: #16161e;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #2a2b37;
}

.chat-header {
  padding: 18px;
  border-bottom: 1px solid #2a2b37;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #1a1b26;
}

.chat-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #c0caf5;
}

.chat-messages {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: #1a1b26;
}

.chat-message {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #16161e;
  border-radius: 10px;
  border: 1px solid #2a2b37;
  transition: all 0.2s ease;
}

.chat-message:hover {
  background: #1f2029;
  border-color: #7dcfff33;
}

.message-avatar {
  flex-shrink: 0;
}

.message-body {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  gap: 8px;
}

.message-user {
  font-size: 13px;
  color: #7dcfff;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}

.message-user.teacher {
  color: #ff9e64;
}

.message-content {
  font-size: 14px;
  color: #c0caf5;
  line-height: 1.5;
  word-wrap: break-word;
}

.message-time {
  font-size: 11px;
  color: #565f89;
  white-space: nowrap;
}

.empty-chat {
  text-align: center;
  color: #565f89;
  padding: 60px 20px;
  font-size: 14px;
}

.chat-input {
  padding: 16px;
  border-top: 1px solid #2a2b37;
  background: #16161e;
}

@media (max-width: 1024px) {
  .live-body {
    flex-direction: column;
  }
  
  .chat-section {
    width: 100%;
    height: 300px;
  }
}
</style>
