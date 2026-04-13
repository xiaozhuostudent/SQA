<template>
  <div class="schedule-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>排课管理</span>
          <el-button type="primary" @click="showAddDialog">添加排课</el-button>
        </div>
      </template>

      <!-- 筛选和搜索 -->
      <el-form :inline="true" class="filter-form">
        <el-form-item label="学期">
          <el-select v-model="queryParams.semester" placeholder="请选择学期" style="width: 180px" @change="loadScheduleList">
            <el-option
              v-for="sem in semesterList"
              :key="sem"
              :label="sem"
              :value="sem"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="搜索">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索课程名/教师/教室"
            style="width: 250px"
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button @click="clearSearch">清除高亮</el-button>
        </el-form-item>
      </el-form>

      <!-- 教室-时间课表视图 -->
      <div class="schedule-table" v-if="queryParams.semester">
        <table class="course-table">
          <thead>
            <tr>
              <th class="classroom-header">教室 \ 时间</th>
              <th v-for="slot in timeSlots" :key="slot.key" class="time-header">
                <div>{{ slot.day }}</div>
                <div class="time-range">{{ slot.period }}</div>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="classroom in classrooms" :key="classroom">
              <td class="classroom-cell">{{ classroom }}</td>
              <td 
                v-for="slot in timeSlots" 
                :key="slot.key" 
                class="course-cell"
                :class="{ 'highlight': isHighlighted(classroom, slot) }"
              >
                <div 
                  v-for="schedule in getScheduleForSlot(classroom, slot.dayOfWeek, slot.periodValue)" 
                  :key="schedule.id"
                  class="course-item"
                  :class="{ 'search-highlight': isSearchMatch(schedule) }"
                  @click="editSchedule(schedule)"
                >
                  <div class="course-name">{{ schedule.courseName }}</div>
                  <div class="course-info">{{ schedule.teacherName }}</div>
                  <div class="course-info">第{{ schedule.startWeek }}-{{ schedule.endWeek }}周</div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <el-empty v-else description="请选择学期查看排课" />
    </el-card>

    <!-- 添加/编辑排课对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="600px"
      @close="resetForm"
    >
      <el-form :model="scheduleForm" label-width="100px">
        <el-form-item label="课程">
          <el-select 
            v-model="scheduleForm.courseId" 
            placeholder="请选择课程" 
            style="width: 100%"
            :disabled="isEdit"
            @change="handleCourseChange"
          >
            <el-option
              v-for="course in courseList"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        
        <!-- 显示课程详细信息 -->
        <el-descriptions v-if="selectedCourse" :column="2" border style="margin-bottom: 20px">
          <el-descriptions-item label="课程名称">{{ selectedCourse.name }}</el-descriptions-item>
          <el-descriptions-item label="学分">{{ selectedCourse.credits }}</el-descriptions-item>
          <el-descriptions-item label="课程类型">{{ selectedCourse.courseType }}</el-descriptions-item>
          <el-descriptions-item label="学时">{{ selectedCourse.hours }}</el-descriptions-item>
          <el-descriptions-item label="授课教师" :span="2">{{ selectedCourse.teacherName }}</el-descriptions-item>
        </el-descriptions>

        <el-form-item label="教室">
          <el-input v-model="scheduleForm.classroom" placeholder="请输入教室，如：A101" />
        </el-form-item>
        <el-form-item label="星期">
          <el-select v-model="scheduleForm.dayOfWeek" placeholder="请选择" style="width: 100%">
            <el-option 
              v-for="day in weekDays" 
              :key="day.value" 
              :label="day.label" 
              :value="day.value" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="节次">
          <el-select v-model="scheduleForm.period" placeholder="请选择" style="width: 100%">
            <el-option
              v-for="period in periods"
              :key="period.value"
              :label="`第${period.label}节 (${period.time})`"
              :value="period.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="起始周">
          <el-input-number v-model="scheduleForm.startWeek" :min="1" :max="20" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束周">
          <el-input-number v-model="scheduleForm.endWeek" :min="1" :max="20" style="width: 100%" />
        </el-form-item>
        <el-form-item label="学期">
          <el-select v-model="scheduleForm.semester" placeholder="请选择学期" style="width: 100%">
            <el-option
              v-for="sem in semesterList"
              :key="sem"
              :label="sem"
              :value="sem"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="danger" v-if="isEdit" @click="handleDelete">删除</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { 
  getScheduleList,
  addSchedule, 
  updateSchedule, 
  deleteSchedule,
  getAllSemesters,
  getCourseList
} from '@/api/admin'

const dialogVisible = ref(false)
const dialogTitle = ref('添加排课')
const isEdit = ref(false)
const searchKeyword = ref('')

const queryParams = reactive({
  semester: ''
})

const scheduleForm = reactive({
  id: null,
  courseId: null,
  dayOfWeek: 1,
  period: 1,
  classroom: '',
  startWeek: 1,
  endWeek: 16,
  semester: ''
})

const scheduleList = ref([])
const semesterList = ref([])
const courseList = ref([])
const selectedCourse = ref(null)

// 所有可能的教室列表
const classrooms = ref([])

