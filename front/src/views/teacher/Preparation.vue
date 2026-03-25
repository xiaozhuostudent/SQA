<template>
  <div class="preparation-container">
    <!-- 编辑器视图 -->
    <div v-if="currentTab === 'editor'" class="editor-view">
      <div class="editor-header">
        <div class="header-left">
          <el-button @click="backToHome" :icon="Back" size="small">返回首页</el-button>
          <span class="doc-title">{{ currentDocTitle }}</span>
        </div>
        <div class="header-right">
          <el-button 
            v-if="!editorLoaded" 
            type="primary" 
            size="small" 
            @click="retryLoad"
          >
            重新加载
          </el-button>
          <span v-if="connectionStatus !== 'connected'" class="connection-badge" :class="connectionStatus">
            {{ connectionText }}
          </span>
        </div>
      </div>
      <div class="editor-wrapper">
        <div id="onlyoffice-editor" class="editor-container"></div>
        <div v-if="!editorLoaded" class="loading-overlay">
          <el-icon class="is-loading" :size="50" color="#7aa2f7">
            <Loading />
          </el-icon>
          <p>正在加载编辑器...</p>
          <p class="hint">如长时间未加载，请点击"重新加载"或检查OnlyOffice服务</p>
        </div>
      </div>
    </div>

    <!-- 首页视图 -->
    <div v-else class="home-view">
      <div class="home-header">
        <h2> 备课中心</h2>
        <div class="header-actions">
          <el-button type="primary" @click="createDoc('word')" :icon="Document">
            新建 Word
          </el-button>
          <el-button type="success" @click="createDoc('excel')" :icon="Grid">
            新建 Excel
          </el-button>
          <el-button type="warning" @click="createDoc('powerpoint')" :icon="Monitor">
            新建 PPT
          </el-button>
        </div>
      </div>

      <div class="home-content">
        <!-- 快速操作区 -->
        <el-card class="quick-actions-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Star /></el-icon>
              <span>快速开始</span>
            </div>
          </template>
          <div class="quick-grid">
            <div class="quick-item" @click="loadSample('word')">
              <el-icon :size="40" color="#409EFF"><Document /></el-icon>
              <span>示例文档</span>
            </div>
            <div class="quick-item" @click="loadSample('excel')">
              <el-icon :size="40" color="#67C23A"><Grid /></el-icon>
              <span>示例表格</span>
            </div>
            <div class="quick-item" @click="loadSample('ppt')">
              <el-icon :size="40" color="#E6A23C"><Monitor /></el-icon>
              <span>示例课件</span>
            </div>
          </div>
        </el-card>

        <!-- 最近编辑 -->
        <el-card class="recent-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Clock /></el-icon>
              <span>最近编辑</span>
              <el-button 
                v-if="recentDocs.length > 0" 
                link 
                type="danger" 
                size="small"
                @click="clearHistory"
              >
                清空记录
              </el-button>
            </div>
          </template>
          
          <div v-if="recentDocs.length === 0" class="empty-state">
            <el-icon :size="60" color="#909399"><FolderOpened /></el-icon>
            <p>暂无编辑记录</p>
          </div>

          <div v-else class="recent-grid">
            <div 
              v-for="doc in recentDocs" 
              :key="doc.id"
              class="doc-card"
              @click="openDocument(doc)"
            >
              <div class="doc-icon">
                <el-icon :size="50" :color="getDocColor(doc.type)">
                  <component :is="getDocIcon(doc.type)" />
                </el-icon>
              </div>
              <div class="doc-info">
                <div class="doc-name">{{ doc.title }}</div>
                <div class="doc-meta">
                  <span class="doc-type">{{ getDocTypeLabel(doc.type) }}</span>
                  <span class="doc-time">{{ formatTime(doc.time) }}</span>
                </div>
              </div>
              <el-button 
                class="delete-btn"
                :icon="Delete" 
                circle 
                size="small" 
                type="danger"
                @click.stop="removeFromHistory(doc.id)"
              />
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Setting, Document, Grid, Monitor, EditPen, Back, 
  Star, Clock, FolderOpened, Delete, Loading
} from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()
const currentTab = ref('home') // 'home' | 'editor'
const currentDocTitle = ref('')
const connectionStatus = ref('disconnected')
const connectionText = ref('检测中...')
const recentDocs = ref([])
const editorLoaded = ref(false)
const currentEditorConfig = ref(null)

