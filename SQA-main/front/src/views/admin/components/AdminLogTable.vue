<template>
  <div class="admin-log-table">
    <!-- 操作类型筛选 -->
    <div class="operation-filter">
      <span class="filter-label">操作类型</span>
      <el-radio-group v-model="operationType" @change="handleOperationChange">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="新增">新增</el-radio-button>
        <el-radio-button label="修改">修改</el-radio-button>
        <el-radio-button label="删除">删除</el-radio-button>
        <el-radio-button label="查询">查询</el-radio-button>
        <el-radio-button label="登录">登录</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 搜索栏 -->
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="操作人">
        <el-input v-model="searchForm.adminName" placeholder="管理员姓名" clearable style="width: 150px" />
      </el-form-item>
      <el-form-item label="操作模块">
        <el-select v-model="searchForm.module" placeholder="全部" clearable style="width: 160px">
          <el-option label="全部" value="" />
          <el-option
            v-for="module in moduleOptions"
            :key="module"
            :label="module"
            :value="module"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="时间范围">
        <el-date-picker
          v-model="searchForm.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 260px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadLogs">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 日志表格 -->
    <el-table 
      :data="logs" 
      v-loading="loading"
      stripe
      style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="adminName" label="操作人" width="120" />
      <el-table-column prop="operation" label="操作类型" min-width="150" />
      <el-table-column prop="module" label="操作模块" width="120">
        <template #default="scope">
          <el-tag size="small">{{ scope.row.module }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="requestUrl" label="请求路径" min-width="250" show-overflow-tooltip />
      <el-table-column prop="ipAddress" label="IP地址" width="140" />
      <el-table-column prop="operationTime" label="操作时间" width="180" />
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="loadLogs"
      @current-change="loadLogs"
      style="margin-top: 20px; justify-content: flex-end;"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAdminLogs } from '@/api/log'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const logs = ref([])
const operationType = ref('')
const DEFAULT_MODULES = [
  '用户管理',
  '教师管理',
  '学生管理',
  '资源管理',
  '系统管理',
  '认证授权'
]
const moduleOptions = ref([...DEFAULT_MODULES])

const searchForm = reactive({
  adminName: '',
  operation: '',
  module: '',
  dateRange: null
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})



const syncModuleOptions = (records = []) => {
  const set = new Set(moduleOptions.value)
  records.forEach(record => {
    if (record?.module) {
      set.add(record.module)
    }
  })
  moduleOptions.value = Array.from(set)
}

const loadLogs = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      adminName: searchForm.adminName,
      operation: operationType.value || searchForm.operation,
      module: searchForm.module
    }
    
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    
    const res = await getAdminLogs(params)
    logs.value = res.data.records
    pagination.total = res.data.total
    syncModuleOptions(res.data.records)
  } catch (error) {
    console.error('加载管理员日志失败:', error)
    ElMessage.error('加载管理员日志失败: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

const handleOperationChange = () => {
  pagination.page = 1
  loadLogs()
}

const resetSearch = () => {
  searchForm.adminName = ''
  searchForm.operation = ''
  searchForm.module = ''
  searchForm.dateRange = null
  operationType.value = ''
  pagination.page = 1
  loadLogs()
}

onMounted(() => {
  loadLogs()
})

// 暴露方法给父组件
defineExpose({
  loadLogs
})
</script>

<style scoped>
.admin-log-table {
  padding: 20px;
}

.operation-filter {
  margin-bottom: 16px;
  padding: 14px 20px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-label {
  color: #d8dee9;
  font-size: 14px;
}

.operation-filter :deep(.el-radio-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.operation-filter :deep(.el-radio-button__inner) {
  background: transparent;
  border-color: rgba(255, 255, 255, 0.2);
  color: #e5eaf3;
  min-width: 72px;
  text-align: center;
  transition: all 0.2s ease;
}

.operation-filter :deep(.el-radio-button__inner:hover) {
  border-color: rgba(79, 139, 255, 0.6);
}

.operation-filter :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #4f8bff, #7c5dff);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 6px 16px rgba(79, 139, 255, 0.28);
}

.search-form {
  margin-bottom: 20px;
  padding: 18px 20px 6px;
  border-radius: 12px;
  background: rgba(10, 15, 35, 0.65);
  border: 1px solid rgba(255, 255, 255, 0.06);
  column-gap: 18px;
  row-gap: 12px;
}

.search-form :deep(.el-form-item__label) {
  color: #cfd3dc;
}

.search-form :deep(.el-input__wrapper),
.search-form :deep(.el-select__wrapper),
.search-form :deep(.el-range-editor.el-input__wrapper) {
  background: rgba(15, 23, 42, 0.6);
  border-radius: 10px;
  border-color: transparent;
  box-shadow: none;
}

.search-form :deep(.el-input__wrapper:hover),
.search-form :deep(.el-select__wrapper:hover),
.search-form :deep(.el-range-editor.el-input__wrapper:hover) {
  border-color: rgba(79, 139, 255, 0.6);
}


</style>