const weekDays = [
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
  { label: '周六', value: 6 },
  { label: '周日', value: 7 }
]

const periods = [
  { label: '一', value: 1, time: '08:00-09:40' },
  { label: '二', value: 2, time: '10:00-11:40' },
  { label: '三', value: 3, time: '13:30-15:10' },
  { label: '四', value: 4, time: '15:30-17:10' },
  { label: '五', value: 5, time: '19:00-20:40' }
]

// 生成时间槽（横轴）：周一第1节、周一第2节...周日第5节
const timeSlots = computed(() => {
  const slots = []
  weekDays.forEach(day => {
    periods.forEach(period => {
      slots.push({
        key: `${day.value}-${period.value}`,
        day: day.label,
        period: `第${period.label}节\n${period.time}`,
        dayOfWeek: day.value,
        periodValue: period.value
      })
    })
  })
  return slots
})

// 获取指定教室和时间的排课
const getScheduleForSlot = (classroom, dayOfWeek, period) => {
  return scheduleList.value.filter(s => 
    s.classroom === classroom && 
    s.dayOfWeek === dayOfWeek && 
    s.period === period
  )
}

// 搜索匹配检查
const isSearchMatch = (schedule) => {
  if (!searchKeyword.value) return false
  const keyword = searchKeyword.value.toLowerCase()
  return (
    schedule.courseName?.toLowerCase().includes(keyword) ||
    schedule.teacherName?.toLowerCase().includes(keyword) ||
    schedule.classroom?.toLowerCase().includes(keyword)
  )
}

// 单元格高亮检查
const isHighlighted = (classroom, slot) => {
  if (!searchKeyword.value) return false
  const schedules = getScheduleForSlot(classroom, slot.dayOfWeek, slot.periodValue)
  return schedules.some(s => isSearchMatch(s))
}

// 处理搜索
const handleSearch = () => {
  // 搜索时自动高亮，不需要额外逻辑
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
}

// 加载排课列表
const loadScheduleList = async () => {
  if (!queryParams.semester) {
    ElMessage.warning('请选择学期')
    return
  }
  try {
    // 修改这里，将参数包装成 query 对象以符合后端接口要求
    const res = await getScheduleList({ query: { semester: queryParams.semester } })
    const data = res.data || res
    scheduleList.value = data.list || data || []
    
    // 提取所有教室并去重
    const classroomSet = new Set()
    scheduleList.value.forEach(s => {
      if (s.classroom) classroomSet.add(s.classroom)
    })
    classrooms.value = Array.from(classroomSet).sort()
    
    // 如果没有教室，添加默认教室
    if (classrooms.value.length === 0) {
      classrooms.value = ['A101', 'A102', 'A201', 'A202', 'B101', 'B102', 'C101', 'C102']
    }
  } catch (error) {
    console.error('加载排课列表失败:', error)
    ElMessage.error('加载排课列表失败: ' + (error.message || '未知错误'))
    scheduleList.value = []
    classrooms.value = ['A101', 'A102', 'A201', 'A202', 'B101', 'B102', 'C101', 'C102']
  }
}

// 加载学期列表
const loadSemesterList = async () => {
  try {
    const res = await getAllSemesters()
    const data = res.data || res
    semesterList.value = data.list || data || []
    if (semesterList.value.length === 0) {
      semesterList.value = ['2025-2026-1', '2024-2025-2', '2024-2025-1']
    }
    // 默认选择第一个学期
    if (semesterList.value.length > 0 && !queryParams.semester) {
      queryParams.semester = semesterList.value[0]
      // 等待下一个tick再加载排课列表，确保学期参数已设置
      setTimeout(() => {
        loadScheduleList()
      }, 0)
    }
  } catch (error) {
    console.error('加载学期列表失败:', error)
    ElMessage.error('加载学期列表失败: ' + (error.message || '未知错误'))
    semesterList.value = ['2025-2026-1', '2024-2025-2', '2024-2025-1']
    queryParams.semester = semesterList.value[0]
    setTimeout(() => {
      loadScheduleList()
    }, 0)
  }
}

// 加载课程列表
const loadCourseList = async () => {
  try {
    const res = await getCourseList({ page: 1, size: 100 })
    const data = res.data || res
    courseList.value = data.list || data || []
  } catch (error) {
    console.error('加载课程列表失败:', error)
    ElMessage.error('加载课程列表失败: ' + (error.message || '未知错误'))
    courseList.value = []
  }
}

// 课程选择变化
const handleCourseChange = (courseId) => {
  selectedCourse.value = courseList.value.find(c => c.id === courseId)
}

// 显示添加对话框
const showAddDialog = () => {
  if (!queryParams.semester) {
    ElMessage.warning('请先选择学期')
    return
  }
  isEdit.value = false
  dialogTitle.value = '添加排课'
  resetForm()
  scheduleForm.semester = queryParams.semester
  dialogVisible.value = true
}

