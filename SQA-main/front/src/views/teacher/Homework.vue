<template>
  <div class="homework-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>作业管理</span>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>
            创建作业
          </el-button>
        </div>
      </template>

      <!-- 作业列表 -->
      <el-table :data="homeworkList" v-loading="loading" style="width: 100%">
        <el-table-column prop="title" label="作业标题" min-width="180" />
        <el-table-column prop="courseName" label="课程" width="150" />
        <el-table-column prop="totalScore" label="总分" width="80" />
        <el-table-column prop="deadline" label="截止时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 'draft'" type="info">草稿</el-tag>
            <el-tag v-else-if="scope.row.status === 'published'" type="success">已发布</el-tag>
            <el-tag v-else-if="scope.row.status === 'closed'" type="warning">已关闭</el-tag>
            <el-tag v-else type="default">已归档</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交情况" width="140">
          <template #default="scope">
            <el-button link type="primary" @click="viewStats(scope.row)">
              {{ scope.row.stats ? `${scope.row.stats.submitted}/${getCourseStudentTotal(scope.row.courseId) || 0}` : '-' }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="320">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row)">查看</el-button>
            <el-button size="small" type="primary" @click="editHomework(scope.row)" v-if="scope.row.status === 'draft'">编辑</el-button>
            <el-button size="small" type="success" @click="publishHomework(scope.row)" v-if="scope.row.status === 'draft'">发布</el-button>
            <el-button size="small" @click="viewSubmissions(scope.row)" v-if="scope.row.status !== 'draft'">提交列表</el-button>
            <el-button size="small" type="danger" @click="deleteHomework(scope.row)" v-if="scope.row.status === 'draft'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑作业对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="80%" 
      :close-on-click-modal="false"
    >
      <el-form :model="homeworkForm" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课程">
              <el-select v-model="homeworkForm.courseId" placeholder="请选择课程" style="width: 100%">
                <el-option
                  v-for="course in myCourses"
                  :key="course.id"
                  :label="course.name"
                  :value="course.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作业标题">
              <el-input v-model="homeworkForm.title" placeholder="请输入作业标题" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="作业描述">
          <el-input
            v-model="homeworkForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入作业描述"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="截止时间">
              <el-date-picker
                v-model="homeworkForm.deadline"
                type="datetime"
                placeholder="选择截止时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="允许迟交">
              <el-switch v-model="homeworkForm.allowLateSubmission" />
              <span v-if="homeworkForm.allowLateSubmission" style="margin-left: 10px">
                扣分比例:
                <el-input-number 
                  v-model="homeworkForm.latePenalty" 
                  :min="0" 
                  :max="100" 
                  size="small" 
                  style="width: 120px"
                /> %
              </span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="提交后显示答案">
          <el-switch v-model="homeworkForm.showAnswer" />
        </el-form-item>

        <el-divider>选择题目</el-divider>

        <div style="margin-bottom: 15px">
          <el-button type="primary" @click="showQuestionSelector">
            <el-icon><Plus /></el-icon>
            从题库选择题目
          </el-button>
          <el-button 
            type="success" 
            @click="createQuestionVisible = true" 
            :disabled="!homeworkForm.courseId"
            style="margin-left: 10px"
          >
            <el-icon><EditPen /></el-icon>
            创建新题目
          </el-button>
        </div>

        <!-- 已选题目列表 -->
        <el-table :data="selectedQuestions" style="width: 100%">
          <el-table-column label="序号" width="60">
            <template #default="scope">
              {{ scope.$index + 1 }}
            </template>
          </el-table-column>
          <el-table-column prop="content" label="题目内容" min-width="300" />
          <el-table-column prop="questionType" label="题型" width="100">
            <template #default="scope">
              {{ getQuestionTypeName(scope.row.questionType) }}
            </template>
          </el-table-column>
          <el-table-column label="分值" width="150">
            <template #default="scope">
              <el-input-number 
                v-model="scope.row.questionScore" 
                :min="1" 
                :max="100" 
                size="small"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button size="small" type="danger" @click="removeQuestion(scope.$index)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div style="margin-top: 15px; text-align: right">
          <el-text type="primary" size="large">
            总分: {{ totalScore }} 分
          </el-text>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 题目选择对话框 -->
    <el-dialog 
      v-model="questionSelectorVisible" 
      title="选择题目" 
      width="80%"
      :close-on-click-modal="false"
    >
      <!-- 筛选条件 -->
      <el-form :inline="true" :model="questionFilters" style="margin-bottom: 15px">
        <el-form-item label="题型">
          <el-select v-model="questionFilters.type" placeholder="全部" clearable style="width: 150px">
            <el-option label="单选题" value="single_choice" />
            <el-option label="多选题" value="multiple_choice" />
            <el-option label="判断题" value="true_false" />
            <el-option label="填空题" value="fill_blank" />
            <el-option label="简答题" value="short_answer" />
            <el-option label="编程题" value="programming" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="questionFilters.difficulty" placeholder="全部" clearable style="width: 120px">
            <el-option label="简单" value="easy" />
            <el-option label="中等" value="medium" />
            <el-option label="困难" value="hard" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadQuestionBank">查询</el-button>
        </el-form-item>
      </el-form>

      <!-- 题库列表 -->
      <el-table 
        :data="questionBank" 
        @selection-change="handleQuestionSelection"
        style="width: 100%"
        max-height="400"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="content" label="题目内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="questionType" label="题型" width="100">
          <template #default="scope">
            {{ getQuestionTypeName(scope.row.questionType) }}
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.difficulty === 'easy'" type="success" size="small">简单</el-tag>
            <el-tag v-else-if="scope.row.difficulty === 'medium'" type="warning" size="small">中等</el-tag>
            <el-tag v-else type="danger" size="small">困难</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="建议分值" width="90" />
      </el-table>

      <template #footer>
        <el-button @click="questionSelectorVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmQuestionSelection">
          确定选择 ({{ tempSelectedQuestions.length }})
        </el-button>
      </template>
    </el-dialog>

    <!-- 提交列表对话框 -->
    <el-dialog 
      v-model="submissionsDialogVisible" 
      title="作业提交列表" 
      width="85%"
      class="submission-dialog"
    >
      <div class="submission-summary">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-number" style="color: #7aa2f7;">{{ submissions.length }}</div>
              <div class="stat-label">总提交数</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-number" style="color: #e0af68;">{{ pendingSubmissions.length }}</div>
              <div class="stat-label">待批改</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-number" style="color: #9ece6a;">{{ gradedSubmissions.length }}</div>
              <div class="stat-label">已批改</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <el-tabs v-model="submissionTab" class="submission-tabs">
        <el-tab-pane name="pending">
          <template #label>
            <span class="tab-label">待批改 <span class="tab-count">({{ pendingSubmissions.length }})</span></span>
          </template>
          <el-table 
            :data="pendingSubmissions" 
            style="width: 100%" 
            empty-text="暂无待批改提交"
            stripe
          >
            <el-table-column type="index" label="序号" width="130" align="center" />
            <el-table-column prop="studentName" label="学生姓名" width="230" />
            <el-table-column prop="studentNumber" label="学号" width="270" />
            <el-table-column prop="submitTime" label="提交时间" width="260">
              <template #default="scope">
                <div>{{ scope.row.submitTime }}</div>
                <el-tag v-if="scope.row.isLate" type="danger" size="small" style="margin-top: 4px;">
                  迟交
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="150" align="center">
              <template #default="scope">
                <el-tag type="warning">待批改</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right" align="center">
              <template #default="scope">
                <el-button size="small" type="primary" @click="gradeSubmission(scope.row)">
                  <el-icon><EditPen /></el-icon>
                  批改
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane name="graded">
          <template #label>
            <span class="tab-label">已批改 <span class="tab-count">({{ gradedSubmissions.length }})</span></span>
          </template>
          <el-table 
            :data="gradedSubmissions" 
            style="width: 100%" 
            empty-text="暂无已批改提交"
            stripe
          >
            <el-table-column type="index" label="序号" width="120" align="center" />
            <el-table-column prop="studentName" label="学生姓名" width="220" />
            <el-table-column prop="studentNumber" label="学号" width="240" />
            <el-table-column prop="submitTime" label="提交时间" width="260">
              <template #default="scope">
                <div>{{ scope.row.submitTime }}</div>
                <el-tag v-if="scope.row.isLate" type="danger" size="small" style="margin-top: 4px;">
                  迟交
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="得分" width="150" align="center">
              <template #default="scope">
                <el-tag 
                  :type="getScoreType(scope.row.score)" 
                  v-if="scope.row.score !== null && scope.row.score !== undefined"
                  size="large"
                  effect="dark"
                >
                  {{ scope.row.score }} 分
                </el-tag>
                <span v-else style="color: #909399;">未评分</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="150" align="center">
              <template #default="scope">
                <el-tag type="success">已批改</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right" align="center">
              <template #default="scope">
                <el-button size="small" @click="gradeSubmission(scope.row)">
                  <el-icon><View /></el-icon>
                  查看
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- 作业详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="作业详情" 
      width="70%"
    >
      <div v-if="currentHomeworkDetail.homework">
        <el-descriptions :column="2" border style="margin-bottom: 20px">
          <el-descriptions-item label="作业标题">{{ currentHomeworkDetail.homework.title }}</el-descriptions-item>
          <el-descriptions-item label="课程">{{ currentHomeworkDetail.homework.courseName }}</el-descriptions-item>
          <el-descriptions-item label="总分">{{ currentHomeworkDetail.homework.totalScore }}</el-descriptions-item>
          <el-descriptions-item label="截止时间">{{ currentHomeworkDetail.homework.deadline }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="currentHomeworkDetail.homework.status === 'draft'" type="info">草稿</el-tag>
            <el-tag v-else-if="currentHomeworkDetail.homework.status === 'published'" type="success">已发布</el-tag>
            <el-tag v-else-if="currentHomeworkDetail.homework.status === 'closed'" type="warning">已关闭</el-tag>
            <el-tag v-else type="default">已归档</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="允许迟交">
            <el-tag v-if="currentHomeworkDetail.homework.allowLateSubmission" type="success">是</el-tag>
            <el-tag v-else type="info">否</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="作业描述" :span="2">{{ currentHomeworkDetail.homework.description }}</el-descriptions-item>
        </el-descriptions>

        <!-- 题目列表 -->
        <el-divider content-position="left">题目列表</el-divider>
        <div v-for="(question, index) in currentHomeworkDetail.questions" :key="question.id" style="margin-bottom: 20px">
          <el-card>
            <template #header>
              <div style="display: flex; justify-content: space-between; align-items: center">
                <span>
                  第 {{ index + 1 }} 题 
                  <el-tag size="small" style="margin-left: 10px">{{ getQuestionTypeName(question.questionType) }}</el-tag>
                  <el-tag 
                    size="small" 
                    style="margin-left: 5px"
                    :type="question.difficulty === 'easy' ? 'success' : question.difficulty === 'medium' ? 'warning' : 'danger'"
                  >
                    {{ question.difficulty === 'easy' ? '简单' : question.difficulty === 'medium' ? '中等' : '困难' }}
                  </el-tag>
                </span>
                <el-tag type="primary">{{ question.score }} 分</el-tag>
              </div>
            </template>
            <div style="margin-bottom: 12px;">
              <strong>题目内容:</strong> {{ question.content }}
            </div>
            <div v-if="question.questionType === 'single_choice' || question.questionType === 'multiple_choice'" style="margin-bottom: 12px;">
              <strong>选项:</strong>
              <span v-if="question.options" style="margin-left: 10px;">
                {{ formatOptions(question.options) }}
              </span>
            </div>
            <div style="margin-bottom: 10px;">
              <strong>答案:</strong> {{ question.answer }}
            </div>
          </el-card>
        </div>
      </div>
    </el-dialog>

    <!-- 批改对话框 -->
    <el-dialog 
      v-model="gradeDialogVisible" 
      title="批改作业" 
      width="80%"
      :close-on-click-modal="false"
    >
      <div v-if="currentSubmission.id">
        <el-descriptions :column="2" border style="margin-bottom: 20px">
          <el-descriptions-item label="学生">{{ currentSubmission.studentName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentSubmission.studentNumber }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ currentSubmission.submitTime }}</el-descriptions-item>
          <el-descriptions-item label="是否迟交">
            <el-tag v-if="currentSubmission.isLate" type="danger">是</el-tag>
            <el-tag v-else type="success">否</el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 答题详情 -->
        <div v-for="(answer, index) in currentSubmission.answers" :key="answer.id" style="margin-bottom: 30px">
          <el-card>
            <template #header>
              <div style="display: flex; justify-content: space-between; align-items: center">
                <span>第 {{ index + 1 }} 题</span>
                <el-tag v-if="answer.isCorrect !== null">
                  {{ answer.isCorrect ? '✓ 正确' : '✗ 错误' }}
                </el-tag>
              </div>
            </template>

            <div style="margin-bottom: 12px;">
              <strong>题目:</strong> {{ answer.question.content }}
            </div>

            <div v-if="(answer.question.questionType === 'single_choice' || answer.question.questionType === 'multiple_choice') && answer.question.options" 
                 style="margin-bottom: 12px;">
              <strong>选项:</strong>
              <span style="margin-left: 10px;">
                {{ formatOptions(answer.question.options) }}
              </span>
            </div>

            <div style="margin-bottom: 10px;">
              <strong>学生答案:</strong> {{ answer.studentAnswer || '未作答' }}
            </div>

            <div style="margin-bottom: 10px;" v-if="answer.question.questionType !== 'programming'">
              <strong>标准答案:</strong> {{ answer.question.answer }}
            </div>

            <el-divider />

            <el-form :inline="true">
              <el-form-item label="得分">
                <el-input-number 
                  v-model="answer.score" 
                  :min="0" 
                  :max="answer.question.score" 
                  :disabled="answer.isCorrect !== null && answer.question.questionType !== 'programming'"
                />
                <span style="margin-left: 5px">/ {{ answer.question.score }} 分</span>
              </el-form-item>
              <el-form-item label="评语">
                <el-input 
                  v-model="answer.teacherComment" 
                  placeholder="选填" 
                  style="width: 300px"
                />
              </el-form-item>
            </el-form>

            <div v-if="showAiFeedback(answer)" class="ai-feedback-block">
              <div class="ai-feedback-header">
                <el-tag type="warning" size="small">AI评分</el-tag>
                <el-tag v-if="answer.aiScore !== undefined && answer.aiScore !== null" type="primary" size="small">
                  {{ answer.aiScore }} 分
                </el-tag>
              </div>
              <p v-if="answer.aiFeedback?.comment" class="ai-feedback-text">{{ answer.aiFeedback.comment }}</p>
              <div v-if="answer.aiFeedback?.strengths?.length" class="ai-list">
                <strong>优点：</strong>
                <span>{{ answer.aiFeedback.strengths.join('；') }}</span>
              </div>
              <div v-if="answer.aiFeedback?.improvements?.length" class="ai-list">
                <strong>改进建议：</strong>
                <span>{{ answer.aiFeedback.improvements.join('；') }}</span>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 总评 -->
        <el-form label-width="100px" style="margin-top: 20px">
          <el-form-item label="总分">
            <el-text type="primary" size="large">{{ calculateTotalScore() }} 分</el-text>
          </el-form-item>
          <el-form-item label="总评">
            <el-input
              v-model="gradeForm.teacherComment"
              type="textarea"
              :rows="3"
              placeholder="请输入总评"
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="gradeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleGrade">提交批改</el-button>
      </template>
    </el-dialog>

    <!-- 创建题目对话框 -->
    <CreateQuestionDialog 
      v-model="createQuestionVisible" 
      :courseId="homeworkForm.courseId"
      @success="handleQuestionCreated"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, EditPen, View } from '@element-plus/icons-vue'
