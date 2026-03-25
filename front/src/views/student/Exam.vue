<template>
  <div class="exam-page">
    <el-card shadow="never" class="exam-card">
      <template #header>
        <div class="card-header">
          <div>
            <h2 class="page-title">在线考试</h2>
          </div>
          <div class="card-actions">
            <el-tag type="info">今日可考 {{ upcomingExams.length }} 场</el-tag>
            <el-button text type="primary" @click="refreshExams">刷新列表</el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 待考试列表 -->
        <el-tab-pane label="待考试" name="upcoming">
          <el-table 
            :data="upcomingExams" 
            stripe 
            v-loading="loading" 
            element-loading-text="加载考试数据中..."
            style="width: 100%">
            <el-table-column prop="title" label="考试名称" min-width="200"/>
            <el-table-column prop="courseName" label="课程" width="150"/>
            <el-table-column prop="duration" label="时长" width="100" align="center">
              <template #default="{ row }">
                {{ row.duration }}分钟
              </template>
            </el-table-column>
            <el-table-column prop="totalScore" label="总分" width="80" align="center"/>
            <el-table-column prop="startTime" label="开始时间" width="180"/>
            <el-table-column prop="endTime" label="结束时间" width="180"/>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getExamStatusType(row)">{{ getExamStatusText(row) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right" align="center">
              <template #default="{ row }">
                <el-button 
                  v-if="!row.studentExamRecord || isPracticeExam(row)"
                  type="primary" 
                  size="small" 
                  @click="startExam(row)"
                  :disabled="!canStartExam(row)">
                  开始考试
                </el-button>
                <el-button
                  v-else
                  type="success" 
                  size="small" 
                  @click="viewResult(row.studentExamRecord)">
                  查看结果
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 考试记录 -->
        <el-tab-pane label="考试记录" name="history">
          <el-table 
            :data="paginatedExamHistory" 
            stripe 
            v-loading="historyLoading" 
            element-loading-text="加载考试记录中..."
            style="width: 100%">
            <el-table-column prop="title" label="考试名称" min-width="200"/>
            <el-table-column prop="courseName" label="课程" width="150"/>
            <el-table-column prop="submitTime" label="提交时间" width="180"/>
            <el-table-column label="客观题" width="100" align="center">
              <template #default="{ row }">
                <span class="score-text">{{ row.objectiveScore || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="主观题" width="100" align="center">
              <template #default="{ row }">
                <span class="score-text">{{ row.subjectiveScore || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="总分" width="80" align="center">
              <template #default="{ row }">
                <span class="score-text primary">{{ formatScore(row.totalScore) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getSubmissionStatusType(row.status)">
                  {{ getSubmissionStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right" align="center">
              <template #default="{ row }">
                <el-button type="success" size="small" @click="viewResult(row)">
                  查看结果
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 分页组件 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[5, 10, 20, 50]"
              :total="totalRecords"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 考试对话框 -->
    <el-dialog 
      v-model="examDialogVisible" 
      :title="currentExam?.title" 
      fullscreen
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      class="exam-fullscreen-dialog">
      <div v-if="currentExam" class="exam-content">
        <section class="exam-toolbar">
          <div class="toolbar-left">
            <div class="toolbar-tags">
              <el-tag type="info">{{ currentExam.courseName }}</el-tag>
              <el-tag effect="dark">总分 {{ currentExam.totalScore }} 分</el-tag>
              <el-tag type="warning">{{ currentExam.duration }} 分钟</el-tag>
            </div>
            <h3>{{ currentExam.title }}</h3>
            <p class="toolbar-desc">共 {{ questions.length }} 题 · 当前卷已开启</p>
            <div class="toolbar-meta">
              <span>
                <el-icon><Document /></el-icon>
                试卷编号 #{{ currentExam.id }}
              </span>
              <span>
                <el-icon><Edit /></el-icon>
                及格线 {{ currentExam.passScore || 60 }} 分
              </span>
            </div>
          </div>
          <div class="toolbar-right">
            <div class="timer-chip" :class="{ warning: timeWarning }">
              <el-icon><Clock /></el-icon>
              <div>
                <span>剩余时间</span>
                <strong>{{ formatTime(remainingSeconds) }}</strong>
              </div>
            </div>
            <div class="toolbar-progress">
              <div class="progress-value">{{ answerProgress }}%</div>
              <p>完成度 · 已答 {{ answeredCount }}/{{ questions.length }}</p>
              <el-progress :percentage="answerProgress" :stroke-width="10" :text-inside="false" />
            </div>
            
            <!-- 摄像头监控窗口（仅在启用人脸识别时显示） -->
            <div v-if="cameraStream && currentExam?.faceRecognitionEnabled !== false" class="camera-monitor">
              <div class="camera-header">
                <el-icon><VideoCamera /></el-icon>
                <span>考试监控</span>
                <el-tag v-if="faceDetected" type="success" size="small" effect="dark">人脸检测</el-tag>
                <el-tag v-else type="danger" size="small" effect="dark">未检测到人脸</el-tag>
              </div>
              <video 
                ref="videoElement" 
                autoplay 
                muted 
                playsinline
                class="camera-preview"
              ></video>
              <canvas ref="canvasElement" style="display: none;"></canvas>
            </div>
          </div>
        </section>

        <div class="exam-layout">
          <div class="questions-pane">
            <div 
              v-for="(question, index) in questions" 
              :key="question.id" 
              class="question-card"
              :id="`question-${question.id}`">
              <div class="question-header">
                <div class="question-title">
                  <span class="question-number">第 {{ index + 1 }} 题</span>
                  <el-tag :type="getQuestionTypeTag(question.questionType)" size="small">
                    {{ getQuestionTypeName(question.questionType) }}
                  </el-tag>
                </div>
                <span class="question-score">{{ question.score }} 分</span>
              </div>

              <div class="question-content">
                <p class="question-text">{{ question.content }}</p>

                <el-radio-group 
                  v-if="question.questionType === 'single_choice'" 
                  v-model="answers[question.id]"
                  class="answer-options">
                  <el-radio 
                    v-for="(option, idx) in parseOptions(question.options)" 
                    :key="idx" 
                    :label="getOptionLabel(idx)"
                    class="answer-option">
                    <span class="option-text">{{ getOptionLabel(idx) }}. {{ option }}</span>
                  </el-radio>
                </el-radio-group>

                <el-checkbox-group 
                  v-else-if="question.questionType === 'multiple_choice'" 
                  v-model="answers[question.id]"
                  class="answer-options">
                  <el-checkbox 
                    v-for="(option, idx) in parseOptions(question.options)" 
                    :key="idx" 
                    :label="getOptionLabel(idx)"
                    class="answer-option">
                    <span class="option-text">{{ getOptionLabel(idx) }}. {{ option }}</span>
                  </el-checkbox>
                </el-checkbox-group>

                <el-radio-group 
                  v-else-if="question.questionType === 'true_false'" 
                  v-model="answers[question.id]"
                  class="answer-options">
                  <el-radio :label="'对'" class="answer-option">
                    <span class="option-text">对</span>
                  </el-radio>
                  <el-radio :label="'错'" class="answer-option">
                    <span class="option-text">错</span>
                  </el-radio>
                </el-radio-group>

                <el-input 
                  v-else
                  v-model="answers[question.id]"
                  type="textarea"
                  :rows="question.questionType === 'programming' ? 12 : (question.questionType === 'short_answer' ? 6 : 4)"
                  :placeholder="getPlaceholder(question.questionType)"
                  class="answer-textarea"
                />
              </div>
            </div>
          </div>

          <aside class="exam-sidebar">
            <div class="sidebar-card progress-card">
              <h4>实时进度</h4>
              <div class="progress-stats">
                <div>
                  <span class="stat-label">已答</span>
                  <strong>{{ answeredCount }}</strong>
                </div>
                <div>
                  <span class="stat-label">剩余</span>
                  <strong>{{ questions.length - answeredCount }}</strong>
                </div>
                <div>
                  <span class="stat-label">完成度</span>
                  <strong>{{ answerProgress }}%</strong>
                </div>
              </div>
              <el-progress type="dashboard" :percentage="answerProgress" :stroke-width="12" :color="progressColors" />
            </div>

            <div class="sidebar-card navigator-card">
              <div class="navigator-header">
                <h4>题目导航</h4>
                <span class="muted">点击跳转</span>
              </div>
              <div class="navigator-grid">
                <div 
                  v-for="nav in questionNavigator" 
                  :key="nav.id" 
                  class="navigator-item"
                  :class="nav.status"
                  @click="scrollToQuestion(nav.id)">
                  {{ nav.index }}
                </div>
              </div>
            </div>

            <div class="sidebar-card ai-card">
              <div class="ai-card-header">
                <el-icon><MagicStick /></el-icon>
                <h4>AI 考试助手</h4>
              </div>
              <div v-if="examAssistTips.length" class="ai-tip" v-for="(tip, idx) in examAssistTips" :key="idx">
                <strong>{{ tip.title }}</strong>
                <p>{{ tip.content }}</p>
              </div>
              <div v-else class="ai-tip empty">AI 正在根据试卷生成建议...</div>
            </div>
          </aside>
        </div>
      </div>

      <template #footer>
        <div class="exam-footer">
          <div class="answer-progress">
            已答题: {{ answeredCount }} / {{ questions.length }}
          </div>
          <div class="footer-actions">
            <el-button @click="handleExitExam" :loading="submitting">放弃考试</el-button>
            <el-button type="primary" @click="handleSubmitExam" :loading="submitting">
              提交试卷
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Edit, Clock, MagicStick, VideoCamera } from '@element-plus/icons-vue'
import * as faceapi from 'face-api.js'
import * as examApi from '@/api/exam'
import * as courseApi from '@/api/course'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const router = useRouter()

// 状态变量
const activeTab = ref('upcoming')
const loading = ref(false)
const historyLoading = ref(false)
const submitting = ref(false)

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const totalRecords = ref(0)

// 摄像头监控相关
const videoElement = ref(null)
const canvasElement = ref(null)
const cameraStream = ref(null)
const faceDetected = ref(false)
const faceCount = ref(0)
const modelLoaded = ref(false)
let faceDetectionInterval = null

// 考试列表
const upcomingExams = ref([])
const examHistory = ref([])

// 考试相关
const examDialogVisible = ref(false)
const currentExam = ref(null)
const studentExamId = ref(null)
const questions = ref([])
const answers = ref({})
const remainingSeconds = ref(0)
const timeWarning = ref(false)
let timer = null
const progressColors = [
  { color: '#f7768e', percentage: 40 },
  { color: '#ff9e64', percentage: 70 },
  { color: '#7aa2f7', percentage: 100 }
]

// 计算属性
const answeredCount = computed(() => {
  return Object.keys(answers.value).filter(key => {
    const answer = answers.value[key]
    if (Array.isArray(answer)) {
      return answer.length > 0
    }
    return answer !== '' && answer !== null && answer !== undefined
  }).length
})

const answerProgress = computed(() => {
  if (!questions.value.length) return 0
  return Math.round((answeredCount.value / questions.value.length) * 100)
})

// 分页后的考试记录
const paginatedExamHistory = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return examHistory.value.slice(start, end)
})

const questionNavigator = computed(() => {
  let highlighted = false
  return questions.value.map((question, index) => {
    const answered = isAnswered(answers.value[question.id])
    let status = answered ? 'answered' : 'pending'
    if (!answered && !highlighted) {
      status = 'current'
      highlighted = true
    }
    return {
      id: question.id,
      index: index + 1,
      status
    }
  })
})

const examAssistTips = computed(() => {
  if (!currentExam.value) return []
  const tips = []
  if (currentExam.value.duration && currentExam.value.duration > 0) {
    tips.push({
      title: '时间分配',
      content: `建议每题平均用时 ${(currentExam.value.duration / Math.max(questions.value.length || 1, 1)).toFixed(1)} 分钟，先完成把握大的题目。`
    })
  }
  if (questions.value.some(q => q.questionType === 'programming')) {
    tips.push({
      title: '编程题技巧',
      content: '先写出伪代码与边界测试，再动手实现，防止因为语法错误浪费时间。'
    })
  }
  if (questions.value.filter(q => q.questionType === 'multiple_choice').length >= 5) {
    tips.push({
      title: '多选题策略',
      content: '多选题务必逐项验证，不要轻易全选；不确定时可先标记稍后回看。'
    })
  }
  if (!tips.length) {
    tips.push({ title: '保持专注', content: '合理规划作答节奏，遇到难题先跳过，确保整张卷子都能覆盖。' })
  }
  return tips
})

const isAnswered = (value) => {
  if (Array.isArray(value)) {
    return value.some(item => item !== undefined && item !== null && item !== '')
  }
  return value !== '' && value !== null && value !== undefined && String(value).trim() !== ''
}

// 加载人脸识别模型
const loadFaceDetectionModels = async () => {
  try {
    console.log('开始加载人脸识别模型...')
    const MODEL_URL = '/models'
    
    // 加载 Tiny Face Detector 模型（轻量级）
    await faceapi.nets.tinyFaceDetector.loadFromUri(MODEL_URL)
    
    modelLoaded.value = true
    console.log('✓ 人脸识别模型加载成功')
  } catch (error) {
    console.error('人脸识别模型加载失败:', error)
    ElMessage.warning('人脸识别模型加载失败，将使用基础检测模式')
    modelLoaded.value = false
  }
}

// 生命周期
onMounted(() => {
  loadUpcomingExams()
  loadExamHistory()
  loadFaceDetectionModels()
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
  stopCamera()
})

// 摄像头相关方法
const requestCameraPermission = async () => {
  try {
    await ElMessageBox.confirm(
      '为确保考试公平性，系统需要使用摄像头进行人脸识别监控。考试过程中将实时检测您的人脸，请确保正对屏幕。',
      '开启摄像头监控',
      {
        confirmButtonText: '同意并开启',
        cancelButtonText: '拒绝',
        type: 'warning',
        distinguishCancelAndClose: true
      }
    )
    return true
  } catch (error) {
    return false
  }
}

const startCamera = async () => {
  try {
    console.log('正在请求摄像头权限...')
    
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      throw new Error('您的浏览器不支持摄像头访问，请使用Chrome、Edge或Firefox浏览器')
    }
    
    const stream = await navigator.mediaDevices.getUserMedia({ 
      video: { 
        width: { ideal: 640 },
        height: { ideal: 480 },
        facingMode: 'user'
      } 
    })
    
    console.log('摄像头权限已获取，视频轨道:', stream.getVideoTracks())
    cameraStream.value = stream
    
    // 等待video元素准备好
    await nextTick()
    
    if (!videoElement.value) {
      throw new Error('视频元素未找到，请刷新页面重试')
    }
    
    console.log('设置视频流到video元素')
    videoElement.value.srcObject = stream
    
    // 确保视频播放
    try {
      await videoElement.value.play()
      console.log('视频播放成功')
    } catch (playError) {
      console.warn('视频自动播放失败，尝试手动播放:', playError)
    }
    
    ElMessage.success('摄像头已启动')
  } catch (error) {
    console.error('启动摄像头失败:', error)
    let errorMsg = '无法启动摄像头'
    
    if (error.name === 'NotAllowedError') {
      errorMsg = '摄像头权限被拒绝，请在浏览器设置中允许摄像头访问'
    } else if (error.name === 'NotFoundError') {
      errorMsg = '未检测到摄像头设备，请连接摄像头后重试'
    } else if (error.name === 'NotReadableError') {
      errorMsg = '摄像头正被其他程序占用，请关闭其他使用摄像头的程序'
    } else if (error.message) {
      errorMsg = error.message
    }
    
    ElMessage.error(errorMsg)
    throw error
  }
}

const stopCamera = () => {
  if (cameraStream.value) {
    cameraStream.value.getTracks().forEach(track => track.stop())
    cameraStream.value = null
  }
  if (faceDetectionInterval) {
    clearInterval(faceDetectionInterval)
    faceDetectionInterval = null
  }
  faceDetected.value = false
}

const startFaceDetection = () => {
  // 使用 face-api.js 进行真正的深度学习人脸识别
  // 检测频率：每1秒一次，实时性更好
  faceDetectionInterval = setInterval(async () => {
    if (videoElement.value && cameraStream.value) {
      try {
        const video = videoElement.value
        
        // 检查视频是否就绪
        if (video.videoWidth === 0 || video.videoHeight === 0) {
          console.log('视频尚未就绪')
          faceDetected.value = false
          faceCount.value = 0
          return
        }
        
        if (modelLoaded.value) {
          // 方案1：使用 face-api.js 深度学习模型（精准）
          const options = new faceapi.TinyFaceDetectorOptions({
            inputSize: 224,        // 输入尺寸，越大越精确但越慢
            scoreThreshold: 0.5    // 置信度阈值，0.5表示50%以上的置信度才算检测到
          })
          
          const detections = await faceapi.detectAllFaces(video, options)
          
          faceCount.value = detections.length
          faceDetected.value = detections.length > 0
          
          // 输出详细检测信息
          if (detections.length > 0) {
            const scores = detections.map(d => (d.score * 100).toFixed(1))
            console.log(`✓ 检测到 ${detections.length} 张人脸，置信度: [${scores.join(', ')}]%`)
            
            // 警告：检测到多张人脸
            if (detections.length > 1) {
              console.warn('⚠️  检测到多张人脸，可能有其他人在场')
            }
          } else {
            console.log('✗ 未检测到人脸')
          }
        } else {
          // 方案2：降级到基础亮度检测（模型未加载时的备用方案）
          const canvas = canvasElement.value
          if (!canvas) return
          
          const context = canvas.getContext('2d')
          canvas.width = video.videoWidth
          canvas.height = video.videoHeight
          
          context.drawImage(video, 0, 0, canvas.width, canvas.height)
          const imageData = context.getImageData(0, 0, canvas.width, canvas.height)
          const data = imageData.data
          
          // 简化的亮度和方差检测
          let totalBrightness = 0
          let darkPixels = 0
          
          for (let i = 0; i < data.length; i += 4) {
            const brightness = (data[i] + data[i + 1] + data[i + 2]) / 3
            totalBrightness += brightness
            if (brightness < 20) darkPixels++
          }
          
          const pixelCount = data.length / 4
          const avgBrightness = totalBrightness / pixelCount
          const darkRatio = darkPixels / pixelCount
          
          // 基础判断：亮度合理且不是黑屏
          faceDetected.value = avgBrightness > 60 && avgBrightness < 200 && darkRatio < 0.7
          faceCount.value = faceDetected.value ? 1 : 0
          
          console.log(`基础检测 - 亮度: ${avgBrightness.toFixed(1)}, 结果: ${faceDetected.value ? '✓' : '✗'}`)
        }
      } catch (error) {
        console.error('人脸检测失败:', error)
        faceDetected.value = false
        faceCount.value = 0
      }
    }
  }, 1000) // 每1秒检测一次
}

// 方法
const loadUpcomingExams = async () => {
  loading.value = true
  try {
    const studentId = userStore.userInfo.id || 1
    
    // 获取学生的考试记录
    const historyRes = await examApi.getStudentExams(studentId)
    const historyRecords = historyRes.data || []
    
    const courseRes = await courseApi.getMyCourses(studentId)
    const courses = courseRes.data || []
    
    if (courses.length === 0) {
      upcomingExams.value = []
      return
    }
    
    const promises = courses.map(course => 
      examApi.getExamPapers(course.id).catch(() => ({ data: [] }))
    )
    const results = await Promise.all(promises)
    
    const allExams = results.flatMap(res => res.data || [])
    
    // 为每个考试添加学生考试记录信息
    upcomingExams.value = allExams
      .filter(exam => {
        const status = typeof exam.status === 'string' ? exam.status.toLowerCase() : exam.status
        return status === 'published' || status === 'active' || status === 1
      })
      .map(exam => {
        const historyRecord = historyRecords.find(h => h.examPaperId === exam.id)
        return {
          ...exam,
          studentExamRecord: historyRecord
        }
      })
  } catch (error) {
    console.error('加载考试列表失败:', error)
    ElMessage.error('加载考试列表失败')
  } finally {
    loading.value = false
  }
}

const loadExamHistory = async () => {
  historyLoading.value = true
  try {
    const studentId = userStore.userInfo.id || 1
    const res = await examApi.getStudentExams(studentId)
    examHistory.value = res.data || []
    totalRecords.value = examHistory.value.length
    // 重置到第一页
    currentPage.value = 1
  } catch (error) {
    console.error('加载考试记录失败:', error)
    ElMessage.error('加载考试记录失败')
    examHistory.value = []
    totalRecords.value = 0
  } finally {
    historyLoading.value = false
  }
}

// 分页事件处理
const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
}

const handleCurrentChange = (newPage) => {
  currentPage.value = newPage
}

const refreshExams = () => {
  loadUpcomingExams()
  loadExamHistory()
}

// 根据课程ID获取课程名称的辅助函数
const getCouseNameById = (courseId, courses) => {
  const course = courses.find(c => c.id === courseId)
  return course ? course.name : null
}

// 获取试卷详细信息的方法
const getExamPaperDetails = async (examPaperId) => {
  try {
    const res = await examApi.getExamPapersByCourse(examPaperId)
    if (res.data && res.data.length > 0) {
      return res.data[0] // 返回第一个匹配的试卷
    }
    return null
  } catch (error) {
    console.error('获取试卷详情失败:', error)
    return null
  }
}

const handleTabChange = (tab) => {
  if (tab === 'history') {
    loadExamHistory()
  }
}

const canStartExam = (exam) => {
  const now = new Date().getTime()
  const start = new Date(exam.startTime).getTime()
  const end = new Date(exam.endTime).getTime()
  return now >= start && now <= end
}

const getExamStatusType = (exam) => {
  const now = new Date().getTime()
  const start = new Date(exam.startTime).getTime()
  const end = new Date(exam.endTime).getTime()
  
  if (now < start) return 'info'
  if (now > end) return 'danger'
  return 'success'
}

const getExamStatusText = (exam) => {
  const now = new Date().getTime()
  const start = new Date(exam.startTime).getTime()
  const end = new Date(exam.endTime).getTime()
  
  if (now < start) return '未开始'
  if (now > end) return '已结束'
  return '进行中'
}

const startExam = async (exam) => {
  try {
    if (!canStartExam(exam)) {
      ElMessage.warning('考试未开始或已结束')
      return
    }
    
    // 检查是否需要人脸识别
    const needFaceRecognition = exam.faceRecognitionEnabled !== false
    
    // 如果需要人脸识别，先请求摄像头权限
    if (needFaceRecognition) {
      const cameraGranted = await requestCameraPermission()
      if (!cameraGranted) {
        ElMessage.error('此考试需要开启摄像头监控，请允许摄像头权限')
        return
      }
    }
    
    const confirmMessage = needFaceRecognition
      ? `确定要开始考试《${exam.title}》吗？考试时长${exam.duration}分钟，开始后无法暂停。\n\n注意：考试过程中将进行人脸识别监控。`
      : `确定要开始考试《${exam.title}》吗？考试时长${exam.duration}分钟，开始后无法暂停。`
    
    await ElMessageBox.confirm(
      confirmMessage, 
      '开始考试', 
      {
        confirmButtonText: '开始',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    loading.value = true
    const studentId = userStore.userInfo.id || 1
    const startRes = await examApi.startExam({
      examPaperId: exam.id,
      studentId: studentId,
      studentName: userStore.userInfo.realName || '学生',
      studentNumber: userStore.userInfo.username || '未知',
      ipAddress: '127.0.0.1'
    })
    
    if (startRes.code === 200) {
      studentExamId.value = startRes.data.id
      currentExam.value = { ...exam, ...startRes.data }
      
      // 直接从后端API加载题目（更可靠）
      try {
        const questionsRes = await examApi.getExamQuestions(exam.id)
        questions.value = questionsRes.data || []
        
        if (questions.value.length === 0) {
          throw new Error('试卷 没有题目')
        }
        
        // 初始化答案对象
        answers.value = {}
        questions.value.forEach(q => {
          if (q.questionType === 'multiple_choice') {
            answers.value[q.id] = []
          } else {
            answers.value[q.id] = ''
          }
        })
        
        remainingSeconds.value = exam.duration * 60
        startTimer()
        
        examDialogVisible.value = true
        
        // 在对话框打开后启动摄像头（仅当需要人脸识别时）
        await nextTick()
        
        if (needFaceRecognition) {
          try {
            console.log('开始启动摄像头...')
            await startCamera()
            console.log('摄像头启动成功，开始人脸检测')
            startFaceDetection()
          } catch (cameraError) {
            console.error('摄像头启动失败:', cameraError)
            // 摄像头失败不影响考试继续
          }
        } else {
          console.log('此考试未启用人脸识别，跳过摄像头启动')
        }
      } catch (error) {
        console.error('加载试卷题目失败:', error)
        ElMessage.error('加载试卷题目失败，请联系老师检查试卷配置')
        throw error
      }
    } else {
      // 处理后端返回的错误信息
      throw new Error(startRes.message || '开始考试失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('开始考试失败:', error)
      ElMessage.error('开始考试失败: ' + (error.message || '未知错误'))
    }
  } finally {
    loading.value = false
  }
}

const startTimer = () => {
  timer = setInterval(() => {
    remainingSeconds.value--
    
    if (remainingSeconds.value <= 300) {
      timeWarning.value = true
    }
    
    if (remainingSeconds.value <= 0) {
      clearInterval(timer)
      ElMessage.warning('考试时间已到，系统将自动提交试卷')
      submitExamAuto()
    }
  }, 1000)
}

const formatTime = (seconds) => {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}

const handleExitExam = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要放弃本次考试吗？放弃后将无法继续作答。', 
      '警告', 
      {
        confirmButtonText: '确定放弃',
        cancelButtonText: '继续答题',
        type: 'warning'
      }
    )
    
    if (timer) {
      clearInterval(timer)
    }
    examDialogVisible.value = false
    currentExam.value = null
    loadExamHistory()
  } catch (error) {
    // 用户取消
  }
}

const handleSubmitExam = async () => {
  const unanswered = questions.value.length - answeredCount.value
  if (unanswered > 0) {
    try {
      await ElMessageBox.confirm(
        `还有${unanswered}道题未作答，确定要提交吗？`, 
        '提示', 
        {
          confirmButtonText: '确定提交',
          cancelButtonText: '继续答题',
          type: 'warning'
        }
      )
    } catch (error) {
      return
    }
  }
  
  await submitExamAuto()
}

const submitExamAuto = async () => {
  submitting.value = true
  try {
    const answerList = questions.value.map((question, index) => {
      let studentAnswer = answers.value[question.id]
      if (Array.isArray(studentAnswer)) {
        studentAnswer = studentAnswer.join(',')
      }
      return {
        questionId: question.id,
        questionOrder: index + 1,
        studentAnswer: studentAnswer || ''
      }
    })
    
    // 提交答案
    const submitRes = await examApi.submitExam({
      studentExamId: studentExamId.value,
      answers: answerList
    })
    
    if (timer) {
      clearInterval(timer)
    }
    
    ElMessage.success('提交成功！正在生成成绩报告...')
    
    // 关闭考试对话框和摄像头
    examDialogVisible.value = false
    stopCamera()
    
    // 等待一下让后端处理完成
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 加载成绩和答题详情
    await loadExamResultAndShow(studentExamId.value)
    
    // 刷新考试记录
    loadExamHistory()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败：' + (error.message || '网络连接失败，请检查网络设置'))
  } finally {
    submitting.value = false
  }
}

const openResultPage = (examId) => {
  const routeLocation = router.resolve({
    name: 'StudentExamResult',
    params: { studentExamId: examId }
  })
  window.open(routeLocation.href, '_blank', 'noopener,noreferrer')
}

// 加载并显示考试结果
const loadExamResultAndShow = async (examId) => {
  openResultPage(examId)
}

// 查看考试结果（重构版）
const viewResult = (exam) => {
  const examId = exam?.studentExamId || exam?.id
  if (!examId) {
    ElMessage.error('未找到考试记录')
    return
  }
  openResultPage(examId)
}

// 辅助方法
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

// 获取选项标签（A, B, C, D...）
const getOptionLabel = (index) => {
  return String.fromCharCode(65 + index) // 65是'A'的ASCII码
}

const getQuestionTypeName = (type) => {
  const names = {
    'single_choice': '单选题',
    'multiple_choice': '多选题',
    'true_false': '判断题',
    'fill_blank': '填空题',
    'short_answer': '简答题',
    'programming': '编程题'
  }
  return names[type] || type
}

const getQuestionTypeTag = (type) => {
  const tags = {
    'single_choice': 'primary',
    'multiple_choice': 'success',
    'true_false': 'warning',
    'fill_blank': 'info',
    'short_answer': 'danger',
    'programming': ''
  }
  return tags[type] || ''
}

const getPlaceholder = (type) => {
  const placeholders = {
    'fill_blank': '请输入答案...',
    'short_answer': '请输入简答内容...',
    'programming': '请输入代码...'
  }
  return placeholders[type] || '请输入答案...'
}

const getSubmissionStatusType = (status) => {
  const types = {
    'submitted': 'success',
    'grading': 'warning',
    'graded': 'info'
  }
  return types[status] || 'info'
}

const getSubmissionStatusText = (status) => {
  const texts = {
    'submitted': '已提交',
    'grading': '批改中',
    'graded': '已批改'
  }
  return texts[status] || status
}

const formatScore = (score) => {
  if (score === null || score === undefined) {
    return '-'
  }
  return score
}

const scrollToQuestion = (questionId) => {
  const el = document.getElementById(`question-${questionId}`)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

// 判断是否为练习试卷
const isPracticeExam = (exam) => {
  // 检查试卷是否为练习试卷
  // 通过标题是否包含特定关键词判断
  return exam.title && (exam.title.includes('测试考试') || exam.title.includes('练习') || exam.title.includes('练习试卷'));
}


</script>

<style scoped>
.exam-page {
  padding: 24px;
  min-height: calc(100vh - 64px);
  background: radial-gradient(circle at 0% 0%, rgba(122, 162, 247, 0.12), transparent 55%),
    radial-gradient(circle at 80% 20%, rgba(125, 207, 255, 0.1), transparent 45%),
    var(--bg-primary);
}

.exam-card {
  border-radius: 20px;
  border: 1px solid var(--border);
  background: var(--bg-float);
  box-shadow: var(--shadow-md);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
}

.page-subtitle {
  margin: 0;
  color: var(--text-muted);
  letter-spacing: 0.2em;
  text-transform: uppercase;
  font-size: 13px;
}

.page-title {
  margin: 4px 0 0;
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-actions :deep(.el-button) {
  font-weight: 600;
}

.exam-fullscreen-dialog :deep(.el-dialog__body) {
  padding: 0;
  height: calc(100vh - 120px);
  overflow: hidden;
}

.exam-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, rgba(36, 40, 59, 0.95), rgba(31, 35, 53, 0.98));
}

.exam-toolbar {
  display: flex;
  justify-content: space-between;
  gap: 32px;
  padding: 28px 36px 16px;
  border-bottom: 1px solid var(--border);
}

.toolbar-left h3 {
  margin: 8px 0 4px;
  font-size: 26px;
}

.toolbar-tags {
  display: flex;
  gap: 8px;
}

.toolbar-desc {
  margin: 6px 0 12px;
  color: var(--text-secondary);
}

.toolbar-meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  color: var(--text-muted);
  font-size: 14px;
}

.toolbar-meta span {
  display: flex;
  align-items: center;
  gap: 6px;
}

.toolbar-right {
  display: flex;
  gap: 20px;
  align-items: center;
}

.timer-chip {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 12px 18px;
  border-radius: 16px;
  border: 1px solid var(--border);
  background: rgba(255, 255, 255, 0.03);
}

.timer-chip strong {
  display: block;
  font-size: 22px;
  line-height: 1.2;
}

.timer-chip span {
  color: var(--text-muted);
  font-size: 13px;
}

.timer-chip.warning {
  border-color: var(--accent-orange);
  background: rgba(255, 158, 100, 0.1);
  color: var(--accent-orange);
}

.toolbar-progress {
  background: rgba(122, 162, 247, 0.08);
  border: 1px solid rgba(122, 162, 247, 0.3);
  border-radius: 16px;
  padding: 16px 22px;
  min-width: 220px;
}

.progress-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--accent-cyan);
}

