<template>
  <div class="resources-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的教学资源</span>
          <div class="header-actions">
            <el-select
              v-model="selectedCourseId"
              placeholder="选择课程"
              clearable
              style="width: 200px; margin-right: 12px"
              @change="handleCourseChange"
            >
              <el-option label="全部课程" :value="null" />
              <el-option
                v-for="course in myCourses"
                :key="course.id"
                :label="course.name"
                :value="course.id"
              />
            </el-select>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索资源"
              prefix-icon="Search"
              style="width: 300px; margin-right: 12px"
              clearable
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            />
            <el-button type="primary" @click="showUploadDialog">
              <el-icon style="margin-right: 4px;"><Upload /></el-icon>
              上传资源
            </el-button>
          </div>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="4">
          <el-menu :default-active="activeCategory" @select="handleCategoryChange">
            <el-menu-item index="all">
              <el-icon><Folder /></el-icon>
              <span>全部资源</span>
            </el-menu-item>
            <el-menu-item index="document">
              <el-icon><Document /></el-icon>
              <span>文档课件</span>
            </el-menu-item>
            <el-menu-item index="video">
              <el-icon><VideoPlay /></el-icon>
              <span>视频教程</span>
            </el-menu-item>
            <el-menu-item index="code">
              <el-icon><Tickets /></el-icon>
              <span>示例代码</span>
            </el-menu-item>
            <el-menu-item index="other">
              <el-icon><Files /></el-icon>
              <span>其他资源</span>
            </el-menu-item>
          </el-menu>
        </el-col>

        <el-col :span="20">
          <el-table 
            :data="displayResources" 
            v-loading="loading"
            style="width: 100%"
          >
            <el-table-column prop="name" label="资源名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="courseName" label="所属课程" width="150" />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="scope">
                <el-tag :type="getTypeTagType(scope.row.type)">
                  {{ getTypeLabel(scope.row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="size" label="大小" width="100" />
            <el-table-column prop="downloadCount" label="下载次数" width="100" />
            <el-table-column prop="uploadTime" label="上传时间" width="180" />
            <el-table-column label="操作" fixed="right" width="280">
              <template #default="scope">
                <el-button 
                  size="small" 
                  @click="previewResource(scope.row)"
                  :disabled="!canPreview(scope.row.name)"
                >
                  预览
                </el-button>
                <el-button size="small" type="primary" @click="editResource(scope.row)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteResource(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-if="resources.length > 0"
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="filteredTotal"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            style="margin-top: 20px; justify-content: flex-end"
          />
        </el-col>
      </el-row>
    </el-card>

    <!-- 预览对话框 -->
    <el-dialog 
      v-model="previewVisible" 
      :title="previewTitle"
      width="80%"
      :fullscreen="isFullscreen"
      destroy-on-close
    >
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>{{ previewTitle }}</span>
          <el-button 
            :icon="isFullscreen ? 'FullScreen' : 'FullScreen'" 
            @click="toggleFullscreen"
            circle
          />
        </div>
      </template>
      
      <div class="preview-container">
        <!-- 视频预览 -->
        <video 
          v-if="previewType === 'video'" 
          :src="previewUrl" 
          controls 
          autoplay
          style="width: 100%; max-height: 70vh;"
        >
          您的浏览器不支持视频播放
        </video>
        
        <!-- PDF预览 -->
        <iframe 
          v-else-if="previewType === 'pdf'" 
          :src="previewUrl"
          style="width: 100%; height: 70vh; border: none;"
          frameborder="0"
        />
        
        <!-- 图片预览 -->
        <img 
          v-else-if="previewType === 'image'"
          :src="previewUrl"
          style="max-width: 100%; max-height: 70vh; display: block; margin: 0 auto;"
          alt="图片预览"
        />
        
        <!-- 其他类型 -->
        <div v-else class="no-preview">
          <el-icon :size="60"><Document /></el-icon>
          <p>此文件类型暂不支持在线预览</p>
          <el-button type="primary" @click="handleDownload(currentResource)">
            下载查看
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 编辑资源对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑资源" width="600px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="资源名称">
          <el-input v-model="editForm.name" placeholder="请输入资源名称" />
        </el-form-item>
        <el-form-item label="资源描述">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入资源描述"
          />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="editForm.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="display: flex; justify-content: space-between; width: 100%;">
          <el-button 
            v-if="canEditOnline(editForm.name)"
            type="success" 
            @click="openOnlineEditor"
          >
            <el-icon style="margin-right: 4px;"><Edit /></el-icon>
            在线编辑
          </el-button>
          <div v-else></div>
          <div>
            <el-button @click="editDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleEdit">确定</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 上传资源对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="上传资源" width="600px">
      <el-form :model="resourceForm" label-width="100px">
        <el-form-item label="课程">
          <el-select v-model="resourceForm.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option
              v-for="course in myCourses"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="资源名称">
          <el-input v-model="resourceForm.name" placeholder="请输入资源名称" />
        </el-form-item>
        <el-form-item label="资源类型">
          <el-select v-model="resourceForm.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="文档课件" value="document" />
            <el-option label="视频教程" value="video" />
            <el-option label="示例代码" value="code" />
            <el-option label="其他资源" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="资源描述">
          <el-input
            v-model="resourceForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入资源描述"
          />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="resourceForm.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
        <el-form-item label="文件URL">
          <el-input 
            v-model="resourceForm.fileUrl" 
            placeholder="请输入文件URL (如: http://120.26.212.210/resources/ppt/xxx.pptx)" 
          />
          <div style="margin-top: 8px; font-size: 12px; color: #909399;">
            提示: 文件需要先上传到服务器资源目录，然后填写访问URL
          </div>
        </el-form-item>
        <el-form-item label="文件大小">
          <el-input 
            v-model.number="resourceForm.fileSize" 
            type="number"
            placeholder="请输入文件大小(字节)" 
          />
          <div style="margin-top: 8px; font-size: 12px; color: #909399;">
            示例: 1MB = 1048576字节, 可以用在线转换工具计算
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpload">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 教师端资源管理
 * 
 * 权限说明：
 * - 教师：可以上传资源、查看自己的资源、编辑/删除自己的资源、预览所有资源
 * - 管理员：可以管理所有资源（在管理员端）
 * - 学生：只能查看和预览已审核的资源（在学生端）
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Folder, Document, VideoPlay, Tickets, Files, Upload, FullScreen, Edit
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import { getTeacherCourses } from '@/api/course'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const uploadDialogVisible = ref(false)
const resources = ref([])
const myCourses = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const activeCategory = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const selectedCourseId = ref(null)

const getBackendOrigin = () => {
  const apiBase = import.meta.env.VITE_API_BASE_URL
  if (apiBase && /^https?:\/\//i.test(apiBase)) {
    try {
      return new URL(apiBase).origin
    } catch (error) {
      console.warn('VITE_API_BASE_URL 解析失败，回退到默认后端地址:', error)
    }
  }
  return `${window.location.protocol}//${window.location.hostname}:8080`
}

const BACKEND_ORIGIN = getBackendOrigin()

const normalizeResourceAccessUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('/')) {
    return `${BACKEND_ORIGIN}${url}`
  }
  return url
    .replace(/^https?:\/\/(localhost|127\.0\.0\.1):8080/i, BACKEND_ORIGIN)
    .replace(/^https?:\/\/120\.26\.212\.210(?::\d+)?/i, BACKEND_ORIGIN)
}

// 预览相关
const previewVisible = ref(false)
const previewUrl = ref('')
const previewType = ref('')
const previewTitle = ref('')
const currentResource = ref(null)
const isFullscreen = ref(false)

const resourceForm = ref({
  courseId: null,
  courseName: '',
  name: '',
  type: 'document',
  description: '',
  tags: '',
  fileUrl: '',
  fileSize: 0
})

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
}

