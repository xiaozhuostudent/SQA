<template>
  <div class="log-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ pageTitle }}</span>
          <div>
            <el-button 
              type="primary" 
              :icon="Download" 
              @click="exportLogs"
              v-if="hasPermission('log:export')">
              导出日志
            </el-button>
            <el-button 
              type="danger" 
              :icon="Delete" 
              @click="clearOldLogs"
              v-if="hasPermission('log:delete')">
              清理旧日志
            </el-button>
          </div>
        </div>
      </template>

      <!-- 根据路由参数显示对应的日志表格 -->
      <AdminLogTable 
        v-if="currentLogType === 'admin'" 
        ref="adminLogTable" />
      <TeacherLogTable 
        v-if="currentLogType === 'teacher'" 
        ref="teacherLogTable" />
      <StudentLogTable 
        v-if="currentLogType === 'student'" 
        ref="studentLogTable" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Delete } from '@element-plus/icons-vue'
import { hasPermission } from '@/utils/permission'
import AdminLogTable from './components/AdminLogTable.vue'
import TeacherLogTable from './components/TeacherLogTable.vue'
import StudentLogTable from './components/StudentLogTable.vue'
import { exportAllLogs, clearLogs } from '@/api/log'

const route = useRoute()
const adminLogTable = ref(null)
const teacherLogTable = ref(null)
const studentLogTable = ref(null)

// 从路由meta获取当前日志类型
const currentLogType = computed(() => {
  return route.meta.logType || 'admin'
})

// 页面标题
const pageTitle = computed(() => {
  const titles = {
    admin: '管理员日志',
    teacher: '教师日志',
    student: '学生日志'
  }
  return titles[currentLogType.value] || '系统日志管理'
})

const exportLogs = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要导出当前日志吗？导出可能需要一些时间',
      '确认导出',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await exportAllLogs({ type: currentLogType.value })
    
    // 创建下载链接
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    const typeNames = {
      admin: '管理员',
      teacher: '教师',
      student: '学生'
    }
    link.download = `${typeNames[currentLogType.value]}_日志_${new Date().getTime()}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('日志导出成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('导出日志失败:', error)
      ElMessage.error('导出日志失败')
    }
  }
}

const clearOldLogs = async () => {
  try {
    const { value: days } = await ElMessageBox.prompt(
      '请输入要保留的天数（将删除更早的日志）',
      '清理旧日志',
      {
        confirmButtonText: '确定清理',
        cancelButtonText: '取消',
        inputPattern: /^\d+$/,
        inputErrorMessage: '请输入有效的天数',
        inputValue: '90',
        inputPlaceholder: '例如：90',
        type: 'warning'
      }
    )

    const daysNum = parseInt(days)
    if (daysNum < 30) {
      ElMessage.warning('为保证数据安全，至少需要保留30天的日志')
      return
    }

    await ElMessageBox.confirm(
      `确定要清理 ${daysNum} 天前的旧日志吗？此操作不可恢复！`,
      '最终确认',
      {
        confirmButtonText: '确定清理',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    const res = await clearLogs({ days: daysNum })
    if (res.code === 200) {
      ElMessage.success(`已清理 ${res.data.deletedCount} 条旧日志`)
      // 刷新当前日志表
      if (currentLogType.value === 'admin') {
        adminLogTable.value?.loadLogs()
      } else if (currentLogType.value === 'teacher') {
        teacherLogTable.value?.loadLogs()
      } else if (currentLogType.value === 'student') {
        studentLogTable.value?.loadLogs()
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清理日志失败:', error)
      ElMessage.error('清理日志失败')
    }
  }
}
</script>

<style scoped>
.log-management {
  padding: 24px;
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
</style>
