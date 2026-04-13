<template>
  <div class="course-selection-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span class="title">
            <el-icon><Setting /></el-icon>
            选课管理
          </span>
          <div class="header-actions">
            <el-button 
              type="success" 
              :icon="Check" 
              @click="batchOpen"
              :disabled="selectedCourses.length === 0">
              批量开放选课
            </el-button>
            <el-button 
              type="danger" 
              :icon="Close" 
              @click="batchClose"
              :disabled="selectedCourses.length === 0">
              批量关闭选课
            </el-button>
            <el-button 
              type="primary" 
              :icon="Refresh" 
              @click="loadCourses">
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <div class="filter-section">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索课程名称或课程代码"
          :prefix-icon="Search"
          clearable
          style="width: 300px; margin-right: 10px;"
          @clear="loadCourses"
          @keyup.enter="handleSearch"
        />
        <el-select
          v-model="filterStatus"
          placeholder="选课状态"
          clearable
          style="width: 150px; margin-right: 10px;"
          @change="handleFilter"
        >
          <el-option label="全部" value="" />
          <el-option label="已开放" :value="true" />
          <el-option label="已关闭" :value="false" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      </div>

      <!-- 课程列表 -->
      <el-table
        v-loading="loading"
        :data="filteredCourses"
        style="width: 100%; margin-top: 20px;"
        @selection-change="handleSelectionChange"
        stripe
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="courseCode" label="课程代码" width="120" />
        <el-table-column prop="name" label="课程名称" min-width="200">
          <template #default="{ row }">
            <div class="course-name">
              <el-tag v-if="row.category" size="small" style="margin-right: 8px;">
                {{ row.category }}
              </el-tag>
              {{ row.name }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="teacherName" label="授课教师" width="120" />
        <el-table-column prop="semester" label="学期" width="120" />
        <el-table-column label="选课情况" width="150" align="center">
          <template #default="{ row }">
            <el-progress 
              :percentage="getEnrollmentPercentage(row)" 
              :color="getProgressColor(row)"
            >
              <span class="progress-text">{{ row.enrolled }}/{{ row.capacity }}</span>
            </el-progress>
          </template>
        </el-table-column>
        <el-table-column label="选课状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isOpenForSelection ? 'success' : 'danger'">
              {{ row.isOpenForSelection ? '已开放' : '已关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="!row.isOpenForSelection"
              type="success"
              size="small"
              :icon="Check"
              @click="toggleSelectionStatus(row, true)"
            >
              开放
            </el-button>
            <el-button
              v-else
              type="danger"
              size="small"
              :icon="Close"
              @click="toggleSelectionStatus(row, false)"
            >
              关闭
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 统计信息 -->
      <div class="statistics">
        <el-row :gutter="20" style="margin-top: 20px; ">
          <el-col :span="6">
            <el-statistic title="总课程数" :value="courses.length" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="已开放选课" :value="openedCount" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="已关闭选课" :value="closedCount" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="已选中" :value="selectedCourses.length" />
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Setting, 
  Check, 
  Close, 
  Refresh, 
  Search 
} from '@element-plus/icons-vue'
import { 
  getAllCoursesForAdmin, 
  updateCourseSelectionStatus, 
  batchUpdateCourseSelectionStatus 
} from '@/api/course'

const loading = ref(false)
const courses = ref([])
const selectedCourses = ref([])
const searchKeyword = ref('')
const filterStatus = ref('')

// 加载课程列表
const loadCourses = async () => {
  loading.value = true
  try {
    const res = await getAllCoursesForAdmin()
    if (res.code === 200) {
      courses.value = res.data || []
      ElMessage.success('课程列表加载成功')
    } else {
      ElMessage.error(res.message || '加载课程列表失败')
    }
  } catch (error) {
    console.error('加载课程列表失败:', error)
    ElMessage.error('加载课程列表失败')
  } finally {
    loading.value = false
  }
}

// 过滤后的课程列表
const filteredCourses = computed(() => {
  let result = courses.value

  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(course => 
      course.name?.toLowerCase().includes(keyword) ||
      course.courseCode?.toLowerCase().includes(keyword)
    )
  }

  // 状态过滤
  if (filterStatus.value !== '') {
    result = result.filter(course => course.isOpenForSelection === filterStatus.value)
  }

  return result
})

// 统计
const openedCount = computed(() => 
  courses.value.filter(c => c.isOpenForSelection).length
)

const closedCount = computed(() => 
  courses.value.filter(c => !c.isOpenForSelection).length
)

// 选课百分比
const getEnrollmentPercentage = (course) => {
  if (!course.capacity || course.capacity === 0) return 0
  return Math.round((course.enrolled / course.capacity) * 100)
}

// 进度条颜色
const getProgressColor = (course) => {
  const percentage = getEnrollmentPercentage(course)
  if (percentage >= 90) return '#f56c6c'
  if (percentage >= 70) return '#e6a23c'
  return '#67c23a'
}

// 处理选择变化
const handleSelectionChange = (selection) => {
  selectedCourses.value = selection
}

// 搜索
const handleSearch = () => {
  // 过滤逻辑已在computed中实现
}

// 筛选
const handleFilter = () => {
  // 过滤逻辑已在computed中实现
}

// 切换单个课程的选课状态
const toggleSelectionStatus = async (course, isOpen) => {
  try {
    const action = isOpen ? '开放' : '关闭'
    await ElMessageBox.confirm(
      `确定要${action}课程《${course.name}》的选课吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    const res = await updateCourseSelectionStatus(course.id, isOpen)
    if (res.code === 200) {
      ElMessage.success(res.message || `${action}成功`)
      // 更新本地数据
      course.isOpenForSelection = isOpen
    } else {
      ElMessage.error(res.message || `${action}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新选课状态失败:', error)
      ElMessage.error('操作失败')
    }
  } finally {
    loading.value = false
  }
}

// 批量开放选课
const batchOpen = async () => {
  try {
    const count = selectedCourses.value.length
    await ElMessageBox.confirm(
      `确定要开放 ${count} 门课程的选课吗？`,
      '批量开放选课',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    const courseIds = selectedCourses.value.map(c => c.id)
    const res = await batchUpdateCourseSelectionStatus(courseIds, true)
    if (res.code === 200) {
      ElMessage.success(res.message || '批量开放成功')
      await loadCourses()
    } else {
      ElMessage.error(res.message || '批量开放失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量开放失败:', error)
      ElMessage.error('操作失败')
    }
  } finally {
    loading.value = false
  }
}

// 批量关闭选课
const batchClose = async () => {
  try {
    const count = selectedCourses.value.length
    await ElMessageBox.confirm(
      `确定要关闭 ${count} 门课程的选课吗？学生将无法选择这些课程。`,
      '批量关闭选课',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    const courseIds = selectedCourses.value.map(c => c.id)
    const res = await batchUpdateCourseSelectionStatus(courseIds, false)
    if (res.code === 200) {
      ElMessage.success(res.message || '批量关闭成功')
      await loadCourses()
    } else {
      ElMessage.error(res.message || '批量关闭失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量关闭失败:', error)
      ElMessage.error('操作失败')
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCourses()
})
</script>

<style scoped>
.statistics :deep(.el-statistic__head),
.statistics :deep(.el-statistic__content) {
  color: #ffffff;
}
.course-selection-management {
  padding: 20px;
}

.box-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: bold;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.filter-section {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.course-name {
  display: flex;
  align-items: center;
}

.progress-text {
  font-size: 12px;
}

.statistics {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #d2def300;
}
</style>
