<template>
  <div class="experiment-problems-page">
    <el-card>
      <template #header>
        <div class="header-actions">
          <span>{{ experimentTitle }} - 题目列表</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <!-- 实验资源区域 -->
      <div v-if="experimentResources && experimentResources.length > 0" class="resources-section">
        <el-divider content-position="left">
          <span style="font-size: 16px; font-weight: bold;">实验资源</span>
        </el-divider>
        <div class="resources-list">
          <div 
            v-for="(file, index) in experimentResources" 
            :key="index"
            class="resource-item"
          >
            <el-icon class="file-icon"><Document /></el-icon>
            <span class="file-name">{{ file.name }}</span>
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
      </div>

      <el-divider content-position="left" v-if="experimentResources && experimentResources.length > 0">
        <span style="font-size: 16px; font-weight: bold;">题目列表</span>
      </el-divider>

      <el-table :data="problems" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="题目名称">
          <template #default="scope">
            <el-link type="primary" @click="goToProblem(scope.row.id)">{{ scope.row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="100">
          <template #default="scope">
            <el-tag :type="getDifficultyType(scope.row.difficulty)">{{ scope.row.difficulty }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分数" width="100" />
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 'passed'" type="success">已通过</el-tag>
            <el-tag v-else-if="scope.row.status === 'failed'" type="danger">未通过</el-tag>
            <el-tag v-else type="info">未提交</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" type="primary" @click="goToProblem(scope.row.id)">
              做题
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getExperimentProblems, getExperimentDetail } from '@/api/experiment'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const experimentId = route.params.id
const experimentTitle = ref('')
const experimentResources = ref([])
const problems = ref([])
const loading = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    // Get experiment info
    const expRes = await getExperimentDetail(experimentId)
    if (expRes.code === 200) {
      experimentTitle.value = expRes.data.title
      
      // 解析实验资源
      if (expRes.data.resources) {
        try {
          if (Array.isArray(expRes.data.resources)) {
            experimentResources.value = expRes.data.resources
          } else if (typeof expRes.data.resources === 'string') {
            experimentResources.value = JSON.parse(expRes.data.resources)
          }
        } catch (e) {
          console.error('解析实验资源失败:', e)
          experimentResources.value = []
        }
      }
    }
    
    // Get problems
    const probRes = await getExperimentProblems(experimentId)
    if (probRes.code === 200) {
      problems.value = probRes.data
    }
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const downloadFile = (url) => {
  window.open(url, '_blank')
}

const getDifficultyType = (diff) => {
  const map = {
    'easy': 'success',
    'medium': 'warning',
    'hard': 'danger'
  }
  return map[diff] || 'info'
}

const goToProblem = (problemId) => {
  console.log('点击做题按钮, experimentId:', experimentId, 'problemId:', problemId)
  const targetPath = `/student/experiment/${experimentId}/problem/${problemId}`
  console.log('跳转到:', targetPath)
  router.push(targetPath)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.experiment-problems-page {
  padding: 20px;
  background-color: var(--bg-primary);
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.resources-section {
  margin-bottom: 20px;
}

.resources-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 20px;
}

.resource-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background-color: var(--bg-secondary);
  border-radius: 6px;
  border: 1px solid var(--border);
  transition: all 0.3s;
}

.resource-item:hover {
  background-color: var(--bg-hover);
  border-color: var(--color-primary);
}

.file-icon {
  font-size: 20px;
  color: var(--color-primary);
}

.file-name {
  flex: 1;
  font-size: 15px;
  color: var(--text-primary);
  font-weight: 500;
}

:deep(.el-card) {
  background-color: var(--bg-float);
  border-color: var(--border);
  color: var(--text-primary);
}

:deep(.el-card__header) {
  background-color: var(--bg-secondary);
  border-bottom-color: var(--border);
}

:deep(.el-table) {
  background-color: transparent;
  color: var(--text-primary);
}

:deep(.el-table th),
:deep(.el-table tr) {
  background-color: transparent;
  color: var(--text-primary);
}

:deep(.el-table td),
:deep(.el-table th.is-leaf) {
  border-bottom-color: var(--border);
}

:deep(.el-button) {
  pointer-events: auto;
  cursor: pointer;
}
</style>
