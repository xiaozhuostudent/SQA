<template>
  <div class="statistics-page">
    <!-- 用户活跃度 -->
    <el-card>
      <template #header>
        <span>用户活跃度</span>
      </template>
      <div ref="userActivityChart" style="height: 300px;"></div>
    </el-card>

    <!-- 课程选课统计 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>课程选课排行</span>
          </template>
          <el-table :data="topCourses" style="width: 100%">
            <el-table-column type="index" label="排名" width="80" />
            <el-table-column prop="name" label="课程名称" />
            <el-table-column prop="enrolled" label="选课人数" width="120" />
            <el-table-column label="选课率" width="120">
              <template #default="scope">
                <el-progress 
                  :percentage="Math.round((scope.row.enrolled / scope.row.capacity) * 100)" 
                  :color="getProgressColor(scope.row.enrolled / scope.row.capacity)"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>作业提交率</span>
          </template>
          <div ref="homeworkChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 资源下载排行 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>资源下载排行</span>
          </template>
          <el-table :data="topResources" style="width: 100%">
            <el-table-column type="index" label="排名" width="80" />
            <el-table-column prop="name" label="资源名称" />
            <el-table-column prop="downloadCount" label="下载次数" width="120" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>系统性能</span>
          </template>
          <div class="performance-stats">
            <div class="stat-item">
              <div class="stat-label">CPU使用率</div>
              <el-progress :percentage="45" color="#409eff" />
            </div>
            <div class="stat-item">
              <div class="stat-label">内存使用率</div>
              <el-progress :percentage="62" color="#67c23a" />
            </div>
            <div class="stat-item">
              <div class="stat-label">磁盘使用率</div>
              <el-progress :percentage="78" color="#e6a23c" />
            </div>
            <div class="stat-item">
              <div class="stat-label">网络带宽</div>
              <el-progress :percentage="35" color="#909399" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { getStatistics } from '@/api/admin'

const userActivityChart = ref(null)
const homeworkChart = ref(null)

const topCourses = ref([
  { name: 'Java程序设计', enrolled: 180, capacity: 200 },
  { name: '数据结构', enrolled: 165, capacity: 180 },
  { name: '数据库原理', enrolled: 150, capacity: 200 },
  { name: 'Web开发', enrolled: 140, capacity: 160 },
  { name: '操作系统', enrolled: 130, capacity: 150 }
])

const topResources = ref([
  { name: 'Java基础教程.pdf', downloadCount: 856 },
  { name: '数据结构课件.pptx', downloadCount: 742 },
  { name: 'Spring Boot实战.pdf', downloadCount: 638 },
  { name: 'MySQL指南.pdf', downloadCount: 521 },
  { name: '算法导论.pdf', downloadCount: 489 }
])

const getProgressColor = (ratio) => {
  if (ratio < 0.6) return '#67c23a'
  if (ratio < 0.8) return '#e6a23c'
  return '#f56c6c'
}

const initUserActivityChart = () => {
  const chart = echarts.init(userActivityChart.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['学生', '教师', '管理员'] },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '学生',
        type: 'line',
        data: [320, 450, 380, 420, 390, 280, 150],
        smooth: true
      },
      {
        name: '教师',
        type: 'line',
        data: [80, 95, 88, 92, 85, 45, 20],
        smooth: true
      },
      {
        name: '管理员',
        type: 'line',
        data: [15, 18, 16, 20, 17, 12, 8],
        smooth: true
      }
    ]
  })
}

const initHomeworkChart = () => {
  const chart = echarts.init(homeworkChart.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
          { value: 850, name: '已提交' },
          { value: 120, name: '未提交' },
          { value: 50, name: '逾期提交' }
        ],
        label: {
          formatter: '{b}: {c} ({d}%)'
        }
      }
    ]
  })
}

const loadStatistics = async () => {
  try {
    const res = await getStatistics()
    // 处理统计数据
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadStatistics()
  setTimeout(() => {
    initUserActivityChart()
    initHomeworkChart()
  }, 100)
})
</script>

<style scoped>
.statistics-page {
  padding: 24px;
  background-color: var(--bg-primary);
}

.performance-stats {
  padding: 24px;
}

.stat-item {
  margin-bottom: 28px;
}

.stat-label {
  margin-bottom: 12px;
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
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

:deep(.el-progress__text) {
  color: var(--text-primary);
}
</style>