.toolbar-progress p {
  margin: 4px 0 10px;
  color: var(--text-secondary);
}

.exam-layout {
  flex: 1;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 24px;
  padding: 24px 36px 0;
  overflow: hidden;
}

.questions-pane {
  overflow-y: auto;
  padding-right: 8px;
}

.questions-pane::-webkit-scrollbar {
  width: 6px;
}

.questions-pane::-webkit-scrollbar-thumb {
  background: rgba(122, 162, 247, 0.4);
  border-radius: 3px;
}

.question-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 18px;
  padding: 20px 24px;
  margin-bottom: 20px;
  box-shadow: var(--shadow-sm);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.question-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.question-number {
  font-weight: 600;
  color: var(--accent-cyan);
  font-size: 15px;
}

.question-score {
  font-weight: 600;
  color: var(--accent-green);
}

.question-text {
  margin: 0 0 16px;
  color: var(--text-primary);
  line-height: 1.7;
}

.answer-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-start;
}

.answer-option {
  margin-right: 0;
  padding: 10px 12px;
  border-radius: 12px;
  text-align: left;
}

.answer-option:hover {
  background: rgba(122, 162, 247, 0.08);
}

.option-text {
  margin-left: 8px;
  text-align: left;
}

.answer-textarea {
  margin-top: 8px;
}

