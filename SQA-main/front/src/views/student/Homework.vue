<template>
  <div class="homework-page">
    <el-card>
      <template #header>
        <span>课程作业</span>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="待提交" name="pending">
          <el-table :data="pendingHomework" style="width: 100%" v-loading="loading" element-loading-text="加载作业数据中...">
            <el-table-column prop="title" label="作业标题" min-width="180" />
            <el-table-column prop="courseName" label="课程" width="150" />
            <el-table-column prop="totalScore" label="总分" width="80" />
            <el-table-column prop="deadline" label="截止时间" width="180" />
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button size="small" type="primary" @click="startHomework(scope.row)">
                  开始作业
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="已提交" name="submitted">
          <el-table :data="submittedHomework" style="width: 100%" v-loading="loading" element-loading-text="加载作业数据中...">
            <el-table-column prop="title" label="作业标题" min-width="180" />
            <el-table-column prop="courseName" label="课程" width="150" />
            <el-table-column prop="submitTime" label="提交时间" width="180" />
            <el-table-column prop="score" label="成绩" width="100">
              <template #default="scope">
                <span v-if="scope.row.score !== null && scope.row.score !== undefined">
                  <el-tag type="success">{{ scope.row.score }} / {{ scope.row.totalScore }}</el-tag>
                </span>
                <el-tag v-else type="info">待批改</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag v-if="scope.row.status === 'submitted'" type="warning">待批改</el-tag>
                <el-tag v-else-if="scope.row.status === 'graded'" type="success">已批改</el-tag>
                <el-tag v-else type="info">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button size="small" @click="viewSubmission(scope.row)">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 答题对话框 -->
    <el-dialog 
      v-model="answerDialogVisible" 
      :title="currentHomework.title" 
      width="80%"
      :close-on-click-modal="false"
    >
      <div v-if="currentHomework.homework">
        <el-alert 
          :title="`截止时间: ${currentHomework.homework.deadline}`" 
          type="warning" 
          :closable="false" 
          style="margin-bottom: 20px"
        />

        <!-- 题目列表 -->
        <div v-for="(question, index) in currentHomework.questions" :key="question.id" style="margin-bottom: 30px">
          <el-card>
            <template #header>
              <div style="display: flex; justify-content: space-between; align-items: center">
                <span>
                  第 {{ index + 1 }} 题
                  <el-tag size="small" style="margin-left: 10px">{{ getQuestionTypeName(question.questionType) }}</el-tag>
                </span>
                <el-tag type="primary">{{ question.score }} 分</el-tag>
              </div>
            </template>

            <div style="margin-bottom: 15px">
              <strong>题目:</strong> {{ question.content }}
            </div>

            <!-- 单选题 -->
            <div v-if="question.questionType === 'single_choice'" style="margin-left: 20px; margin-bottom: 15px;">
              <el-radio-group v-model="answers[question.id]">
                <el-radio 
                  v-for="(option, i) in (typeof question.options === 'string' ? JSON.parse(question.options) : question.options)" 
                  :key="i" 
                  :label="option.key || String.fromCharCode(65 + i)"
                  style="display: block; margin-bottom: 8px;">
                  {{ option.key || String.fromCharCode(65 + i) }}. {{ option.value || option }}
                </el-radio>
              </el-radio-group>
            </div>

            <!-- 多选题 -->
            <div v-if="question.questionType === 'multiple_choice'" style="margin-left: 20px; margin-bottom: 15px;">
              <el-checkbox-group v-model="answers[question.id]">
                <el-checkbox 
                  v-for="(option, i) in (typeof question.options === 'string' ? JSON.parse(question.options) : question.options)" 
                  :key="i" 
                  :label="option.key || String.fromCharCode(65 + i)"
                  style="display: block; margin-bottom: 8px;">
                  {{ option.key || String.fromCharCode(65 + i) }}. {{ option.value || option }}
                </el-checkbox>
              </el-checkbox-group>
            </div>

            <!-- 判断题 -->
            <div v-if="question.questionType === 'true_false'">
              <el-radio-group v-model="answers[question.id]">
                <el-radio label="true">正确</el-radio>
                <el-radio label="false">错误</el-radio>
              </el-radio-group>
            </div>

            <!-- 填空题 -->
            <div v-if="question.questionType === 'fill_blank'">
              <el-input 
                v-model="answers[question.id]" 
                placeholder="请输入答案"
                type="textarea"
                :rows="2"
              />
            </div>

            <!-- 简答题 -->
            <div v-if="question.questionType === 'short_answer'">
              <el-input 
                v-model="answers[question.id]" 
                placeholder="请输入答案"
                type="textarea"
                :rows="4"
              />
            </div>

            <!-- 编程题 -->
            <div v-if="question.questionType === 'programming'">
              <el-input 
                v-model="answers[question.id]" 
                placeholder="请输入代码"
                type="textarea"
                :rows="8"
              />
            </div>
          </el-card>
        </div>
      </div>

      <template #footer>
        <el-button @click="answerDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">提交作业</el-button>
      </template>
    </el-dialog>

    <!-- 查看提交详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="作业详情" 
      width="80%"
    >
      <div v-if="submissionDetail.homework">
        <el-descriptions :column="2" border style="margin-bottom: 20px">
          <el-descriptions-item label="作业标题">{{ submissionDetail.homework.title }}</el-descriptions-item>
          <el-descriptions-item label="课程">{{ submissionDetail.homework.courseName }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ submissionDetail.studentHomework.submitTime }}</el-descriptions-item>
          <el-descriptions-item label="得分">
            <span v-if="submissionDetail.studentHomework.score !== null">
              {{ submissionDetail.studentHomework.score }} / {{ submissionDetail.homework.totalScore }}
            </span>
            <el-tag v-else type="info">待批改</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="是否迟交">
            <el-tag v-if="submissionDetail.studentHomework.isLate" type="danger">是</el-tag>
            <el-tag v-else type="success">否</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="submissionDetail.studentHomework.status === 'graded'" type="success">已批改</el-tag>
            <el-tag v-else type="warning">待批改</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="教师评语" :span="2" v-if="submissionDetail.studentHomework.teacherComment">
            {{ submissionDetail.studentHomework.teacherComment }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 答题详情 -->
        <el-divider content-position="left">答题详情</el-divider>
        <div v-for="(answer, index) in submissionDetail.answers" :key="answer.id" style="margin-bottom: 20px">
          <el-card>
            <template #header>
              <div style="display: flex; justify-content: space-between; align-items: center">
                <span>第 {{ index + 1 }} 题</span>
                <div>
                  <el-tag v-if="answer.isCorrect !== null && answer.isCorrect" type="success">✓ 正确</el-tag>
                  <el-tag v-else-if="answer.isCorrect !== null && !answer.isCorrect" type="danger">✗ 错误</el-tag>
                  <el-tag v-if="answer.score !== null" type="primary" style="margin-left: 10px">
                    得分: {{ answer.score }} / {{ answer.question.score }}
                  </el-tag>
                </div>
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
              <strong>我的答案:</strong> 
              <span v-if="answer.studentAnswer">{{ answer.studentAnswer }}</span>
              <el-tag v-else type="info" size="small">未作答</el-tag>
            </div>

            <div style="margin-bottom: 10px;" v-if="answer.question.questionType !== 'programming' && answer.question.questionType !== 'short_answer'">
              <strong>标准答案:</strong> {{ answer.question.answer }}
            </div>

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

            <div v-if="answer.teacherComment" style="margin-top: 10px; padding: 10px; background: #f5f7fa; border-radius: 4px">
              <strong>教师点评:</strong> {{ answer.teacherComment }}
            </div>
          </el-card>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getStudentHomeworks, 
  getStudentSubmissions, 
  startHomework as startHomeworkApi,
  submitHomework as submitHomeworkApi,
  getHomeworkDetail 
} from '@/api/homework'
import { useUserStore } from '@/stores/user'
import { formatOptionsText } from '@/utils/question'

