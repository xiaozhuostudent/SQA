<template>
  <div class="exam-result-page" v-loading="loading">
    <div v-if="detail" class="result-wrapper">
      <header class="result-hero">
        <div class="hero-info">
          <div class="hero-breadcrumb">
            <el-button link type="primary" @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              返回考试中心
            </el-button>
            <el-tag type="info" size="large">{{ detail.courseName }}</el-tag>
            <el-tag :type="getResultStatusType()" size="large">{{ getResultStatusText() }}</el-tag>
          </div>
          <h1>{{ detail.examTitle }}</h1>
          <p class="hero-subtext">考试时间：{{ formatDateTime(detail.startTime) }} - {{ formatDateTime(detail.submitTime) }}</p>
          <div class="hero-meta">
            <span>考试用时 {{ getExamDuration(detail.startTime, detail.submitTime) }}</span>
            <span>尝试次数第 {{ detail.attemptNumber }} 次</span>
            <span>IP {{ detail.ipAddress || '未知' }}</span>
          </div>
        </div>
        <div class="hero-score">
          <div class="score-ring" :style="{ '--score-percent': scorePercent + '%' }">
            <div class="score-core">
              <div class="score-value">{{ detail.studentTotalScore || 0 }}</div>
              <div class="score-full">/{{ detail.totalScore || 100 }}</div>
              <div class="score-label">总得分</div>
            </div>
          </div>
          <div class="score-breakdown">
            <div class="score-item">
              <span>客观题</span>
              <strong>{{ detail.objectiveScore || 0 }} 分</strong>
            </div>
            <div class="score-item">
              <span>主观题</span>
              <strong>{{ detail.subjectiveScore || 0 }} 分</strong>
            </div>
            <div class="score-item">
              <span>及格分</span>
              <strong>{{ detail.passScore || 60 }} 分</strong>
            </div>
          </div>
        </div>
      </header>

      <section class="result-grid">
        <el-card class="metric-card" shadow="hover">
          <template #header>
            <h3>考试概览</h3>
          </template>
          <div class="metric-grid">
            <div class="metric-item">
              <label>考试时长</label>
              <span>{{ detail.duration }} 分钟</span>
            </div>
            <div class="metric-item">
              <label>考试状态</label>
              <span>{{ detail.status === 'graded' ? '已批改' : '已提交' }}</span>
            </div>
            <div class="metric-item">
              <label>批改教师</label>
              <span>{{ detail.graderName || '系统自动' }}</span>
            </div>
            <div class="metric-item">
              <label>批改时间</label>
              <span>{{ detail.gradeTime ? formatDateTime(detail.gradeTime) : '待批改' }}</span>
            </div>
          </div>
        </el-card>

        <el-card class="analysis-card" shadow="hover">
          <template #header>
            <div class="analysis-header">
              <h3>考试统计</h3>
              <el-tag type="info">正确率 {{ statistics.accuracy }}%</el-tag>
            </div>
          </template>
          <div class="analysis-body">
            <div class="stat-circles">
              <div class="circle" v-for="item in summaryStats" :key="item.label">
                <div class="circle-value">{{ item.value }}</div>
                <div class="circle-label">{{ item.label }}</div>
              </div>
            </div>
            <div class="type-table">
              <el-table :data="formatTypeStats()" border size="small">
                <el-table-column prop="typeName" label="题型" width="140" />
                <el-table-column prop="total" label="总题数" width="100" align="center" />
                <el-table-column prop="correct" label="正确" width="100" align="center">
                  <template #default="{ row }">
                    <span class="text-success">{{ row.correct }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="wrong" label="错误" width="100" align="center">
                  <template #default="{ row }">
                    <span class="text-danger">{{ row.wrong }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="accuracy" label="正确率" align="center">
                  <template #default="{ row }">
                    <el-progress :percentage="parseFloat(row.accuracy)" :stroke-width="10" :color="getAccuracyColor(row.accuracy)" />
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-card>

      </section>

      <section class="answer-section">
        <div class="answer-header">
          <h3>答题详情</h3>
          <span>共 {{ resultAnswers.length }} 题</span>
        </div>
        <div class="answer-list">
          <div v-for="answer in resultAnswers" :key="answer.answerId" class="answer-card" :class="{ correct: answer.isCorrect, wrong: answer.isCorrect === false }">
            <div class="answer-meta">
              <div class="question-label">第 {{ answer.questionOrder }} 题 · {{ getQuestionTypeName(answer.questionType) }}</div>
              <div class="question-score">{{ answer.score || 0 }} / {{ answer.questionScore }} 分</div>
            </div>
            <p class="question-content">{{ answer.content }}</p>
            <p class="answer-line">
              <span>我的答案</span>
              <strong>{{ formatStudentAnswer(answer) }}</strong>
            </p>
            <p class="answer-line">
              <span>正确答案</span>
              <strong class="text-success">{{ formatCorrectAnswer(answer) }}</strong>
            </p>
            <p class="analysis" v-if="answer.analysis">{{ answer.analysis }}</p>
          </div>
        </div>
      </section>

      <transition name="ai-panel-fade">
        <div v-if="aiPanelOpen" class="ai-panel" @click.stop>
          <div class="ai-panel-header">
            <div>
              <div class="ai-panel-title">AI 个性化建议</div>
              <p class="ai-panel-subtitle">基于本次考试表现生成的学习规划</p>
            </div>
            <div class="ai-panel-actions">
              <el-tag size="small" effect="dark" :type="aiStatusTagType">{{ aiStatusText }}</el-tag>
              <el-button size="small" text @click="refreshAIRecommendations" :loading="aiLoading">重新生成</el-button>
              <el-button size="small" text type="danger" @click="closeAIInsights">收起</el-button>
            </div>
          </div>
          <div class="ai-panel-body">
            <div class="ai-panel-columns">
              <section class="ai-section">
                <header class="section-header">
                  <h4>整体学习建议</h4>
                  <span v-if="statistics.scoreRate">得分率 {{ statistics.scoreRate }}%</span>
                </header>
                <div class="ai-scroll">
                  <div v-for="(rec, idx) in aiRecommendations" :key="idx" class="ai-item">
                    <div class="ai-marker"></div>
                    <div>
                      <h4>{{ rec.title }}</h4>
                      <p>{{ rec.content }}</p>
                    </div>
                  </div>
                  <div v-if="!aiRecommendations.length && !aiLoading" class="ai-empty">暂无建议，等待生成...</div>
                </div>
              </section>
              <section class="ai-section practice">
                <header class="section-header">
                  <h4><el-icon><Collection /></el-icon> 推荐练习</h4>
                  <span>AI 精选题目</span>
                </header>
                <div class="related-list">
                  <template v-if="relatedQuestions.length">
                    <div v-for="(question, idx) in relatedQuestions" :key="idx" class="related-item">
                      <div class="related-type">
                        <el-tag :type="getQuestionTypeTag(question.questionType)" size="small">
                          {{ getQuestionTypeName(question.questionType) }}
                        </el-tag>
                        <el-tag type="info" size="small">难度 {{ question.difficulty || '中等' }}</el-tag>
                      </div>
                      <p>{{ truncateText(question.content, 80) }}</p>
                      <div class="related-action">
                        <el-button type="primary" size="small" plain @click="openPracticePreview(question)">开始练习</el-button>
                      </div>
                    </div>
                  </template>
                  <div v-else class="ai-empty">暂无推荐题目</div>
                </div>
              </section>
            </div>
            <section class="ai-chat">
              <header class="chat-header">
                <div>
                  <strong>AI 对话助手</strong>
                  <p>针对本场考试继续追问</p>
                </div>
                <el-tag size="small" type="info">Beta</el-tag>
              </header>
              <div class="chat-window" ref="chatWindowRef">
                <div
                  v-for="(msg, idx) in chatMessages"
                  :key="idx"
                  :class="['chat-bubble', msg.role]"
                  v-html="msg.html || msg.content"
                ></div>
                <div v-if="!chatMessages.length && !chatLoading" class="chat-empty">
                  还没有内容，向 AI 提问吧。
                </div>
                <div v-if="chatLoading" class="chat-typing">AI 正在思考...</div>
              </div>
              <div class="chat-input-row">
                <el-input v-model="chatInput" type="textarea" :rows="2" placeholder="例如：为什么这道题错了？" />
                <el-button type="primary" :loading="chatLoading" @click="sendChatMessage">发送</el-button>
              </div>
            </section>
          </div>
        </div>
      </transition>
      <div v-if="aiPanelOpen" class="ai-panel-mask" @click="closeAIInsights"></div>

      <button class="ai-fab" :class="{ active: aiPanelOpen }" type="button" @click="toggleAIInsights">
        <span class="ai-avatar">
          <el-icon><ChatLineRound /></el-icon>
        </span>
        <span class="ai-fab-text">
          <strong>AI 学习助手</strong>
          <small>{{ aiStatusChecked ? (aiHealthy ? '随时待命' : '离线(模拟)') : '检测中...' }}</small>
        </span>
      </button>

      <el-dialog v-model="practiceDialogVisible" :title="practiceDialogTitle" width="560px" destroy-on-close>
        <div v-if="practiceQuestionDetail">
          <p class="practice-label">题型：{{ getQuestionTypeName(practiceQuestionDetail.questionType) }} · 难度：{{ practiceQuestionDetail.difficulty || '中等' }}</p>
          <p class="practice-content">{{ practiceQuestionDetail.content }}</p>
          <p v-if="practiceQuestionDetail.analysis" class="practice-analysis">解析：{{ practiceQuestionDetail.analysis }}</p>
        </div>
        <div v-else class="ai-empty">暂无题目信息</div>
        <template #footer>
          <el-button @click="practiceDialogVisible = false">稍后再练</el-button>
          <el-button type="primary" @click="goPracticeCenter(practiceQuestionDetail)">前往题库练习</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Collection, ChatLineRound } from '@element-plus/icons-vue'
import * as examApi from '@/api/exam'
import { useUserStore } from '@/stores/user'
import { marked } from 'marked'

marked.setOptions({ breaks: true })

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const detail = ref(null)
const resultAnswers = ref([])
const statistics = ref({
  correctCount: 0,
  wrongCount: 0,
  unansweredCount: 0,
  totalQuestions: 0,
  accuracy: 0,
  scoreRate: 0,
  typeStats: {},
  weakTypes: [],
  recommendations: []
})
const aiRecommendations = ref([])
const relatedQuestions = ref([])
const aiLoading = ref(false)
const aiHealthy = ref(false)
const aiStatusChecked = ref(false)
const aiPanelOpen = ref(false)
const chatMessages = ref([])
const chatInput = ref('')
const chatLoading = ref(false)
const chatSessionId = ref('')
const practiceDialogVisible = ref(false)
const practiceQuestionDetail = ref(null)
const chatWindowRef = ref(null)

const renderMarkdown = (text) => marked.parse(text || '')

const studentExamId = computed(() => route.params.studentExamId)

const summaryStats = computed(() => [
  { label: '答对', value: statistics.value.correctCount },
  { label: '答错', value: statistics.value.wrongCount },
  { label: '未答', value: statistics.value.unansweredCount },
  { label: '得分率', value: `${statistics.value.scoreRate}%` }
])

const practiceDialogTitle = computed(() => practiceQuestionDetail.value?.title || 'AI 推荐练习')

const scorePercent = computed(() => {
  if (!detail.value || !detail.value.totalScore) return 0
  return Math.min(100, Math.round(((detail.value.studentTotalScore || 0) / detail.value.totalScore) * 100))
})

const aiStatusTagType = computed(() => {
  if (!aiStatusChecked.value) return 'info'
  return aiHealthy.value ? 'success' : 'warning'
})

const aiStatusText = computed(() => {
  if (!aiStatusChecked.value) return 'AI 服务检测中'
  return aiHealthy.value ? 'AI 服务在线' : 'AI 服务离线，使用本地建议'
})

onMounted(() => {
  loadDetail()
  checkAIService()
})

watch([detail, statistics], ([detailVal]) => {
  if (!detailVal || chatMessages.value.length) return
  seedChatIntro()
})

const checkAIService = async () => {
  aiStatusChecked.value = false
  try {
    const aiModule = await import('@/api/ai.js')
    const res = await aiModule.checkAIHealth()
    aiHealthy.value = res.code === 200
  } catch (error) {
    console.warn('AI 服务健康检查失败:', error)
    aiHealthy.value = false
  } finally {
    aiStatusChecked.value = true
  }
}

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await examApi.getExamResultDetail(studentExamId.value)
    if (res.code !== 200 || !res.data) throw new Error(res.message || '获取考试结果失败')
    detail.value = res.data
    resultAnswers.value = res.data.answers || []
    calculateStatistics(resultAnswers.value, detail.value.totalScore || 100)
  } catch (error) {
    console.error('加载考试结果失败:', error)
    ElMessage.error(error.message || '加载考试结果失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push({ name: 'StudentExam' })
}

const getResultStatusType = () => {
  if (!detail.value) return 'info'
  const percent = scorePercent.value
  if (percent >= 90) return 'success'
  if (percent >= 70) return 'primary'
  if (percent >= 60) return 'warning'
  return 'danger'
}

const getResultStatusText = () => {
  const percent = scorePercent.value
  if (percent >= 90) return '优秀'
  if (percent >= 70) return '良好'
  if (percent >= 60) return '及格'
  return '未及格'
}

const seedChatIntro = () => {
  if (!detail.value) return
  const name = userStore.userInfo?.nickname || userStore.userInfo?.name || '同学'
  const accuracy = statistics.value.accuracy || 0
  const wrong = statistics.value.wrongCount || 0
  chatSessionId.value = `exam-${studentExamId.value}-${Date.now()}`
  playAssistantReply(`你好，${name}。我已分析完本场《${detail.value.examTitle}》考试：正确率约 ${accuracy}% ，错题 ${wrong} 道。可以继续问我任意错题、薄弱章节或复习建议。`)
}

const parseOptions = (options) => {
  if (Array.isArray(options)) return options
  if (typeof options === 'string') {
    try {
      return JSON.parse(options)
    } catch {
      return options.split(',').map(o => o.trim())
    }
  }
  return []
}

const getQuestionTypeName = (type) => {
  const names = {
    single_choice: '单选题',
    multiple_choice: '多选题',
    true_false: '判断题',
    fill_blank: '填空题',
    short_answer: '简答题',
    programming: '编程题'
  }
  return names[type] || type
}

const getQuestionTypeTag = (type) => {
  const tags = {
    single_choice: 'primary',
    multiple_choice: 'success',
    true_false: 'warning',
    fill_blank: 'info',
    short_answer: 'danger',
    programming: ''
  }
  return tags[type] || ''
}

const formatStudentAnswer = (answer) => {
  if (!answer.studentAnswer) return '未作答'
  if (['single_choice', 'multiple_choice'].includes(answer.questionType)) {
    const options = parseOptions(answer.options)
    if (answer.questionType === 'single_choice') {
      const index = answer.studentAnswer.charCodeAt(0) - 65
      return options[index] || answer.studentAnswer
    }
    return answer.studentAnswer.split(',').map(letter => {
      const index = letter.trim().charCodeAt(0) - 65
      return options[index] || letter
    }).join('，')
  }
  return answer.studentAnswer
}

const formatCorrectAnswer = (answer) => {
  if (!answer.correctAnswer) return '-'
  if (['single_choice', 'multiple_choice'].includes(answer.questionType)) {
    const options = parseOptions(answer.options)
    if (answer.questionType === 'single_choice') {
      const index = answer.correctAnswer.charCodeAt(0) - 65
      return options[index] || answer.correctAnswer
    }
    return answer.correctAnswer.split(',').map(letter => {
      const index = letter.trim().charCodeAt(0) - 65
      return options[index] || letter
    }).join('，')
  }
  return answer.correctAnswer
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const getExamDuration = (startTime, endTime) => {
  if (!startTime || !endTime) return '-'
  const diff = new Date(endTime) - new Date(startTime)
  const minutes = Math.floor(diff / 60000)
  const seconds = Math.floor((diff % 60000) / 1000)
  return `${minutes}分${seconds}秒`
}

const calculateStatistics = (answerDetails, fullScore) => {
  const stats = {
    correctCount: 0,
    wrongCount: 0,
    unansweredCount: 0,
    totalQuestions: answerDetails.length,
    accuracy: 0,
    scoreRate: 0,
    typeStats: {},
    weakTypes: [],
    recommendations: []
  }

  let obtainedScore = 0

  answerDetails.forEach(answer => {
    const type = answer.questionType
    if (!stats.typeStats[type]) {
      stats.typeStats[type] = { total: 0, correct: 0, wrong: 0, typeName: getQuestionTypeName(type) }
    }

    stats.typeStats[type].total++

    if (!answer.studentAnswer || !answer.studentAnswer.toString().trim()) {
      stats.unansweredCount++
    } else if (answer.isCorrect) {
      stats.correctCount++
      stats.typeStats[type].correct++
    } else {
      stats.wrongCount++
      stats.typeStats[type].wrong++
    }

    obtainedScore += answer.score || 0
  })

  stats.accuracy = stats.totalQuestions ? ((stats.correctCount / stats.totalQuestions) * 100).toFixed(1) : 0
  stats.scoreRate = fullScore ? ((obtainedScore / fullScore) * 100).toFixed(1) : 0

  Object.values(stats.typeStats).forEach(item => {
    const accuracy = item.total ? (item.correct / item.total) * 100 : 0
    if (accuracy < 60 && item.total >= 2) {
      stats.weakTypes.push({ typeName: item.typeName, accuracy: accuracy.toFixed(1), wrongCount: item.wrong })
    }
  })

  statistics.value = stats
  generateAIRecommendations(answerDetails)
}

const formatTypeStats = () => {
  return Object.entries(statistics.value.typeStats).map(([type, data]) => ({
    type,
    typeName: data.typeName,
    total: data.total,
    correct: data.correct,
    wrong: data.wrong,
    accuracy: data.total ? (data.correct / data.total) * 100 : 0
  }))
}

const getAccuracyColor = (accuracy) => {
  if (accuracy >= 80) return '#67c23a'
  if (accuracy >= 60) return '#409eff'
  if (accuracy >= 40) return '#e6a23c'
  return '#f56c6c'
}

const generateAIRecommendations = async (answerDetails) => {
  aiLoading.value = true
  try {
    const wrongAnswers = answerDetails.filter(answer => !answer.isCorrect)
    const payload = {
      studentId: userStore.userInfo.id,
      wrongAnswers: wrongAnswers.map(answer => ({
        questionId: answer.questionId,
        content: answer.content,
        questionType: answer.questionType,
        studentAnswer: answer.studentAnswer,
        correctAnswer: answer.correctAnswer,
        analysis: answer.analysis
      })),
      statistics: statistics.value
    }

    try {
      const aiModule = await import('@/api/ai.js')
      const res = await aiModule.getAIRecommendations(payload)
      if (res.code === 200) {
        aiHealthy.value = true
        aiStatusChecked.value = true
        aiRecommendations.value = res.data.recommendations
        relatedQuestions.value = res.data.relatedQuestions
        return
      }
      aiHealthy.value = false
      aiStatusChecked.value = true
    } catch (apiError) {
      console.warn('AI 接口不可用，使用本地模拟', apiError)
      aiHealthy.value = false
      aiStatusChecked.value = true
    }

    const mock = await mockAIRecommendations(payload)
    aiRecommendations.value = mock.recommendations
    relatedQuestions.value = mock.relatedQuestions
  } catch (error) {
    console.error('生成 AI 建议失败:', error)
    ElMessage.error('生成 AI 建议失败')
  } finally {
    aiLoading.value = false
  }
}

const mockAIRecommendations = async (data) => {
  await new Promise(resolve => setTimeout(resolve, 600))
  const recommendations = []
  const related = []

  if (data.wrongAnswers.length) {
    recommendations.push({
      title: '知识点回顾',
      content: `建议重点复习 ${data.wrongAnswers.length} 道错题涉及的知识点，尤其是 ${statistics.value.weakTypes.map(t => t.typeName).join('、') || '易错题型'}。`
    })
    data.wrongAnswers.slice(0, 2).forEach((answer, index) => {
      recommendations.push({
        title: `错题 ${index + 1} 诊断`,
        content: `题目：${answer.content.substring(0, 40)}... 正确做法：${answer.analysis || '加强相关概念理解'}`
      })
      related.push({
        questionId: answer.questionId + 999,
        content: answer.content,
        questionType: answer.questionType,
        difficulty: '中等'
      })
    })
  } else {
    recommendations.push({ title: '保持优势', content: '本次考试表现优秀，建议继续保持练习节奏，并尝试挑战更高难度题目。' })
  }

  return { recommendations, relatedQuestions: related }
}

const refreshAIRecommendations = () => {
  if (resultAnswers.value.length) {
    generateAIRecommendations(resultAnswers.value)
  }
}

const truncateText = (text, len) => {
  if (!text) return ''
  return text.length > len ? `${text.slice(0, len)}...` : text
}

const scrollChatToBottom = () => {
  nextTick(() => {
    if (chatWindowRef.value) {
      chatWindowRef.value.scrollTop = chatWindowRef.value.scrollHeight
    }
  })
}

const pushUserMessage = (content) => {
  chatMessages.value.push({
    role: 'user',
    content,
    html: renderMarkdown(content)
  })
  scrollChatToBottom()
}

const playAssistantReply = (text) => {
  return new Promise((resolve) => {
    const finalText = text || '好的，我收到啦。'
    const chars = Array.from(finalText)
    const message = {
      role: 'assistant',
      content: '',
      html: '',
      streaming: true
    }
    chatMessages.value.push(message)
    scrollChatToBottom()

    if (!chars.length) {
      message.streaming = false
      message.html = renderMarkdown(finalText)
      resolve()
      return
    }

    const tick = () => {
      message.content += chars.shift()
      message.html = renderMarkdown(message.content)
      scrollChatToBottom()
      if (!chars.length) {
        clearInterval(timer)
        message.streaming = false
        resolve()
      }
    }

    tick()
    const timer = setInterval(() => {
      if (!chars.length) {
        clearInterval(timer)
        return
      }
      tick()
    }, 20)
  })
}

const openPracticePreview = (question) => {
  practiceQuestionDetail.value = {
    ...question,
    title: question.title || truncateText(question.content, 12)
  }
  practiceDialogVisible.value = true
}

const goPracticeCenter = (question) => {
  practiceDialogVisible.value = false
  aiPanelOpen.value = false
  const keyword = question?.content ? question.content.slice(0, 20) : ''
  router.push({
    name: 'StudentPractice',
    query: keyword ? { keyword } : {}
  })

  if (keyword) {
    ElMessage.success('已为你打开题库，自动填入推荐题目关键字')
  }
}

const sendChatMessage = async () => {
  const content = chatInput.value.trim()
  if (!content) {
    ElMessage.warning('请输入提问内容')
    return
  }
  if (!detail.value) return

  pushUserMessage(content)
  chatInput.value = ''
  chatLoading.value = true

  try {
    const aiModule = await import('@/api/ai.js')
    const payload = {
      message: content,
      sessionId: chatSessionId.value,
      exam: {
        examId: detail.value.studentExamId,
        examTitle: detail.value.examTitle,
        courseName: detail.value.courseName
      },
      statistics: statistics.value,
      wrongTopics: statistics.value.weakTypes
    }
    const res = await aiModule.chatWithAI(payload)
    const replyText = res.reply || res.answer_content || res.message || '暂时没有更多建议，稍后再试试。'
    chatSessionId.value = res.session_id || res.sessionId || chatSessionId.value || payload.sessionId || ''
    aiHealthy.value = true
    aiStatusChecked.value = true
    await playAssistantReply(replyText)
  } catch (error) {
    console.warn('AI 对话失败', error)
    aiHealthy.value = false
    await playAssistantReply('网络开小差了，我稍后再回答。')
  } finally {
    chatLoading.value = false
  }
}

const toggleAIInsights = () => {
  aiPanelOpen.value = !aiPanelOpen.value
}

const closeAIInsights = () => {
  aiPanelOpen.value = false
}
</script>

<style scoped>
.exam-result-page {
  min-height: 100vh;
  padding: 32px;
  background: radial-gradient(circle at top, rgba(122,162,247,0.15), transparent 60%), var(--bg-primary);
}

.result-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  color: var(--text-primary);
}

.result-hero {
  background: linear-gradient(135deg, rgba(122,162,247,0.2), rgba(125,207,255,0.15));
  border: 1px solid var(--border);
  border-radius: 20px;
  padding: 32px;
  display: flex;
  justify-content: space-between;
  gap: 32px;
  box-shadow: var(--shadow-md);
}

.hero-info h1 {
  margin: 16px 0 12px;
  font-size: 28px;
}

.hero-subtext {
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.hero-breadcrumb {
  display: flex;
  gap: 12px;
  align-items: center;
}

.hero-meta {
  display: flex;
  gap: 16px;
  color: var(--text-secondary);
  font-size: 14px;
}

.hero-score {
  min-width: 260px;
  text-align: center;
}

.score-ring {
  width: 180px;
  height: 180px;
  margin: 0 auto;
  border-radius: 50%;
  background: conic-gradient(var(--accent-cyan) 0 var(--score-percent), #2d3250 var(--score-percent) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
}

.score-core {
  width: 100%;
  height: 100%;
  background: var(--bg-primary);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 2px solid var(--border);
}

.score-value {
  font-size: 42px;
  font-weight: 700;
}

.score-full {
  color: var(--text-secondary);
}

.score-breakdown {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.score-item {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 12px;
}

.score-item span {
  color: var(--text-secondary);
  font-size: 13px;
}

.score-item strong {
  display: block;
  margin-top: 4px;
}

.result-grid {
  margin-top: 30px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 20px;
}

.metric-card,
.analysis-card {
  background: var(--bg-float);
  border: 1px solid var(--border);
  border-radius: 16px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.metric-item {
  background: var(--bg-secondary);
  border-radius: 12px;
  padding: 16px;
  border: 1px solid var(--border);
}

.metric-item label {
  color: var(--text-muted);
  font-size: 13px;
}

.metric-item span {
  display: block;
  margin-top: 8px;
  font-size: 18px;
  font-weight: 600;
}

.analysis-body {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stat-circles {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.circle {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 14px;
  text-align: center;
}

.circle-value {
  font-size: 22px;
  font-weight: 700;
}

.circle-label {
  color: var(--text-secondary);
}

.ai-item {
  display: flex;
  gap: 14px;
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}

.ai-marker {
  width: 6px;
  border-radius: 4px;
  background: linear-gradient(180deg, var(--accent-cyan), var(--primary));
}

.ai-item h4 {
  margin: 0 0 6px;
}

.related-item {
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 12px;
  margin-bottom: 12px;
}

.answer-section {
  margin-top: 30px;
}

.answer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.answer-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.answer-card {
  background: var(--bg-float);
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 20px;
  box-shadow: var(--shadow-sm);
}

.answer-card.correct {
  border-color: rgba(158, 206, 106, 0.6);
}

.answer-card.wrong {
  border-color: rgba(247, 118, 142, 0.6);
}

.answer-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-weight: 600;
}

.answer-line {
  display: flex;
  justify-content: space-between;
  margin: 6px 0;
}

.answer-line span {
  color: var(--text-secondary);
}

.analysis {
  margin-top: 10px;
  padding: 12px;
  background: rgba(123, 206, 255, 0.08);
  border-radius: 10px;
}

.text-success {
  color: var(--accent-green);
}

.text-danger {
  color: var(--accent-red);
}

.ai-fab {
  position: fixed;
  right: 40px;
  bottom: 40px;
  display: flex;
  align-items: center;
  gap: 12px;
  border: none;
  border-radius: 999px;
  padding: 12px 18px;
  background: linear-gradient(120deg, #7aa2f7, #7dcfff);
  color: #0d1117;
  font-size: 14px;
  box-shadow: 0 10px 35px rgba(0,0,0,0.35);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  z-index: 30;
}

.ai-fab.active {
  transform: translateY(-4px);
  box-shadow: 0 14px 40px rgba(0,0,0,0.4);
}

.ai-avatar {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  background: #0d1117;
  display: grid;
  place-items: center;
  color: #7dcfff;
  font-size: 24px;
}

.ai-fab-text {
  display: flex;
  flex-direction: column;
  text-align: left;
  line-height: 1.2;
}

.ai-fab-text small {
  opacity: 0.8;
}

.ai-panel {
  position: fixed;
  right: 120px;
  bottom: 110px;
  width: min(900px, calc(100% - 96px));
  max-height: 80vh;
  background: var(--bg-float);
  border: 1px solid var(--border);
  border-radius: 24px;
  padding: 26px;
  box-shadow: var(--shadow-lg, 0 30px 80px rgba(0,0,0,0.55));
  z-index: 35;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow: hidden;
}

.ai-panel-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.ai-panel-title {
  font-size: 18px;
  font-weight: 600;
}

.ai-panel-subtitle {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.ai-panel-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.ai-panel-body {
  display: flex;
  gap: 20px;
  height: 100%;
  min-height: 320px;
  overflow: hidden;
}

.ai-panel-columns {
  flex: 1.3;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow-y: auto;
  padding-right: 4px;
  min-width: 0;
}

.ai-section {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 18px;
  padding: 18px;
  min-width: 0;
}

.ai-section.practice {
  border-style: dashed;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-weight: 600;
}

.ai-scroll {
  max-height: 220px;
  overflow-y: auto;
  padding-right: 4px;
}

.related-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.related-item {
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 12px;
  background: var(--bg-primary);
}

.related-type {
  display: flex;
  gap: 8px;
  margin-bottom: 6px;
}

.related-action {
  display: flex;
  justify-content: flex-end;
}

.ai-chat {
  flex: 1;
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 18px;
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
  overflow: hidden;
}

.chat-window {
  flex: 1;
  overflow-y: auto;
  background: rgba(125,207,255,0.05);
  border-radius: 12px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 280px;
}
.chat-bubble {
  max-width: 100%;
  padding: 10px 12px;
  border-radius: 12px;
  background: rgba(148,226,213,0.08);
  line-height: 1.5;
  word-break: break-word;
}
.chat-bubble :deep(pre) {
  background: rgba(15,23,42,0.6);
  padding: 8px;
  border-radius: 8px;
  overflow-x: auto;
}
.chat-bubble :deep(code) {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', monospace;
}

.chat-bubble.user {
  align-self: flex-end;
  background: rgba(122,162,247,0.25);
}

.chat-empty,
.chat-typing {
  text-align: center;
  color: var(--text-secondary);
  font-size: 13px;
}

.chat-input-row {
  display: flex;
  gap: 12px;
}

.chat-input-row .el-input :deep(.el-textarea__inner) {
  min-height: 80px !important;
}

.practice-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 10px;
}

.practice-content {
  font-size: 16px;
  line-height: 1.5;
  margin-bottom: 10px;
}

.practice-analysis {
  padding: 12px;
  background: rgba(125,207,255,0.08);
  border-radius: 10px;
}

.ai-empty {
  text-align: center;
  color: var(--text-secondary);
  padding: 12px 0;
  font-size: 13px;
}

@media (max-width: 1280px) {
  .ai-panel {
    right: 40px;
    width: calc(100% - 80px);
  }
}

@media (max-width: 960px) {
  .result-hero {
    flex-direction: column;
  }

  .score-breakdown {
    grid-template-columns: repeat(2, 1fr);
  }

  .ai-panel {
    right: 16px;
    width: calc(100% - 32px);
    bottom: 120px;
  }

  .ai-panel-body {
    flex-direction: column;
    min-height: auto;
  }

  .ai-panel-columns {
    max-height: none;
  }
}

@media (max-width: 640px) {
  .exam-result-page {
    padding: 16px;
  }

  .hero-meta {
    flex-direction: column;
  }

  .ai-panel {
    right: 16px;
    left: 16px;
    width: auto;
    bottom: 120px;
    max-height: 70vh;
  }

  .ai-panel-body {
    max-height: none;
  }

  .ai-fab {
    right: 16px;
    left: 16px;
    width: calc(100% - 32px);
    justify-content: center;
  }
}

.ai-panel-mask {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.35);
  z-index: 25;
}

.ai-panel-fade-enter-active,
.ai-panel-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.ai-panel-fade-enter-from,
.ai-panel-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