.exam-sidebar {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.sidebar-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 18px;
  padding: 18px;
  box-shadow: var(--shadow-sm);
}

.progress-card h4,
.navigator-card h4,
.ai-card h4 {
  margin: 0 0 14px;
}

.progress-stats {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.progress-stats strong {
  font-size: 20px;
}

.stat-label {
  color: var(--text-muted);
  font-size: 13px;
}

.navigator-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.navigator-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(42px, 1fr));
  gap: 8px;
}

.navigator-item {
  border-radius: 10px;
  padding: 8px 0;
  text-align: center;
  cursor: pointer;
  font-weight: 600;
  border: 1px solid var(--border);
  transition: all 0.2s ease;
}

.navigator-item.pending {
  color: var(--text-muted);
  background: rgba(255, 255, 255, 0.02);
}

.navigator-item.answered {
  color: var(--accent-green);
  border-color: rgba(158, 206, 106, 0.4);
  background: rgba(158, 206, 106, 0.08);
}

.navigator-item.current {
  color: var(--accent-cyan);
  border-color: rgba(125, 207, 255, 0.7);
  background: rgba(125, 207, 255, 0.08);
}

.navigator-item:hover {
  transform: translateY(-2px);
}

.ai-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--accent-purple);
  margin-bottom: 10px;
}

