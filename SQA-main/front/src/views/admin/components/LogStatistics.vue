<template>
  <div class="log-statistics">
    <el-row :gutter="20">
      <!-- 概览卡片 -->
      <el-col :span="6" v-for="stat in overviewStats" :key="stat.title">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-icon" :style="{ backgroundColor: stat.color }">
              <el-icon :size="24">
                <component :is="stat.icon" />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">{{ stat.title }}</div>
              <div class="stat-value">{{ stat.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 操作趋势图 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近7天操作趋势</span>
              <el-button size="small" @click="loadTrendData">刷新</el-button>
            </div>
          </template>
          <div ref="trendChart" style="height: 300px"></div>
        </el-card>
      </el-col>

      <!-- 操作类型分布 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>操作类型分布</span>
              <el-select v-model="pieType" size="small" @change="loadPieData">
                <el-option label="管理员" value="admin" />
                <el-option label="教师" value="teacher" />
                <el-option label="学生" value="student" />
              </el-select>
            </div>
          </template>
          <div ref="pieChart" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 活跃用户排行 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>活跃用户排行榜（最近7天）</span>
            </div>
          </template>
          <el-table :data="activeUsers" max-height="300">
            <el-table-column type="index" label="排名" width="60" />
            <el-table-column prop="name" label="用户名" />
            <el-table-column prop="type" label="类型" width="80">
              <template #default="scope">
                <el-tag :type="getUserTypeTag(scope.row.type)" size="small">
                  {{ getUserTypeName(scope.row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="count" label="操作次数" width="100" sortable />
          </el-table>
        </el-card>
      </el-col>

      <!-- 失败操作记录 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>近期失败操作</span>
            </div>
          </template>
          <el-table :data="failedOperations" max-height="300">
            <el-table-column prop="userName" label="用户" width="100" />
            <el-table-column prop="operation" label="操作" width="120" />
            <el-table-column prop="errorMessage" label="错误信息" show-overflow-tooltip />
            <el-table-column prop="time" label="时间" width="160" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { 
  User, 
  Document, 
  Warning, 
  Clock 
} from '@element-plus/icons-vue'
import { getLogStatistics } from '@/api/log'
import { ElMessage } from 'element-plus'

const trendChart = ref(null)
const pieChart = ref(null)
const pieType = ref('admin')

let trendChartInstance = null
let pieChartInstance = null

const overviewStats = ref([
  {
    title: '今日总操作',
    value: 0,
    icon: Document,
    color: '#409EFF'
  },
  {
    title: '今日活跃用户',
    value: 0,
    icon: User,
    color: '#67C23A'
  },
  {
    title: '失败操作',
    value: 0,
    icon: Warning,
    color: '#F56C6C'
  },
  {
    title: '平均响应时间',
    value: '0ms',
    icon: Clock,
    color: '#E6A23C'
  }
])

const activeUsers = ref([])
const failedOperations = ref([])

const loadStatistics = async () => {
  try {
    const res = await getLogStatistics()
    if (res.code === 200) {
      const { overview, activeUsers: users, failedOps } = res.data
      
      overviewStats.value[0].value = overview.todayTotal || 0
      overviewStats.value[1].value = overview.activeUsers || 0
      overviewStats.value[2].value = overview.failedCount || 0
      overviewStats.value[3].value = `${overview.avgResponseTime || 0}ms`
      
      activeUsers.value = users || []
      failedOperations.value = failedOps || []
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  }
}

const loadTrendData = async () => {
  try {
    const res = await getLogStatistics({ type: 'trend' })
    if (res.code === 200 && trendChartInstance) {
      const { dates, adminCount, teacherCount, studentCount } = res.data
      
      trendChartInstance.setOption({
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['管理员', '教师', '学生']
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: dates || []
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '管理员',
            type: 'line',
            data: adminCount || [],
            smooth: true,
            itemStyle: { color: '#409EFF' }
          },
          {
            name: '教师',
            type: 'line',
            data: teacherCount || [],
            smooth: true,
            itemStyle: { color: '#67C23A' }
          },
          {
            name: '学生',
            type: 'line',
            data: studentCount || [],
            smooth: true,
            itemStyle: { color: '#E6A23C' }
          }
        ]
      })
    }
  } catch (error) {
    console.error('加载趋势数据失败:', error)
  }
}

const loadPieData = async () => {
  try {
    const res = await getLogStatistics({ type: 'distribution', userType: pieType.value })
    if (res.code === 200 && pieChartInstance) {
      const data = res.data.operations || []
      
      pieChartInstance.setOption({
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [
          {
            name: '操作类型',
            type: 'pie',
            radius: '50%',
            data: data,
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      })
    }
  } catch (error) {
    console.error('加载分布数据失败:', error)
  }
}

const getUserTypeName = (type) => {
  const nameMap = {
    admin: '管理员',
    teacher: '教师',
    student: '学生'
  }
  return nameMap[type] || type
}

const getUserTypeTag = (type) => {
  const tagMap = {
    admin: 'danger',
    teacher: 'success',
    student: 'primary'
  }
  return tagMap[type] || ''
}

onMounted(() => {
  // 初始化图表
  if (trendChart.value) {
    trendChartInstance = echarts.init(trendChart.value)
    window.addEventListener('resize', () => {
      trendChartInstance?.resize()
    })
  }
  
  if (pieChart.value) {
    pieChartInstance = echarts.init(pieChart.value)
    window.addEventListener('resize', () => {
      pieChartInstance?.resize()
    })
  }
  
  // 加载数据
  loadStatistics()
  loadTrendData()
  loadPieData()
})

onUnmounted(() => {
  trendChartInstance?.dispose()
  pieChartInstance?.dispose()
})

defineExpose({
  loadStatistics,
  loadTrendData,
  loadPieData
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-card {
  height: 100px;
}

.stat-item {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 15px;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: var(--text-primary);
}

:deep(.el-card) {
  background-color: var(--bg-float);
  color: var(--text-primary);
}

:deep(.el-card__header) {
  background-color: var(--bg-secondary);
  border-bottom-color: var(--border-color);
}

:deep(.el-table) {
  background-color: transparent;
  color: var(--text-primary);
}

:deep(.el-table th.el-table__cell) {
  background-color: var(--bg-secondary);
  color: var(--text-primary);
}
</style>
