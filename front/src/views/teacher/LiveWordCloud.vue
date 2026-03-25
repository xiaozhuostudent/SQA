<template>
  <div class="word-cloud-view">
    <div class="page-header">
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2>{{ liveStream?.title }} · 分析</h2>
      <el-button type="primary" @click="refreshWordCloud" :loading="loading">
        <el-icon><Refresh /></el-icon>
        刷新数据
      </el-button>
    </div>

    <div v-loading="loading" class="content">
      <el-row :gutter="20">
        <el-col :span="24">
          <el-card class="stats-card">
            <div class="stats-grid">
              <div class="stat-item">
                <div class="stat-icon">
                  <el-icon size="32"><ChatDotRound /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ wordCloudData?.totalMessages || 0 }}</div>
                  <div class="stat-label">总消息数</div>
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon">
                  <el-icon size="32"><Document /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ wordCloudData?.totalWords || 0 }}</div>
                  <div class="stat-label">总词数</div>
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon">
                  <el-icon size="32"><Document /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ feedbackTotal }}</div>
                  <div class="stat-label">总反馈数</div>
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon">
                  <el-icon size="28"><Clock /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ formatDateOnly(wordCloudData?.generatedAt) }}</div>
                  <div class="stat-label">生成日期</div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <div class="analysis-panels">
        <el-tabs v-model="activePanel" class="analysis-tabs">
        <el-tab-pane label="聊天词云与排行" name="word">
          <el-row :gutter="20">
            <el-col :span="24">
              <el-card class="word-cloud-card">
                <template #header>
                  <div class="card-header">
                    <span>聊天词云图</span>
                    <div class="analysis-actions">
                      <el-button text @click="downloadWordCloud">
                        <el-icon><Download /></el-icon>
                        下载图片
                      </el-button>
                      <el-button text @click="openEnlargeWordCloud">
                        <el-icon><FullScreen /></el-icon>
                        放大查看
                      </el-button>
                    </div>
                  </div>
                </template>
                <div v-if="wordList.length > 0" ref="wordCloudChart" class="word-cloud-container"></div>
                <el-empty v-else description="暂无词云数据"></el-empty>
              </el-card>
            </el-col>

            <el-col :span="24">
              <el-card class="word-list-card">
                <template #header>
                  <span>高频词排行榜（前10）</span>
                </template>
                <el-table
                  v-if="wordList.length > 0"
                  :data="wordList.slice(0, 10)"
                  stripe
                  class="compact-word-table"
                >
                  <el-table-column type="index" label="排名" width="80" align="center" header-align="center" />
                  <el-table-column prop="word" label="词语" align="center" header-align="center" />
                  <el-table-column prop="count" label="出现次数" width="140" align="center" header-align="center">
                    <template #default="{ row }">
                      <el-tag type="primary">{{ row.count }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="频率分布" width="260" align="center" header-align="center">
                    <template #default="{ row }">
                      <div class="frequency-progress">
                        <el-progress 
                          :percentage="Math.round((row.count / maxCount) * 100)" 
                          :show-text="false"
                        />
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
                <el-empty v-else description="暂无数据"></el-empty>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>

        <el-tab-pane label="学生反馈与AI报告" name="analysis">
          <el-row :gutter="20">
            <el-col :span="24">
              <el-card
                class="analysis-card"
                v-loading="reportLoading || reportGenerating"
              >
                <template #header>
                  <div class="card-header">
                    <span>AI 直播分析报告</span>
                    <div class="analysis-actions">
                      <el-button text @click="loadAnalysisReport">
                        <el-icon><Refresh /></el-icon>
                        刷新
                      </el-button>
                      <el-button type="primary" @click="handleGenerateReport" :loading="reportGenerating">
                        <el-icon><Download /></el-icon>
                        生成报告
                      </el-button>
                    </div>
                  </div>
                </template>
                <div v-if="analysisReport?.reportContent" class="analysis-content">
                  <p class="analysis-meta">
                    最后更新：{{ formatDateTime(analysisReport.updatedAt || analysisReport.generatedAt) }}
                  </p>
                  <pre>{{ analysisReport.reportContent }}</pre>
                </div>
                <el-empty v-else :description="analysisStatus || '尚未生成报告'" />
              </el-card>
            </el-col>

            <el-col :span="24">
              <el-card class="feedback-card">
                <template #header>
                  <div class="card-header">
                    <span>学生反馈</span>
                    <el-tag effect="dark" type="info">{{ feedbackList.length }} 条</el-tag>
                  </div>
                </template>
                <div v-if="feedbackList.length > 0" class="feedback-scroll">
                  <el-table :data="feedbackList" stripe>
                    <el-table-column prop="userName" label="学生" width="140" />
                    <el-table-column label="阶段" width="120">
                      <template #default="{ row }">
                        <el-tag :type="row.phase === 'live' ? 'success' : 'info'" size="small">
                          {{ formatPhase(row.phase) }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="content" label="反馈内容" />
                    <el-table-column label="时间" width="200">
                      <template #default="{ row }">
                        {{ formatDateTime(row.createdAt) }}
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
                <el-empty v-else description="暂无学生反馈" />
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <el-dialog
      v-model="enlargeVisible"
      width="70%"
      :destroy-on-close="false"
      class="word-cloud-dialog"
      @closed="handleLargeDialogClosed"
    >
      <template #title>
        聊天词云图（放大）
      </template>
      <div v-if="wordList.length > 0" ref="largeWordCloudChart" class="word-cloud-container large"></div>
      <el-empty v-else description="暂无词云数据" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Refresh, ChatDotRound, Document, Clock, Download, FullScreen } from '@element-plus/icons-vue'
import * as livestreamApi from '@/api/livestream'
import * as liveChatApi from '@/api/liveChat'
import * as liveFeedbackApi from '@/api/liveFeedback'
import * as liveAnalysisApi from '@/api/liveAnalysis'
import * as echarts from 'echarts'
import 'echarts-wordcloud'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const liveStream = ref(null)
const wordCloudData = ref(null)
const wordList = ref([])
const wordCloudChart = ref(null)
const largeWordCloudChart = ref(null)
const feedbackList = ref([])
const feedbackTotal = computed(() => feedbackList.value.length)
const analysisReport = ref(null)
const analysisStatus = ref('')
const reportLoading = ref(false)
const reportGenerating = ref(false)
const activePanel = ref('word')
const enlargeVisible = ref(false)
let chartInstance = null
let largeChartInstance = null

const handleResize = () => {
  chartInstance?.resize()
  largeChartInstance?.resize()
}

const maxCount = computed(() => {
  if (wordList.value.length === 0) return 0
  return Math.max(...wordList.value.map(w => w.count))
})

onMounted(() => {
  loadLiveStream()
  loadWordCloud()
  loadFeedback()
  loadAnalysisReport()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  if (largeChartInstance) {
    largeChartInstance.dispose()
    largeChartInstance = null
  }
})

const loadLiveStream = async () => {
  try {
    const res = await livestreamApi.getLiveStreamById(route.params.id)
    if (res.code === 200) {
      liveStream.value = res.data
    }
  } catch (error) {
    console.error('加载直播信息失败:', error)
  }
}

const loadWordCloud = async () => {
  loading.value = true
  try {
    const res = await liveChatApi.getWordCloud(route.params.id)
    if (res.code === 200 && res.data) {
      wordCloudData.value = res.data
      wordList.value = JSON.parse(res.data.wordData)
      
      // 渲染词云
      nextTick(() => {
        renderWordCloud()
      })
    } else {
      // 词云不存在，尝试生成
      await generateWordCloud()
    }
  } catch (error) {
    console.error('加载词云失败:', error)
    ElMessage.error('加载词云失败')
  } finally {
    loading.value = false
  }
}

const generateWordCloud = async () => {
  loading.value = true
  try {
    const res = await liveChatApi.generateWordCloud(route.params.id)
    if (res.code === 200 && res.data) {
      wordCloudData.value = res.data
      wordList.value = JSON.parse(res.data.wordData)
      
      nextTick(() => {
        renderWordCloud()
      })
      
      ElMessage.success('词云生成成功')
    } else {
      ElMessage.warning(res.message || '暂无聊天数据')
    }
  } catch (error) {
    console.error('生成词云失败:', error)
    ElMessage.error('生成词云失败')
  } finally {
    loading.value = false
  }
}

const renderWordCloud = () => {
  if (!wordCloudChart.value || wordList.value.length === 0) return
  
  if (chartInstance) {
    chartInstance.dispose()
  }
  
  chartInstance = echarts.init(wordCloudChart.value)
  chartInstance.setOption(buildWordCloudOption())
}

const renderLargeWordCloud = () => {
  if (!largeWordCloudChart.value || wordList.value.length === 0) return
  if (largeChartInstance) {
    largeChartInstance.dispose()
  }
  largeChartInstance = echarts.init(largeWordCloudChart.value)
  largeChartInstance.setOption(buildWordCloudOption())
}

const buildWordCloudOption = () => {
  return {
    tooltip: {
      show: true,
      formatter: (params) => `${params.name}: ${params.value} 次`
    },
    series: [{
      type: 'wordCloud',
      gridSize: 8,
      sizeRange: [14, 60],
      rotationRange: [-45, 45],
      shape: 'circle',
      width: '100%',
      height: '100%',
      textStyle: {
        fontFamily: 'sans-serif',
        fontWeight: 'bold',
        color: () => {
          const colors = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc']
          return colors[Math.floor(Math.random() * colors.length)]
        }
      },
      emphasis: {
        focus: 'self',
        textStyle: {
          shadowBlur: 10,
          shadowColor: '#333'
        }
      },
      data: wordList.value.map(item => ({
        name: item.word,
        value: item.count
      }))
    }]
  }
}

const refreshWordCloud = async () => {
  await generateWordCloud()
  await loadFeedback()
  await loadAnalysisReport()
}

const downloadWordCloud = () => {
  if (!chartInstance) {
    ElMessage.warning('暂无词云数据')
    return
  }
  
  const url = chartInstance.getDataURL({
    type: 'png',
    pixelRatio: 2,
    backgroundColor: '#24283b'
  })
  
  const link = document.createElement('a')
  link.href = url
  link.download = `${liveStream.value?.title || '直播'}-词云.png`
  link.click()
  
  ElMessage.success('下载成功')
}

const openEnlargeWordCloud = () => {
  if (wordList.value.length === 0) {
    ElMessage.warning('暂无词云数据')
    return
  }
  enlargeVisible.value = true
  nextTick(() => {
    renderLargeWordCloud()
    largeChartInstance?.resize()
  })
}

const handleLargeDialogClosed = () => {
  if (largeChartInstance) {
    largeChartInstance.dispose()
    largeChartInstance = null
  }
}

const formatDateTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const formatDateOnly = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const formatPhase = (phase) => {
  if (phase === 'live') return '直播中'
  return '课后'
}

const loadFeedback = async () => {
  try {
    const res = await liveFeedbackApi.getFeedbackByLiveStream(route.params.id)
    if (res.code === 200) {
      feedbackList.value = res.data || []
    }
  } catch (error) {
    console.error('加载反馈失败', error)
  }
}

const loadAnalysisReport = async () => {
  reportLoading.value = true
  try {
    const res = await liveAnalysisApi.getAnalysisReport(route.params.id)
    analysisReport.value = res.data || null
    analysisStatus.value = res.message || ''
  } catch (error) {
    const message = error?.message || ''
    if (message.includes('暂未生成分析报告')) {
      analysisReport.value = null
      analysisStatus.value = '暂未生成分析报告'
    } else {
      console.error('加载分析报告失败', error)
      ElMessage.error('加载分析报告失败')
      analysisReport.value = null
    }
  } finally {
    reportLoading.value = false
  }
}

const handleGenerateReport = async () => {
  reportGenerating.value = true
  try {
    const res = await liveAnalysisApi.generateAnalysisReport(route.params.id)
    if (res.code === 200) {
      analysisReport.value = res.data
      analysisStatus.value = 'AI 报告已生成'
      ElMessage.success('AI 报告生成成功')
    } else {
      ElMessage.error(res.message || '生成失败')
    }
  } catch (error) {
    console.error('生成分析报告失败', error)
    ElMessage.error('生成分析报告失败')
  } finally {
    reportGenerating.value = false
  }
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.word-cloud-view {
  padding: 24px;
  background: radial-gradient(circle at top, rgba(41, 83, 255, 0.18), transparent 55%) #0b1222;
  min-height: 100%;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  padding: 18px 24px;
  background: rgba(15, 23, 43, 0.95);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  box-shadow: 0 15px 35px rgba(2, 8, 20, 0.6);
}

.page-header h2 {
  margin: 0;
  flex: 1;
  font-size: 20px;
  color: #f2f6ff;
}

.word-cloud-view :deep(.el-card) {
  background: rgba(12, 19, 38, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.04);
  box-shadow: 0 18px 35px rgba(3, 6, 18, 0.55);
  color: #e1e8ff;
}

.word-cloud-view :deep(.el-card__header) {
  border-color: rgba(255, 255, 255, 0.05);
  background: transparent;
  color: #f6fbff;
}

.analysis-panels {
  margin-left: 8px;
  padding-right: 6px;
}

.word-cloud-view :deep(.el-card__body) {
  color: #d6e0ff;
}
.stats-card {
  margin-bottom: 12px;
  padding: 18px 20px;
  background: linear-gradient(135deg, #1f2a49 0%, #1e3c72 60%, #2a6edc 100%);
  border: none;
  box-shadow: 0 20px 35px rgba(10, 14, 35, 0.28);
  color: #f8fbff;
}

:deep(.analysis-tabs .el-tabs__header) {
  margin: 20px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

:deep(.analysis-tabs .el-tabs__item) {
  color: rgba(255, 255, 255, 0.55);
  font-weight: 600;
}

:deep(.analysis-tabs .el-tabs__item.is-active) {
  color: #ffffff;
}

:deep(.analysis-tabs .el-tabs__nav-wrap::after) {
  background-color: transparent;
}

:deep(.analysis-tabs .el-tabs__content) {
  color: inherit;
}

:deep(.stats-card .el-card__body) {
  background: transparent;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 14px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stat-icon {
  width: 46px;
  height: 46px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at 30% 30%, #7ed3ff, #2b7dff);
  color: #08132b;
  border-radius: 18px;
  box-shadow: inset 0 0 12px rgba(255, 255, 255, 0.35);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 21px;
  font-weight: 700;
  color: #fdfefe;
  margin-bottom: 2px;
}

.stat-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 0.5px;
}

.word-cloud-card {
  margin-bottom: 20px;
}

.analysis-card,
.feedback-card {
  margin-bottom: 20px;
}

.word-cloud-container.large {
  height: 520px;
}

.word-cloud-dialog :deep(.el-dialog) {
  background: #0f172b;
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 35px 70px rgba(2, 6, 23, 0.85);
}

.word-cloud-dialog :deep(.el-dialog__title) {
  color: #f5f9ff;
}

.word-cloud-dialog :deep(.el-dialog__body) {
  padding: 10px 20px 30px;
}

.feedback-scroll {
  max-height: 360px;
  overflow-y: auto;
  padding-right: 6px;
}

.feedback-scroll::-webkit-scrollbar {
  width: 6px;
}

.feedback-scroll::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.25);
  border-radius: 4px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.analysis-content pre {
  white-space: pre-wrap;
  margin: 0;
  font-family: 'JetBrains Mono', monospace;
}

.analysis-meta {
  margin: 0 0 12px 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
}

.word-cloud-container {
  width: 100%;
  height: 500px;
}

.word-list-card {
  margin-bottom: 20px;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table__header) {
  font-weight: 600;
}

.frequency-progress {
  width: 200px;
  margin: 0 auto;
}

.word-list-card :deep(.compact-word-table th),
.word-list-card :deep(.compact-word-table td) {
  padding: 12px 8px;
}

.word-list-card :deep(.compact-word-table .cell) {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
