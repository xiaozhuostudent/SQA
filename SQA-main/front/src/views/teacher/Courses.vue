<template>
  <div class="courses-page">
    <!-- 顶部操作栏 -->
    <el-card class="header-card" shadow="never">
      <div class="header-actions">
        <el-button type="primary" @click="showCreateRequestDialog">申请创建课程</el-button>
        <el-button @click="showRequestList">查看我的申请</el-button>
      </div>
    </el-card>

    <!-- 课程卡片展示 -->
    <div v-loading="loading" element-loading-text="加载课程数据中...">
      <el-empty v-if="!loading && courses.length === 0" description="暂无课程数据" />
      
      <div v-else class="courses-grid">
        <el-card v-for="course in courses" :key="course.id" class="course-card" shadow="hover">
          <div class="course-header">
            <div class="course-title">
              <h3>{{ course.name }}</h3>
              <el-tag :type="getCategoryType(course.category)" size="small">
                {{ course.category }}
              </el-tag>
            </div>
            <div class="course-code">{{ course.courseCode }}</div>
          </div>
          
          <div class="course-body">
            <div class="course-info">
              <div class="info-item">
                <span class="label">学期:</span>
                <span>{{ course.semester }}</span>
              </div>
              <div class="info-item">
                <span class="label">学分:</span>
                <span>{{ course.credit }} 学分</span>
              </div>
              <div class="info-item">
                <span class="label">容量:</span>
                <span>{{ course.enrolled || 0 }} / {{ course.capacity }} 人</span>
              </div>
            </div>
            
            <div class="course-description">
              <p>{{ course.description || '暂无课程简介' }}</p>
            </div>
          </div>
          
          <div class="course-footer">
            <el-button-group style="width: 100%">
              <el-button size="small" @click="viewCourse(course)" style="flex: 1">查看</el-button>
              <el-button size="small" type="danger" @click="handleDeleteCourse(course)" style="flex: 1">
                申请删除
              </el-button>
            </el-button-group>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 创建课程申请对话框 -->
    <el-dialog v-model="createDialogVisible" title="申请创建课程" width="700px">
      <el-form :model="courseForm" label-width="100px">
        <el-form-item label="课程名称">
          <el-input v-model="courseForm.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="课程代码">
          <el-input v-model="courseForm.courseCode" placeholder="请输入课程代码，如：CS101" />
        </el-form-item>
        <el-form-item label="学分">
          <el-input-number v-model="courseForm.credit" :min="0.5" :max="10" :step="0.5" />
        </el-form-item>
        <el-form-item label="容量">
          <el-input-number v-model="courseForm.capacity" :min="1" :max="200" />
        </el-form-item>
        <el-form-item label="学期">
          <el-input v-model="courseForm.semester" placeholder="如：2025-2026-1" />
        </el-form-item>
        <el-form-item label="课程类别">
          <el-select v-model="courseForm.category" placeholder="请选择课程类别" style="width: 100%">
            <el-option label="专业必修" value="专业必修" />
            <el-option label="专业选修" value="专业选修" />
            <el-option label="通识必修" value="通识必修" />
            <el-option label="通识选修" value="通识选修" />
            <el-option label="公共必修" value="公共必修" />
            <el-option label="公共基础" value="公共基础" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程简介">
          <el-input
            v-model="courseForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入课程简介"
          />
        </el-form-item>
        <el-form-item label="申请理由">
          <el-input
            v-model="courseForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请说明创建该课程的原因和必要性"
          />
        </el-form-item>
        <el-form-item label="附件材料">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            accept=".pdf,.doc,.docx"
          >
            <el-button>选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持上传PDF、Word文档，文件大小不超过10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreateRequest" :loading="submitting">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 删除课程申请对话框 -->
    <el-dialog v-model="deleteDialogVisible" title="申请删除课程" width="600px">
      <el-alert
        title="提示"
        type="warning"
        description="删除课程需要管理员审核，请填写删除理由"
        :closable="false"
        style="margin-bottom: 20px"
      />
      <el-form :model="deleteForm" label-width="100px">
        <el-form-item label="课程名称">
          <el-input v-model="deletingCourse.name" disabled />
        </el-form-item>
        <el-form-item label="课程代码">
          <el-input v-model="deletingCourse.courseCode" disabled />
        </el-form-item>
        <el-form-item label="删除理由">
          <el-input
            v-model="deleteForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请说明删除该课程的原因"
          />
        </el-form-item>
        <el-form-item label="附件材料">
          <el-upload
            ref="deleteUploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleDeleteFileChange"
            :on-remove="handleDeleteFileRemove"
            accept=".pdf,.doc,.docx"
          >
            <el-button>选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持上传PDF、Word文档，文件大小不超过10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submitDeleteRequest" :loading="submitting">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 查看课程详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="课程详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="课程名称" :span="2">{{ currentCourse.name }}</el-descriptions-item>
        <el-descriptions-item label="课程代码">{{ currentCourse.courseCode }}</el-descriptions-item>
        <el-descriptions-item label="学分">{{ currentCourse.credit }}</el-descriptions-item>
        <el-descriptions-item label="学期">{{ currentCourse.semester }}</el-descriptions-item>
        <el-descriptions-item label="类别">{{ currentCourse.category }}</el-descriptions-item>
        <el-descriptions-item label="容量">
          {{ currentCourse.enrolled || 0 }} / {{ currentCourse.capacity }} 人
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentCourse.status === 'approved' ? 'success' : 'warning'">
            {{ currentCourse.status === 'approved' ? '已批准' : '待审核' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="课程简介" :span="2">
          {{ currentCourse.description || '暂无简介' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 申请记录对话框 -->
    <el-dialog v-model="requestListVisible" title="我的申请记录" width="900px">
      <el-table :data="requests" v-loading="requestLoading">
        <el-table-column prop="requestType" label="申请类型" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.requestType === 'create' ? 'success' : 'danger'" size="small">
              {{ scope.row.requestType === 'create' ? '创建课程' : '删除课程' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="courseName" label="课程名称" width="150" />
        <el-table-column prop="courseCode" label="课程代码" width="100" />
        <el-table-column prop="reason" label="申请理由" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag 
              :type="scope.row.status === 'approved' ? 'success' : 
                     scope.row.status === 'rejected' ? 'danger' : 'warning'"
              size="small"
            >
              {{ scope.row.status === 'approved' ? '已批准' : 
                 scope.row.status === 'rejected' ? '已拒绝' : '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="160" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTeacherCourses } from '@/api/course'
import { submitCourseRequest, getTeacherRequests, uploadRequestFile } from '@/api/courseRequest'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loading = ref(false)
const courses = ref([])
const createDialogVisible = ref(false)
const deleteDialogVisible = ref(false)
const viewDialogVisible = ref(false)
const requestListVisible = ref(false)
const submitting = ref(false)
const requestLoading = ref(false)

const courseForm = ref({
  name: '',
  courseCode: '',
  credit: 3,
  capacity: 50,
  semester: '',
  category: '',
  description: '',
  reason: ''
})

const deleteForm = ref({
  reason: ''
})

const deletingCourse = ref({})
const currentCourse = ref({})
const requests = ref([])

const uploadFile = ref(null)
const deleteUploadFile = ref(null)

// 加载课程列表
const loadCourses = async () => {
  loading.value = true
  try {
    const teacherId = userStore.userInfo?.id
    if (!teacherId) {
      ElMessage.error('未获取到教师ID，请重新登录')
      return
    }
    
    const res = await getTeacherCourses(teacherId)
    
    if (res.code === 200) {
      courses.value = res.data || []
    } else {
      ElMessage.error(res.message || '加载课程失败')
    }
  } catch (error) {
    console.error('加载课程失败:', error)
    ElMessage.error('加载课程失败')
  } finally {
    loading.value = false
  }
}

// 加载申请记录
const loadRequests = async () => {
  requestLoading.value = true
  try {
    const teacherId = userStore.userInfo?.id
    const res = await getTeacherRequests(teacherId)
    if (res.code === 200) {
      requests.value = res.data || []
    }
  } catch (error) {
    console.error('加载申请记录失败:', error)
  } finally {
    requestLoading.value = false
  }
}

// 显示创建对话框
const showCreateRequestDialog = () => {
  courseForm.value = {
    name: '',
    courseCode: '',
    credit: 3,
    capacity: 50,
    semester: '2025-2026-1',
    category: '专业必修',
    description: '',
    reason: ''
  }
  uploadFile.value = null
  createDialogVisible.value = true
}

// 查看课程
const viewCourse = (course) => {
  currentCourse.value = course
  viewDialogVisible.value = true
}

// 申请删除课程
const handleDeleteCourse = (course) => {
  deletingCourse.value = course
  deleteForm.value = {
    reason: ''
  }
  deleteUploadFile.value = null
  deleteDialogVisible.value = true
}

// 文件选择处理
const handleFileChange = (file) => {
  uploadFile.value = file.raw
}

const handleFileRemove = () => {
  uploadFile.value = null
}

const handleDeleteFileChange = (file) => {
  deleteUploadFile.value = file.raw
}

const handleDeleteFileRemove = () => {
  deleteUploadFile.value = null
}

// 上传文件到Redis
const uploadFileToRedis = async (file) => {
  if (!file) return null
  
  const formData = new FormData()
  formData.append('file', file)
  
  const res = await uploadRequestFile(formData)
  if (res.code === 200) {
    return res.data
  } else {
    throw new Error(res.message || '文件上传失败')
  }
}

// 提交创建申请
const submitCreateRequest = async () => {
  if (!courseForm.value.name || !courseForm.value.courseCode || !courseForm.value.reason) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  submitting.value = true
  try {
    let fileData = null
    if (uploadFile.value) {
      fileData = await uploadFileToRedis(uploadFile.value)
    }
    
    const teacherId = userStore.userInfo?.id
    const teacherName = userStore.userInfo?.name
    
    const requestData = {
      requestType: 'create',
      teacherId,
      teacherName,
      courseName: courseForm.value.name,
      courseCode: courseForm.value.courseCode,
      credit: courseForm.value.credit,
      capacity: courseForm.value.capacity,
      semester: courseForm.value.semester,
      category: courseForm.value.category,
      description: courseForm.value.description,
      reason: courseForm.value.reason,
      fileName: fileData?.fileName,
      fileKey: fileData?.fileKey
    }
    
    const res = await submitCourseRequest(requestData)
    if (res.code === 200) {
      ElMessage.success('创建申请已提交，等待管理员审核')
      createDialogVisible.value = false
      await loadCourses()
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error) {
    console.error('提交创建申请失败:', error)
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

// 提交删除申请
const submitDeleteRequest = async () => {
  if (!deleteForm.value.reason) {
    ElMessage.warning('请填写删除理由')
    return
  }
  
  submitting.value = true
  try {
    let fileData = null
    if (deleteUploadFile.value) {
      fileData = await uploadFileToRedis(deleteUploadFile.value)
    }
    
    const teacherId = userStore.userInfo?.id
    const teacherName = userStore.userInfo?.name
    
    const requestData = {
      requestType: 'delete',
      teacherId,
      teacherName,
      courseId: deletingCourse.value.id,
      courseName: deletingCourse.value.name,
      courseCode: deletingCourse.value.courseCode,
      reason: deleteForm.value.reason,
      fileName: fileData?.fileName,
      fileKey: fileData?.fileKey
    }
    
    const res = await submitCourseRequest(requestData)
    if (res.code === 200) {
      ElMessage.success('删除申请已提交，等待管理员审核')
      deleteDialogVisible.value = false
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error) {
    console.error('提交删除申请失败:', error)
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

// 查看申请列表
const showRequestList = async () => {
  requestListVisible.value = true
  await loadRequests()
}

// 获取类别标签类型
const getCategoryType = (category) => {
  const typeMap = {
    '专业必修': 'danger',
    '专业选修': 'warning',
    '通识必修': 'success',
    '通识选修': 'info',
    '公共必修': 'primary',
    '公共基础': ''
  }
  return typeMap[category] || ''
}

onMounted(() => {
  loadCourses()
})
</script>

<style scoped>
.header-card {
  margin-bottom: 20px;
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.course-card {
  transition: all 0.3s;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.course-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.course-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.course-body {
  flex: 1;
}

.course-info {
  margin-bottom: 12px;
}

.course-info p {
  margin: 8px 0;
  color: #606266;
  display: flex;
  align-items: center;
}

.course-info p strong {
  width: 80px;
  color: #303133;
}

.course-description {
  margin-top: 12px;
  padding: 12px 15px;
  background: linear-gradient(135deg, #95bee6ce 0%, #73b4f5d6 100%);
  border-radius: 8px;
  color: #ffffff;
  font-size: 14px;
  line-height: 1.6;
  max-height: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
}

.course-footer {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
  display: flex;
  gap: 10px;
}

.requests-table {
  margin-top: 20px;
}

.upload-tip {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
}

.courses-page {
  padding: 24px;
  background-color: var(--bg-primary);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

:deep(.el-input__wrapper) {
  background-color: var(--bg-secondary);
}

:deep(.el-textarea__inner) {
  background-color: var(--bg-secondary);
  color: var(--text-primary);
}
</style>
