<template>
  <div class="live-monitor">
    <el-card class="header-card">
      <template #header>
        <div class="card-header">
          <span class="title">
            <el-icon><VideoCamera /></el-icon>
            直播服务器监控
          </span>
          <el-button 
            type="primary" 
            size="small" 
            :loading="loading"
            @click="refreshData">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      
      <div v-if="error" class="error-message">
        <el-alert 
          type="error" 
          :title="error" 
          :closable="false"
          show-icon />
      </div>

      <div v-else-if="serverData" class="monitor-content">
        <!-- 服务器基础信息 -->
        <div class="info-section">
          <h3>服务器信息</h3>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="info-card">
                <div class="label">服务器版本</div>
                <div class="value">{{ serverData.self?.version || 'N/A' }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-card">
                <div class="label">进程ID</div>
                <div class="value">{{ serverData.self?.pid || 'N/A' }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-card">
                <div class="label">运行时长</div>
                <div class="value">{{ formatUptime(serverData.self?.srs_uptime) }}</div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 系统资源使用 -->
        <div class="info-section">
          <h3>系统资源</h3>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-card shadow="hover">
                <div class="resource-item">
                  <div class="resource-header">
                    <span class="resource-label">CPU 使用率</span>
                    <span class="resource-value">{{ serverData.system?.cpu_percent?.toFixed(2) || 0 }}%</span>
                  </div>
                  <el-progress 
                    :percentage="serverData.system?.cpu_percent || 0" 
                    :color="getProgressColor(serverData.system?.cpu_percent)" />
                </div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card shadow="hover">
                <div class="resource-item">
                  <div class="resource-header">
                    <span class="resource-label">内存使用率</span>
                    <span class="resource-value">{{ serverData.system?.mem_ram_percent?.toFixed(2) || 0 }}%</span>
                  </div>
                  <el-progress 
                    :percentage="serverData.system?.mem_ram_percent || 0" 
                    :color="getProgressColor(serverData.system?.mem_ram_percent)" />
                </div>
              </el-card>
            </el-col>
          </el-row>

          <el-row :gutter="20" style="margin-top: 20px;">
            <el-col :span="8">
              <el-card shadow="hover">
                <el-statistic title="系统负载 (1分钟)" :value="serverData.system?.load_1m || 0" />
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover">
                <el-statistic title="系统负载 (5分钟)" :value="serverData.system?.load_5m || 0" />
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover">
                <el-statistic title="系统负载 (15分钟)" :value="serverData.system?.load_15m || 0" />
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 网络状态 -->
        <div class="info-section">
          <h3>网络状态</h3>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-card shadow="hover">
                <el-statistic 
                  title="SRS 接收流量" 
                  :value="serverData.system?.srs_recv_bytes || 0"
                  :formatter="(value) => formatBytes(value)" />
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover">
                <el-statistic 
                  title="SRS 发送流量" 
                  :value="serverData.system?.srs_send_bytes || 0"
                  :formatter="(value) => formatBytes(value)" />
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover">
                <el-statistic 
                  title="系统连接数" 
                  :value="serverData.system?.conn_sys || 0" />
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover">
                <el-statistic 
                  title="SRS 连接数" 
                  :value="serverData.system?.conn_srs || 0" />
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 磁盘状态 -->
        <div class="info-section">
          <h3>磁盘状态</h3>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-card shadow="hover">
                <el-statistic 
                  title="磁盘读取速度" 
                  :value="serverData.system?.disk_read_KBps || 0"
                  suffix="KB/s" />
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover">
                <el-statistic 
                  title="磁盘写入速度" 
                  :value="serverData.system?.disk_write_KBps || 0"
                  suffix="KB/s" />
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover">
                <el-statistic 
                  title="磁盘繁忙度" 
                  :value="serverData.system?.disk_busy_percent || 0"
                  suffix="%" />
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 最后更新时间 -->
        <div class="update-time">
          最后更新: {{ lastUpdateTime }}
        </div>
      </div>

      <div v-else class="loading-placeholder">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { VideoCamera, Refresh, Loading } from '@element-plus/icons-vue'
import request from '@/utils/request'

const serverData = ref(null)
const loading = ref(false)
const error = ref('')
const lastUpdateTime = ref('')
let refreshInterval = null

// 获取服务器状态
const fetchServerStatus = async () => {
  try {
    loading.value = true
    error.value = ''
    const response = await request.get('/livestream/monitor/status')
    // 后端返回 {code: 200, data: {code: 0, data: {...}}}
    // 需要取第三层 data
    serverData.value = response.data?.data?.data || response.data?.data || response.data
    lastUpdateTime.value = new Date().toLocaleString('zh-CN')
  } catch (err) {
    error.value = err.message || '获取服务器状态失败'
    ElMessage.error('获取服务器状态失败')
  } finally {
    loading.value = false
  }
}

// 刷新数据
const refreshData = () => {
  fetchServerStatus()
}

// 格式化运行时长
const formatUptime = (seconds) => {
  if (!seconds) return '0秒'
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = Math.floor(seconds % 60)
  
  const parts = []
  if (hours > 0) parts.push(`${hours}小时`)
  if (minutes > 0) parts.push(`${minutes}分钟`)
  if (secs > 0 || parts.length === 0) parts.push(`${secs}秒`)
  
  return parts.join(' ')
}

// 格式化字节
const formatBytes = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// 根据百分比获取进度条颜色
const getProgressColor = (percentage) => {
  if (percentage < 50) return '#67c23a'
  if (percentage < 80) return '#e6a23c'
  return '#f56c6c'
}

onMounted(() => {
  fetchServerStatus()
  // 每30秒自动刷新
  refreshInterval = setInterval(fetchServerStatus, 30000)
})

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})
</script>