const serverConfig = ref({
  url: 'http://127.0.0.1:8081'
})

// 获取当前用户信息
const getCurrentUser = () => {
  const userInfo = userStore.userInfo
  return {
    id: String(userInfo.id || Date.now()),
    name: userInfo.realName || userInfo.username || '用户'
  }
}

// 生成文档key（相同URL生成相同key，实现协同编辑）
const generateDocKey = (url) => {
  // 使用简单的哈希函数生成固定key
  let hash = 0
  for (let i = 0; i < url.length; i++) {
    const char = url.charCodeAt(i)
    hash = ((hash << 5) - hash) + char
    hash = hash & hash // Convert to 32bit integer
  }
  return 'doc_' + Math.abs(hash).toString(36)
}

const sampleDocs = {
  word: 'http://120.26.212.210/resources/testpaper/Java程序设计A卷.docx',
  excel: 'https://www.learningcontainer.com/wp-content/uploads/2019/09/sample-xlsx-file-for-testing.xlsx',
  ppt: 'http://120.26.212.210/resources/ppt/Java程序设计课件.pptx'
}

// 新建文档使用的模板（包含基础内容的轻量级模板）
const templateDocs = {
  word: 'http://120.26.212.210/resources/testpaper/新建word.docx',
  excel: 'http://120.26.212.210/resources/testpaper/新建excel.xlsx',
  powerpoint: 'http://120.26.212.210/resources/ppt/新建ppt.pptx',
  ppt: 'http://120.26.212.210/resources/ppt/新建ppt.pptx'  // 兼容两种写法
}

// 从localStorage加载历史记录
const loadHistory = () => {
  try {
    const history = localStorage.getItem('onlyoffice_recent_docs')
    if (history) {
      recentDocs.value = JSON.parse(history)
    }
  } catch (error) {
    console.error('加载历史记录失败:', error)
  }
}

// 保存到历史记录
const saveToHistory = (doc) => {
  try {
    const existing = recentDocs.value.findIndex(d => d.url === doc.url)
    if (existing !== -1) {
      recentDocs.value.splice(existing, 1)
    }
    
    recentDocs.value.unshift({
      id: Date.now(),
      title: doc.title,
      url: doc.url,
      type: doc.type,
      time: new Date().toISOString()
    })
    
    // 只保留最近10条
    if (recentDocs.value.length > 10) {
      recentDocs.value = recentDocs.value.slice(0, 10)
    }
    
    localStorage.setItem('onlyoffice_recent_docs', JSON.stringify(recentDocs.value))
  } catch (error) {
    console.error('保存历史记录失败:', error)
  }
}

// 从历史记录中删除
const removeFromHistory = (id) => {
  recentDocs.value = recentDocs.value.filter(d => d.id !== id)
  localStorage.setItem('onlyoffice_recent_docs', JSON.stringify(recentDocs.value))
  ElMessage.success('已删除')
}

// 清空历史记录
const clearHistory = () => {
  ElMessageBox.confirm('确定要清空所有历史记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    recentDocs.value = []
    localStorage.removeItem('onlyoffice_recent_docs')
    ElMessage.success('已清空历史记录')
  }).catch(() => {})
}

// 格式化时间
const formatTime = (time) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  
  return date.toLocaleDateString()
}

// 获取文档类型标签
const getDocTypeLabel = (type) => {
  const labels = {
    word: 'Word',
    cell: 'Excel',
    slide: 'PPT'
  }
  return labels[type] || '文档'
}

// 获取文档图标
const getDocIcon = (type) => {
  const icons = {
    word: Document,
    cell: Grid,
    slide: Monitor
  }
  return icons[type] || Document
}

// 获取文档颜色
const getDocColor = (type) => {
  const colors = {
    word: '#409EFF',
    cell: '#67C23A',
    slide: '#E6A23C'
  }
  return colors[type] || '#909399'
}

// 返回首页
const backToHome = () => {
  currentTab.value = 'home'
  currentDocTitle.value = ''
  editorLoaded.value = false
  currentEditorConfig.value = null
  const editorDiv = document.getElementById('onlyoffice-editor')
  if (editorDiv) {
    editorDiv.innerHTML = ''
  }
}