// 编辑排课
const editSchedule = (schedule) => {
  isEdit.value = true
  dialogTitle.value = '编辑排课'
  Object.assign(scheduleForm, {
    id: schedule.id,
    courseId: schedule.courseId,
    dayOfWeek: schedule.dayOfWeek,
    period: schedule.period,
    classroom: schedule.classroom,
    startWeek: schedule.startWeek,
    endWeek: schedule.endWeek,
    semester: schedule.semester
  })
  selectedCourse.value = {
    name: schedule.courseName,
    teacherName: schedule.teacherName,
    credits: schedule.credits || '-',
    courseType: schedule.courseType || '-',
    hours: schedule.hours || '-'
  }
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!scheduleForm.courseId) {
    ElMessage.warning('请选择课程')
    return
  }
  if (!scheduleForm.classroom) {
    ElMessage.warning('请输入教室')
    return
  }
  if (scheduleForm.startWeek > scheduleForm.endWeek) {
    ElMessage.warning('起始周不能大于结束周')
    return
  }

  try {
    if (isEdit.value) {
      const res = await updateSchedule(scheduleForm.id, scheduleForm)
      const data = res.data || res
      if (data.success !== false) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        loadScheduleList()
      } else {
        ElMessage.error(data.message || '更新失败')
      }
    } else {
      const res = await addSchedule(scheduleForm)
      const data = res.data || res
      if (data.success !== false) {
        ElMessage.success('添加成功')
        dialogVisible.value = false
        loadScheduleList()
      } else {
        ElMessage.error(data.message || '添加失败')
      }
    }
  } catch (error) {
    // request.js已经显示了错误信息，这里只记录日志
    console.error('提交失败:', error)
    // 如果是400错误（时间冲突），后端返回的错误信息已经在request.js中显示
    // 不需要再次显示错误消息
  }
}

// 删除排课
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这条排课吗?', '提示', {
      type: 'warning'
    })
    const res = await deleteSchedule(scheduleForm.id)
    const data = res.data || res
    if (data.success !== false) {
      ElMessage.success('删除成功')
      dialogVisible.value = false
      loadScheduleList()
    } else {
      ElMessage.error(data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(scheduleForm, {
    id: null,
    courseId: null,
    dayOfWeek: 1,
    period: 1,
    classroom: '',
    startWeek: 1,
    endWeek: 16,
    semester: queryParams.semester || ''
  })
  selectedCourse.value = null
}

onMounted(() => {
  loadSemesterList()
  loadCourseList()
})
</script>

<style scoped>
.schedule-page {
  padding: 24px;
  background-color: var(--bg-primary);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-form {
  margin-bottom: 20px;
}

.schedule-table {
  margin-top: 24px;
  overflow-x: auto;
  overflow-y: auto;
  max-height: calc(100vh - 300px);
}

.course-table {
  width: max-content;
  min-width: 100%;
  border-collapse: collapse;
  background: var(--bg-float);
}

.course-table th,
.course-table td {
  border: 1px solid var(--border);
  padding: 8px;
  text-align: center;
  min-width: 80px;
}

.course-table th {
  background: var(--bg-secondary);
  font-weight: 600;
  color: var(--text-primary);
  position: sticky;
  top: 0;
  z-index: 10;
}

.classroom-header {
  position: sticky;
  left: 0;
  z-index: 20;
  background: var(--bg-secondary);
  min-width: 100px;
}

.time-header {
  min-width: 120px;
  white-space: pre-line;
}

.time-range {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.classroom-cell {
  position: sticky;
  left: 0;
  background: var(--bg-highlight);
  font-weight: 500;
  color: var(--text-primary);
  z-index: 5;
  min-width: 100px;
}

.course-cell {
  vertical-align: top;
  min-height: 80px;
  min-width: 120px;
  background: var(--bg-float);
  padding: 6px;
  transition: background-color 0.3s;
}

.course-cell.highlight {
  background: rgba(122, 162, 247, 0.05);
}

.course-item {
  background: rgba(122, 162, 247, 0.1);
  border: 1px solid var(--primary);
  border-radius: 4px;
  padding: 8px;
  margin-bottom: 6px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.course-item:hover {
  background: var(--primary);
  transform: scale(1.03);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.course-item.search-highlight {
  background: rgba(255, 193, 7, 0.3);
  border-color: #ffc107;
  animation: highlight-pulse 1.5s ease-in-out infinite;
}

@keyframes highlight-pulse {
  0%, 100% {
    box-shadow: 0 0 5px rgba(255, 193, 7, 0.5);
  }
  50% {
    box-shadow: 0 0 15px rgba(255, 193, 7, 0.8);
  }
}

.course-name {
  font-weight: 600;
  margin-bottom: 4px;
  color: var(--text-primary);
  font-size: 13px;
}

.course-info {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.course-item:hover .course-name,
.course-item:hover .course-info {
  color: var(--bg-primary);
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

:deep(.el-dialog) {
  background-color: var(--bg-float);
}

:deep(.el-dialog__header) {
  background-color: var(--bg-secondary);
}

:deep(.el-input__wrapper) {
  background-color: var(--bg-secondary);
}

:deep(.el-empty) {
  padding: 60px 0;
}

:deep(.el-descriptions) {
  --el-descriptions-item-bordered-label-background: var(--bg-secondary);
}
</style>