// 获取文件扩展名
const getFileExtension = (filename) => {
  if (!filename || typeof filename !== 'string') return ''
  const lastDot = filename.lastIndexOf('.')
  return lastDot === -1 ? '' : filename.slice(lastDot + 1).toLowerCase()
}

// 判断是否可以预览
const canPreview = (filename) => {
  const ext = getFileExtension(filename)
  const previewableExts = ['pdf', 'jpg', 'jpeg', 'png', 'gif', 'svg', 'mp4', 'webm', 'ogg', 'docx', 'pptx']
  return previewableExts.includes(ext)
}

// 过滤后的资源列表
const filteredResources = computed(() => {
  let filtered = resources.value
  
  // 按课程过滤
  if (selectedCourseId.value !== null) {
    filtered = filtered.filter(r => r.courseId === selectedCourseId.value)
  }
  
  // 按类型过滤
  if (activeCategory.value !== 'all') {
    filtered = filtered.filter(r => r.type === activeCategory.value)
  }
  
  // 按关键词过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(r => 
      r.name?.toLowerCase().includes(keyword) ||
      r.courseName?.toLowerCase().includes(keyword) ||
      r.tags?.toLowerCase().includes(keyword)
    )
  }
  
  return filtered
})

// 过滤后的总数
const filteredTotal = computed(() => filteredResources.value.length)