// 重新加载编辑器
const retryLoad = () => {
  if (currentEditorConfig.value) {
    console.log('重新加载编辑器...')
    editorLoaded.value = false
    createEditor(currentEditorConfig.value)
  }
}

// 打开文档
const openDocument = async (doc) => {
  const user = getCurrentUser()
  const docKey = generateDocKey(doc.url)
  
  const config = {
    document: {
      fileType: doc.type === 'word' ? 'docx' : doc.type === 'cell' ? 'xlsx' : 'pptx',
      key: docKey, // 使用固定key实现协同编辑
      title: doc.title,
      url: doc.url,
      permissions: {
        edit: true,
        download: true,
        print: true,
        comment: true,
        review: true
      }
    },
    documentType: doc.type,
    editorConfig: {
      lang: 'zh-CN',
      mode: 'edit',
      user: user, // 使用真实用户信息
      coEditing: {
        mode: 'fast', // 快速协作模式
        change: true   // 允许实时查看其他用户的更改
      }
    }
  }
  
  currentDocTitle.value = doc.title
  currentTab.value = 'editor'
  
  // 等待 DOM 更新完成后再创建编辑器
  await nextTick()
  createEditor(config)
}

const testConnection = async () => {
  try {
    const response = await fetch(serverConfig.value.url + '/welcome/', {
      method: 'GET',
      mode: 'no-cors'
    })
    connectionStatus.value = 'connected'
    connectionText.value = '已连接'
  } catch (error) {
    connectionStatus.value = 'disconnected'
    connectionText.value = '未连接'
  }
}

const createEditor = (config) => {
  const editorDiv = document.getElementById('onlyoffice-editor')
  if (!editorDiv) {
    console.error('编辑器容器未找到')
    ElMessage.error('编辑器容器未找到')
    return
  }
  
  // 保存配置以便重新加载
  currentEditorConfig.value = config
  editorDiv.innerHTML = ''
  
  console.log('正在加载编辑器，配置:', config)
  
  if (!window.DocsAPI) {
    console.log('加载 OnlyOffice API 脚本...')
    const script = document.createElement('script')
    script.src = serverConfig.value.url + '/web-apps/apps/api/documents/api.js'
    script.onload = () => {
      console.log('OnlyOffice API 脚本加载成功')
      setTimeout(() => initEditor(config), 100)
    }
    script.onerror = (error) => {
      console.error('无法加载 OnlyOffice API 脚本:', error)
      ElMessage.error('无法加载编辑器，请检查 OnlyOffice 服务是否启动')
      editorLoaded.value = false
      backToHome()
    }
    document.head.appendChild(script)
  } else {
    console.log('使用已加载的 OnlyOffice API')
    setTimeout(() => initEditor(config), 100)
  }
}

const initEditor = (config) => {
  try {
    console.log('初始化编辑器，原始配置:', config)
    
    // 确保配置完整
    if (!config.editorConfig) {
      config.editorConfig = {}
    }
    
    // 启用协同编辑功能
    config.editorConfig.customization = {
      ...config.editorConfig.customization,
      uiTheme: 'theme-dark',
      chat: true,           // 启用聊天功能
      comments: true,       // 启用评论
      compactHeader: false,
      compactToolbar: false,
      help: false,
      hideRightMenu: false,
      toolbar: true,
      autosave: true,       // 启用自动保存
      forcesave: false
    }
    
    // 确保协同编辑配置存在
    if (!config.editorConfig.coEditing) {
      config.editorConfig.coEditing = {
        mode: 'fast',  // 快速模式：实时协作
        change: true   // 显示其他用户的更改
      }
    }
    
    // 设置编辑器尺寸 - 必须设置！
    config.width = '100%'
    config.height = '100%'
    
    console.log('最终配置:', config)
    console.log('文档Key:', config.document.key, '- 相同Key的用户可以协同编辑')
    console.log('用户信息:', config.editorConfig.user)
    console.log('协同编辑模式:', config.editorConfig.coEditing)
    
    const editor = new window.DocsAPI.DocEditor('onlyoffice-editor', config)
    console.log('编辑器实例创建成功:', editor)
    editorLoaded.value = true
    ElMessage.success('编辑器加载成功 - 支持多人协同编辑')
  } catch (e) {
    console.error('编辑器初始化失败:', e)
    editorLoaded.value = false
    ElMessage.error('编辑器初始化失败: ' + e.message)
    backToHome()
  }
}

