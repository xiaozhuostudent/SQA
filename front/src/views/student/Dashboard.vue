<template>
  <div class="dashboard">
    <!-- 公告通知轮播 -->
    <el-card class="announcement-card" v-if="announcements.length > 0">
      <el-carousel height="160px" :interval="5000" indicator-position="outside">
        <el-carousel-item v-for="item in announcements" :key="item.id">
          <div class="announcement-content" @click="viewAnnouncement(item)">
            <div class="announcement-header">
              <el-tag :type="getAnnouncementTypeTag(item.type)" size="small">
                {{ getAnnouncementTypeText(item.type) }}
              </el-tag>
              <el-tag :type="getPriorityTag(item.priority)" size="small" style="margin-left: 10px">
                {{ getPriorityText(item.priority) }}
              </el-tag>
              <span class="announcement-time">{{ formatDateTime(item.publishTime) }}</span>
            </div>
            <h3 class="announcement-title">{{ item.title }}</h3>
            <p class="announcement-text">{{ item.content }}</p>
          </div>
        </el-carousel-item>
      </el-carousel>
    </el-card>

    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#409eff"><Reading /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.courseCount }}</div>
              <div class="stat-label">选修课程</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#67c23a"><Document /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.homeworkCount }}</div>
              <div class="stat-label">待提交作业</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#e6a23c"><DataAnalysis /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.experimentCount }}</div>
              <div class="stat-label">实验任务</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#f56c6c"><TrophyBase /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.avgScore }}</div>
              <div class="stat-label">平均成绩</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>最近课程</span>
          </template>
          <el-table :data="recentCourses" style="width: 100%" v-loading="loading" element-loading-text="加载中...">
            <el-table-column prop="name" label="课程名称" />
            <el-table-column prop="teacher" label="教师" />
            <el-table-column prop="time" label="上课时间" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-icon><Bell /></el-icon>
              <span>通知公告</span>
            </div>
          </template>
          <div v-if="todoList.length === 0" class="empty-notice">
            <el-icon class="empty-icon"><InfoFilled /></el-icon>
            <p>暂无新通知</p>
          </div>
          <el-timeline v-else>
            <el-timeline-item v-for="item in todoList" :key="item.id" :timestamp="item.deadline">
              {{ item.title }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- AI助手悬浮按钮 -->
    <div class="ai-float-button" @click="showAIAssistant = true">
      <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="ai-icon">
        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z" fill="currentColor"/>
        <circle cx="8.5" cy="10.5" r="1.5" fill="currentColor"/>
        <circle cx="15.5" cy="10.5" r="1.5" fill="currentColor"/>
        <path d="M12 17.5c2.33 0 4.32-1.45 5.12-3.5H6.88c.8 2.05 2.79 3.5 5.12 3.5z" fill="currentColor"/>
      </svg>
      <div class="ai-label">AI助手</div>
    </div>
    
    <!-- AI助手对话框 -->
    <AIAssistant 
      :visible="showAIAssistant"
      :role="'student'"
      :userId="userStore.user?.id"
      :username="userStore.user?.username"
      @update:visible="showAIAssistant = $event"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStudentDashboard } from '@/api/statistics'
import { getAnnouncementsByRole } from '@/api/admin'
import { useUserStore } from '@/stores/user'
import AIAssistant from '@/components/AIAssistant.vue'

const userStore = useUserStore()

const loading = ref(false)
const showAIAssistant = ref(false)

const stats = ref({
  courseCount: 0,
  homeworkCount: 0,
  experimentCount: 0,
  avgScore: 0
})

const recentCourses = ref([])
const todoList = ref([])
const announcements = ref([])

// 加载公告
const loadAnnouncements = async () => {
  try {
    const res = await getAnnouncementsByRole('student', 5)
    const data = res.data || res
    if (data.success !== false) {
      announcements.value = data || []
    }
  } catch (error) {
    console.error('加载公告失败:', error)
  }
}

// 查看公告详情
const viewAnnouncement = (announcement) => {
  ElMessageBox.alert(
    `<div style="line-height: 1.6; white-space: pre-wrap;">${announcement.content}</div>`,
    announcement.title,
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭'
    }
  )
}

// 获取公告类型标签
const getAnnouncementTypeTag = (type) => {
  const map = {
    system: 'primary',
    course: 'success',
    exam: 'warning',
    other: 'info'
  }
  return map[type] || 'info'
}

// 获取公告类型文本
const getAnnouncementTypeText = (type) => {
  const map = {
    system: '系统通知',
    course: '课程通知',
    exam: '考试通知',
    other: '其他'
  }
  return map[type] || type
}

