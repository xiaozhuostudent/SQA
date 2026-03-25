<template>
  <div class="logs-container">
    <el-card shadow="never">
      <!-- 搜索筛选 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="用户名">
          <el-input 
            v-model="queryParams.username" 
            placeholder="请输入用户名" 
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-input 
            v-model="queryParams.operation" 
            placeholder="请输入操作类型" 
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="成功" value="success" />
            <el-option label="失败" value="failed" />
          </el-select>
        </el-form-item>
        <el-form-item label="IP地址">
          <el-input 
            v-model="queryParams.ip" 
            placeholder="请输入IP" 
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 380px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮 -->
      <div class="table-actions">
        <el-button 
          type="danger" 
          :disabled="selectedIds.length === 0"
          @click="handleBatchDelete"
        >
          批量删除
        </el-button>
        <el-button type="warning" @click="handleClearAll">清空日志</el-button>
        <el-button type="info" @click="showStats">统计分析</el-button>
      </div>

      <!-- 日志表格 -->
      <el-table 
        :data="logList" 
        style="width: 100%; margin-top: 20px"
        @selection-change="handleSelectionChange"
        v-loading="loading"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="operation" label="操作类型" width="180" show-overflow-tooltip />
        <el-table-column prop="method" label="请求方法" width="250" show-overflow-tooltip />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'success' ? 'success' : 'danger'" size="small">
              {{ row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="location" label="归属地" width="120" />
        <el-table-column label="执行时长" width="100">
          <template #default="{ row }">
            {{ row.executionTime ? row.executionTime + 'ms' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadLogList"
        @current-change="loadLogList"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="日志详情" width="800px">
      <el-descriptions :column="2" border v-if="currentLog">
        <el-descriptions-item label="日志ID">{{ currentLog.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentLog.username || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ currentLog.operation }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentLog.status === 'success' ? 'success' : 'danger'" size="small">
            {{ currentLog.status === 'success' ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="请求方法" :span="2">{{ currentLog.method || '-' }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.ip || '-' }}</el-descriptions-item>
        <el-descriptions-item label="归属地">{{ currentLog.location || '-' }}</el-descriptions-item>
        <el-descriptions-item label="执行时长">
          {{ currentLog.executionTime ? currentLog.executionTime + 'ms' : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ currentLog.createTime }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre class="params-content">{{ formatParams(currentLog.params) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="currentLog.errorMsg">
          <pre class="error-content">{{ currentLog.errorMsg }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 统计分析对话框 -->
    <el-dialog v-model="statsVisible" title="统计分析" width="900px">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="never">
            <template #header>
              <span>操作类型统计（TOP 10）</span>
            </template>
            <el-table :data="operationStats" height="300">
              <el-table-column prop="operation" label="操作类型" />
              <el-table-column prop="count" label="次数" width="100" />
            </el-table>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never">
            <template #header>
              <span>用户操作统计（TOP 10）</span>
            </template>
            <el-table :data="userStats" height="300">
              <el-table-column prop="username" label="用户名" />
              <el-table-column prop="count" label="操作次数" width="100" />
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getLogList, 
  getLogDetail, 
  batchDeleteLogs, 
  clearAllLogs,
  getOperationStats,
  getUserOperationStats
} from '@/api/admin'

const loading = ref(false)
const logList = ref([])
const total = ref(0)
const selectedIds = ref([])
const detailVisible = ref(false)
const statsVisible = ref(false)
const currentLog = ref(null)
const timeRange = ref([])
const operationStats = ref([])
const userStats = ref([])

const queryParams = reactive({
  page: 1,
  size: 10,
  username: '',
  operation: '',
  status: '',
  ip: '',
  startTime: '',
  endTime: ''
})

// 监听时间范围变化
watch(timeRange, (newVal) => {
  if (newVal && newVal.length === 2) {
    queryParams.startTime = newVal[0]
    queryParams.endTime = newVal[1]
  } else {
    queryParams.startTime = ''
    queryParams.endTime = ''
  }
})

// 加载日志列表
const loadLogList = async () => {
  loading.value = true
  try {
    const res = await getLogList(queryParams)
    const data = res.data || res
    if (data.success !== false) {
      const result = data.data || data
      logList.value = result.list || []
      total.value = result.total || 0
    } else {
      ElMessage.error(data.message || '加载失败')
    }
  } catch (error) {
    console.error('加载日志失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.page = 1
  loadLogList()
}

// 重置查询
const resetQuery = () => {
  Object.assign(queryParams, {
    page: 1,
    size: 10,
    username: '',
    operation: '',
    status: '',
    ip: '',
    startTime: '',
    endTime: ''
  })
  timeRange.value = []
  loadLogList()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 查看详情
const viewDetail = async (row) => {
  try {
    const res = await getLogDetail(row.id)
    const data = res.data || res
    if (data.success !== false) {
      currentLog.value = data.data || data
      detailVisible.value = true
    } else {
      ElMessage.error(data.message || '获取详情失败')
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条日志吗？`, '提示', {
      type: 'warning'
    })
    const res = await batchDeleteLogs(selectedIds.value)
    const data = res.data || res
    if (data.success !== false) {
      ElMessage.success('删除成功')
      loadLogList()
    } else {
      ElMessage.error(data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 清空日志
const handleClearAll = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有日志吗？此操作不可恢复！', '警告', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    const res = await clearAllLogs()
    const data = res.data || res
    if (data.success !== false) {
      ElMessage.success('清空成功')
      loadLogList()
    } else {
      ElMessage.error(data.message || '清空失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空失败:', error)
    }
  }
}

// 显示统计
const showStats = async () => {
  try {
    const [opsRes, usersRes] = await Promise.all([
      getOperationStats(),
      getUserOperationStats()
    ])
    
    const opsData = opsRes.data || opsRes
    const usersData = usersRes.data || usersRes
    
    if (opsData.success !== false) {
      operationStats.value = opsData.data || []
    }
    if (usersData.success !== false) {
      userStats.value = usersData.data || []
    }
    
    statsVisible.value = true
  } catch (error) {
    console.error('获取统计失败:', error)
    ElMessage.error('获取统计失败')
  }
}

// 格式化参数
const formatParams = (params) => {
  if (!params) return '-'
  try {
    return JSON.stringify(JSON.parse(params), null, 2)
  } catch {
    return params
  }
}

onMounted(() => {
  loadLogList()
})
</script>

<style scoped>
.logs-container {
  padding: 20px;
}

.search-form {
  margin-bottom: 0;
}

.table-actions {
  margin-bottom: 15px;
}

.params-content,
.error-content {
  max-height: 200px;
  overflow-y: auto;
  padding: 10px;
  background-color: var(--el-fill-color-light);
  border-radius: 4px;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-family: 'Courier New', monospace;
  font-size: 12px;
}

.error-content {
  color: var(--el-color-danger);
}

:deep(.el-table) {
  font-size: 13px;
}

:deep(.el-descriptions__label) {
  font-weight: 600;
}
</style>