const createDoc = (type) => {
  const typeConfig = {
    word: { fileName: '新建文档.docx', docType: 'word', fileType: 'docx' },
    excel: { fileName: '新建表格.xlsx', docType: 'cell', fileType: 'xlsx' },
    powerpoint: { fileName: '新建演示.pptx', docType: 'slide', fileType: 'pptx' }
  }
  
  const cfg = typeConfig[type]
  
  if (!cfg) {
    ElMessage.error('不支持的文档类型')
    return
  }
  
  // 使用模板文档（包含基础内容）创建新文档
  const templateUrl = templateDocs[type]
  
  if (!templateUrl) {
    ElMessage.error('未找到模板文档')
    return
  }
  
  const user = getCurrentUser()
  const docKey = generateDocKey(templateUrl)
  
  const config = {
    document: {
      fileType: cfg.fileType,
      key: docKey, // 使用固定key
      title: cfg.fileName,
      url: templateUrl,
      permissions: {
        edit: true,
        download: true,
        print: true,
        comment: true,
        review: true
      }
    },
    documentType: cfg.docType,
    editorConfig: {
      lang: 'zh-CN',
      mode: 'edit',
      user: user, // 使用真实用户信息
      coEditing: {
        mode: 'fast',
        change: true
      }
    }
  }
  
  currentDocTitle.value = cfg.fileName
  currentTab.value = 'editor'
  
  // 保存到历史记录
  saveToHistory({
    title: cfg.fileName,
    url: templateUrl,
    type: cfg.docType
  })
  
  // 等待 DOM 更新完成后再创建编辑器
  nextTick(() => {
    createEditor(config)
  })
}

const loadSample = (type) => {
  const typeConfig = {
    word: { fileType: 'docx', title: 'Java程序设计A卷.docx', docType: 'word', url: sampleDocs.word },
    excel: { fileType: 'xlsx', title: '示例表格.xlsx', docType: 'cell', url: sampleDocs.excel },
    ppt: { fileType: 'pptx', title: 'Java课件.pptx', docType: 'slide', url: sampleDocs.ppt }
  }
  
  const cfg = typeConfig[type]
  const user = getCurrentUser()
  const docKey = generateDocKey(cfg.url)
  
  const config = {
    document: {
      fileType: cfg.fileType,
      key: docKey, // 使用固定key
      title: cfg.title,
      url: cfg.url,
      permissions: {
        edit: true,
        download: true,
        print: true,
        comment: true,
        review: true
      }
    },
    documentType: cfg.docType,
    editorConfig: {
      lang: 'zh-CN',
      mode: 'edit',
      user: user, // 使用真实用户信息
      coEditing: {
        mode: 'fast',
        change: true
      }
    }
  }
  
  currentDocTitle.value = cfg.title
  currentTab.value = 'editor'
  
  // 保存到历史记录
  saveToHistory({
    title: cfg.title,
    url: cfg.url,
    type: cfg.docType
  })
  
  // 等待 DOM 更新完成后再创建编辑器
  nextTick(() => {
    createEditor(config)
  })
}

