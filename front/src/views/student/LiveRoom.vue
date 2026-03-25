<template>
  <div class="student-live-room">
    <div v-loading="loading" class="live-container">
      <div class="live-header">
        <el-button @click="goBack" class="back-button">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <div class="live-info">
          <h2>{{ liveStream?.title }}</h2>
          <el-tag type="success">直播中</el-tag>
        </div>
      </div>

      <div class="live-body">
        <div class="video-section">
          <div class="video-player">
            <!-- 集成视频播放组件 -->
            <div class="video-wrapper" v-if="liveStream?.playUrl">
              <video 
                ref="videoPlayer"
                id="live-video-player"
                class="live-video"
                controls
                autoplay
                playsinline
                muted
              >
                您的浏览器不支持视频播放
              </video>
              <div v-if="videoLoading" class="video-overlay">
                <el-icon class="is-loading"><Loading /></el-icon>
                <p>正在加载视频流...</p>
                <p class="hint">请确保老师已开始推流</p>
              </div>
              <div v-if="videoError" class="video-overlay error">
                <el-icon><VideoPause /></el-icon>
                <p>{{ errorMessage }}</p>
                <el-button size="small" type="primary" @click="retryLoadVideo">重新加载</el-button>
              </div>
            </div>
            <div class="video-placeholder" v-else>
              <el-icon size="80"><VideoCamera /></el-icon>
              <p>等待直播开始...</p>
              <p class="hint">老师尚未开始推流</p>
            </div>
            
            <!-- 播放信息和操作按钮 -->
            <div class="player-controls" v-if="liveStream?.playUrl">
              <div class="play-info">
                <el-alert 
                  type="info" 
                  :closable="false"
                  show-icon
                >
                  <template #title>
                    <div class="info-content">
                      <span>播放地址：{{ replaceLocalAddress(liveStream?.playUrl) }}</span>
                      <span v-if="hlsSupported" class="status-success">✓ HLS 已加载</span>
                      <span v-else class="status-warning">⚠ 浏览器不支持 HLS</span>
                    </div>
                  </template>
                </el-alert>
              </div>
              <div class="control-buttons">
                <el-button type="primary" @click="copyPlayUrl(replaceLocalAddress(liveStream?.playUrl))">
                  <el-icon><CopyDocument /></el-icon>
                  复制播放地址
                </el-button>
                <el-button @click="openInPlayer(replaceLocalAddress(liveStream?.playUrl))">
                  <el-icon><VideoPlay /></el-icon>
                  使用外部播放器
                </el-button>
                <el-button @click="retryLoadVideo">
                  <el-icon><Refresh /></el-icon>
                  刷新
                </el-button>
              </div>
            </div>
          </div>
          
          <div class="live-details">
            <div class="teacher-info">
              <el-avatar :size="48">{{ liveStream?.teacherName?.charAt(0) }}</el-avatar>
              <div class="info">
                <div class="name">{{ liveStream?.teacherName }}</div>
                <div class="course">{{ liveStream?.courseName }}</div>
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
            </div>
            
            <div class="description">
              <h3>直播简介</h3>
              <p>{{ liveStream?.description || '暂无简介' }}</p>
            </div>
          </div>
        </div>

        <div class="chat-section">
          <div class="chat-header">
            <h3>聊天室</h3>
            <div class="chat-actions">
              <el-tag>{{ messages.length }} 条消息</el-tag>
              <el-button type="primary" plain size="small" @click="openFeedbackDialog">
                <el-icon><EditPen /></el-icon>
                反馈
              </el-button>
            </div>
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
              暂无消息，快来聊聊吧~
            </div>
          </div>
          
          <div class="chat-input">
            <el-input
              v-model="messageInput"
              placeholder="输入消息..."
              @keyup.enter="sendMessage"
              :maxlength="200"
              show-word-limit
            >
              <template #append>
                <el-button @click="sendMessage" :disabled="!messageInput.trim()">
                  <el-icon><Promotion /></el-icon>
                </el-button>
              </template>
            </el-input>
          </div>

          <el-dialog
            v-model="feedbackDialogVisible"
            width="480px"
            title="课堂反馈"
          >
            <el-input
              v-model="feedbackContent"
              type="textarea"
              :rows="5"
              maxlength="300"
              show-word-limit
              placeholder="随时记录你的感受、建议或不解之处"
            />
            <template #footer>
              <el-button @click="feedbackDialogVisible = false">取消</el-button>
              <el-button type="primary" :loading="feedbackSubmitting" @click="submitLiveFeedback">
                提交反馈
              </el-button>
            </template>
          </el-dialog>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, VideoCamera, User, View, Promotion, Loading, VideoPause, CopyDocument, VideoPlay, Refresh, EditPen } from '@element-plus/icons-vue'
