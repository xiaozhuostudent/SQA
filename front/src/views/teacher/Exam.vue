<template>
  <div class="exam-manage-page">
    <el-card shadow="hover" class="header-card">
      <template #header>
        <div class="card-header">
          <h2 class="page-title">考试管理</h2>
          <el-button type="primary" @click="showCreateDialog" :icon="Plus">创建试卷</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 试卷列表 -->
        <el-tab-pane label="试卷列表" name="papers">
          <div class="toolbar">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索试卷名称或课程"
              prefix-icon="Search"
              clearable
              style="width: 300px"
            />
          </div>

          <el-table 
            :data="filteredPapers" 
            stripe 
            v-loading="loading" 
            element-loading-text="加载试卷数据中..."
            style="width: 100%; margin-top: 20px">
            <el-table-column prop="title" label="试卷名称" min-width="200" />
            <el-table-column prop="courseName" label="课程" width="150" />
            <el-table-column prop="duration" label="时长(分钟)" width="120" align="center" />
            <el-table-column prop="totalScore" label="总分" width="100" align="center" />
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="startTime" label="开始时间" width="180" />
            <el-table-column prop="endTime" label="结束时间" width="180" />
            <el-table-column label="操作" width="320" fixed="right" align="center">
              <template #default="{ row }">
                <el-button-group>
                  <el-button size="small" @click="viewPaper(row)" :icon="View">查看</el-button>
                  <el-button size="small" type="primary" @click="editPaper(row)" :icon="Edit">编辑</el-button>
                  <el-button 
                    v-if="row.status === 'draft'" 
                    size="small" 
                    type="success" 
                    @click="publishPaper(row)" 
                    :icon="Checked">
                    发布
                  </el-button>
                  <el-button size="small" type="danger" @click="deletePaper(row)" :icon="Delete">删除</el-button>
                </el-button-group>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 学生答卷 -->
        <el-tab-pane label="学生答卷" name="submissions">
          <div class="toolbar">
            <el-select 
              v-model="selectedPaperId" 
              placeholder="筛选试卷（默认显示全部）" 
              clearable 
              style="width: 300px"
              @change="loadSubmissions">
              <el-option 
                v-for="paper in examPapers" 
                :key="paper.id" 
                :label="paper.title" 
                :value="paper.id" />
            </el-select>
            <div style="flex: 1"></div>
            <span style="color: var(--text-secondary); font-size: 14px;">
              共 {{ submissionTotal }} 条答卷记录
            </span>
          </div>

          <el-table 
            :data="submissions" 
            stripe 
            v-loading="submissionsLoading" 
            element-loading-text="加载答卷数据中..."
            style="width: 100%; margin-top: 20px">
            <el-table-column prop="paperTitle" label="试卷名称" width="200" show-overflow-tooltip />
            <el-table-column prop="studentName" label="学生姓名" width="120" />
            <el-table-column prop="studentNumber" label="学号" width="150" />
            <el-table-column prop="startTime" label="开始时间" width="180" sortable />
            <el-table-column prop="submitTime" label="提交时间" width="180" sortable />
            <el-table-column label="客观题得分" width="120" align="center">
              <template #default="{ row }">
                <span class="score-text">{{ row.objectiveScore || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="主观题得分" width="120" align="center">
              <template #default="{ row }">
                <span class="score-text">{{ row.subjectiveScore || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="总分" width="100" align="center">
              <template #default="{ row }">
                <span class="score-text primary">{{ row.totalScore || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getSubmissionStatusType(row.status)">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right" align="center">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="viewAnswers(row)" :icon="View">
                  查看答卷
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="submissionPage"
            v-model:page-size="submissionPageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="submissionTotal"
            layout="total, sizes, prev, pager, next, jumper"
            style="margin-top: 20px; justify-content: flex-end"
            @size-change="loadSubmissions"
            @current-change="loadSubmissions"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 创建/编辑试卷对话框 -->
    <el-dialog 
      v-model="createDialogVisible" 
      :title="isEdit ? '编辑试卷' : '创建试卷'" 
      width="700px"
      :close-on-click-modal="false">
      <el-form :model="paperForm" :rules="paperRules" ref="paperFormRef" label-width="120px">
        <el-form-item label="试卷名称" prop="title">
          <el-input v-model="paperForm.title" placeholder="请输入试卷名称" />
        </el-form-item>
        <el-form-item label="所属课程" prop="courseId">
          <el-select v-model="paperForm.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option 
              v-for="course in courses" 
              :key="course.id" 
              :label="course.name" 
              :value="course.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试时长" prop="duration">
          <el-input-number v-model="paperForm.duration" :min="30" :max="300" :step="10" />
          <span style="margin-left: 10px">分钟</span>
        </el-form-item>
        <el-form-item label="总分" prop="totalScore">
          <el-input-number v-model="paperForm.totalScore" :min="1" :max="200" />
        </el-form-item>
        <el-form-item label="及格分" prop="passScore">
          <el-input-number
            v-model="paperForm.passScore"
            :min="0"
            :max="paperForm.totalScore || 200"
          />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="paperForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="paperForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="考试说明">
          <el-input 
            v-model="paperForm.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入考试说明" />
        </el-form-item>
        <el-form-item label="人脸识别">
          <el-switch 
            v-model="paperForm.faceRecognitionEnabled" 
            active-text="启用" 
            inactive-text="禁用" />
          <span style="margin-left: 10px; color: var(--text-secondary); font-size: 13px;">
            开启后学生需要进行人脸验证才能参加考试
          </span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePaper" :loading="saveLoading">保存</el-button>
      </template>
    </el-dialog>

    <!-- 查看/编辑试卷题目对话框 -->
    <el-dialog 
      v-model="viewPaperDialogVisible" 
      :title="currentPaper?.title" 
      width="90%"
      :close-on-click-modal="false">
      <div v-if="currentPaper">
        <div class="paper-info-header">
          <div class="paper-info-content">
            <span class="paper-info-item">
              <el-icon><Reading /></el-icon>
              课程: {{ currentPaper.courseName }}
            </span>
            <span class="paper-info-item">
              <el-icon><Clock /></el-icon>
              时长: {{ currentPaper.duration }}分钟
            </span>
            <span class="paper-info-item">
              <el-icon><Trophy /></el-icon>
              总分: {{ currentPaper.totalScore }}分
            </span>
          </div>
          <el-button type="primary" size="small" @click="showAddQuestionDialog" :icon="Plus">
            添加题目
          </el-button>
        </div>

        <el-table :data="paperQuestions" stripe>
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column label="题型" width="100">
            <template #default="{ row }">
              <el-tag size="small">{{ getQuestionTypeName(row.questionType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="题目内容" min-width="300" show-overflow-tooltip />
          <el-table-column prop="score" label="分值" width="80" align="center" />
          <el-table-column label="操作" width="120" fixed="right" align="center">
            <template #default="{ row }">
              <el-button size="small" type="danger" @click="removeQuestion(row)" :icon="Delete">
                移除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 添加题目对话框 -->
    <el-dialog 
      v-model="addQuestionDialogVisible" 
      title="添加题目" 
      width="90%"
      :close-on-click-modal="false">
      <div v-if="currentPaper" class="custom-alert">
        <el-icon class="alert-icon"><InfoFilled /></el-icon>
        <div class="alert-content">
          当前显示《{{ currentPaper.courseName }}》课程的题目和通用题库
        </div>
      </div>
      <div class="toolbar">
        <el-select v-model="questionTypeFilter" placeholder="题型筛选" clearable style="width: 150px">
          <el-option label="单选题" value="single_choice" />
          <el-option label="多选题" value="multiple_choice" />
          <el-option label="判断题" value="true_false" />
          <el-option label="填空题" value="fill_blank" />
          <el-option label="简答题" value="short_answer" />
          <el-option label="编程题" value="programming" />
        </el-select>
        <el-button type="primary" @click="loadQuestionBank" :icon="Refresh">刷新题库</el-button>
      </div>

      <el-table 
        :data="filteredQuestionBank" 
        stripe 
        max-height="500" 
        style="margin-top: 20px"
        @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column label="题型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ getQuestionTypeName(row.questionType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="题目内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="score" label="分值" width="80" align="center" />
        <el-table-column label="难度" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getDifficultyType(row.difficulty)" size="small">
              {{ row.difficulty }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="addQuestionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddQuestions" :loading="addingQuestions">
          添加选中题目
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看答卷对话框 -->
    <el-dialog 
      v-model="viewAnswersDialogVisible" 
      title="查看答卷" 
      width="90%"
      :close-on-click-modal="false">
      <div v-if="currentSubmission">
        <el-descriptions :column="2" border style="margin-bottom: 20px">
          <el-descriptions-item label="学生姓名">{{ currentSubmission.studentName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentSubmission.studentNumber }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ currentSubmission.startTime }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ currentSubmission.submitTime }}</el-descriptions-item>
          <el-descriptions-item label="客观题得分">{{ currentSubmission.objectiveScore || 0 }}</el-descriptions-item>
          <el-descriptions-item label="主观题得分">{{ currentSubmission.subjectiveScore || 0 }}</el-descriptions-item>
          <el-descriptions-item label="总分" :span="2">
            <span style="font-size: 18px; font-weight: bold; color: var(--accent-blue);">
              {{ currentSubmission.totalScore || 0 }}
            </span>
          </el-descriptions-item>
        </el-descriptions>

        <div v-for="(answer, index) in currentAnswers" :key="answer.id" class="answer-item">
          <div class="answer-header">
            <span class="answer-number">第{{ index + 1 }}题</span>
            <el-tag :type="getQuestionTypeTag(answer.questionType)">
              {{ getQuestionTypeName(answer.questionType) }}
            </el-tag>
            <span class="answer-score">{{ answer.score }}分</span>
          </div>
          <div class="answer-content">
            <p><strong>题目:</strong> {{ answer.content }}</p>
            <p><strong>学生答案:</strong> {{ answer.studentAnswer || '未作答' }}</p>
            <p v-if="answer.correctAnswer"><strong>正确答案:</strong> {{ answer.correctAnswer }}</p>
            <p><strong>得分:</strong> {{ answer.scoreObtained || 0 }}</p>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, View, Edit, Delete, Checked, Refresh, Reading, Clock, Trophy, InfoFilled } from '@element-plus/icons-vue'
import * as examApi from '@/api/exam'
import * as courseApi from '@/api/course'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 状态变量
const activeTab = ref('papers')
const loading = ref(false)
const submissionsLoading = ref(false)
const saveLoading = ref(false)
const addingQuestions = ref(false)

// 试卷相关
const searchKeyword = ref('')
const examPapers = ref([])
const validatePassScore = (rule, value, callback) => {
  if (value === null || value === undefined) {
    return callback(new Error('请输入及格分'))
  }
  if (value < 0) {
    return callback(new Error('及格分不能小于 0'))
  }
  if (value > paperForm.value.totalScore) {
    return callback(new Error('及格分不能大于总分'))
  }
  return callback()
}

const validateStartTime = (rule, value, callback) => {
  if (!value) {
    return callback(new Error('请选择开始时间'))
  }
  
  // 获取今天的开始时间（0点）
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  
  const selectedTime = new Date(value)
  
  // 编辑模式下允许修改过去的试卷
  if (!isEdit.value && selectedTime < today) {
    return callback(new Error('开始时间不能早于今天'))
  }
  
  return callback()
}

const validateEndTime = (rule, value, callback) => {
  if (!value) {
    return callback(new Error('请选择结束时间'))
  }
  
  if (!paperForm.value.startTime) {
    return callback(new Error('请先选择开始时间'))
  }
  
  const startTime = new Date(paperForm.value.startTime)
  const endTime = new Date(value)
  
  if (endTime <= startTime) {
    return callback(new Error('结束时间必须晚于开始时间'))
  }
  
  return callback()
}

const paperForm = ref({
  title: '',
  courseId: null,
  duration: 120,
  totalScore: 100,
  passScore: 60,
  startTime: '',
  endTime: '',
  description: '',
  status: 'draft'
})
const paperRules = {
  title: [{ required: true, message: '请输入试卷名称', trigger: 'blur' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  duration: [{ required: true, message: '请输入考试时长', trigger: 'blur' }],
  totalScore: [{ required: true, message: '请输入总分', trigger: 'blur' }],
  passScore: [{ validator: validatePassScore, trigger: 'blur' }],
  startTime: [{ validator: validateStartTime, trigger: 'change' }],
  endTime: [{ validator: validateEndTime, trigger: 'change' }]
}
const createDialogVisible = ref(false)
const viewPaperDialogVisible = ref(false)
const addQuestionDialogVisible = ref(false)
const isEdit = ref(false)
const currentPaper = ref(null)
const paperFormRef = ref(null)
const courses = ref([])

// 题目相关
const paperQuestions = ref([])
const questionBank = ref([])
const questionTypeFilter = ref('')
const selectedQuestions = ref([])

// 答卷相关
const selectedPaperId = ref(null)
const submissions = ref([])
const submissionPage = ref(1)
const submissionPageSize = ref(10)
const submissionTotal = ref(0)
const viewAnswersDialogVisible = ref(false)
const currentSubmission = ref(null)
const currentAnswers = ref([])

// 计算属性
const filteredPapers = computed(() => {
  if (!searchKeyword.value) return examPapers.value
  const keyword = searchKeyword.value.toLowerCase()
  return examPapers.value.filter(paper => 
    paper.title.toLowerCase().includes(keyword) || 
    (paper.courseName && paper.courseName.toLowerCase().includes(keyword))
  )
})

const filteredQuestionBank = computed(() => {
  if (!questionTypeFilter.value) return questionBank.value
  return questionBank.value.filter(q => q.questionType === questionTypeFilter.value)
})

// 生命周期
onMounted(() => {
  loadExamPapers()
  loadCourses()
})

// 方法
const loadExamPapers = async () => {
  loading.value = true
  try {
    const teacherId = userStore.userInfo.id || 1
    // 使用新的API根据创建者ID获取试卷列表
    const res = await examApi.getExamPapersByCreator(teacherId)
    examPapers.value = res.data || []
    
    // 如果当前在学生答卷标签页，刷新答卷数据
    if (activeTab.value === 'submissions') {
      loadSubmissions()
    }
  } catch (error) {
    console.error('加载试卷失败:', error)
    ElMessage.error('加载试卷失败')
  } finally {
    loading.value = false
  }
}

const loadCourses = async () => {
  try {
    const teacherId = userStore.userInfo.id || 1
    const res = await courseApi.getTeacherCourses(teacherId)
    courses.value = res.data || []
  } catch (error) {
    console.error('加载课程失败:', error)
  }
}

const loadSubmissions = async () => {
  submissionsLoading.value = true
  try {
    // 如果没有选择试卷，则加载所有试卷的答卷记录
    if (!selectedPaperId.value) {
      // 获取教师所有试卷的ID
      const paperIds = examPapers.value.map(p => p.id)
      if (paperIds.length === 0) {
        submissions.value = []
        submissionTotal.value = 0
        return
      }
      
      // 加载所有试卷的答卷
      const allSubmissions = []
      for (const paperId of paperIds) {
        try {
          const res = await examApi.getSubmissions(paperId)
          const paperSubmissions = res.data || []
          // 为每条答卷记录添加试卷名称
          const paper = examPapers.value.find(p => p.id === paperId)
          paperSubmissions.forEach(sub => {
            sub.paperTitle = paper?.title || ''
            sub.paperId = paperId
          })
          allSubmissions.push(...paperSubmissions)
        } catch (error) {
          console.error(`加载试卷${paperId}的答卷失败:`, error)
        }
      }
      
      // 按提交时间降序排序
      allSubmissions.sort((a, b) => {
        const timeA = new Date(a.submitTime || a.startTime).getTime()
        const timeB = new Date(b.submitTime || b.startTime).getTime()
        return timeB - timeA
      })
      
      // 分页处理
      submissionTotal.value = allSubmissions.length
      const start = (submissionPage.value - 1) * submissionPageSize.value
      const end = start + submissionPageSize.value
      submissions.value = allSubmissions.slice(start, end)
    } else {
      // 选中了具体试卷，只加载该试卷的答卷
      const res = await examApi.getSubmissions(selectedPaperId.value)
      const allSubmissions = res.data || []
      
      // 添加试卷名称
      const paper = examPapers.value.find(p => p.id === selectedPaperId.value)
      allSubmissions.forEach(sub => {
        sub.paperTitle = paper?.title || ''
        sub.paperId = selectedPaperId.value
      })
      
      // 按提交时间降序排序
      allSubmissions.sort((a, b) => {
        const timeA = new Date(a.submitTime || a.startTime).getTime()
        const timeB = new Date(b.submitTime || b.startTime).getTime()
        return timeB - timeA
      })
      
      // 分页处理
      submissionTotal.value = allSubmissions.length
      const start = (submissionPage.value - 1) * submissionPageSize.value
      const end = start + submissionPageSize.value
      submissions.value = allSubmissions.slice(start, end)
    }
  } catch (error) {
    console.error('加载答卷失败:', error)
    ElMessage.error('加载答卷失败')
  } finally {
    submissionsLoading.value = false
  }
}

const loadQuestionBank = async () => {
  try {
    const res = await examApi.getAllQuestions()
    const data = res.data
    const allQuestions = Array.isArray(data) ? data : (data.list || [])
    
    // 根据当前试卷的课程ID过滤题目
    if (currentPaper.value && currentPaper.value.courseId) {
      // 只显示当前课程的题目或通用题库(courseId为null)
      questionBank.value = allQuestions.filter(q => 
        q.courseId === currentPaper.value.courseId || !q.courseId
      )
      
      if (questionBank.value.length === 0) {
        ElMessage.warning(`当前课程《${currentPaper.value.courseName}》还没有题目，请先创建题目`)
      }
    } else {
      questionBank.value = allQuestions
    }
  } catch (error) {
    console.error('加载题库失败:', error)
    ElMessage.error('加载题库失败')
  }
}

const handleTabChange = (tab) => {
  if (tab === 'submissions') {
    // 重置分页
    submissionPage.value = 1
    // 加载答卷数据（默认显示全部）
    loadSubmissions()
  }
}

const showCreateDialog = () => {
  if (courses.value.length === 0) {
    ElMessage.warning('请先创建课程')
    return
  }
  isEdit.value = false
  paperForm.value = {
    title: '',
    courseId: null,
    duration: 120,
    totalScore: 100,
    passScore: 60,
    startTime: '',
    endTime: '',
    description: '',
    status: 'draft',
    faceRecognitionEnabled: false
  }
  createDialogVisible.value = true
}
const editPaper = (paper) => {
  isEdit.value = true
  paperForm.value = { 
    ...paper, 
    passScore: paper.passScore ?? Math.floor((paper.totalScore || 100) * 0.6),
    faceRecognitionEnabled: paper.faceRecognitionEnabled ?? false
  }
  createDialogVisible.value = true
}

const savePaper = async () => {
  if (!paperFormRef.value) return
  
  await paperFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    saveLoading.value = true
    try {
      // 补充课程名称和创建者信息
      const course = courses.value.find(c => c.id === paperForm.value.courseId)
      if (course) {
        paperForm.value.courseName = course.name
      }
      paperForm.value.creatorId = userStore.userInfo.id
      paperForm.value.creatorName = userStore.userInfo.realName || userStore.userInfo.username
      
      console.log('准备保存的试卷数据:', JSON.stringify(paperForm.value, null, 2))
      
      if (isEdit.value) {
        console.log('调用更新API, ID:', paperForm.value.id)
        await examApi.updateExamPaper(paperForm.value.id, paperForm.value)
        ElMessage.success('更新试卷成功')
      } else {
        await examApi.createExamPaper(paperForm.value)
        ElMessage.success('创建试卷成功')
      }
      createDialogVisible.value = false
      loadExamPapers()
    } catch (error) {
      console.error('保存试卷失败:', error)
      console.error('错误详情:', error.response?.data)
      ElMessage.error(isEdit.value ? '更新试卷失败' : '创建试卷失败')
    } finally {
      saveLoading.value = false
    }
  })
}


const viewPaper = async (paper) => {
  currentPaper.value = paper
  viewPaperDialogVisible.value = true
  try {
    const res = await examApi.getExamQuestions(paper.id)
    paperQuestions.value = res.data || []
  } catch (error) {
    console.error('加载试卷题目失败:', error)
    ElMessage.error('加载试卷题目失败')
  }
}

const publishPaper = async (paper) => {
  try {
    await ElMessageBox.confirm(`确定要发布试卷《${paper.title}》吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    console.log('开始发布试卷:', paper.id)
    const res = await examApi.publishExamPaper(paper.id)
    console.log('发布结果:', res)
    ElMessage.success('发布成功')
    loadExamPapers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布试卷失败:', error)
      console.error('错误详情:', error.response?.data)
      const errorMsg = error.response?.data?.message || error.message || '发布失败'
      ElMessage.error('发布试卷失败: ' + errorMsg)
    }
  }
}

const deletePaper = async (paper) => {
  try {
    await ElMessageBox.confirm(`确定要删除试卷《${paper.title}》吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await examApi.deleteExamPaper(paper.id)
    ElMessage.success('删除成功')
    loadExamPapers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除试卷失败:', error)
      ElMessage.error('删除试卷失败')
    }
  }
}

const showAddQuestionDialog = () => {
  addQuestionDialogVisible.value = true
  loadQuestionBank()
}

const handleSelectionChange = (selection) => {
  selectedQuestions.value = selection
}

const confirmAddQuestions = async () => {
  if (selectedQuestions.value.length === 0) {
    ElMessage.warning('请选择要添加的题目')
    return
  }
  
  addingQuestions.value = true
  try {
    const questionIds = selectedQuestions.value.map(q => q.id)
    await examApi.addQuestionsToExam(currentPaper.value.id, questionIds)
    ElMessage.success('添加题目成功')
    addQuestionDialogVisible.value = false
    viewPaper(currentPaper.value)
  } catch (error) {
    console.error('添加题目失败:', error)
    ElMessage.error('添加题目失败')
  } finally {
    addingQuestions.value = false
  }
}

const removeQuestion = async (question) => {
  try {
    await ElMessageBox.confirm('确定要移除这道题目吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await examApi.removeQuestionFromExam(currentPaper.value.id, question.id)
    ElMessage.success('移除成功')
    viewPaper(currentPaper.value)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除题目失败:', error)
      ElMessage.error('移除题目失败')
    }
  }
}

const viewAnswers = async (submission) => {
  currentSubmission.value = submission
  viewAnswersDialogVisible.value = true
  try {
    const res = await examApi.getAnswers(submission.id)
    currentAnswers.value = res.data || []
  } catch (error) {
    console.error('加载答卷详情失败:', error)
    ElMessage.error('加载答卷详情失败')
  }
}

// 辅助方法
const getStatusType = (status) => {
  const types = {
    'draft': 'info',
    'published': 'success',
    'ended': 'warning'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    'draft': '草稿',
    'published': '已发布',
    'ended': '已结束'
  }
  return texts[status] || status
}

const getSubmissionStatusType = (status) => {
  const types = {
    'submitted': 'success',
    'grading': 'warning',
    'graded': 'info'
  }
  return types[status] || 'info'
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

const getDifficultyType = (difficulty) => {
  const types = {
    '简单': 'success',
    '中等': 'warning',
    '困难': 'danger'
  }
  return types[difficulty] || ''
}
</script>

<style scoped>
.exam-manage-page {
  padding: 0;
}

.header-card {
  background: var(--bg-float);
  border: 1px solid var(--border);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  margin: 0;
  color: var(--text-primary);
  font-size: 20px;
  font-weight: 600;
}

.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
}

.score-text {
  font-weight: 600;
  color: var(--text-primary);
}

.score-text.primary {
  color: var(--accent-blue);
  font-size: 16px;
}

.answer-item {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.answer-header {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border);
}

.answer-number {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 15px;
}

.answer-score {
  margin-left: auto;
  color: var(--accent-blue);
  font-weight: 600;
}

.answer-content p {
  margin: 8px 0;
  line-height: 1.6;
  color: var(--text-primary);
}

.answer-content strong {
  color: var(--text-secondary);
}

/* 试卷信息头部 */
.paper-info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  margin-bottom: 20px;
  background: linear-gradient(135deg, rgba(122, 162, 247, 0.1) 0%, rgba(125, 207, 255, 0.1) 100%);
  border: 1px solid rgba(125, 207, 255, 0.3);
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.paper-info-content {
  display: flex;
  gap: 32px;
  align-items: center;
}

.paper-info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-primary);
  font-size: 15px;
  font-weight: 500;
}

.paper-info-item .el-icon {
  color: #7dcfff;
  font-size: 18px;
}

.exam-manage-page :deep(.el-card) {
  background: var(--bg-float);
  border: 1px solid var(--border);
}

.exam-manage-page :deep(.el-table) {
  background: transparent;
  color: var(--text-primary);
}

.exam-manage-page :deep(.el-table th) {
  background: var(--bg-secondary);
  color: var(--text-primary);
  border-color: var(--border);
}

.exam-manage-page :deep(.el-table td) {
  border-color: var(--border);
}

.exam-manage-page :deep(.el-table tr:hover > td) {
  background: var(--bg-highlight) !important;
}

.exam-manage-page :deep(.el-dialog) {
  background: var(--bg-float);
  border: 1px solid var(--border);
}

.exam-manage-page :deep(.el-input__wrapper) {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  box-shadow: none;
}

.exam-manage-page :deep(.el-input__inner) {
  color: var(--text-primary);
}

.exam-manage-page :deep(.el-select .el-input__wrapper) {
  background: var(--bg-secondary);
}

.exam-manage-page :deep(.el-textarea__inner) {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  color: var(--text-primary);
}

.exam-manage-page :deep(.el-descriptions__label) {
  color: var(--text-secondary);
}

.exam-manage-page :deep(.el-descriptions__content) {
  color: var(--text-primary);
}

.exam-manage-page :deep(.el-pagination) {
  color: var(--text-primary);
}

.exam-manage-page :deep(.el-pagination button) {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.exam-manage-page :deep(.el-pagination .el-pager li) {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.exam-manage-page :deep(.el-pagination .el-pager li.is-active) {
  background: var(--accent-blue);
  color: #ffffff;
}

/* 自定义 Alert 样式 */
.custom-alert {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  margin-bottom: 16px;
  background: linear-gradient(135deg, rgba(122, 162, 247, 0.12) 0%, rgba(125, 207, 255, 0.12) 100%);
  border: 1px solid rgba(125, 207, 255, 0.35);
  border-radius: 8px;
  backdrop-filter: blur(8px);
}

.alert-icon {
  flex-shrink: 0;
  font-size: 20px;
  color: #7dcfff;
}

.alert-content {
  flex: 1;
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 500;
  line-height: 1.6;
}

/* 日期选择器主题色样式 */
.exam-manage-page :deep(.el-date-picker) {
  background: var(--bg-float);
  border: 1px solid var(--border);
}

.exam-manage-page :deep(.el-date-picker__header) {
  color: var(--text-primary);
}

.exam-manage-page :deep(.el-date-picker__header-label) {
  color: var(--text-primary);
}

.exam-manage-page :deep(.el-date-table th) {
  color: var(--text-secondary);
}

.exam-manage-page :deep(.el-date-table td) {
  color: var(--text-primary);
}

.exam-manage-page :deep(.el-date-table td.available:hover) {
  background: var(--bg-highlight);
  color: var(--accent-blue);
}

.exam-manage-page :deep(.el-date-table td.today .el-date-table-cell__text) {
  color: var(--accent-blue);
  font-weight: 700;
}

.exam-manage-page :deep(.el-date-table td.current:not(.disabled) .el-date-table-cell__text) {
  background: var(--accent-blue);
  color: #ffffff;
}

.exam-manage-page :deep(.el-date-table td.disabled) {
  color: var(--text-disabled);
  opacity: 0.4;
}

.exam-manage-page :deep(.el-time-panel) {
  background: var(--bg-float);
  border: 1px solid var(--border);
}

.exam-manage-page :deep(.el-time-panel__content) {
  color: var(--text-primary);
}

.exam-manage-page :deep(.el-time-spinner__item) {
  color: var(--text-primary);
}

.exam-manage-page :deep(.el-time-spinner__item:hover:not(.is-disabled):not(.is-active)) {
  background: var(--bg-highlight);
}

.exam-manage-page :deep(.el-time-spinner__item.is-active:not(.is-disabled)) {
  color: var(--accent-blue);
  font-weight: 700;
}

.exam-manage-page :deep(.el-picker-panel__icon-btn) {
  color: var(--text-primary);
}

.exam-manage-page :deep(.el-picker-panel__icon-btn:hover) {
  color: var(--accent-blue);
}

</style>