// 从URL参数加载文件并打开编辑器
const loadFromUrl = () => {
  const fileUrl = route.query.fileUrl
  const fileName = route.query.fileName
  
  if (fileUrl && fileName) {
    const ext = fileName.split('.').pop().toLowerCase()
    let docType, fileType
    
    if (ext === 'docx' || ext === 'doc') {
      docType = 'word'
      fileType = 'docx'
    } else if (ext === 'xlsx' || ext === 'xls') {
      docType = 'cell'
      fileType = 'xlsx'
    } else if (ext === 'pptx' || ext === 'ppt') {
      docType = 'slide'
      fileType = 'pptx'
    } else {
      ElMessage.error('不支持的文件类型')
      return
    }
    
    const user = getCurrentUser()
    const docKey = generateDocKey(fileUrl)
    
    const config = {
      document: {
        fileType: fileType,
        key: docKey, // 使用固定key
        title: fileName,
        url: fileUrl,
        permissions: {
          edit: true,
          download: true,
          print: true,
          comment: true,
          review: true
        }
      },
      documentType: docType,
      editorConfig: {
        lang: 'zh-CN',
        mode: 'edit',
        user: user, // 使用真实用户信息
        coEditing: {
          mode: 'fast',
          change: true
        }
      }
    }
    
    currentDocTitle.value = fileName
    currentTab.value = 'editor'
    
    // 保存到历史记录
    saveToHistory({
      title: fileName,
      url: fileUrl,
      type: docType
    })
    
    ElMessage.success('正在加载文档...')
    
    // 等待 DOM 更新完成后再创建编辑器
    nextTick(() => {
      createEditor(config)
    })
  }
}

onMounted(() => {
  testConnection()
  loadHistory()
  
  // 检查是否有URL参数
  setTimeout(() => {
    loadFromUrl()
  }, 500)
})
</script>

<style scoped>
/* 容器基础样式 */
.preparation-container {
  width: 100%;
  height: 100%;
  overflow: hidden;
}

/* 编辑器视图 */
.editor-view {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.editor-header {
  padding: 12px 20px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.doc-title {
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
}

.connection-badge {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.connection-badge.connected {
  background: rgba(158, 206, 106, 0.15);
  color: var(--accent-green);
  border: 1px solid var(--accent-green);
}

.connection-badge.disconnected {
  background: rgba(247, 118, 142, 0.15);
  color: var(--accent-red);
  border: 1px solid var(--accent-red);
}

.editor-wrapper {
  flex: 1;
  overflow: hidden;
  position: relative;
  min-height: 0;
}

.editor-container {
  width: 100%;
  height: 100%;
}

/* 首页视图 */
.home-view {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.home-header {
  padding: 24px 32px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
}

.home-header h2 {
  color: var(--text-primary);
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 20px 0;
}

.header-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.home-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px 32px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: var(--text-primary);
}

.card-header .el-icon {
  color: var(--accent-cyan);
}

/* 快速操作卡片 */
.quick-actions-card {
  margin-bottom: 24px;
  background: var(--bg-secondary) !important;
  border: 1px solid var(--border) !important;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 16px;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: var(--bg-float);
  border: 1px solid var(--border);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.quick-item:hover {
  background: var(--bg-highlight);
  border-color: var(--accent-cyan);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(125, 207, 255, 0.2);
}

.quick-item span {
  margin-top: 12px;
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 500;
}

/* 最近编辑卡片 */
.recent-card {
  background: var(--bg-secondary) !important;
  border: 1px solid var(--border) !important;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.empty-state p {
  margin-top: 16px;
  color: var(--text-muted);
  font-size: 14px;
}

.recent-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.doc-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: var(--bg-float);
  border: 1px solid var(--border);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.doc-card:hover {
  background: var(--bg-highlight);
  border-color: var(--accent-cyan);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(125, 207, 255, 0.2);
}

.doc-card:hover .delete-btn {
  opacity: 1;
}

.doc-icon {
  flex-shrink: 0;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-primary);
  border-radius: 8px;
}

.doc-info {
  flex: 1;
  min-width: 0;
}

.doc-name {
  color: var(--text-primary);
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.doc-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
}

.doc-type {
  padding: 2px 8px;
  background: var(--bg-highlight);
  color: var(--accent-purple);
  border-radius: 4px;
  font-weight: 500;
}

.doc-time {
  color: var(--text-muted);
}

.delete-btn {
  opacity: 0;
  transition: opacity 0.3s;
}

:deep(.el-card__header) {
  background: var(--bg-float);
  border-bottom: 1px solid var(--border);
  padding: 16px 20px;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-empty__description p) {
  color: var(--text-muted);
}

/* 加载覆盖层 */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(26, 27, 38, 0.95);
  z-index: 1000;
  gap: 16px;
}

.loading-overlay p {
  color: var(--text-primary);
  font-size: 16px;
  margin: 0;
}

.loading-overlay .hint {
  font-size: 14px;
  color: var(--text-muted);
}
</style>
