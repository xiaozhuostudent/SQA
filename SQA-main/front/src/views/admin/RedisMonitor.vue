<template>
  <div class="redis-monitor">
    <el-card class="header-card">
      <div class="header">
        <h2>Redis 监控中心</h2>
        <div class="header-actions">
          <el-button type="success" @click="warmupCache" :loading="warmupLoading">
            <el-icon><MagicStick /></el-icon>
            数据预热
          </el-button>
          <el-button type="primary" @click="refreshData" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 概览卡片 -->
    <el-row :gutter="20" class="overview-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#409EFF"><Connection /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.connected_clients || 0 }}</div>
              <div class="stat-label">连接客户端数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#67C23A"><User /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ onlineUsers }}</div>
              <div class="stat-label">在线用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#E6A23C"><Coin /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.used_memory_human || '0B' }}</div>
              <div class="stat-label">已用内存</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#F56C6C"><Key /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ keyCount }}</div>
              <div class="stat-label">键总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Grafana 高级可视化面板 -->
    <el-card class="grafana-section">
      <template #header>
        <div class="section-header">
          <span><el-icon><TrendCharts /></el-icon> Grafana 高级监控面板</span>
          <el-switch 
            v-model="showGrafana" 
            active-text="显示" 
            inactive-text="隐藏"
            style="margin-left: auto;"
          />
        </div>
      </template>
      <div v-if="showGrafana" class="grafana-container">
        <el-tabs v-model="activeTab" type="border-card">
          <el-tab-pane label="Redis 概览" name="overview">
            <div class="iframe-container">
              <iframe
                :src="grafanaUrl + '/d-solo/redis-overview/redis-monitoring?orgId=1&refresh=5s&theme=dark&panelId=1'"
                frameborder="0"
                allowfullscreen
              ></iframe>
            </div>
          </el-tab-pane>
          <el-tab-pane label="内存使用" name="memory">
            <div class="iframe-container">
              <iframe
                :src="grafanaUrl + '/d-solo/redis-overview/redis-monitoring?orgId=1&refresh=5s&theme=dark&panelId=2'"
                frameborder="0"
                allowfullscreen
              ></iframe>
            </div>
          </el-tab-pane>
          <el-tab-pane label="命令统计" name="commands">
            <div class="iframe-container">
              <iframe
                :src="grafanaUrl + '/d-solo/redis-overview/redis-monitoring?orgId=1&refresh=5s&theme=dark&panelId=3'"
                frameborder="0"
                allowfullscreen
              ></iframe>
            </div>
          </el-tab-pane>
          <el-tab-pane label="完整面板" name="full">
            <div class="iframe-container" style="height: 800px;">
              <iframe
                :src="grafanaUrl + '/d/redis-overview/redis-monitoring?orgId=1&refresh=5s&theme=dark&kiosk'"
                frameborder="0"
                allowfullscreen
              ></iframe>
            </div>
          </el-tab-pane>
        </el-tabs>
        <div class="grafana-hint">
          <el-icon><InfoFilled /></el-icon>
          <span>Grafana 提供实时数据可视化，每5秒自动刷新。如果无法显示，请确保 Grafana 服务已启动。</span>
          <el-button link type="primary" @click="openGrafana">直接访问 Grafana</el-button>
        </div>
      </div>
    </el-card>

    <!-- 详细信息 -->
    <el-row :gutter="20" class="detail-section">
      <!-- 服务器信息 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span><el-icon><Monitor /></el-icon> 服务器信息</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="Redis 版本">
              {{ stats.redis_version || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="运行模式">
              {{ stats.redis_mode || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="操作系统">
              {{ stats.os || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="运行时长">
              {{ formatUptime(stats.uptime_in_seconds) }}
            </el-descriptions-item>
            <el-descriptions-item label="总连接数">
              {{ stats.total_connections_received || 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="总命令数">
              {{ stats.total_commands_processed || 0 }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <!-- 内存信息 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span><el-icon><Histogram /></el-icon> 内存使用</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="已用内存">
              {{ stats.used_memory_human || '0B' }}
            </el-descriptions-item>
            <el-descriptions-item label="RSS内存">
              {{ stats.used_memory_rss_human || '0B' }}
            </el-descriptions-item>
            <el-descriptions-item label="峰值内存">
              {{ stats.used_memory_peak_human || '0B' }}
            </el-descriptions-item>
            <el-descriptions-item label="碎片率">
              {{ stats.mem_fragmentation_ratio || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="输入流量">
              {{ formatBytes(stats.total_net_input_bytes) }}
            </el-descriptions-item>
            <el-descriptions-item label="输出流量">
              {{ formatBytes(stats.total_net_output_bytes) }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <!-- 持久化信息 -->
    <el-row :gutter="20" class="detail-section">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span><el-icon><FolderOpened /></el-icon> 持久化信息</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="RDB 变更数">
              {{ stats.rdb_changes_since_last_save || 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="最后保存时间">
              {{ formatTimestamp(stats.rdb_last_save_time) }}
            </el-descriptions-item>
            <el-descriptions-item label="RDB 状态">
              <el-tag :type="stats.rdb_last_bgsave_status === 'ok' ? 'success' : 'danger'">
                {{ stats.rdb_last_bgsave_status || '-' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="AOF 启用">
              <el-tag :type="stats.aof_enabled === '1' ? 'success' : 'info'">
                {{ stats.aof_enabled === '1' ? '是' : '否' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <!-- 键空间信息 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span><el-icon><DataAnalysis /></el-icon> 键空间</span>
          </template>
          <div v-if="keyspaceData.length > 0">
            <div v-for="db in keyspaceData" :key="db.name" class="keyspace-item">
              <div class="keyspace-header">{{ db.name }}</div>
              <el-progress :percentage="db.percentage" :color="customColors">
                <span>{{ db.keys }} 个键</span>
              </el-progress>
            </div>
          </div>
          <el-empty v-else description="暂无键数据" :image-size="100" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 键管理 -->
    <el-card class="key-management">
      <template #header>
        <div class="header">
          <span><el-icon><Key /></el-icon> 键管理</span>
          <div>
            <el-input
              v-model="keyPattern"
              placeholder="输入键模式 (如 test:*)"
              style="width: 200px; margin-right: 10px"
              @keyup.enter="searchKeys"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="searchKeys">搜索</el-button>
            <el-button @click="showAddKeyDialog = true">添加键</el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="keysList" stripe>
        <el-table-column prop="key" label="键名" min-width="200" />
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewKey(scope.row)">
              查看
            </el-button>
            <el-popconfirm
              title="确定删除这个键吗？"
              @confirm="deleteKey(scope.row)"
            >
              <template #reference>
                <el-button type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      
      <div v-if="keysTotal > keysList.length" class="keys-hint">
        显示 {{ keysList.length }} / {{ keysTotal }} 个键
      </div>
    </el-card>

    <!-- 查看键值对话框 -->
    <el-dialog v-model="showKeyDialog" title="键详情" width="80%" destroy-on-close>
      <div v-if="currentKey">
        <el-descriptions :column="2" border style="margin-bottom: 20px">
          <el-descriptions-item label="键名" :span="2">{{ currentKey.key }}</el-descriptions-item>
          <el-descriptions-item label="过期时间">{{ currentKey.ttl === -1 ? '永久' : currentKey.ttl + ' 秒' }}</el-descriptions-item>
          <el-descriptions-item label="数据类型">{{ getValueType(currentKey.value) }}</el-descriptions-item>
        </el-descriptions>

        <!-- 数组类型 - 用表格展示 -->
        <div v-if="isArray(currentKey.value)">
          <h4 style="margin: 16px 0 12px 0">数组内容 ({{ currentKey.value.length }} 项)</h4>
          <el-table :data="currentKey.value" border stripe max-height="500">
            <el-table-column 
              v-for="(col, index) in getTableColumns(currentKey.value)" 
              :key="index"
              :prop="col"
              :label="col"
              show-overflow-tooltip
              min-width="150"
            >
              <template #default="scope">
                <span v-if="typeof scope.row[col] === 'object'">{{ JSON.stringify(scope.row[col]) }}</span>
                <span v-else>{{ scope.row[col] }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 对象类型 - 用描述列表展示 -->
        <div v-else-if="isObject(currentKey.value)">
          <h4 style="margin: 16px 0 12px 0">对象内容</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item 
              v-for="(val, key) in currentKey.value" 
              :key="key"
              :label="key"
            >
              <pre v-if="typeof val === 'object'" class="key-value-inline">{{ JSON.stringify(val, null, 2) }}</pre>
              <span v-else>{{ val }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 字符串/数字等简单类型 -->
        <div v-else>
          <h4 style="margin: 16px 0 12px 0">值</h4>
          <pre class="key-value-content">{{ currentKey.value }}</pre>
        </div>
      </div>
    </el-dialog>

    <!-- 添加键对话框 -->
    <el-dialog v-model="showAddKeyDialog" title="添加键" width="500px">
      <el-form :model="newKey" label-width="80px">
        <el-form-item label="键名">
          <el-input v-model="newKey.key" placeholder="请输入键名" />
        </el-form-item>
        <el-form-item label="值">
          <el-input v-model="newKey.value" type="textarea" :rows="3" placeholder="请输入值" />
        </el-form-item>
        <el-form-item label="过期时间">
          <el-input v-model.number="newKey.ttl" placeholder="秒数，留空为永久">
            <template #append>秒</template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddKeyDialog = false">取消</el-button>
        <el-button type="primary" @click="addKey">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getRedisStats, getKeys, getKeyValue, deleteKeyApi, setKeyValue } from '@/api/redis'
import request from '@/utils/request'

const loading = ref(false)
const warmupLoading = ref(false)
const stats = ref({})
const keyPattern = ref('*')
const keysList = ref([])
const keysTotal = ref(0)
const showKeyDialog = ref(false)
const showAddKeyDialog = ref(false)
const currentKey = ref(null)
const onlineUsers = ref(0)
const showGrafana = ref(true)
const activeTab = ref('overview')
const grafanaUrl = ref('http://localhost:3000')
const newKey = ref({
  key: '',
  value: '',
  ttl: null
})

const customColors = [
  { color: '#f56c6c', percentage: 20 },
  { color: '#e6a23c', percentage: 40 },
  { color: '#5cb87a', percentage: 60 },
  { color: '#1989fa', percentage: 80 },
  { color: '#6f7ad3', percentage: 100 }
]

// 计算键总数
const keyCount = computed(() => {
  const db0 = stats.value.db0
  if (!db0) return 0
  const match = db0.match(/keys=(\d+)/)
  return match ? parseInt(match[1]) : 0
})

// 键空间数据
const keyspaceData = computed(() => {
  const data = []
  const db0 = stats.value.db0
  if (db0) {
    const match = db0.match(/keys=(\d+),expires=(\d+)/)
    if (match) {
      const keys = parseInt(match[1])
      const expires = parseInt(match[2])
      data.push({
        name: 'db0',
        keys: keys,
        expires: expires,
        percentage: Math.min((keys / 10000) * 100, 100)
      })
    }
  }
  return data
})

// 格式化运行时长
const formatUptime = (seconds) => {
  if (!seconds) return '-'
  const days = Math.floor(seconds / 86400)
  const hours = Math.floor((seconds % 86400) / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  return `${days}天 ${hours}小时 ${minutes}分钟`
}

// 格式化字节
const formatBytes = (bytes) => {
  if (!bytes || bytes === '0') return '0B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
}

// 格式化时间戳
const formatTimestamp = (timestamp) => {
  if (!timestamp) return '-'
  const date = new Date(timestamp * 1000)
  return date.toLocaleString('zh-CN')
}

// 获取在线用户数
const fetchOnlineUsers = async () => {
  try {
    const res = await request.get('/api/admin/cache/online-users')
    if (res.data.code === 200) {
      onlineUsers.value = res.data.data.count
    }
  } catch (error) {
    console.error('获取在线用户数失败:', error)
  }
}

// 数据预热
const warmupCache = async () => {
  warmupLoading.value = true
  try {
    const res = await request.post('/admin/cache/warmup')
    if (res.data.code === 200) {
      ElMessage.success(res.data.data.message)
      // 刷新数据
      await refreshData()
      await searchKeys()
    } else {
      ElMessage.error(res.data.message || '预热失败')
    }
  } catch (error) {
    ElMessage.error('预热失败: ' + (error.response?.data?.message || error.message))
  } finally {
    warmupLoading.value = false
  }
}

// 刷新数据
const refreshData = async () => {
  loading.value = true
  try {
    const res = await getRedisStats()
    if (res.code === 200) {
      stats.value = res.data
    }
    await fetchOnlineUsers()
  } catch (error) {
    ElMessage.error('获取 Redis 统计信息失败')
  } finally {
    loading.value = false
  }
}

// 搜索键
const searchKeys = async () => {
  try {
    const res = await getKeys(keyPattern.value, 100)
    if (res.code === 200) {
      keysList.value = res.data.keys.map(key => ({ key }))
      keysTotal.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('搜索键失败')
  }
}

// 查看键详情
const viewKey = async (row) => {
  try {
    const res = await getKeyValue(row.key)
    if (res.code === 200) {
      currentKey.value = res.data
      showKeyDialog.value = true
    }
  } catch (error) {
    ElMessage.error('获取键值失败')
  }
}

// 判断是否为数组
const isArray = (value) => {
  return Array.isArray(value)
}

// 判断是否为对象
const isObject = (value) => {
  return value !== null && typeof value === 'object' && !Array.isArray(value)
}

// 获取值的类型描述
const getValueType = (value) => {
  if (Array.isArray(value)) {
    return `数组 (${value.length} 项)`
  } else if (value === null) {
    return '空值'
  } else if (typeof value === 'object') {
    return `对象 (${Object.keys(value).length} 个属性)`
  } else {
    return typeof value
  }
}

// 获取表格列（从数组第一个对象中提取）
const getTableColumns = (arr) => {
  if (!arr || arr.length === 0) return []
  const firstItem = arr[0]
  if (typeof firstItem === 'object' && firstItem !== null) {
    return Object.keys(firstItem)
  }
  return ['value']
}

// 删除键
const deleteKey = async (row) => {
  try {
    const res = await deleteKeyApi(row.key)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      searchKeys()
      refreshData()
    }
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// 添加键
const addKey = async () => {
  if (!newKey.value.key || !newKey.value.value) {
    ElMessage.warning('请填写键名和值')
    return
  }
  
  try {
    const res = await setKeyValue(newKey.value)
    if (res.code === 200) {
      ElMessage.success('添加成功')
      showAddKeyDialog.value = false
      newKey.value = { key: '', value: '', ttl: null }
      searchKeys()
      refreshData()
    }
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

// 打开 Grafana
const openGrafana = () => {
  window.open(grafanaUrl.value, '_blank')
}

onMounted(() => {
  refreshData()
  searchKeys()
})
</script>

<style scoped lang="scss">
.redis-monitor {
  padding: 20px;
  background: #0f1419;
  min-height: 100vh;

  :deep(.el-card) {
    background: #1a1f2e;
    border: 1px solid rgba(64, 158, 255, 0.2);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 6px 16px rgba(64, 158, 255, 0.2);
      border-color: rgba(64, 158, 255, 0.3);
    }

    .el-card__header {
      background: linear-gradient(135deg, #1e3a5f 0%, #2c5282 100%);
      border-bottom: 1px solid rgba(64, 158, 255, 0.3);
      color: #fff;
    }

    .el-card__body {
      background: #1a1f2e;
    }
  }

  .header-card {
    margin-bottom: 20px;
    background: linear-gradient(135deg, #1e3a5f 0%, #2c5282 100%);
    border: 1px solid rgba(64, 158, 255, 0.3);

    :deep(.el-card__body) {
      background: transparent;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      h2 {
        margin: 0;
        font-size: 24px;
        color: #fff;
        font-weight: 600;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
      }

      .header-actions {
        display: flex;
        gap: 10px;
      }
    }
  }

  .overview-cards {
    margin-bottom: 20px;

    .stat-card {
      background: linear-gradient(135deg, #1a1f2e 0%, #232933 100%);
      border: 1px solid rgba(64, 158, 255, 0.25);
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 20px rgba(64, 158, 255, 0.25);
        border-color: rgba(64, 158, 255, 0.4);
      }

      :deep(.el-card__body) {
        background: transparent;
      }

      .stat-content {
        display: flex;
        align-items: center;
        padding: 10px;

        .stat-icon {
          font-size: 48px;
          margin-right: 20px;
          filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
        }

        .stat-info {
          flex: 1;

          .stat-value {
            font-size: 28px;
            font-weight: bold;
            color: #ffffff;
            margin-bottom: 5px;
            text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
          }

          .stat-label {
            font-size: 14px;
            color: #a8b3c5;
          }
        }
      }
    }
  }

  .detail-section {
    margin-bottom: 20px;
  }

  .grafana-section {
    margin-bottom: 20px;
    
    :deep(.el-card__header) {
      background: linear-gradient(135deg, #1e3a5f 0%, #2c5282 100%);
      border-bottom: 1px solid rgba(64, 158, 255, 0.3);
      padding: 16px 20px;
    }

    :deep(.el-card__body) {
      background: #1a1f2e;
      padding: 0;
    }

    .section-header {
      display: flex;
      align-items: center;
      color: #fff;
      font-weight: 500;
      
      .el-icon {
        margin-right: 8px;
        font-size: 18px;
        color: #409EFF;
      }
    }

    .grafana-container {
      padding: 0;

      :deep(.el-tabs) {
        .el-tabs__header {
          background: #232933;
          margin: 0;
          padding: 0 20px;
          border-bottom: 2px solid rgba(64, 158, 255, 0.2);
        }

        .el-tabs__nav-wrap {
          &::after {
            background: transparent;
          }
        }

        .el-tabs__item {
          color: #a8b3c5;
          font-weight: 500;
          padding: 0 24px;
          height: 48px;
          line-height: 48px;
          border: none;
          transition: all 0.3s ease;

          &:hover {
            color: #409EFF;
            background: rgba(64, 158, 255, 0.1);
          }

          &.is-active {
            color: #409EFF;
            background: rgba(64, 158, 255, 0.15);
            border-bottom: 2px solid #409EFF;
          }
        }

        .el-tabs__content {
          background: #1a1f2e;
          padding: 20px;
        }
      }

      .iframe-container {
        width: 100%;
        height: 500px;
        position: relative;
        background: linear-gradient(135deg, #0f1419 0%, #1a1f2e 100%);
        border-radius: 8px;
        overflow: hidden;
        box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.3),
                    0 4px 12px rgba(0, 0, 0, 0.15);
        border: 1px solid rgba(64, 158, 255, 0.2);

        iframe {
          width: 100%;
          height: 100%;
          border: none;
          border-radius: 8px;
        }

        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          height: 2px;
          background: linear-gradient(90deg, 
            transparent 0%, 
            rgba(64, 158, 255, 0.5) 50%, 
            transparent 100%);
          animation: shimmer 2s infinite;
        }
      }

      .grafana-hint {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-top: 16px;
        padding: 12px 16px;
        background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(64, 158, 255, 0.05) 100%);
        border-left: 3px solid #409EFF;
        color: #a8b3c5;
        font-size: 14px;
        border-radius: 4px;
        backdrop-filter: blur(10px);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

        .el-icon {
          color: #409EFF;
          font-size: 18px;
          flex-shrink: 0;
        }

        span {
          flex: 1;
          line-height: 1.6;
        }

        .el-button {
          flex-shrink: 0;
          font-weight: 500;
        }
      }
    }
  }

  @keyframes shimmer {
    0%, 100% {
      opacity: 0.3;
    }
    50% {
      opacity: 1;
    }
  }

  .keyspace-item {
    margin-bottom: 15px;
    padding: 12px;
    background: rgba(64, 158, 255, 0.05);
    border-radius: 6px;
    border: 1px solid rgba(64, 158, 255, 0.15);

    .keyspace-header {
      font-weight: bold;
      margin-bottom: 5px;
      color: #409EFF;
      font-size: 15px;
    }

    :deep(.el-progress) {
      .el-progress__text {
        color: #a8b3c5;
      }
    }
  }

  .key-management {
    :deep(.el-card__body) {
      background: #1a1f2e;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
    }

    :deep(.el-input) {
      .el-input__wrapper {
        background: #232933;
        border: 1px solid rgba(64, 158, 255, 0.2);
        box-shadow: none;

        &:hover {
          border-color: rgba(64, 158, 255, 0.4);
        }

        &.is-focus {
          border-color: #409EFF;
          box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
        }

        .el-input__inner {
          color: #fff;

          &::placeholder {
            color: #6b7280;
          }
        }
      }
    }

    :deep(.el-table) {
      background: #1a1f2e;
      color: #e5e7eb;

      th {
        background: #232933;
        color: #a8b3c5;
        border-bottom: 1px solid rgba(64, 158, 255, 0.2);
      }

      tr {
        background: #1a1f2e;

        &:hover > td {
          background: rgba(64, 158, 255, 0.08) !important;
        }
      }

      td {
        border-bottom: 1px solid rgba(64, 158, 255, 0.1);
      }

      &::before {
        display: none;
      }
    }

    .keys-hint {
      margin-top: 10px;
      text-align: center;
      color: #6b7280;
      font-size: 14px;
      padding: 8px;
      background: rgba(64, 158, 255, 0.05);
      border-radius: 4px;
    }
  }

  :deep(.el-dialog) {
    background: #1a1f2e;
    border: 1px solid rgba(64, 158, 255, 0.3);

    .el-dialog__header {
      background: linear-gradient(135deg, #1e3a5f 0%, #2c5282 100%);
      border-bottom: 1px solid rgba(64, 158, 255, 0.3);

      .el-dialog__title {
        color: #fff;
      }

      .el-dialog__headerbtn .el-dialog__close {
        color: #fff;

        &:hover {
          color: #409EFF;
        }
      }
    }

    .el-dialog__body {
      background: #1a1f2e;
      color: #e5e7eb;
    }

    .el-dialog__footer {
      background: #232933;
      border-top: 1px solid rgba(64, 158, 255, 0.2);
    }

    .el-descriptions {
      .el-descriptions__header {
        color: #fff;
      }

      .el-descriptions__body {
        background: #232933;
      }

      .el-descriptions__label {
        background: #1e3a5f;
        color: #a8b3c5;
      }

      .el-descriptions__content {
        background: #1a1f2e;
        color: #e5e7eb;
      }
    }

    .el-form-item__label {
      color: #a8b3c5;
    }
  }

  .key-value {
    max-height: 400px;
    overflow: auto;
    background: #0f1419;
    color: #d4d4d4;
    padding: 12px;
    border-radius: 6px;
    margin: 0;
    font-family: 'Courier New', Consolas, monospace;
    font-size: 13px;
    line-height: 1.6;
    border: 1px solid rgba(64, 158, 255, 0.2);
    box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.3);
  }

  .key-value-inline {
    background: #0f1419;
    color: #d4d4d4;
    padding: 8px;
    border-radius: 4px;
    margin: 0;
    font-family: 'Courier New', Consolas, monospace;
    font-size: 12px;
    line-height: 1.5;
    max-height: 200px;
    overflow: auto;
    border: 1px solid rgba(64, 158, 255, 0.15);
  }

  .key-value-content {
    background: #0f1419;
    color: #d4d4d4;
    padding: 12px;
    border-radius: 6px;
    margin: 0;
    font-family: 'Courier New', Consolas, monospace;
    font-size: 13px;
    line-height: 1.6;
    border: 1px solid rgba(64, 158, 255, 0.2);
    white-space: pre-wrap;
    word-break: break-all;
  }
}
</style>