// 获取优先级标签
const getPriorityTag = (priority) => {
  const map = {
    high: 'danger',
    medium: 'warning',
    low: 'info'
  }
  return map[priority] || 'info'
}

// 获取优先级文本
const getPriorityText = (priority) => {
  const map = {
    high: '高优先级',
    medium: '中优先级',
    low: '低优先级'
  }
  return map[priority] || priority
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN', { 
    month: 'numeric', 
    day: 'numeric', 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

const loadDashboard = async () => {
  loading.value = true
  try {
    const res = await getStudentDashboard()
    if (res.code === 200) {
      const data = res.data
      stats.value = {
        courseCount: data.courseCount || 0,
        homeworkCount: data.homeworkCount || 0,
        experimentCount: data.experimentCount || 0,
        avgScore: data.avgScore || 0
      }
      recentCourses.value = data.recentCourses || []
      todoList.value = data.todoList || []
    } else {
      ElMessage.error(res.message || '获取数据失败')
    }
  } catch (error) {
    console.error('获取学生仪表板数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadAnnouncements()
  loadDashboard()
})
</script>

<style scoped>
.dashboard {
  padding: 24px;
  background-color: var(--bg-primary);
}

/* 公告卡片样式 */
.announcement-card {
  margin-bottom: 20px;
  cursor: pointer;
  background-color: var(--bg-float);
  border: 1px solid var(--border);
}

.announcement-card:hover {
  border-color: var(--primary);
}

.announcement-content {
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.announcement-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.announcement-time {
  margin-left: auto;
  font-size: 12px;
  color: var(--text-secondary);
}

.announcement-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 10px 0;
}

.announcement-text {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  max-height: 70px;
}

:deep(.el-carousel__indicator--horizontal) {
  padding: 8px 4px;
}

:deep(.el-carousel__button) {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.stat-card {
  height: 120px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: var(--bg-float);
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
  display: flex;
  align-items: center;
}

.stat-card:hover {
  transform: translateY(-4px);
  border-color: var(--primary);
  box-shadow: var(--shadow-md);
}

.stat-card :deep(.el-card__body) {
  width: 100%;
  padding: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 18px;
  height: 100%;
}

.stat-icon {
  font-size: 48px;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: 8px;
  font-weight: 500;
}

/* Tokyo Night 卡片样式 */
.dashboard ::v-deep .el-card {
  background: var(--bg-float);
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
}

.dashboard ::v-deep .el-card__header {
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border);
  color: var(--text-primary);
  font-weight: 600;
}

.dashboard ::v-deep .el-table {
  background: transparent;
  color: var(--text-primary);
}

.dashboard ::v-deep .el-table th {
  background: var(--bg-secondary);
  color: var(--text-primary);
  border-color: var(--border);
}

.dashboard ::v-deep .el-table td {
  border-color: var(--border);
}

.dashboard ::v-deep .el-table tr {
  background: transparent;
}

.dashboard ::v-deep .el-table tr:hover > td {
  background: var(--bg-highlight) !important;
}

/* 空状态样式 */
.empty-notice {
  text-align: center;
  padding: 40px 20px;
  color: var(--text-secondary);
}

.empty-icon {
  font-size: 48px;
  color: var(--text-disabled);
  margin-bottom: 12px;
}

/* AI助手悬浮按钮 */
.ai-float-button {
  position: fixed;
  right: 30px;
  bottom: 30px;
  width: 160px;
  height: 56px;
  z-index: 999;
  background: linear-gradient(135deg, #7aa2f7 0%, #7dcfff 100%);
  border-radius: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  cursor: pointer;
  box-shadow: 0 8px 24px rgba(122, 162, 247, 0.4), 
              0 4px 12px rgba(125, 207, 255, 0.3);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
}

.ai-float-button::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(135deg, #7aa2f7, #7dcfff, #b4f9f8, #89ddff);
  border-radius: 28px;
  opacity: 0;
  z-index: -1;
  transition: opacity 0.3s ease;
  animation: rotate 3s linear infinite;
}

.ai-float-button:hover::before {
  opacity: 1;
}

.ai-float-button:hover {
  transform: translateY(-4px) scale(1.05);
  box-shadow: 0 12px 32px rgba(122, 162, 247, 0.5), 
              0 6px 16px rgba(125, 207, 255, 0.4);
}

.ai-float-button:active {
  transform: translateY(-2px) scale(1.02);
  transition: all 0.1s ease;
}

.ai-icon {
  width: 28px;
  height: 28px;
  color: white;
}

.ai-label {
  color: white;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

@keyframes rotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

</style>