const userStore = useUserStore()
const studentId = computed(() => userStore.userInfo?.id)

const loading = ref(false)
const submitting = ref(false)
const activeTab = ref('pending')
const answerDialogVisible = ref(false)
const detailDialogVisible = ref(false)

const pendingHomework = ref([])
const submittedHomework = ref([])
const currentHomework = ref({})
const answers = reactive({})
const submissionDetail = ref({
  homework: null,
  studentHomework: null,
  answers: []
})

const formatOptions = (options) => formatOptionsText(options)

const SUBJECTIVE_TYPES = ['short_answer', 'programming']

const showAiFeedback = (answer) => {
  if (!answer?.question || !SUBJECTIVE_TYPES.includes(answer.question.questionType)) {
    return false
  }
  const hasScore = answer.aiScore !== null && answer.aiScore !== undefined
  const hasFeedback = !!answer.aiFeedback
  return hasScore || hasFeedback
}

// 题型映射
const getQuestionTypeName = (type) => {
  const typeMap = {
    'single_choice': '单选题',
    'multiple_choice': '多选题',
    'true_false': '判断题',
    'fill_blank': '填空题',
    'short_answer': '简答题',
    'programming': '编程题'
  }
  return typeMap[type] || type
}

// 加载作业列表
const loadHomework = async () => {
  if (!studentId.value) {
    ElMessage.error('请先登录')
    return
  }

  loading.value = true
  try {
    // 加载所有作业
    const allRes = await getStudentHomeworks(studentId.value)
    const allHomeworks = allRes.data || []
    
    // 加载已提交的作业
    const submittedRes = await getStudentSubmissions(studentId.value)
    const submittedIds = (submittedRes.data || []).map(item => item.homeworkId)
    
    // 分类：待提交和已提交
    pendingHomework.value = allHomeworks.filter(hw => 
      hw.status === 'published' && !submittedIds.includes(hw.id)
    )
    
    submittedHomework.value = submittedRes.data || []
  } catch (error) {
    console.error('加载作业失败:', error)
    ElMessage.error('加载作业失败')
  } finally {
    loading.value = false
  }
}