import {
  getTeacherHomeworks,
  createHomework,
  updateHomework,
  deleteHomework as deleteHomeworkApi,
  publishHomework as publishHomeworkApi,
  getHomeworkDetail,
  getHomeworkSubmissions,
  getHomeworkStats,
  gradeHomework as gradeHomeworkApi
} from '@/api/homework'
import { getQuestionBankByCourse } from '@/api/question'
import { getTeacherCourses } from '@/api/course'
import { useUserStore } from '@/stores/user'
import { formatOptionsText } from '@/utils/question'
import CreateQuestionDialog from '@/components/CreateQuestionDialog.vue'

const userStore = useUserStore()
const teacherId = computed(() => userStore.userInfo?.id)

const loading = ref(false)
const dialogVisible = ref(false)
const questionSelectorVisible = ref(false)
const createQuestionVisible = ref(false)
const submissionsDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const gradeDialogVisible = ref(false)
const isEdit = ref(false)

const homeworkList = ref([])
const myCourses = ref([])
const questionBank = ref([])
const selectedQuestions = ref([])
const tempSelectedQuestions = ref([])
const submissions = ref([])
const submissionTab = ref('pending')
const currentSubmission = ref({})
const currentHomeworkDetail = ref({ homework: null, questions: [] })

const extractSubmissionStatus = (submission) => {
  const candidates = [
    submission?.normalizedStatus,
    submission?.status,
    submission?.studentHomework?.status
  ]
  for (const value of candidates) {
    if (typeof value === 'string' && value.trim()) {
      return value.trim().toLowerCase()
    }
  }
  return ''
}