import * as livestreamApi from '@/api/livestream'
import * as liveChatApi from '@/api/liveChat'
import * as liveFeedbackApi from '@/api/liveFeedback'
import { useUserStore } from '@/stores/user'
import Hls from 'hls.js'
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
const videoPlayer = ref(null)
const videoLoading = ref(true)
const videoError = ref(false)
const errorMessage = ref('视频加载失败，请稍后重试')
const hlsSupported = ref(false)
let hls = null
let chatWebSocket = null
const feedbackDialogVisible = ref(false)
const feedbackContent = ref('')
const feedbackSubmitting = ref(false)

onMounted(async () => {
  await loadLiveStream()
  joinLive()
  await loadChatHistory()
  // 延迟初始化WebSocket，确保DOM已渲染
  setTimeout(() => {
    initWebSocket()
  }, 500)
})

onBeforeUnmount(() => {
  leaveLive()
  destroyPlayer()
  disconnectWebSocket()
})

const loadLiveStream = async () => {
  loading.value = true
  try {
    const res = await livestreamApi.getLiveStreamById(route.params.id)
    if (res.code === 200) {
      liveStream.value = res.data
      if (res.data.status !== 'live') {
        ElMessage.warning('直播已结束')
        goBack()
      } else if (res.data.playUrl) {
        // 直播进行中，初始化播放器
        nextTick(() => {
          initPlayer(replaceLocalAddress(res.data.playUrl))
        })
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

const joinLive = async () => {
  try {
    await livestreamApi.joinLiveStream(route.params.id)
  } catch (error) {
    console.error('加入直播失败:', error)
  }
}

const leaveLive = async () => {
  try {
    await livestreamApi.leaveLiveStream(route.params.id)
  } catch (error) {
    console.error('离开直播失败:', error)
  }
}

const goBack = () => {
  router.push({ name: 'StudentLiveStream' })
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
    userName: userStore.userInfo?.name || userStore.userInfo?.nickname || userStore.userInfo?.username || '学生',
    content,
    userRole: 'student'
  }
  
  // 通过WebSocket发送消息
  if (chatWebSocket && chatWebSocket.isConnected()) {
    chatWebSocket.sendMessage(message)
    messageInput.value = ''
  } else {
    ElMessage.warning('聊天室未连接，请刷新页面重试')
  }
}

const openFeedbackDialog = () => {
  feedbackContent.value = ''
  feedbackDialogVisible.value = true
}

const submitLiveFeedback = async () => {
  const content = feedbackContent.value.trim()
  if (!content) {
    ElMessage.warning('请输入反馈内容')
    return
  }
  feedbackSubmitting.value = true
  try {
    const res = await liveFeedbackApi.submitFeedback({
      liveStreamId: Number(route.params.id),
      userId: userStore.userInfo.id,
      userName: userStore.userInfo?.name || userStore.userInfo?.nickname || '学生',
      userRole: 'student',
      phase: 'live',
      content
    })
    if (res.code === 200) {
      ElMessage.success('反馈已提交')
      feedbackDialogVisible.value = false
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error) {
    console.error('提交反馈失败', error)
    ElMessage.error('提交反馈失败')
  } finally {
    feedbackSubmitting.value = false
  }
}

const formatTime = (time) => {
  return new Date(time).toLocaleTimeString('zh-CN')
}

const copyPlayUrl = (url) => {
  navigator.clipboard.writeText(url).then(() => {
    ElMessage.success('播放地址已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

const openInPlayer = (url) => {
  // 尝试在新窗口中打开播放地址
  window.open(url, '_blank')
}

const replaceLocalAddress = (value) => {
  if (!value) return ''
  // 替换 localhost 为实际的主机地址
  if (runtimeHost && runtimeHost !== 'localhost' && runtimeHost !== '127.0.0.1') {
    return value.replace(/localhost|127\.0\.0\.1/g, runtimeHost)
  }
  return value
}

// 初始化播放器
const initPlayer = (playUrl) => {
  console.log('初始化播放器，播放地址:', playUrl)
  videoLoading.value = true
  videoError.value = false
  
  if (!videoPlayer.value) {
    console.error('视频元素未找到')
    videoError.value = true
    errorMessage.value = '播放器初始化失败'
    videoLoading.value = false
    return
  }

  // 检查 HLS 支持
  if (Hls.isSupported()) {
    console.log('浏览器支持 HLS.js')
    hlsSupported.value = true
    
    // 销毁旧的实例
    if (hls) {
      hls.destroy()
    }
    
    // 创建新的 HLS 实例
    hls = new Hls({
      debug: false,
      enableWorker: true,
      lowLatencyMode: true,
      backBufferLength: 90
    })
    
    // 加载播放源
    hls.loadSource(playUrl)
    hls.attachMedia(videoPlayer.value)
    
    // 监听事件
    hls.on(Hls.Events.MANIFEST_PARSED, () => {
      console.log('HLS manifest 解析成功')
      videoLoading.value = false
      videoError.value = false
      videoPlayer.value.play().catch(e => {
        console.warn('自动播放失败:', e)
        ElMessage.warning('请手动点击播放按钮')
      })
    })
    
    hls.on(Hls.Events.ERROR, (event, data) => {
      console.error('HLS 错误:', data)
      if (data.fatal) {
        switch (data.type) {
          case Hls.ErrorTypes.NETWORK_ERROR:
            console.error('网络错误，尝试恢复...')
            errorMessage.value = '网络连接失败，正在重试...'
            videoError.value = true
            hls.startLoad()
            break
          case Hls.ErrorTypes.MEDIA_ERROR:
            console.error('媒体错误，尝试恢复...')
            errorMessage.value = '媒体加载失败，正在重试...'
            videoError.value = true
            hls.recoverMediaError()
            break
          default:
            console.error('无法恢复的错误')
            errorMessage.value = '播放失败，请检查直播是否已开始'
            videoError.value = true
            videoLoading.value = false
            break
        }
      }
    })
  } else if (videoPlayer.value.canPlayType('application/vnd.apple.mpegurl')) {
    // Safari 原生支持 HLS
    console.log('使用原生 HLS 支持')
    hlsSupported.value = true
    videoPlayer.value.src = playUrl
    videoPlayer.value.addEventListener('loadedmetadata', () => {
      console.log('视频元数据加载成功')
      videoLoading.value = false
      videoError.value = false
      videoPlayer.value.play().catch(e => {
        console.warn('自动播放失败:', e)
      })
    })
    videoPlayer.value.addEventListener('error', (e) => {
      console.error('视频播放错误:', e)
      videoLoading.value = false
      videoError.value = true
      errorMessage.value = '播放失败，请确认直播已开始'
    })
  } else {
    console.error('浏览器不支持 HLS 播放')
    hlsSupported.value = false
    videoLoading.value = false
    videoError.value = true
    errorMessage.value = '浏览器不支持 HLS 播放，请使用 Chrome、Safari 或 Edge 浏览器'
  }
}

// 销毁播放器
const destroyPlayer = () => {
  if (hls) {
    hls.destroy()
    hls = null
  }
}

// 重新加载视频
const retryLoadVideo = () => {
  if (liveStream.value?.playUrl) {
    destroyPlayer()
    initPlayer(replaceLocalAddress(liveStream.value.playUrl))
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
</script>

<style scoped>
.student-live-room {
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

.back-button {
  background-color: #1a1b26 !important;
  border-color: #2a2b37 !important;
  color: #c0caf5 !important;
}

.back-button:hover {
  background-color: #24283b !important;
  border-color: #7dcfff !important;
  color: #7dcfff !important;
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
  background: #1a1b26;
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
  min-height: 400px;
  border: 1px solid #2a2b37;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.video-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  background: #000;
}

.live-video {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: #000;
}

.video-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.85);
  color: #fff;
  gap: 16px;
  z-index: 10;
}

.video-overlay p {
  font-size: 16px;
  margin: 0;
}

.video-overlay .hint {
  font-size: 14px;
  color: #9aa5ce;
  opacity: 0.8;
}

.video-overlay.error {
  background: rgba(26, 27, 38, 0.95);
}

.video-overlay.error {
  background: rgba(0, 0, 0, 0.8);
}

.video-overlay .el-icon {
  font-size: 48px;
}

.video-overlay p {
  margin: 0;
  font-size: 16px;
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

.player-controls {
  background: rgba(26, 27, 38, 0.9);
  padding: 16px;
  border-top: 1px solid #2a2b37;
}

.play-info {
  margin-bottom: 12px;
}

.info-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  font-family: 'Consolas', 'Monaco', monospace;
}

.status-success {
  color: #67c23a;
  font-weight: 600;
}

.status-warning {
  color: #e6a23c;
  font-weight: 600;
}

.control-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.live-details {
  background: #16161e;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid #2a2b37;
}

.teacher-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid #2a2b37;
}

.teacher-info .info {
  flex: 1;
}

.teacher-info .name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
  color: #c0caf5;
}

.teacher-info .course {
  font-size: 14px;
  color: #9aa5ce;
}

.live-stats {
  display: flex;
  gap: 24px;
  padding: 16px 0;
  border-bottom: 1px solid #2a2b37;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #c0caf5;
}

.description {
  padding-top: 16px;
}

.description h3 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: #c0caf5;
}

.description p {
  margin: 0;
  font-size: 14px;
  color: #9aa5ce;
  line-height: 1.6;
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

.chat-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
  background: #1a1b26;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chat-message {
  display: flex;
  gap: 10px;
  padding: 10px;
  background: #16161e;
  border-radius: 10px;
  border: 1px solid #2a2b37;
  transition: all 0.2s ease;
}

.chat-message:hover {
  background: #1f2029;
  border-color: #7dcfff33;
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
}

.message-user {
  font-size: 13px;
  font-weight: 600;
  color: #7dcfff;
  display: flex;
  align-items: center;
  gap: 6px;
}

.message-user.teacher {
  color: #ff9e64;
}

.message-time {
  font-size: 11px;
  color: #565f89;
}

.message-content {
  font-size: 14px;
  line-height: 1.5;
  word-wrap: break-word;
  color: #c0caf5;
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
    height: 350px;
  }
}
</style>