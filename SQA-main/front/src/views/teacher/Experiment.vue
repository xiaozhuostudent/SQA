<template>
  <div class="experiment-page">
    <el-card shadow="hover" class="header-card">
      <template #header>
        <div class="card-header">
          <h2 class="page-title">实验管理</h2>
          <el-button type="primary" @click="showCreateDialog">创建实验</el-button>
        </div>
      </template>

      <el-table :data="experimentList" style="width: 100%;" v-loading="loading" element-loading-text="加载实验数据中..." stripe>
        <el-table-column prop="title" label="实验名称" min-width="200" />
        <el-table-column prop="courseName" label="课程" width="150" />
        <el-table-column prop="deadline" label="截止时间" width="180" />
        <el-table-column label="提交情况" width="120" align="center">
          <template #default="scope">
            <span>{{ scope.row.submittedCount || 0 }} / {{ scope.row.totalStudents || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="360" align="center">
          <template #default="scope">
            <el-button-group>
              <el-button size="small" @click="viewReports(scope.row)">查看报告</el-button>
              <el-button size="small" type="success" @click="viewDetails(scope.row)">查看详情</el-button>
              <el-button size="small" type="primary" @click="editExperiment(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteExperiment(scope.row)">删除</el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑实验对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px">
      <el-form :model="experimentForm" label-width="120px">
        <el-form-item label="课程">
          <el-select v-model="experimentForm.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option
              v-for="course in myCourses"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="实验名称">
          <el-input v-model="experimentForm.title" placeholder="请输入实验名称" />
        </el-form-item>
        <el-form-item label="实验描述">
          <el-input
            v-model="experimentForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入实验描述"
          />
        </el-form-item>
        <el-form-item label="实验要求">
          <el-input
            v-model="experimentForm.requirements"
            type="textarea"
            :rows="4"
            placeholder="请输入详细要求"
          />
        </el-form-item>
        <el-form-item label="实验步骤">
          <el-input
            v-model="experimentForm.steps"
            type="textarea"
            :rows="5"
            placeholder="请输入实验步骤"
          />
        </el-form-item>
        <el-form-item label="起始时间">
          <el-date-picker
            v-model="experimentForm.startTime"
            type="datetime"
            placeholder="选择起始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker
            v-model="experimentForm.deadline"
            type="datetime"
            placeholder="选择截止时间"
            style="width: 100%"
          />
        </el-form-item>

        <!-- 题目配置 -->
        <div>
          <el-divider content-position="left">题目配置</el-divider>
          <el-button type="primary" size="small" @click="addProblem" style="margin-bottom: 15px">添加题目</el-button>
          
          <el-collapse>
            <el-collapse-item v-for="(problem, pIndex) in experimentForm.problems" :key="pIndex" :title="'题目 ' + (pIndex + 1) + ': ' + (problem.title || '未命名')">
              <el-form-item label="标题" label-width="80px">
                <el-input v-model="problem.title" placeholder="题目名称" />
              </el-form-item>
              <el-form-item label="描述" label-width="80px">
                <el-input v-model="problem.description" type="textarea" :rows="2" placeholder="题目描述" />
              </el-form-item>
              <el-row :gutter="10">
                <el-col :span="8">
                  <el-form-item label="难度" label-width="80px">
                    <el-select v-model="problem.difficulty">
                      <el-option label="简单" value="easy" />
                      <el-option label="中等" value="medium" />
                      <el-option label="困难" value="hard" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="分数" label-width="80px">
                    <el-input-number v-model="problem.score" :min="1" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-button type="danger" size="small" @click="removeProblem(pIndex)">删除题目</el-button>
                </el-col>
              </el-row>
              <el-row :gutter="10">
                <el-col :span="12">
                  <el-form-item label="时间限制" label-width="80px">
                    <el-input v-model="problem.timeLimit" placeholder="ms">
                      <template #append>ms</template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="内存限制" label-width="80px">
                    <el-input v-model="problem.memoryLimit" placeholder="MB">
                      <template #append>MB</template>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>

              <!-- 样例管理 -->
              <div class="sample-container">
                <div style="margin-bottom: 10px;">
                  <span style="font-weight: bold; font-size: 12px;">测试样例</span>
                  <el-button size="small" link type="primary" @click="addSample(pIndex)" style="margin-left: 10px;">+ 添加样例</el-button>
                </div>
                <div v-for="(sample, sIndex) in problem.samples" :key="sIndex" class="sample-item">
                  <el-row :gutter="10">
                    <el-col :span="10">
                      <el-input v-model="sample.input" type="textarea" :rows="1" placeholder="输入样例" />
                    </el-col>
                    <el-col :span="10">
                      <el-input v-model="sample.output" type="textarea" :rows="1" placeholder="输出样例" />
                    </el-col>
                    <el-col :span="4">
                      <el-button type="danger" link size="small" @click="removeSample(pIndex, sIndex)">删除</el-button>
                    </el-col>
                  </el-row>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>

        <el-form-item label="实验资源">
          <el-upload
            action="/api/upload"
            :show-file-list="false"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            multiple
          >
            <el-button size="small" type="primary">上传资源</el-button>
            <template #tip>
              <div style="color: #909399; font-size: 12px; margin-top: 5px;">
                支持上传文档、图片、视频等资源文件，单个文件不超过50MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="" v-if="experimentForm.resources && experimentForm.resources.length > 0">
          <div style="width: 100%;">
            <div 
              v-for="(file, index) in experimentForm.resources" 
              :key="index"
              style="margin-bottom: 12px; display: flex; align-items: center; gap: 12px;"
            >
              <span style="font-size: 15px; color: var(--text-primary); flex: 1;">
                {{ file.name }}
              </span>
              <el-button 
                size="small" 
                type="primary" 
                link 
                @click="downloadFile(file.url)"
              >
                下载
              </el-button>
              <el-button 
                size="small" 
                type="danger" 
                link 
                @click="removeResource(index)"
              >
                删除
              </el-button>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 实验报告列表对话框 -->
    <el-dialog v-model="reportsDialogVisible" title="实验报告列表" width="900px">
      <el-table :data="reports" style="width: 100%">
        <el-table-column prop="studentName" label="学生" width="120" />
        <el-table-column prop="submitTime" label="提交时间" width="180">
          <template #default="scope">
            {{ scope.row.submitTime || '未提交' }}
          </template>
        </el-table-column>
        <el-table-column label="实验总结" min-width="200">
          <template #default="scope">
            <el-text line-clamp="2">{{ scope.row.content || '无' }}</el-text>
          </template>
        </el-table-column>
        <el-table-column label="报告文件" width="280">
          <template #default="scope">
            <div v-if="scope.row.files && scope.row.files.length > 0">
              <div v-for="(file, index) in scope.row.files" :key="index" style="margin: 4px 0; display: flex; align-items: center;">
                <el-tag style="margin-right: 8px; flex: 1; overflow: hidden; text-overflow: ellipsis;">{{ file.name }}</el-tag>
                <el-button 
                  size="small" 
                  type="primary" 
                  link 
                  @click="downloadFile(file.url)"
                >
                  下载
                </el-button>
              </div>
            </div>
            <span v-else>无文件</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 实验详情对话框 - 学生题目完成情况 -->
    <el-dialog 
      v-model="detailsDialogVisible" 
      title="实验详情 - 学生完成情况" 
      width="90%"
      top="5vh"
      :close-on-click-modal="false"
    >
      <div v-if="currentExperimentDetails">
        <h3>{{ currentExperimentDetails.title }}</h3>
        <el-divider />
        
        <el-table 
          :data="studentProblemStats" 
          style="width: 100%" 
          border
          stripe
          :max-height="600"
        >
          <el-table-column prop="studentName" label="学生姓名" width="150" fixed />
          <el-table-column 
            v-for="(problem, index) in currentExperimentDetails.problems" 
            :key="problem.id"
            :label="problem.title || `题目${index + 1}`"
            min-width="120"
            align="center"
          >
            <template #default="scope">
              <el-tag 
                :type="getProblemStatusType(scope.row.problems[problem.id])"
                size="small"
              >
                {{ getProblemStatusText(scope.row.problems[problem.id]) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="完成度" width="150" align="center" fixed="right">
            <template #default="scope">
              <el-progress 
                :percentage="scope.row.completionRate" 
                :color="getProgressColor(scope.row.completionRate)"
              />
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div v-else style="text-align: center; padding: 40px;">
        <el-empty description="暂无数据" />
      </div>
    </el-dialog>

    <!-- 文件预览对话框 -->
    <el-dialog 
      v-model="previewVisible" 
      :title="previewTitle"
      width="80%"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <div class="preview-container">
        <!-- 视频预览 -->
        <video 
          v-if="previewType === 'video'" 
          :src="previewUrl" 
          controls 
          style="width: 100%; max-height: 600px"
        ></video>

        <!-- PDF预览 -->
        <iframe 
          v-else-if="previewType === 'pdf'" 
          :src="previewUrl"
          style="width: 100%; height: 600px; border: none"
        ></iframe>

        <!-- Office文档预览 (Word, Excel, PowerPoint) -->
        <iframe 
          v-else-if="previewType === 'office'" 
          :src="previewUrl"
          style="width: 100%; height: 600px; border: none"
        ></iframe>

        <!-- 文本文件预览 -->
        <iframe 
          v-else-if="previewType === 'text'" 
          :src="previewUrl"
          style="width: 100%; height: 600px; border: none"
        ></iframe>

        <!-- 图片预览 -->
        <img 
          v-else-if="previewType === 'image'"
          :src="previewUrl"
          style="width: 100%; max-height: 600px; object-fit: contain"
          alt="图片预览"
        />

        <!-- 不支持预览的文件 -->
        <div v-else class="no-preview">
          <el-icon :size="80" color="#909399">
            <Document />
          </el-icon>
          <p>此文件类型暂不支持在线预览</p>
          <el-button type="primary" @click="downloadFile(previewUrl)">下载文件</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import { 
  getExperimentList,
  getExperimentDetail,
  createExperiment, 
  updateExperiment, 
  deleteExperiment as deleteExperimentApi,
  getExperimentSubmissions,
  getStudentStats
} from '@/api/experiment'
import { getTeacherCourses } from '@/api/course'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false) // 页面加载状态
const dialogVisible = ref(false)
const reportsDialogVisible = ref(false)
const detailsDialogVisible = ref(false)
const isEdit = ref(false)

// 预览相关
const previewVisible = ref(false)
const previewType = ref('') // 'pdf', 'image', 'video', 'office', 'text', 'other'
const previewUrl = ref('')
const previewTitle = ref('')

const experimentList = ref([])
const myCourses = ref([])
const reports = ref([])
const currentExperimentDetails = ref(null)
const studentProblemStats = ref([])

const experimentForm = ref({
  courseId: null,
  title: '',
  description: '',
  requirements: '',
  steps: '',
  startTime: null,
  deadline: null,
  environmentType: 'local',
  environmentConfig: {},
  resources: [],
  problems: []
})

const dialogTitle = computed(() => isEdit.value ? '编辑实验' : '创建实验')

const addProblem = () => {
  experimentForm.value.problems.push({
    title: '',
    description: '',
    difficulty: 'easy',
    score: 10,
    timeLimit: 1000,
    memoryLimit: 256,
    samples: []
  })
}

const removeProblem = (index) => {
  experimentForm.value.problems.splice(index, 1)
}

const addSample = (problemIndex) => {
  experimentForm.value.problems[problemIndex].samples.push({
    input: '',
    output: '',
    isHidden: false
  })
}

const removeSample = (problemIndex, sampleIndex) => {
  experimentForm.value.problems[problemIndex].samples.splice(sampleIndex, 1)
}

const loadExperimentList = async () => {
  loading.value = true
  try {
    const res = await getExperimentList()
    experimentList.value = res.data || []
  } catch (error) {
    console.error('加载实验列表失败:', error)
    ElMessage.error('加载实验列表失败')
    experimentList.value = []
  } finally {
    loading.value = false
  }
}

const loadMyCourses = async () => {
  try {
    const res = await getTeacherCourses(userStore.userInfo.id)
    myCourses.value = res.data
  } catch (error) {
    myCourses.value = [
      { id: 1, name: 'Java程序设计' },
      { id: 2, name: '数据库原理' }
    ]
  }
}

const showCreateDialog = () => {
  isEdit.value = false
  experimentForm.value = {
    courseId: null,
    title: '',
    description: '',
    requirements: '',
    steps: '',
    startTime: null,
    deadline: null,
    environmentType: 'local',
    environmentConfig: {},
    resources: [],
    problems: []
  }
  dialogVisible.value = true
}

const editExperiment = async (experiment) => {
  isEdit.value = true
  
  try {
    // 先获取实验的完整详情（包含题目）
    const res = await getExperimentDetail(experiment.id)
    const fullExperiment = res.data
    
    // 深拷贝并处理数据格式
    experimentForm.value = {
      ...fullExperiment,
      // 处理日期字段 - 转换为Date对象以便日期选择器显示
      startTime: fullExperiment.startTime ? new Date(fullExperiment.startTime) : null,
      deadline: fullExperiment.deadline ? new Date(fullExperiment.deadline) : null,
      // 确保 resources 是数组格式
      resources: (() => {
        if (!fullExperiment.resources) return []
        if (Array.isArray(fullExperiment.resources)) return [...fullExperiment.resources]
        try {
          const parsed = JSON.parse(fullExperiment.resources)
          return Array.isArray(parsed) ? parsed : []
        } catch {
          return []
        }
      })(),
      // 确保 problems 是数组格式
      problems: (() => {
        if (!fullExperiment.problems) return []
        if (Array.isArray(fullExperiment.problems)) return [...fullExperiment.problems]
        try {
          const parsed = JSON.parse(fullExperiment.problems)
          return Array.isArray(parsed) ? parsed : []
        } catch {
          return []
        }
      })(),
      // 确保 environmentConfig 是对象格式
      environmentConfig: (() => {
        if (!fullExperiment.environmentConfig) return {}
        if (typeof fullExperiment.environmentConfig === 'object') return { ...fullExperiment.environmentConfig }
        try {
          const parsed = JSON.parse(fullExperiment.environmentConfig)
          return typeof parsed === 'object' ? parsed : {}
        } catch {
          return {}
        }
      })()
    }
    
    // 确保每个 problem 都有 samples 数组
    experimentForm.value.problems = experimentForm.value.problems.map(problem => ({
      ...problem,
      samples: Array.isArray(problem.samples) ? problem.samples : []
    }))
    
    dialogVisible.value = true
  } catch (error) {
    console.error('获取实验详情失败:', error)
    ElMessage.error('获取实验详情失败')
  }
}

const deleteExperiment = (experiment) => {
  ElMessageBox.confirm(`确定要删除实验《${experiment.title}》吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteExperimentApi(experiment.id)
      ElMessage.success({ message: '删除成功', duration: 3000 })
      loadExperimentList()
    } catch (error) {
      console.error(error)
    }
  })
}

const handleSubmit = async () => {
  try {
    // 验证必填字段
    if (!experimentForm.value.courseId) {
      ElMessage.warning('请选择课程')
      return
    }
    if (!experimentForm.value.title || experimentForm.value.title.trim() === '') {
      ElMessage.warning('请输入实验名称')
      return
    }
    if (!experimentForm.value.deadline) {
      ElMessage.warning('请选择截止时间')
      return
    }
    
    // 验证是否添加了题目
    if (!experimentForm.value.problems || experimentForm.value.problems.length === 0) {
      ElMessage.warning('请至少添加一道题目')
      return
    }
    
    // 验证每道题目是否完整
    for (let i = 0; i < experimentForm.value.problems.length; i++) {
      const problem = experimentForm.value.problems[i]
      if (!problem.title || problem.title.trim() === '') {
        ElMessage.warning(`题目${i + 1}：请输入题目标题`)
        return
      }
      if (!problem.description || problem.description.trim() === '') {
        ElMessage.warning(`题目${i + 1}：请输入题目描述`)
        return
      }
    }
    
    // 格式化日期为 yyyy-MM-dd HH:mm:ss 格式（标准格式，不使用T分隔符）
    const formatDateTime = (date) => {
      if (!date) return null
      const d = new Date(date)
      const pad = (n) => String(n).padStart(2, '0')
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
    }

    const submitData = {
      id: experimentForm.value.id,
      courseId: experimentForm.value.courseId,
      title: experimentForm.value.title,
      description: experimentForm.value.description,
      requirements: experimentForm.value.requirements,
      steps: experimentForm.value.steps,
      startTime: formatDateTime(experimentForm.value.startTime),
      deadline: formatDateTime(experimentForm.value.deadline),
      environmentType: experimentForm.value.environmentType,
      environmentConfig: JSON.stringify(experimentForm.value.environmentConfig),
      resources: JSON.stringify(experimentForm.value.resources),
      problems: experimentForm.value.problems
    }

    if (isEdit.value) {
      await updateExperiment(experimentForm.value.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await createExperiment(submitData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadExperimentList()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('操作失败: ' + (error.response?.data?.message || error.message))
  }
}

const viewReports = async (experiment) => {
  try {
    const res = await getExperimentSubmissions(experiment.id)
    reports.value = (res.data || []).map(submission => {
      // 解析files字段（JSON字符串）
      let files = []
      if (submission.files) {
        try {
          files = JSON.parse(submission.files)
        } catch (e) {
          console.error('解析文件列表失败:', e)
        }
      }
      return {
        ...submission,
        files: Array.isArray(files) ? files : []
      }
    })
    reportsDialogVisible.value = true
  } catch (error) {
    console.error('加载报告列表失败:', error)
    ElMessage.error('加载报告列表失败')
    reports.value = []
  }
}

// 查看实验详情 - 学生题目完成情况
const viewDetails = async (experiment) => {
  try {
    // 调用真实API获取学生完成情况
    const res = await getStudentStats(experiment.id)
    if (res.code !== 200) {
      ElMessage.error(res.message || '获取统计数据失败')
      return
    }
    
    const data = res.data
    currentExperimentDetails.value = {
      title: experiment.title,
      problems: data.problems || []
    }
    
    studentProblemStats.value = data.studentStats || []
    
    detailsDialogVisible.value = true
  } catch (error) {
    console.error('加载实验详情失败:', error)
    ElMessage.error('加载实验详情失败')
  }
}

// 题目状态类型
const getProblemStatusType = (problemData) => {
  if (!problemData) return 'info'
  switch (problemData.status) {
    case 'passed': return 'success'
    case 'failed': return 'danger'
    case 'not_submitted': return 'info'
    default: return 'info'
  }
}

// 题目状态文本
const getProblemStatusText = (problemData) => {
  if (!problemData) return '未提交'
  switch (problemData.status) {
    case 'passed': return '通过'
    case 'failed': return '未通过'
    case 'not_submitted': return '未提交'
    default: return '未知'
  }
}

// 进度条颜色
const getProgressColor = (percentage) => {
  if (percentage >= 80) return '#67c23a'
  if (percentage >= 60) return '#e6a23c'
  return '#f56c6c'
}

// 上传配置
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

const beforeUpload = (file) => {
  const maxSize = 50 * 1024 * 1024 // 50MB
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过 50MB!')
    return false
  }
  return true
}

const handleUploadSuccess = (response, file, fileList) => {
  if (response.code === 200) {
    if (!experimentForm.value.resources) {
      experimentForm.value.resources = []
    }
    experimentForm.value.resources.push({
      name: response.data.name || file.name,
      url: response.data.url
    })
    ElMessage.success('文件上传成功')
  } else {
    ElMessage.error(response.message || '文件上传失败')
  }
}

const handleUploadError = (error, file) => {
  console.error('文件上传失败:', error)
  ElMessage.error('文件上传失败: ' + (error.message || '未知错误'))
}

const handleRemoveFile = (file, fileList) => {
  if (!experimentForm.value.resources) {
    experimentForm.value.resources = []
  }
  // 从资源列表中移除对应的文件
  const url = file.response?.data?.url || file.url
  experimentForm.value.resources = experimentForm.value.resources.filter(r => r.url !== url)
}

const removeResource = (index) => {
  experimentForm.value.resources.splice(index, 1)
}

/**
 * 获取文件扩展名
 */
const getFileExtension = (url) => {
  if (!url || typeof url !== 'string') return ''
  const filename = url.split('/').pop() || ''
  const lastDot = filename.lastIndexOf('.')
  return lastDot === -1 ? '' : filename.slice(lastDot + 1).toLowerCase()
}

/**
 * 预览文件
 */
const previewFile = (url, filename = '') => {
  if (!url) {
    ElMessage.error('文件URL不存在')
    return
  }
  
  const ext = getFileExtension(url)
  previewTitle.value = filename || url.split('/').pop() || '文件预览'
  
  // 根据文件类型设置预览类型
  if (['jpg', 'jpeg', 'png', 'gif', 'svg', 'webp', 'bmp'].includes(ext)) {
    previewType.value = 'image'
    previewUrl.value = url
  } else if (['mp4', 'webm', 'ogg', 'avi', 'mov'].includes(ext)) {
    previewType.value = 'video'
    previewUrl.value = url
  } else if (ext === 'pdf') {
    previewType.value = 'pdf'
    previewUrl.value = url
  } else if (['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx'].includes(ext)) {
    // Office文档使用Microsoft Office Online Viewer
    previewType.value = 'office'
    // 构建完整的URL（确保是绝对路径）
    const fullUrl = url.startsWith('http') ? url : window.location.origin + url
    previewUrl.value = `https://view.officeapps.live.com/op/embed.aspx?src=${encodeURIComponent(fullUrl)}`
  } else if (['txt', 'json', 'xml', 'csv'].includes(ext)) {
    // 文本文件直接在iframe中打开
    previewType.value = 'text'
    previewUrl.value = url
  } else {
    previewType.value = 'other'
    previewUrl.value = url
  }
  
  previewVisible.value = true
}

/**
 * 下载文件
 */
const downloadFile = (url) => {
  window.open(url, '_blank')
}

onMounted(() => {
  loadExperimentList()
  loadMyCourses()
})
</script>

<style scoped>
.experiment-page {
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

:deep(.el-dialog) {
  background-color: var(--bg-float);
}

:deep(.el-dialog__header) {
  background-color: var(--bg-secondary);
}

:deep(.el-input__wrapper), :deep(.el-textarea__inner) {
  background-color: var(--bg-secondary);
  color: var(--text-primary);
}

:deep(.el-radio-group) {
  color: var(--text-primary);
}

.sample-container {
  margin-left: 80px;
  margin-top: 10px;
  background: var(--bg-secondary);
  padding: 10px;
  border-radius: 4px;
}

.sample-item {
  margin-bottom: 10px;
  border-bottom: 1px dashed var(--border);
  padding-bottom: 10px;
}

:deep(.el-collapse) {
  --el-collapse-header-bg-color: var(--bg-secondary);
  --el-collapse-content-bg-color: var(--bg-float);
  border-color: var(--border);
}

:deep(.el-collapse-item__header) {
  color: var(--text-primary);
  padding-left: 10px;
}

:deep(.el-collapse-item__wrap) {
  border-bottom-color: var(--border);
}

.preview-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.no-preview {
  text-align: center;
  padding: 40px 20px;
  color: var(--text-secondary);
}

.no-preview p {
  margin: 20px 0;
  font-size: 16px;
}
</style>