const withNormalizedStatus = (submission) => ({
  ...submission,
  normalizedStatus: extractSubmissionStatus(submission)
})

const pendingSubmissions = computed(() =>
  submissions.value.filter(item => extractSubmissionStatus(item) !== 'graded')
)

const gradedSubmissions = computed(() =>
  submissions.value.filter(item => extractSubmissionStatus(item) === 'graded')
)

const courseStudentMap = computed(() => {
  const map = {}
  myCourses.value.forEach(course => {
    map[course.id] = course.enrolled ?? course.studentCount ?? course.capacity ?? 0
  })
  return map
})

const getCourseStudentTotal = (courseId) => {
  if (!courseId) {
    return 0
  }
  return courseStudentMap.value[courseId] ?? 0
}

const homeworkForm = ref({
  id: null,
  courseId: null,
  title: '',
  description: '',
  deadline: null,
  allowLateSubmission: false,
  latePenalty: 0,
  showAnswer: false
})

const questionFilters = ref({
  type: '',
  difficulty: ''
})

const gradeForm = ref({
  teacherComment: ''
})

const dialogTitle = computed(() => isEdit.value ? '编辑作业' : '创建作业')

const totalScore = computed(() => {
  return selectedQuestions.value.reduce((sum, q) => sum + (q.questionScore || 0), 0)
})

