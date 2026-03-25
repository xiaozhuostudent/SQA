<template>
  <div class="live-list-page">
    <div class="page-header">
      <h1>课程直播</h1>
      <el-button type="primary" @click="loadLiveStreams">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="正在直播" name="live">
        <template #label>
          <span>
            <el-badge :value="liveCount" :hidden="liveCount === 0">
              正在直播
            </el-badge>
          </span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="即将开始" name="scheduled">
        <template #label>
          <span>即将开始</span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="已结束的直播" name="ended">
        <template #label>
          <span>已结束的直播</span>
        </template>
      </el-tab-pane>
    </el-tabs>

    <div v-loading="loading" class="live-grid">
      <el-empty v-if="filteredStreams.length === 0" description="暂无直播" />
      
            <div
              v-for="stream in filteredStreams"
              :key="stream.id"
              class="live-card"
              @click="enterLive(stream)"
            >
        <div class="card-cover">
          <img v-if="stream.coverImage" :src="stream.coverImage" alt="封面" />
          <div v-else class="default-cover">
            <el-icon :size="80"><VideoCamera /></el-icon>
          </div>
          <div v-if="stream.status === 'live'" class="live-badge">
            <span class="live-dot"></span>
            直播中
          </div>
          <div v-else-if="stream.status === 'scheduled'" class="scheduled-badge">
            即将开始
          </div>
        </div>
        
        <div class="card-body">
          <h3 class="stream-title">{{ stream.title }}</h3>
          <p class="stream-desc">{{ stream.description }}</p>
          
          <div class="stream-info">
            <div class="info-item">
              <el-icon><User /></el-icon>
              <span>{{ stream.teacherName }}</span>
            </div>
            <div class="info-item">
              <el-icon><Reading /></el-icon>
              <span>{{ stream.courseName }}</span>
            </div>
          </div>
          
          <div class="stream-stats">
            <div class="stat">
              <el-icon><View /></el-icon>
              <span>{{ stream.viewerCount || 0 }} 人观看</span>
            </div>
            <div class="stat">
              <el-icon><Clock /></el-icon>
              <span>{{ formatTime(stream.scheduledTime || stream.startTime) }}</span>
            </div>
          </div>
          <div
            v-if="activeTab === 'ended'"
            class="card-footer"
            @click.stop
          >
            <el-button
              type="primary"
              size="small"
              plain
              @click="openFeedbackDialog(stream)"
            >
              <el-icon><EditPen /></el-icon>
              填写反馈
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="feedbackDialogVisible"
      width="480px"
      :title="selectedStreamForFeedback ? `对【${selectedStreamForFeedback.title}】的反馈` : '提交反馈'"
    >
      <el-input
        v-model="feedbackContent"
        type="textarea"
        :rows="5"
        maxlength="300"
        show-word-limit
        placeholder="请写下你对本次直播的建议、感受或希望老师改进的地方"
      />
      <template #footer>
        <el-button @click="feedbackDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="feedbackSubmitting" @click="submitFeedback">
          提交反馈
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh, User, Reading, View, Clock, EditPen } from '@element-plus/icons-vue'
import * as livestreamApi from '@/api/livestream'
import * as liveFeedbackApi from '@/api/liveFeedback'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const activeTab = ref('live')
const liveStreams = ref([])

const feedbackDialogVisible = ref(false)
const feedbackContent = ref('')
const feedbackSubmitting = ref(false)
const selectedStreamForFeedback = ref(null)

onMounted(() => {
  loadLiveStreams()
})

const loadLiveStreams = async () => {
  loading.value = true
  try {
    const res = await livestreamApi.getAllLiveStreams()
    if (res.code === 200) {
      liveStreams.value = res.data || []
    } else {
      ElMessage.error(res.message || '加载直播列表失败')
    }
  } catch (error) {
    console.error('加载直播列表失败:', error)
    ElMessage.error('加载直播列表失败')
  } finally {
    loading.value = false
  }
}

const filteredStreams = computed(() => {
  return liveStreams.value.filter(stream => stream.status === activeTab.value)
})

const liveCount = computed(() => {
  return liveStreams.value.filter(stream => stream.status === 'live').length
})

const handleTabChange = () => {
  // Tab切换时可以刷新数据
}

const enterLive = (stream) => {
  if (stream.status === 'scheduled') {
    ElMessage.warning('直播尚未开始')
    return
  }
  router.push({ name: 'StudentLiveRoom', params: { id: stream.id } })
}

const openFeedbackDialog = (stream) => {
  selectedStreamForFeedback.value = stream
  feedbackContent.value = ''
  feedbackDialogVisible.value = true
}

const submitFeedback = async () => {
  if (!selectedStreamForFeedback.value) {
    return
  }
  const content = feedbackContent.value.trim()
  if (!content) {
    ElMessage.warning('请输入反馈内容')
    return
  }
  feedbackSubmitting.value = true
  try {
    const res = await liveFeedbackApi.submitFeedback({
      liveStreamId: selectedStreamForFeedback.value.id,
      userId: userStore.userInfo.id,
      userName: userStore.userInfo?.name || userStore.userInfo?.nickname || '学生',
      userRole: 'student',
      phase: 'post',
      content
    })
    if (res.code === 200) {
      ElMessage.success('反馈已提交，感谢你的建议')
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
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = date - now
  
  if (diff > 0 && diff < 24 * 60 * 60 * 1000) {
    return `${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')} 开始`
  }
  
  return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
.live-list-page {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.live-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  margin-top: 24px;
}

.live-card {
  background: var(--bg-secondary);
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.35s ease;
  border: 1px solid var(--border);
  box-shadow: var(--shadow-md);
}

.live-card:hover {
  transform: translateY(-6px) scale(1.01);
  box-shadow: var(--shadow-lg);
  border-color: rgba(122, 162, 247, 0.4);
}

.card-cover {
  position: relative;
  width: 100%;
  height: 180px;
  overflow: hidden;
  border-bottom: 1px solid var(--border);
  background: var(--bg-primary);
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.default-cover {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(122, 162, 247, 0.15) 0%, rgba(125, 207, 255, 0.15) 100%);
  color: var(--text-secondary);
}

.live-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(255, 77, 79, 0.95);
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}

.live-dot {
  width: 6px;
  height: 6px;
  background: var(--primary);
  border-radius: 50%;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.4;
  }
}

.scheduled-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(255, 184, 0, 0.95);
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.card-body {
  padding: 20px;
  background: var(--bg-float);
  color: var(--text-primary);
}

.stream-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-primary);
}

.stream-desc {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: 40px;
}

.stream-info {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border);
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-muted);
}

.stream-stats {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--text-muted);
}

.stat {
  display: flex;
  align-items: center;
  gap: 6px;
}

.card-footer {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