<style scoped>
.live-monitor {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
  background: var(--bg-secondary);
  border: 1px solid var(--border);
}

:deep(.el-card__header) {
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border);
}

:deep(.el-card__body) {
  background: var(--bg-secondary);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: bold;
  color: var(--text-primary);
}

.error-message {
  padding: 20px;
}

.monitor-content {
  padding: 10px;
}

.info-section {
  margin-bottom: 30px;
}

.info-section h3 {
  margin-bottom: 15px;
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 600;
  border-left: 4px solid var(--accent-cyan);
  padding-left: 10px;
}

.info-card {
  padding: 20px;
  background: var(--bg-primary);
  border: 1px solid var(--border);
  border-radius: 4px;
  text-align: center;
}

.info-card .label {
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 8px;
}

.info-card .value {
  color: var(--text-primary);
  font-size: 24px;
  font-weight: bold;
}

.resource-item {
  padding: 10px;
}

.resource-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.resource-label {
  font-size: 14px;
  color: var(--text-secondary);
}

.resource-value {
  font-size: 18px;
  font-weight: bold;
  color: var(--accent-cyan);
}

.update-time {
  text-align: right;
  color: var(--text-secondary);
  font-size: 12px;
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid var(--border);
}

.loading-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: var(--text-secondary);
  gap: 10px;
}

.loading-placeholder .el-icon {
  font-size: 32px;
}

/* Element Plus 卡片暗色适配 */
:deep(.el-card) {
  background: var(--bg-primary);
  border: 1px solid var(--border);
}

:deep(.el-card__header) {
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border);
  color: var(--text-primary);
}

:deep(.el-card__body) {
  background: var(--bg-primary);
  color: var(--text-primary);
}

:deep(.el-statistic__head) {
  font-size: 13px;
  color: var(--text-secondary);
}

:deep(.el-statistic__content) {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
}

/* 进度条颜色 */
:deep(.el-progress__text) {
  color: var(--text-primary) !important;
}
</style>