// 当前页显示的资源
const displayResources = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredResources.value.slice(start, end)
})

const getTypeLabel = (type) => {
  const labels = {
    document: '文档',
    video: '视频',
    code: '代码',
    other: '其他'
  }
  return labels[type] || '未知'
}

const getTypeTagType = (type) => {
  const tagTypes = {
    document: '',
    video: 'success',
    code: 'warning',
    other: 'info'
  }
  return tagTypes[type] || 'info'
}

const handleCourseChange = () => {
  currentPage.value = 1
}

const handleCategoryChange = (key) => {
  activeCategory.value = key
  currentPage.value = 1
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
}

const handleDownload = (resource) => {
  if (resource.fileUrl) {
    window.open(normalizeResourceAccessUrl(resource.fileUrl), '_blank')
  } else {
    ElMessage.warning('该资源没有可下载的文件')
  }
}

const loadResources = async () => {
  loading.value = true
  try {
    const userId = userStore.user?.id || userStore.userInfo?.id || 1
    
    const res = await request({
      url: `/resource/teacher/${userId}`,
      method: 'get'
    })
    
    const allResources = res.data || []
    
    // 只显示属于教师当前课程的资源
    const validCourseIds = myCourses.value.map(course => Number(course.id))
    
    if (validCourseIds.length === 0) {
      // 如果没有课程，不显示任何资源
      resources.value = []
    } else {
      // 只保留属于当前课程的资源
      const filteredResources = allResources.filter(item => 
        validCourseIds.includes(Number(item.courseId))
      )
      
      resources.value = filteredResources.map(item => ({
        ...item,
        fileUrl: normalizeResourceAccessUrl(item.fileUrl),
        previewUrl: normalizeResourceAccessUrl(item.previewUrl),
        size: formatFileSize(item.fileSize),
        uploadTime: item.createTime
      }))
    }
  } catch (error) {
    ElMessage.error('加载资源列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadMyCourses = async () => {
  try {
    const userId = userStore.user?.id || userStore.userInfo?.id
    if (!userId) {
      ElMessage.error('未获取到教师ID，请重新登录')
      return
    }
    
    const res = await getTeacherCourses(userId)
    
    if (res.code === 200) {
      myCourses.value = res.data || []
      // 课程加载后重新加载资源
      await loadResources()
    } else {
      ElMessage.error(res.message || '加载课程列表失败')
    }
  } catch (error) {
    console.error('加载课程列表失败:', error)
    ElMessage.error('加载课程列表失败: ' + (error.message || '未知错误'))
  }
}

const showUploadDialog = () => {
  resourceForm.value = {
    courseId: null,
    courseName: '',
    name: '',
    type: 'document',
    description: '',
    tags: '',
    fileUrl: '',
    fileSize: 0
  }
  uploadDialogVisible.value = true
}

const handleUpload = async () => {
  // 验证表单
  if (!resourceForm.value.courseId) {
    ElMessage.warning('请选择课程')
    return
  }
  if (!resourceForm.value.name) {
    ElMessage.warning('请输入资源名称')
    return
  }
  if (!resourceForm.value.fileUrl) {
    ElMessage.warning('请输入文件URL')
    return
  }
  if (!resourceForm.value.fileSize || resourceForm.value.fileSize <= 0) {
    ElMessage.warning('请输入有效的文件大小')
    return
  }
  
  try {
    // 获取选中课程的名称
    const selectedCourse = myCourses.value.find(c => c.id === resourceForm.value.courseId)
    
    // 构建请求数据
    const data = {
      courseId: resourceForm.value.courseId,
      courseName: selectedCourse?.name || '',
      name: resourceForm.value.name,
      type: resourceForm.value.type,
      description: resourceForm.value.description,
      fileUrl: resourceForm.value.fileUrl,
      fileSize: resourceForm.value.fileSize,
      uploaderId: userStore.user?.id || 1,
      uploaderName: userStore.user?.name || '教师',
      tags: resourceForm.value.tags,
      status: 'approved'
    }
    
    await request({
      url: '/resource/upload',
      method: 'post',
      data
    })
    
    ElMessage.success('资源上传成功')
    uploadDialogVisible.value = false
    loadResources()
  } catch (error) {
    ElMessage.error('上传失败: ' + (error.message || '未知错误'))
    console.error(error)
  }
}

const previewResource = (resource) => {
  if (!canPreview(resource.name)) {
    ElMessage.warning('此文件类型不支持在线预览,请下载后查看')
    return
  }
  
  currentResource.value = resource
  const ext = getFileExtension(resource.name)
  
  if (['mp4', 'webm', 'ogg'].includes(ext)) {
    // 视频预览
    previewType.value = 'video'
    previewUrl.value = normalizeResourceAccessUrl(resource.fileUrl)
    previewTitle.value = resource.name
    previewVisible.value = true
  } else if (['docx', 'pptx'].includes(ext)) {
    // DOCX和PPTX：使用PDF地址做iframe预览
    previewType.value = 'pdf'
    const previewCandidate = /\.pdf([?#].*)?$/i.test(resource.previewUrl || '')
      ? resource.previewUrl
      : resource.fileUrl.replace(/\.(docx|pptx)$/i, '.pdf')
    previewUrl.value = normalizeResourceAccessUrl(previewCandidate)
    previewTitle.value = resource.name
    previewVisible.value = true
  } else if (ext === 'xlsx') {
    ElMessage.info('Excel文档请下载后使用本地软件打开')
    handleDownload(resource)
  } else if (ext === 'pdf') {
    // PDF直接预览
    previewType.value = 'pdf'
    previewUrl.value = normalizeResourceAccessUrl(resource.fileUrl)
    previewTitle.value = resource.name
    previewVisible.value = true
  } else if (['jpg', 'jpeg', 'png', 'gif', 'svg'].includes(ext)) {
    // 图片预览
    previewType.value = 'image'
    previewUrl.value = normalizeResourceAccessUrl(resource.fileUrl)
    previewTitle.value = resource.name
    previewVisible.value = true
  }
}

const editDialogVisible = ref(false)
const editForm = ref({})

const editResource = (resource) => {
  editForm.value = { ...resource }
  editDialogVisible.value = true
}

// 判断是否支持在线编辑
const canEditOnline = (filename) => {
  if (!filename) return false
  const ext = getFileExtension(filename)
  return ['docx', 'doc', 'xlsx', 'xls', 'pptx', 'ppt'].includes(ext)
}

// 打开在线编辑器
const openOnlineEditor = () => {
  if (!editForm.value.fileUrl) {
    ElMessage.warning('该资源没有文件URL，无法在线编辑')
    return
  }
  
  // 关闭编辑对话框
  editDialogVisible.value = false
  
  // 跳转到备课页面，带上文件URL和文件名
  router.push({
    name: 'TeacherPreparation',
    query: {
      fileUrl: editForm.value.fileUrl,
      fileName: editForm.value.name
    }
  })
}

const handleEdit = async () => {
  try {
    await request({
      url: `/resource/update/${editForm.value.id}`,
      method: 'put',
      data: {
        name: editForm.value.name,
        description: editForm.value.description,
        tags: editForm.value.tags
      }
    })
    
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    loadResources()
  } catch (error) {
    ElMessage.error('更新失败')
    console.error(error)
  }
}

const deleteResource = (resource) => {
  ElMessageBox.confirm(`确定要删除资源《${resource.name}》吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request({
        url: `/resource/delete/${resource.id}`,
        method: 'delete'
      })
      ElMessage.success('删除成功')
      loadResources()
    } catch (error) {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }).catch(() => {
    // 取消删除
  })
}

onMounted(async () => {
  // 先加载课程，再加载资源（loadMyCourses内部会调用loadResources）
  await loadMyCourses()
})
</script>

<style scoped>
.resources-page {
  padding: 24px;
  background-color: var(--bg-primary);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
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

:deep(.el-menu) {
  background-color: var(--bg-float);
  border-right: 1px solid var(--border);
}

:deep(.el-menu-item) {
  color: var(--text-primary);
}

:deep(.el-menu-item:hover),
:deep(.el-menu-item.is-active) {
  background-color: var(--bg-secondary);
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

:deep(.el-table__row:hover > td) {
  background-color: var(--bg-secondary) !important;
}

:deep(.el-pagination) {
  color: var(--text-primary);
}

:deep(.el-dialog) {
  background-color: var(--bg-float);
}

:deep(.el-dialog__header) {
  background-color: var(--bg-secondary);
  color: var(--text-primary);
}

:deep(.el-dialog__body) {
  background-color: var(--bg-float);
}

:deep(.el-input__wrapper), :deep(.el-textarea__inner) {
  background-color: var(--bg-secondary);
  color: var(--text-primary);
}

:deep(.el-select__wrapper) {
  background-color: var(--bg-secondary);
}

.preview-container {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.no-preview {
  text-align: center;
  padding: 60px 20px;
}

.no-preview .el-icon {
  color: var(--text-secondary);
  margin-bottom: 16px;
}

.no-preview p {
  color: var(--text-secondary);
  margin: 16px 0 24px;
  font-size: 14px;
}
</style>