// 加载我的课程
const loadMyCourses = async () => {
  try {
    const res = await getTeacherCourses(teacherId.value)
    myCourses.value = res.data || []
  } catch (error) {
    console.error('加载课程列表失败:', error)
    ElMessage.error('加载课程列表失败')
  }
}

// 加载作业列表
const loadHomeworkList = async () => {
  loading.value = true
  try {
    const res = await getTeacherHomeworks(teacherId.value)
    homeworkList.value = res.data || []
    
    // 为每个作业加载统计信息
    for (const homework of homeworkList.value) {
      if (homework.status !== 'draft') {
        try {
          const statsRes = await getHomeworkStats(homework.id)
          homework.stats = statsRes.data
        } catch (statsError) {
          console.error('加载统计失败:', statsError)
        }
      }
    }
  } catch (error) {
    console.error('加载作业列表失败:', error)
    console.error('错误详情:', error.response?.data || error.message)
    ElMessage.error('加载作业列表失败: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

// 加载题库
const loadQuestionBank = async () => {
  if (!homeworkForm.value.courseId) {
    ElMessage.warning('请先选择课程')
    return
  }

  try {
    const res = await getQuestionBankByCourse(homeworkForm.value.courseId)
    questionBank.value = (res.data || []).filter(q => {
      if (questionFilters.value.type && q.questionType !== questionFilters.value.type) return false
      if (questionFilters.value.difficulty && q.difficulty !== questionFilters.value.difficulty) return false
      return true
    })
  } catch (error) {
    ElMessage.error('加载题库失败')
  }
}

// 显示创建对话框
const showCreateDialog = () => {
  isEdit.value = false
  homeworkForm.value = {
    id: null,
    courseId: null,
    title: '',
    description: '',
    deadline: null,
    allowLateSubmission: false,
    latePenalty: 0,
    showAnswer: false
  }
  selectedQuestions.value = []
  dialogVisible.value = true
}

// 编辑作业
const editHomework = async (homework) => {
  isEdit.value = true
  
  try {
    const res = await getHomeworkDetail(homework.id)
    const detail = res.data
    
    homeworkForm.value = {
      ...detail.homework,
      deadline: detail.homework.deadline ? new Date(detail.homework.deadline) : null
    }
    
    // 后端返回的是扁平化数据，直接使用 q 对象
    selectedQuestions.value = detail.questions.map(q => ({
      id: q.id,
      content: q.content,
      questionType: q.questionType,
      options: q.options,
      answer: q.answer,
      difficulty: q.difficulty,
      score: q.score,
      questionScore: q.score // 作业中的分值
    }))
    
    dialogVisible.value = true
  } catch (error) {
    console.error('加载作业详情失败:', error)
    ElMessage.error('加载作业详情失败')
  }
}

// 查看详情
const viewDetail = async (homework) => {
  try {
    const res = await getHomeworkDetail(homework.id)
    if (res.code === 200) {
      currentHomeworkDetail.value = res.data
      detailDialogVisible.value = true
    } else {
      ElMessage.error(res.message || '加载作业详情失败')
    }
  } catch (error) {
    console.error('加载作业详情失败:', error)
    ElMessage.error('加载作业详情失败')
  }
}

// 发布作业
const publishHomework = async (homework) => {
  try {
    await ElMessageBox.confirm('确定要发布此作业吗？发布后学生即可查看。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await publishHomeworkApi(homework.id, teacherId.value)
    ElMessage.success('作业发布成功')
    loadHomeworkList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发布失败')
    }
  }
}

// 删除作业
const deleteHomework = async (homework) => {
  try {
    await ElMessageBox.confirm('确定要删除此作业吗？此操作不可恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteHomeworkApi(homework.id, teacherId.value)
    ElMessage.success('删除成功')
    loadHomeworkList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 提交创建/编辑
const handleSubmit = async () => {
  if (!homeworkForm.value.courseId || !homeworkForm.value.title || selectedQuestions.value.length === 0) {
    ElMessage.warning('请完整填写表单并至少选择一道题目')
    return
  }

  const homework = {
    ...homeworkForm.value,
    deadline: homeworkForm.value.deadline ? homeworkForm.value.deadline.toISOString().slice(0, 19) : null
  }

  const questions = selectedQuestions.value.map((q, index) => ({
    questionId: q.id,
    questionScore: q.questionScore || q.score || 10,
    questionOrder: index + 1
  }))

  try {
    if (isEdit.value) {
      await updateHomework(homework, questions, teacherId.value)
      ElMessage.success('作业更新成功')
    } else {
      await createHomework(homework, questions, teacherId.value)
      ElMessage.success('作业创建成功')
    }
    
    dialogVisible.value = false
    loadHomeworkList()
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  }
}

// 显示题目选择器
const showQuestionSelector = async () => {
  if (!homeworkForm.value.courseId) {
    ElMessage.warning('请先选择课程')
    return
  }
  
  questionFilters.value = { type: '', difficulty: '' }
  await loadQuestionBank()
  questionSelectorVisible.value = true
}

// 处理题目选择
const handleQuestionSelection = (selection) => {
  tempSelectedQuestions.value = selection
}

// 确认题目选择
const confirmQuestionSelection = () => {
  const newQuestions = tempSelectedQuestions.value.filter(q => 
    !selectedQuestions.value.some(sq => sq.id === q.id)
  ).map(q => ({
    ...q,
    questionScore: q.score || 10
  }))
  
  selectedQuestions.value.push(...newQuestions)
  questionSelectorVisible.value = false
  ElMessage.success(`已添加 ${newQuestions.length} 道题目`)
}

const appendQuestionToSelection = (question) => {
  if (!question) {
    return
  }
  if (homeworkForm.value.courseId && question.courseId !== homeworkForm.value.courseId) {
    return
  }
  const exists = selectedQuestions.value.some(q => q.id === question.id)
  if (!exists) {
    selectedQuestions.value.push({
      id: question.id,
      content: question.content,
      questionType: question.questionType,
      options: question.options,
      answer: question.answer,
      difficulty: question.difficulty,
      score: question.score,
      questionScore: question.score || 10
    })
  }
}

// 处理题目创建成功
const handleQuestionCreated = async (question) => {
  appendQuestionToSelection(question)
  if (question) {
    ElMessage.success('新题目已加入本次作业')
  }
  await loadQuestionBank()
}

// 移除题目
const removeQuestion = (index) => {
  selectedQuestions.value.splice(index, 1)
}

// 题型名称映射
const getQuestionTypeName = (type) => {
  const typeMap = {
    single_choice: '单选题',
    multiple_choice: '多选题',
    true_false: '判断题',
    fill_blank: '填空题',
    short_answer: '简答题',
    programming: '编程题'
  }
  return typeMap[type] || type
}

const SUBJECTIVE_TYPES = ['short_answer', 'programming']

const formatOptions = (options) => formatOptionsText(options)

const showAiFeedback = (answer) => {
  if (!answer?.question || !SUBJECTIVE_TYPES.includes(answer.question.questionType)) {
    return false
  }
  const hasScore = answer.aiScore !== null && answer.aiScore !== undefined
  const hasFeedback = !!answer.aiFeedback
  return hasScore || hasFeedback
}

// 查看统计
const viewStats = async (homework) => {
  try {
    const res = await getHomeworkStats(homework.id)
    const stats = res.data
    const courseTotal = getCourseStudentTotal(homework.courseId)
    const coverage = courseTotal > 0 ? ((stats.submitted / courseTotal) * 100).toFixed(1) : '0.0'
    ElMessageBox.alert(
      `课程学生数: ${courseTotal}\n已提交: ${stats.submitted}\n覆盖率: ${coverage}%\n待批改: ${stats.ungraded}\n已生成作业记录: ${stats.total}`,
      '作业统计',
      { confirmButtonText: '确定' }
    )
  } catch (error) {
    ElMessage.error('加载统计失败')
  }
}

// 查看提交列表
const viewSubmissions = async (homework, tab = 'pending') => {
  try {
    const res = await getHomeworkSubmissions(homework.id)
    submissions.value = (res.data || []).map(withNormalizedStatus)
    submissionsDialogVisible.value = true
    submissionTab.value = tab
  } catch (error) {
    ElMessage.error('加载提交列表失败')
  }
}

// 批改作业
const gradeSubmission = (submission) => {
  currentSubmission.value = {
    ...submission,
    answers: (submission.answers || []).map(answer => ({
      ...answer,
      score: answer.score || 0,
      teacherComment: answer.teacherComment || ''
    }))
  }
  gradeForm.value.teacherComment = submission.teacherComment || ''
  gradeDialogVisible.value = true
}

// 计算总分
const calculateTotalScore = () => {
  return currentSubmission.value.answers?.reduce((sum, a) => sum + (parseFloat(a.score) || 0), 0) || 0
}

// 提交批改
const handleGrade = async () => {
  const grades = currentSubmission.value.answers.map(answer => ({
    answerId: answer.id,
    score: answer.score,
    comment: answer.teacherComment
  }))

  try {
    await gradeHomeworkApi(
      currentSubmission.value.studentHomework.id,
      grades,
      gradeForm.value.teacherComment
    )
    
    ElMessage.success('批改成功')
    gradeDialogVisible.value = false
    viewSubmissions({ id: currentSubmission.value.studentHomework.homeworkId }, submissionTab.value)
  } catch (error) {
    ElMessage.error('批改失败')
  }
}

// 根据分数返回对应的标签颜色
const getScoreType = (score) => {
  if (score === null || score === undefined) return 'info'
  const percentage = (score / 100) * 100 // 假设满分100，可根据实际情况调整
  if (percentage >= 90) return 'success'
  if (percentage >= 75) return ''
  if (percentage >= 60) return 'warning'
  return 'danger'
}

onMounted(() => {
  loadMyCourses()
  loadHomeworkList()
})
</script>

<style scoped>
.homework-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 提交列表弹框样式 */
.submission-dialog :deep(.el-dialog__body) {
  min-height: 600px;
  max-height: 800px;
  overflow-y: auto;
}

/* 统计卡片样式 */
.submission-summary {
  margin-bottom: 20px;
  padding: 0;
}

.stat-card {
  text-align: center;
  padding: 20px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.stat-card:hover {
  background: rgba(255, 255, 255, 0.08);
  transform: translateY(-2px);
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #a9b1d6;
}

/* Tab标签样式 */
.submission-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 500;
}

.submission-tabs :deep(.el-tabs__item.is-active) {
  color: #7aa2f7 !important;
  font-weight: 600;
}

.tab-label {
  font-size: 15px;
}

.tab-count {
  color: #7aa2f7;
  font-weight: 600;
  margin-left: 4px;
}

.ai-feedback-block {
  margin-top: 12px;
  padding: 12px;
  border: 1px dashed rgba(255, 198, 93, 0.4);
  border-radius: 6px;
  background: transparent;
  color: var(--text-primary, #333);
}

.ai-feedback-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.ai-feedback-text {
  margin: 0 0 6px;
  line-height: 1.6;
  color: var(--text-primary, #333);
}

.ai-list {
  display: flex;
  gap: 6px;
  margin-top: 4px;
  color: var(--text-secondary, #666);
}
</style>