// 开始作业
const startHomework = async (homework) => {
  if (!studentId.value) {
    ElMessage.error('请先登录')
    return
  }

  try {
    // 调用开始作业接口
    const startRes = await startHomeworkApi(homework.id, studentId.value)

    // 获取作业详情（包含题目）
    const detailRes = await getHomeworkDetail(homework.id)
    if (detailRes.code === 200) {
      currentHomework.value = detailRes.data
      currentHomework.value.studentHomeworkId = startRes.data.id // 保存学生作业记录ID
      
      // 清空之前的答案
      Object.keys(answers).forEach(key => delete answers[key])
      
      // 初始化多选题答案为数组
      detailRes.data.questions?.forEach(q => {
        console.log('题目:', q.content, '选项:', q.options, '类型:', q.questionType)
        if (q.questionType === 'multiple_choice') {
          answers[q.id] = []
        }
      })
      
      // 跳转到新页面做作业
      window.open(`/student/homework/${homework.id}/answer`, '_blank')
    } else {
      ElMessage.error('加载作业详情失败')
    }
  } catch (error) {
    console.error('开始作业失败:', error)
    ElMessage.error('开始作业失败')
  }
}

// 提交作业
const handleSubmit = async () => {
  try {
    await ElMessageBox.confirm('确定要提交作业吗？提交后将不能修改。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    submitting.value = true

    // 构造答案数组
    const answerList = currentHomework.value.questions.map(q => {
      const answer = answers[q.id]
      return {
        questionId: q.id,
        studentAnswer: Array.isArray(answer) 
          ? answer.join(',') 
          : (answer ? String(answer) : '')
      }
    })

    console.log('提交的答案列表:', answerList)
    console.log('studentHomeworkId:', currentHomework.value.studentHomeworkId)

    // 响应拦截器已经处理了 code !== 200 的情况，这里直接使用返回值
    const res = await submitHomeworkApi(
      currentHomework.value.studentHomeworkId, 
      answerList
    )

    // 能到这里说明 code === 200，提交成功
    ElMessage.success(res.message || '作业提交成功')
    answerDialogVisible.value = false
    loadHomework()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交作业失败:', error)
      console.error('错误详情:', error.response?.data)
      ElMessage.error(error.response?.data?.message || error.message || '提交失败')
    }
  } finally {
    submitting.value = false
  }
}

