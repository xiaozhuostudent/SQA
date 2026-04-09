<template>
  <div class="resources-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学习资源</span>
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
              style="width: 300px"
              clearable
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            />
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
            :data="resources" 
            v-loading="loading"
            style="width: 100%"
          >
            <el-table-column prop="name" label="资源名称" min-width="250" show-overflow-tooltip />
            <el-table-column prop="courseName" label="所属课程" width="150" />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="scope">
                <el-tag :type="getTypeTagType(scope.row.type)">
                  {{ getTypeLabel(scope.row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="fileSize" label="大小" width="100">
              <template #default="scope">
                {{ formatFileSize(scope.row.fileSize) }}
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="上传时间" width="180" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <el-button 
                  size="small" 
                  @click="previewResource(scope.row)"
                  :disabled="!canPreview(scope.row.name)"
                >
                  预览
                </el-button>
                <el-button 
                  size="small" 
                  type="primary" 
                  @click="handleDownload(scope.row)"
                >
                  下载
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-if="total > 0"
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
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
  </div>
</template>

<script setup>
/**
 * 学生端资源管理
 * 
 * 权限说明：
 * - 学生：只能查看和预览已审核的资源、下载资源，无法上传或删除
 * - 教师：可以在教师端管理自己的资源
 * - 管理员：可以在管理员端管理所有资源
 */
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getResourceList } from '@/api/resource'
import { getMyCourses } from '@/api/course'
import { 
  getResourceUrl, 
  getFileExtension, 
  formatFileSize, 
  canPreview,
  getDocumentPreviewUrl as getPreviewUrl,
  downloadResource as downloadFile
} from '@/utils/resource'

const searchKeyword = ref('')
const activeCategory = ref('all')
const resources = ref([])
const allResources = ref([]) // 存储所有资源
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedCourseId = ref(null)
const myCourses = ref([])

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

const loadResources = async () => {
  loading.value = true
  try {
    // 获取学生ID
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const studentId = userInfo.id
    
    if (!studentId) {
      ElMessage.error('请先登录')
      return
    }
    
    // 首先获取学生的课程
    const coursesResponse = await getMyCourses(studentId)
    
    if (coursesResponse.code !== 200 || !coursesResponse.data) {
      ElMessage.error('获取课程信息失败')
      return
    }
    
    // 保存课程列表到组件变量
    myCourses.value = coursesResponse.data
    const myCourseIds = myCourses.value.map(course => course.id)
    
    if (myCourseIds.length === 0) {
      ElMessage.warning('您还没有选修任何课程')
      resources.value = []
      allResources.value = []
      total.value = 0
      return
    }
    
    // 获取所有资源（不分页）
    const response = await getResourceList({
      page: 1,
      pageSize: 1000, // 获取足够多的数据
      type: activeCategory.value === 'all' ? undefined : activeCategory.value,
      keyword: searchKeyword.value
    })
    
    if (response.code === 200) {
      const allData = response.data.records || response.data
      // 过滤出属于学生课程的资源
      let filteredData = allData.filter(resource => 
        myCourseIds.includes(resource.courseId)
      )
      
      // 如果选择了特定课程，再进行课程筛选
      if (selectedCourseId.value !== null) {
        filteredData = filteredData.filter(resource => 
          resource.courseId === selectedCourseId.value
        )
      }
      
      allResources.value = filteredData.map(item => ({
        ...item,
        fileUrl: normalizeResourceAccessUrl(item.fileUrl),
        previewUrl: normalizeResourceAccessUrl(item.previewUrl)
      }))
      total.value = filteredData.length
      
      // 前端分页
      const start = (currentPage.value - 1) * pageSize.value
      const end = start + pageSize.value
      resources.value = allResources.value.slice(start, end)
    } else {
      ElMessage.error(response.message || '加载资源失败')
    }
  } catch (error) {
    console.error('加载资源失败:', error)
    ElMessage.error('加载资源失败,请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleCourseChange = () => {
  currentPage.value = 1  // 切换课程时重置到第1页
  loadResources()  // 重新加载数据
}

const handleCategoryChange = (key) => {
  activeCategory.value = key
  currentPage.value = 1  // 切换分类时重置到第1页
  loadResources()  // 重新加载数据
}

const handleSearch = () => {
  currentPage.value = 1  // 搜索时重置到第1页
  loadResources()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  // 使用前端分页
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  resources.value = allResources.value.slice(start, end)
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  // 使用前端分页
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  resources.value = allResources.value.slice(start, end)
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
    // DOCX和PPTX：仅使用PDF地址做iframe预览
    previewType.value = 'pdf'
    const previewCandidate = /\.pdf([?#].*)?$/i.test(resource.previewUrl || '')
      ? resource.previewUrl
      : resource.fileUrl.replace(/\.(docx|pptx)$/i, '.pdf')
    previewUrl.value = normalizeResourceAccessUrl(previewCandidate)
    previewTitle.value = resource.name
    previewVisible.value = true
  } else if (ext === 'xlsx') {
    // XLSX暂不支持在线预览，直接下载
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

const getDocumentPreviewUrl = () => {
  return getPreviewUrl(currentResource.value.fileUrl, currentResource.value.name)
}

const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
}

const handleDownload = (resource) => {
  const success = downloadFile(normalizeResourceAccessUrl(resource.fileUrl), resource.name)
  if (success) {
    ElMessage.success(`开始下载: ${resource.name}`)
    // 可选: 调用后端API增加下载次数
    // updateDownloadCount(resource.id)
  } else {
    ElMessage.error('下载失败,请稍后重试')
  }
}

onMounted(() => {
  loadResources()
})
</script>

<style scoped>
.resources-page {
  padding: 0;
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
  margin-bottom: 20px;
  color: var(--el-text-color-secondary);
}

.no-preview p {
  margin: 20px 0;
  color: var(--el-text-color-regular);
  font-size: 16px;
}

.resources-page ::v-deep .el-card {
  background: var(--bg-float);
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
}

.resources-page ::v-deep .el-card__header {
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border);
  color: var(--text-primary);
}

.resources-page ::v-deep .el-menu {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
}

.resources-page ::v-deep .el-menu-item {
  color: var(--text-primary);
}

.resources-page ::v-deep .el-menu-item:hover {
  background: var(--bg-highlight);
  color: var(--accent-cyan);
}

.resources-page ::v-deep .el-menu-item.is-active {
  background: var(--bg-highlight);
  color: var(--accent-cyan);
}

.resources-page ::v-deep .el-table {
  background: transparent;
  color: var(--text-primary);
}

.resources-page ::v-deep .el-table th {
  background: var(--bg-secondary);
  color: var(--text-primary);
  border-color: var(--border);
}

.resources-page ::v-deep .el-table td {
  border-color: var(--border);
}

.resources-page ::v-deep .el-table tr:hover > td {
  background: var(--bg-highlight) !important;
}

.resources-page ::v-deep .el-input__wrapper {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  box-shadow: none;
}

.resources-page ::v-deep .el-input__inner {
  color: var(--text-primary);
}

.resources-page ::v-deep .el-pagination {
  display: flex;
  justify-content: flex-end;
}

.resources-page ::v-deep .el-pagination button,
.resources-page ::v-deep .el-pagination .el-pager li {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.resources-page ::v-deep .el-pagination button:hover,
.resources-page ::v-deep .el-pagination .el-pager li:hover {
  color: var(--accent-cyan);
}

.resources-page ::v-deep .el-pagination .el-pager li.is-active {
  background: var(--accent-cyan);
  color: white;
}

.resources-page ::v-deep .el-dialog {
  background: var(--bg-float);
}

.resources-page ::v-deep .el-dialog__header {
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border);
}

.resources-page ::v-deep .el-dialog__body {
  background: var(--bg-float);
}
</style>
