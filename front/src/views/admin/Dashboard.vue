<template>
  <div class="admin-dashboard">
    <!-- 公告通知轮播 -->
    <el-card class="announcement-card" v-if="announcements.length > 0">
      <el-carousel height="120px" :interval="5000" indicator-position="outside">
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

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff;">
              <el-icon :size="40"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalUsers }}</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67c23a;">
              <el-icon :size="40"><Reading /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalCourses }}</div>
              <div class="stat-label">课程总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6a23c;">
              <el-icon :size="40"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalResources }}</div>
              <div class="stat-label">资源总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f56c6c;">
              <el-icon :size="40"><View /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.todayVisits }}</div>
              <div class="stat-label">今日访问</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据图表 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>用户增长趋势</span>
          </template>
          <div ref="userChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>课程类型分布</span>
          </template>
          <div ref="courseChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 系统通知 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>系统通知</span>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="notice in notices"
              :key="notice.id"
              :timestamp="notice.time"
              :type="notice.type"
            >
              {{ notice.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>待处理事项</span>
          </template>
          <el-table :data="todos" style="width: 100%">
            <el-table-column prop="title" label="事项" />
            <el-table-column prop="priority" label="优先级" width="100">
              <template #default="scope">
                <el-tag :type="getPriorityType(scope.row.priority)">
                  {{ scope.row.priority }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="scope">
                <el-button size="small" type="primary" @click="handleTodo(scope.row)">
                  处理
                </el-button>
              </template>
            </el-table-column>
          </el-table>
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
      :role="'admin'"
      :userId="userStore.user?.id"
      :username="userStore.user?.username"
      @update:visible="showAIAssistant = $event"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { User, Reading, Document, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import AIAssistant from '@/components/AIAssistant.vue'

const userStore = useUserStore()
const showAIAssistant = ref(false)
import * as echarts from 'echarts'
import { getAnnouncementsByRole, getStatistics } from '@/api/admin'

const stats = ref({
  totalUsers: 0,
  totalCourses: 0,
  totalResources: 0,
  todayVisits: 0
})

const announcements = ref([])

const notices = ref([
  { id: 1, time: '2025-11-20 10:00', type: 'success', content: '新学期课程安排已发布' },
  { id: 2, time: '2025-11-19 15:30', type: 'warning', content: '系统将于本周六进行维护' },
  { id: 3, time: '2025-11-18 09:00', type: 'info', content: '新增100个用户注册' }
])

const todos = ref([])

const userChart = ref(null)
const courseChart = ref(null)

// 加载公告
const loadAnnouncements = async () => {
  try {
    const res = await getAnnouncementsByRole('admin', 5)
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

const getPriorityType = (priority) => {
  const types = { '高': 'danger', '中': 'warning', '低': 'info' }
  return types[priority] || 'info'
}

const handleTodo = (todo) => {
  ElMessage.info(`处理事项：${todo.title}`)
}

const loadStats = async () => {
  try {
    const res = await getStatistics()
    if (res.code === 200) {
      const data = res.data
      stats.value = {
        totalUsers: data.users?.totalUsers || 0,
        totalCourses: data.courses?.totalCourses || 0,
        totalResources: data.resources?.totalResources || 0,
        todayVisits: data.users?.activeUsers || 0
      }
      
      // 更新图表数据
      if (data.userActivity?.weekly) {
        initUserChart(data.userActivity.weekly)
      } else {
        initUserChart()
      }
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    // 使用默认值
    stats.value = {
      totalUsers: 0,
      totalCourses: 0,
      totalResources: 0,
      todayVisits: 0
    }
  }
}

const initUserChart = (weeklyData = null) => {
  const chart = echarts.init(userChart.value)
  
  let xAxisData, seriesData
  if (weeklyData && weeklyData.length > 0) {
    xAxisData = weeklyData.map(item => item.day)
    seriesData = weeklyData.map(item => item.students + item.teachers + item.admins)
  } else {
    xAxisData = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    seriesData = [12, 18, 25, 30, 28, 35, 42]
  }
  
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: xAxisData
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '活跃用户',
        type: 'line',
        data: seriesData,
        smooth: true,
        itemStyle: { color: '#409eff' }
      }
    ]
  })
}

const initCourseChart = () => {
  const chart = echarts.init(courseChart.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [
      {
        type: 'pie',
        radius: '60%',
        data: [
          { value: 35, name: '计算机类' },
          { value: 25, name: '数学类' },
          { value: 15, name: '物理类' },
          { value: 10, name: '其他' }
        ]
      }
    ]
  })
}

onMounted(() => {
  loadStats()
  loadAnnouncements()
  setTimeout(() => {
    initCourseChart()
  }, 100)
})
</script>

<style scoped>
.admin-dashboard {
  padding: 24px;
  background-color: var(--bg-primary);
}

.announcement-card {
  margin-bottom: 24px;
  background-color: var(--bg-float);
  border-color: var(--border);
}

.announcement-content {
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.announcement-content:hover {
  background-color: var(--bg-secondary);
}

.announcement-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
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
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

:deep(.el-carousel__indicator--horizontal) {
  padding: 8px 4px;
}

:deep(.el-carousel__button) {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  height: 120px;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: var(--bg-float);
  border: 1px solid var(--border);
  display: flex;
  align-items: center;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary);
}

.stat-card :deep(.el-card__body) {
  width: 100%;
  padding: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 20px;
  box-shadow: var(--shadow-sm);
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
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: 8px;
  font-weight: 500;
}

.charts-row {
  margin-bottom: 24px;
}

:deep(.el-card) {
  background-color: var(--bg-float);
  border-color: var(--border);
}

:deep(.el-card__header) {
  background-color: var(--bg-secondary);
  color: var(--text-primary);
  font-weight: 600;
}

:deep(.el-table) {
  background-color: var(--bg-float);
  color: var(--text-primary);
}

:deep(.el-table th.el-table__cell) {
  background-color: var(--bg-secondary);
  color: var(--text-primary);
}

:deep(.el-table td.el-table__cell) {
  border-color: var(--border);
}

:deep(.el-timeline-item__timestamp) {
  color: var(--text-primary);
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