// 查看提交详情
const viewSubmission = async (submission) => {
  try {
    loading.value = true
    console.log('查看提交详情，submission:', submission)
    
    // 获取作业信息
    const res = await getHomeworkDetail(submission.homeworkId)
    console.log('作业详情响应:', res)
    
    // 响应拦截器确保 code === 200，直接使用返回值
    submissionDetail.value = {
      homework: res.data.homework,
      studentHomework: {
        submitTime: submission.submitTime,
        score: submission.score,
        totalScore: submission.totalScore,
        isLate: submission.isLate,
        status: submission.status,
        teacherComment: submission.teacherComment
      },
      answers: submission.answers || [] // 使用submission中已有的answers
    }
    
    console.log('submissionDetail:', submissionDetail.value)
    console.log('answers数量:', submissionDetail.value.answers.length)
    
    detailDialogVisible.value = true
  } catch (error) {
    console.error('加载详情失败:', error)
    ElMessage.error(error.message || '加载详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadHomework()
})
</script>

<style scoped>
.homework-page {
  padding: 0;
}

.homework-page ::v-deep .el-card {
  background: var(--bg-float);
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
}

.homework-page ::v-deep .el-card__header {
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border);
  color: var(--text-primary);
}

.homework-page ::v-deep .el-tabs__header {
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border);
  margin: 0;
  padding: 0 20px;
}

.homework-page ::v-deep .el-tabs__nav {
  border: none;
}

.homework-page ::v-deep .el-tabs__item {
  color: var(--text-primary);
}

.homework-page ::v-deep .el-tabs__item.is-active {
  color: var(--accent-cyan);
}

.homework-page ::v-deep .el-tabs__active-bar {
  background: var(--accent-cyan);
}

.homework-page ::v-deep .el-table {
  background: transparent;
  color: var(--text-primary);
}

.homework-page ::v-deep .el-table th {
  background: var(--bg-secondary);
  color: var(--text-primary);
  border-color: var(--border);
}

.homework-page ::v-deep .el-table td {
  border-color: var(--border);
}

.homework-page ::v-deep .el-table tr:hover > td {
  background: var(--bg-highlight) !important;
}

.homework-page ::v-deep .el-dialog {
  background: var(--bg-float);
  border: 1px solid var(--border);
}

.homework-page ::v-deep .el-dialog__title {
  color: var(--text-primary);
}

.homework-page ::v-deep .el-form-item__label {
  color: var(--text-primary);
}

.homework-page ::v-deep .el-input__wrapper,
.homework-page ::v-deep .el-textarea__inner {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  color: var(--text-primary);
  box-shadow: none;
}

.ai-feedback-block {
  margin-top: 10px;
  padding: 10px;
  border: 1px dashed rgba(255, 198, 93, 0.4);
  border-radius: 6px;
  background: transparent;
  color: var(--text-primary);
}

.ai-feedback-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.ai-feedback-text {
  margin: 0 0 6px;
  line-height: 1.6;
  color: var(--text-primary);
}

.ai-list {
  display: flex;
  gap: 6px;
  margin-top: 2px;
  color: var(--text-secondary, #adb5bd);
}
</style>