.ai-tip {
  background: rgba(36, 40, 59, 0.8);
  border-radius: 12px;
  padding: 12px 14px;
  border: 1px solid rgba(122, 162, 247, 0.2);
  margin-bottom: 10px;
}

.ai-tip strong {
  display: block;
  margin-bottom: 6px;
  color: var(--accent-cyan);
}

.ai-tip p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.5;
}

.ai-tip.empty {
  text-align: center;
  color: var(--text-muted);
}

.exam-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 36px;
  border-top: 1px solid var(--border);
  background: rgba(26, 27, 38, 0.6);
}

.answer-progress {
  color: var(--text-secondary);
  font-size: 15px;
}

.footer-actions {
  display: flex;
  gap: 12px;
}

.muted {
  color: var(--text-muted);
  font-size: 13px;
}

@media (max-width: 1280px) {
  .exam-layout {
    grid-template-columns: 1fr;
  }

  .exam-sidebar {
    flex-direction: row;
    flex-wrap: wrap;
  }

  .exam-sidebar .sidebar-card {
    flex: 1 1 300px;
  }
}

@media (max-width: 768px) {
  .exam-page {
    padding: 12px;
  }

  .exam-toolbar {
    flex-direction: column;
    padding: 20px;
  }

  .toolbar-right {
 

/* 分页样式 */
.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding: 16px 0;
}

.pagination-container :deep(.el-pagination) {
  font-weight: 500;
}

.pagination-container :deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: var(--accent-cyan);
  color: #fff;
}

.pagination-container :deep(.el-pagination.is-background .el-pager li:hover) {
  color: var(--accent-cyan);
}   width: 100%;
    flex-direction: column;
    align-items: stretch;
  }

  .exam-footer {
    flex-direction: column;
    gap: 12px;
  }

  .footer-actions {
    width: 100%;
    justify-content: space-between;
  }
}

/* 摄像头监控样式 */
.camera-monitor {
  position: relative;
  background: rgba(26, 27, 38, 0.95);
  border-radius: 12px;
  padding: 10px;
  border: 1px solid var(--border);
  min-width: 200px;
}

.camera-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--text-secondary);
}

.camera-header .el-icon {
  font-size: 16px;
}

.camera-preview {
  width: 180px;
  height: 135px;
  border-radius: 8px;
  background: #000;
  object-fit: cover;
  display: block;
}

.detection-status {
  margin-top: 8px;
  display: flex;
  gap: 6px;
  justify-content: center;
}
</style>
