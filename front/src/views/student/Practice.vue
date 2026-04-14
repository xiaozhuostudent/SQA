<template>
  <div class="practice-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span> 题库刷题</span>
          <div>
            <el-button type="success" @click="showCreateExamDialog" :disabled="selectedQuestions.length === 0">
              <el-icon><DocumentAdd /></el-icon>
              组卷练习 ({{ selectedQuestions.length }})
            </el-button>
            <el-button type="primary" @click="randomPractice">
              <el-icon><Refresh /></el-icon>
              随机抽题
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="题目类型">
          <el-select v-model="searchForm.type" placeholder="全部" style="width: 150px">
            <el-option label="全部" value="" />
            <el-option label="单选题" value="single_choice" />
            <el-option label="多选题" value="multiple_choice" />
            <el-option label="判断题" value="true_false" />
            <el-option label="填空题" value="fill_blank" />
            <el-option label="简答题" value="short_answer" />
            <el-option label="编程题" value="programming" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程">
          <el-select v-model="searchForm.courseId" placeholder="全部" style="width: 200px">
            <el-option label="全部" value="" />
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="searchForm.difficulty" placeholder="全部" style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="简单" value="easy" />
            <el-option label="中等" value="medium" />
            <el-option label="困难" value="hard" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="题目内容" style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadQuestions">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 题目列表 -->
      <el-table 
        :data="questions" 
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="content" label="题目内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="questionType" label="题型" width="100">
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.questionType)">
              {{ getTypeName(scope.row.questionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="100">
          <template #default="scope">
            <el-tag :type="getDifficultyTag(scope.row.difficulty)">
              {{ getDifficultyName(scope.row.difficulty) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分值" width="80" />
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="scope">
            <el-button size="small" type="primary" @click="practiceQuestion(scope.row)">
              开始练习
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadQuestions"
        @size-change="loadQuestions"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 练习对话框 -->
    <el-dialog v-model="practiceDialogVisible" title="题目练习" width="800px">
      <div v-if="currentQuestion" class="practice-content">
        <el-tag :type="getTypeTag(currentQuestion.questionType)" style="margin-bottom: 10px;">
          {{ getTypeName(currentQuestion.questionType) }}
        </el-tag>
        <el-tag :type="getDifficultyTag(currentQuestion.difficulty)" style="margin-left: 10px; margin-bottom: 10px;">
          {{ getDifficultyName(currentQuestion.difficulty) }}
        </el-tag>
        
        <div class="question-content">{{ currentQuestion.content }}</div>
        
        <!-- 选择题 -->
        <el-radio-group 
          v-if="currentQuestion.questionType === 'single_choice'" 
          v-model="userAnswer"
          class="answer-group"
        >
          <el-radio
            v-for="option in normalizeOptions(currentQuestion.options)"
            :key="option.key"
            :label="option.key"
          >
            {{ option.key }}. {{ option.value }}
          </el-radio>
        </el-radio-group>
        
        <!-- 多选题 -->
        <el-checkbox-group 
          v-if="currentQuestion.questionType === 'multiple_choice'" 
          v-model="userAnswer"
          class="answer-group"
        >
          <el-checkbox
            v-for="option in normalizeOptions(currentQuestion.options)"
            :key="option.key"
            :label="option.key"
          >
            {{ option.key }}. {{ option.value }}
          </el-checkbox>
        </el-checkbox-group>
        
        <!-- 判断题 -->
        <el-radio-group 
          v-if="currentQuestion.questionType === 'true_false'" 
          v-model="userAnswer"
          class="answer-group"
        >
          <el-radio label="true">对</el-radio>
          <el-radio label="false">错</el-radio>
        </el-radio-group>
        
        <!-- 主观题 -->
        <el-input 
          v-if="['fill_blank', 'short_answer', 'programming'].includes(currentQuestion.questionType)"
          v-model="userAnswer"
          type="textarea"
          :rows="currentQuestion.questionType === 'programming' ? 10 : 4"
          placeholder="请输入答案..."
        />

        <!-- 答案解析 -->
        <div v-if="showAnswer" class="answer-section">
          <el-divider content-position="left">答案解析</el-divider>
          <div class="answer-item">
            <strong>正确答案:</strong> {{ currentQuestion.answer }}
          </div>
          <div class="answer-item" v-if="currentQuestion.explanation">
            <strong>解析:</strong> {{ currentQuestion.explanation }}
          </div>
          <div class="answer-item">
            <el-tag :type="isCorrect ? 'success' : 'danger'" size="large">
              {{ isCorrect ? '✓ 回答正确' : '✗ 回答错误' }}
            </el-tag>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="practiceDialogVisible = false">关闭</el-button>
        <el-button v-if="!showAnswer" type="primary" @click="checkAnswer">提交答案</el-button>
        <el-button v-else type="success" @click="nextQuestion">下一题</el-button>
      </template>
    </el-dialog>

    <!-- 组卷练习对话框 -->
    <el-dialog v-model="examDialogVisible" title="组卷练习" width="900px">
      <div v-if="examQuestions.length > 0" class="exam-content">
        <el-alert type="info" :closable="false" style="margin-bottom: 20px;">
          共 {{ examQuestions.length }} 道题，总分 {{ totalScore }} 分
        </el-alert>

        <div v-for="(question, index) in examQuestions" :key="question.id" class="exam-question-item">
          <div class="question-header">
            <span class="question-number">第 {{ index + 1 }} 题</span>
            <el-tag :type="getTypeTag(question.questionType)" size="small">
              {{ getTypeName(question.questionType) }}
            </el-tag>
            <el-tag type="info" size="small" style="margin-left: 5px;">
              {{ question.score }} 分
            </el-tag>
          </div>
          
          <div class="question-content">{{ question.content }}</div>
          
          <!-- 选择题 -->
          <el-radio-group 
            v-if="question.questionType === 'single_choice'" 
            v-model="examAnswers[question.id]"
            class="answer-group"
          >
            <el-radio
              v-for="option in normalizeOptions(question.options)"
              :key="option.key"
              :label="option.key"
            >
              {{ option.key }}. {{ option.value }}
            </el-radio>
          </el-radio-group>
          
          <!-- 多选题 -->
          <el-checkbox-group 
            v-if="question.questionType === 'multiple_choice'" 
            v-model="examAnswers[question.id]"
            class="answer-group"
          >
            <el-checkbox
              v-for="option in normalizeOptions(question.options)"
              :key="option.key"
              :label="option.key"
            >
              {{ option.key }}. {{ option.value }}
            </el-checkbox>
          </el-checkbox-group>
          
          <!-- 判断题 -->
          <el-radio-group 
            v-if="question.questionType === 'true_false'" 
            v-model="examAnswers[question.id]"
            class="answer-group"
          >
            <el-radio label="true">对</el-radio>
            <el-radio label="false">错</el-radio>
          </el-radio-group>
          
          <!-- 主观题 -->
          <el-input 
            v-if="['fill_blank', 'short_answer', 'programming'].includes(question.questionType)"
            v-model="examAnswers[question.id]"
            type="textarea"
            :rows="question.questionType === 'programming' ? 6 : 3"
            placeholder="请输入答案..."
          />
        </div>
      </div>

      <template #footer>
        <el-button @click="examDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitExam">提交答卷</el-button>
      </template>
    </el-dialog>

    <!-- 成绩对话框 -->
    <el-dialog v-model="resultDialogVisible" title="练习结果" width="700px">
      <el-result :icon="examScore >= 60 ? 'success' : 'error'" :title="`得分: ${examScore}/${totalScore}`">
        <template #sub-title>
          正确 {{ correctCount }} 题，错误 {{ examQuestions.length - correctCount }} 题
        </template>
        <template #extra>
          <el-button type="primary" @click="resultDialogVisible = false">确定</el-button>
          <el-button @click="reviewExam">查看详情</el-button>
        </template>
      </el-result>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, DocumentAdd } from '@element-plus/icons-vue'
import { getQuestions } from '@/api/question'
import { getAllCourses } from '@/api/course'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const searchForm = ref({
  type: '',
  courseId: '',
  difficulty: '',
  keyword: ''
})

const questions = ref([])
const courses = ref([])
const loading = ref(false)
const selectedQuestions = ref([])

const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

// 单题练习
const practiceDialogVisible = ref(false)
const currentQuestion = ref(null)
const userAnswer = ref('')
const showAnswer = ref(false)
const isCorrect = ref(false)

// 组卷练习
const examDialogVisible = ref(false)
const examQuestions = ref([])
const examAnswers = ref({})
const resultDialogVisible = ref(false)
const examScore = ref(0)
const correctCount = ref(0)

const totalScore = computed(() => {
  return examQuestions.value.reduce((sum, q) => sum + (q.score || 0), 0)
})

onMounted(() => {
  loadCourses()
  loadQuestions()
})

const loadCourses = async () => {
  try {
    const res = await getAllCourses()
    if (res.code === 200) {
      courses.value = res.data || []
      console.log('加载课程成功:', courses.value)
    } else {
      console.error('加载课程失败:', res)
    }
  } catch (error) {
    console.error('加载课程失败:', error)
  }
}

const loadQuestions = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size,
      type: searchForm.value.type || undefined,
      courseId: searchForm.value.courseId || undefined,
      difficulty: searchForm.value.difficulty || undefined,
      keyword: searchForm.value.keyword || undefined
    }
    
    console.log('请求参数:', params)
    const res = await getQuestions(params)
    console.log('API响应:', res)
    
    if (res.code === 200) {
      questions.value = res.data.list || res.data || []
      pagination.value.total = res.data.total || questions.value.length
      console.log('加载题目成功:', questions.value.length, '条')
    } else {
      ElMessage.error(res.message || '加载题目失败')
    }
  } catch (error) {
    console.error('加载题目失败:', error)
    ElMessage.error('加载题目失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.value = {
    type: '',
    courseId: '',
    difficulty: '',
    keyword: ''
  }
  pagination.value.page = 1
  loadQuestions()
}

const handleSelectionChange = (selection) => {
  selectedQuestions.value = selection
}

const showCreateExamDialog = () => {
  if (selectedQuestions.value.length === 0) {
    ElMessage.warning('请先选择要练习的题目')
    return
  }
  examQuestions.value = [...selectedQuestions.value]
  examAnswers.value = {}
  examDialogVisible.value = true
}

const randomPractice = () => {
  if (questions.value.length === 0) {
    ElMessage.warning('暂无可用题目')
    return
  }
  const randomIndex = Math.floor(Math.random() * questions.value.length)
  practiceQuestion(questions.value[randomIndex])
}

const practiceQuestion = (question) => {
  currentQuestion.value = { ...question }
  userAnswer.value = question.questionType === 'multiple_choice' ? [] : ''
  showAnswer.value = false
  isCorrect.value = false
  practiceDialogVisible.value = true
}

const parseOptions = (options) => {
  if (!options) return []
  if (Array.isArray(options)) return options
  if (typeof options === 'string') {
    try {
      return JSON.parse(options)
    } catch {
      return options
        .split(/[;；\n\r]/)
        .map(o => o.trim())
        .filter(o => o)
    }
  }
  return []
}

const normalizeOptions = (options) => {
  const raw = parseOptions(options)
  if (!Array.isArray(raw)) return []

  // 兼容：['选项1','选项2'] / [{key,value}] / [{label}] 等多种存储格式
  const normalized = raw
    .map((item, index) => {
      if (item == null) return null
      if (typeof item === 'string') {
        return {
          key: String.fromCharCode(65 + index),
          value: item.trim()
        }
      }

      if (typeof item === 'object') {
        const key = String(item.key || item.K || '').trim() || String.fromCharCode(65 + index)
        const value = String(item.value ?? item.label ?? item.text ?? '').trim()
        return {
          key,
          value
        }
      }

      return {
        key: String.fromCharCode(65 + index),
        value: String(item).trim()
      }
    })
    .filter(Boolean)
    .filter(o => o.value)

  return normalized
}

const normalizeBoolAnswer = (value) => {
  const s = String(value ?? '').trim().toLowerCase()
  if (['true', 't', '1', '对', '正确', '是', 'yes', 'y'].includes(s)) return 'true'
  if (['false', 'f', '0', '错', '错误', '否', 'no', 'n'].includes(s)) return 'false'
  return String(value ?? '').trim()
}

const normalizeChoiceAnswerToKeys = (answer, options) => {
  const answerStr = String(answer ?? '').trim()
  if (!answerStr) return ''

  const valueToKey = new Map(
    (options || [])
      .filter(o => o && o.key)
      .map(o => [String(o.value ?? '').trim().toLowerCase(), String(o.key).trim().toUpperCase()])
  )

  const tokens = answerStr
    .split(/[,|，、\s]+/)
    .map(t => t.trim())
    .filter(Boolean)

  // 单选：支持 'B' 或直接存了选项文本
  if (tokens.length === 1) {
    const t = tokens[0]
    if (/^[A-Za-z]$/.test(t)) return t.toUpperCase()
    const mapped = valueToKey.get(t.toLowerCase())
    return mapped || t
  }

  // 多选：支持 'A,B' / 'A|B' / 直接存选项文本
  const mappedTokens = tokens.map(t => {
    if (/^[A-Za-z]$/.test(t)) return t.toUpperCase()
    return valueToKey.get(t.toLowerCase()) || t
  })

  return mappedTokens.join(',')
}

const judgeAnswer = (question, userAnswerValue) => {
  const questionType = question?.questionType
  const options = normalizeOptions(question?.options)

  // 统一处理正确答案（兼容旧数据：可能存 key 或存选项文本）
  let correctAnswerRaw = question?.answer
  if (questionType === 'true_false') {
    correctAnswerRaw = normalizeBoolAnswer(correctAnswerRaw)
  } else if (questionType === 'single_choice' || questionType === 'multiple_choice') {
    correctAnswerRaw = normalizeChoiceAnswerToKeys(correctAnswerRaw, options)
  } else {
    correctAnswerRaw = String(correctAnswerRaw ?? '').trim()
  }

  // 统一处理用户答案
  let userAnswerNormalized = userAnswerValue
  if (questionType === 'true_false') {
    userAnswerNormalized = normalizeBoolAnswer(userAnswerValue)
  }

  // 多选
  if (questionType === 'multiple_choice') {
    const correctTokens = String(correctAnswerRaw)
      .split(/[,|，、\s]+/)
      .map(t => t.trim().toUpperCase())
      .filter(Boolean)
      .sort()
    const userTokens = Array.isArray(userAnswerNormalized)
      ? userAnswerNormalized.map(t => String(t).trim().toUpperCase()).filter(Boolean).sort()
      : String(userAnswerNormalized ?? '')
          .split(/[,|，、\s]+/)
          .map(t => t.trim().toUpperCase())
          .filter(Boolean)
          .sort()
    return JSON.stringify(userTokens) === JSON.stringify(correctTokens)
  }

  // 单选 / 判断 / 主观
  const correctStr = String(correctAnswerRaw ?? '').trim()
  const userStr = String(userAnswerNormalized ?? '').trim()
  return userStr.toLowerCase() === correctStr.toLowerCase()
}

const checkAnswer = () => {
  if (!userAnswer.value || (Array.isArray(userAnswer.value) && userAnswer.value.length === 0)) {
    ElMessage.warning('请先作答')
    return
  }

  isCorrect.value = judgeAnswer(currentQuestion.value, userAnswer.value)
  showAnswer.value = true
  
  if (isCorrect.value) {
    ElMessage.success('回答正确！')
  } else {
    ElMessage.error('回答错误，请查看解析')
  }
}

const nextQuestion = () => {
  randomPractice()
}

const submitExam = () => {
  // 检查是否所有题目都已作答
  const unanswered = examQuestions.value.filter(q => {
    const answer = examAnswers.value[q.id]
    return !answer || (Array.isArray(answer) && answer.length === 0)
  })
  
  if (unanswered.length > 0) {
    ElMessageBox.confirm(
      `还有 ${unanswered.length} 道题未作答，确定提交吗？`,
      '提示',
      { type: 'warning' }
    ).then(() => {
      gradeExam()
    }).catch(() => {})
  } else {
    gradeExam()
  }
}

const gradeExam = () => {
  let score = 0
  let correct = 0
  
  examQuestions.value.forEach(question => {
    const userAnswer = examAnswers.value[question.id]

    if (judgeAnswer(question, userAnswer)) {
      score += question.score || 0
      correct++
    }
  })
  
  examScore.value = score
  correctCount.value = correct
  examDialogVisible.value = false
  resultDialogVisible.value = true
}

const reviewExam = () => {
  resultDialogVisible.value = false
  examDialogVisible.value = true
}

const getTypeTag = (type) => {
  const map = {
    'single_choice': '',
    'multiple_choice': 'warning',
    'true_false': 'success',
    'fill_blank': 'info',
    'short_answer': 'info',
    'programming': 'danger'
  }
  return map[type] || ''
}

const getTypeName = (type) => {
  const map = {
    'single_choice': '单选题',
    'multiple_choice': '多选题',
    'true_false': '判断题',
    'fill_blank': '填空题',
    'short_answer': '简答题',
    'programming': '编程题'
  }
  return map[type] || type
}

const getDifficultyTag = (difficulty) => {
  const map = {
    'easy': 'success',
    'medium': 'warning',
    'hard': 'danger'
  }
  return map[difficulty] || ''
}

const getDifficultyName = (difficulty) => {
  const map = {
    'easy': '简单',
    'medium': '中等',
    'hard': '困难'
  }
  return map[difficulty] || difficulty
}
</script>

<style scoped>
.practice-container {
  padding: 24px;
  background-color: var(--bg-primary);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

/* 练习对话框样式 */
.practice-content {
  padding: 24px;
  max-height: 70vh;
  overflow-y: auto;
}

.question-content {
  font-size: 17px;
  line-height: 1.8;
  margin: 20px 0;
  padding: 20px;
  background: var(--bg-secondary);
  border-radius: 12px;
  color: var(--text-primary);
  white-space: pre-wrap;
  border: 2px solid var(--border);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 优化选项样式 */
.answer-group {
  margin: 30px 0;
}

:deep(.answer-group .el-radio),
:deep(.answer-group .el-checkbox) {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  margin: 12px 0;
  background: var(--bg-float);
  border: 2px solid var(--border);
  border-radius: 12px;
  transition: all 0.3s ease;
  cursor: pointer;
  font-size: 15px;
  line-height: 1.6;
}

:deep(.answer-group .el-radio:hover),
:deep(.answer-group .el-checkbox:hover) {
  border-color: var(--accent-cyan);
  background: var(--bg-highlight);
  transform: translateX(5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

:deep(.answer-group .el-radio.is-checked),
:deep(.answer-group .el-checkbox.is-checked) {
  border-color: var(--accent-cyan);
  background: rgba(64, 158, 255, 0.1);
  font-weight: 500;
}

:deep(.answer-group .el-radio__label),
:deep(.answer-group .el-checkbox__label) {
  padding-left: 12px;
  color: var(--text-primary);
  flex: 1;
  word-break: break-word;
}

:deep(.answer-group .el-radio__input),
:deep(.answer-group .el-checkbox__input) {
  margin-right: 8px;
}

/* 答案解析样式 */
.answer-section {
  margin-top: 30px;
  padding: 24px;
  background: rgba(64, 158, 255, 0.05);
  border-radius: 12px;
  border-left: 4px solid var(--accent-cyan);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.answer-item {
  margin: 18px 0;
  font-size: 15px;
  line-height: 1.8;
  color: var(--text-primary);
  padding: 12px;
  background: var(--bg-float);
  border-radius: 8px;
}

.answer-item strong {
  color: var(--accent-cyan);
  margin-right: 8px;
  font-size: 16px;
}

/* 组卷练习样式 */
.exam-content {
  max-height: 65vh;
  overflow-y: auto;
  padding: 12px;
}

.exam-question-item {
  margin-bottom: 32px;
  padding: 24px;
  background: var(--bg-float);
  border-radius: 12px;
  border: 2px solid var(--border);
  transition: all 0.3s ease;
}

.exam-question-item:hover {
  border-color: var(--accent-cyan);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.question-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--border);
}

.question-number {
  font-size: 16px;
  font-weight: 600;
  color: var(--accent-cyan);
}

/* 卡片样式 */
:deep(.el-card) {
  background-color: var(--bg-float);
  border-color: var(--border);
  border-radius: 12px;
}

:deep(.el-card__header) {
  background-color: var(--bg-secondary);
  color: var(--text-primary);
  font-weight: 600;
  border-radius: 12px 12px 0 0;
}

/* 表格样式 */
:deep(.el-table) {
  background-color: var(--bg-float);
  color: var(--text-primary);
  border-radius: 8px;
}

:deep(.el-table th) {
  background-color: var(--bg-secondary);
  color: var(--text-primary);
}

:deep(.el-table tr) {
  background-color: var(--bg-float);
}

:deep(.el-table__body tr:hover > td) {
  background-color: var(--bg-highlight) !important;
}

/* 对话框样式 */
:deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  background: linear-gradient(135deg, #409eff 0%, #3a8ee6 100%);
  padding: 20px;
}

:deep(.el-dialog__title) {
  color: white;
  font-size: 18px;
  font-weight: 600;
}

:deep(.el-dialog__headerbtn .el-dialog__close) {
  color: white;
  font-size: 20px;
}

/* 滚动条样式 */
.practice-content::-webkit-scrollbar,
.exam-content::-webkit-scrollbar {
  width: 8px;
}

.practice-content::-webkit-scrollbar-track,
.exam-content::-webkit-scrollbar-track {
  background: var(--bg-secondary);
  border-radius: 4px;
}

.practice-content::-webkit-scrollbar-thumb,
.exam-content::-webkit-scrollbar-thumb {
  background: var(--accent-cyan);
  border-radius: 4px;
}

.practice-content::-webkit-scrollbar-thumb:hover,
.exam-content::-webkit-scrollbar-thumb:hover {
  background: #5a8fd8;
}
</style>
