<template>
  <div class="courses-page">
    <!-- 顶部tab切换 -->
    <el-tabs v-model="activeTab" class="course-tabs">
      <el-tab-pane label="我的课表" name="schedule">
        <el-card class="schedule-card">
          <template #header>
            <div class="card-header">
              <span>本学期课表</span>
              <el-tag type="success">2025-2026学年 第一学期</el-tag>
            </div>
          </template>
          
          <!-- 课表 -->
          <div class="schedule-table-wrapper" v-loading="loading">
            <table class="schedule-table">
              <thead>
                <tr>
                  <th class="time-col">时间\星期</th>
                  <th v-for="day in weekDays" :key="day.value">{{ day.label }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="period in periods" :key="period.value">
                  <td class="time-col">
                    <div class="period-num">第{{ period.value }}节</div>
                    <div class="period-time">{{ period.time }}</div>
                  </td>
                  <td v-for="day in weekDays" :key="`${day.value}-${period.value}`" 
                      class="course-cell"
                      :class="{ 'has-course': getCourseByTime(day.value, period.value) }">
                    <div v-if="getCourseByTime(day.value, period.value)" 
                         class="course-info"
                         @click="viewScheduleCourseDetail(day.value, period.value)">
                      <div class="course-name">{{ getCourseByTime(day.value, period.value).courseName }}</div>
                      <div class="course-location">{{ getCourseByTime(day.value, period.value).classroom }}</div>
                      <div class="course-teacher">{{ getCourseByTime(day.value, period.value).teacherName }}</div>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          
          <!-- 课表说明 -->
          <div class="schedule-legend">
            <el-alert type="info" :closable="false">
              <template #title>
                <span style="font-size: 14px;">
                  提示：点击课程卡片查看详情 | 当前已选 <b>{{ myCourses.length }}</b> 门课程
                </span>
              </template>
            </el-alert>
          </div>
        </el-card>
      </el-tab-pane>
      
      <el-tab-pane label="课程列表" name="list">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>我的课程</span>
              <el-button type="primary" @click="dialogVisible = true" :icon="Plus">选课</el-button>
            </div>
          </template>

          <el-table :data="myCourses" style="width: 100%" v-loading="loading" element-loading-text="加载课程数据中...">
            <el-table-column prop="name" label="课程名称" width="180" />
            <el-table-column prop="courseCode" label="课程代码" width="110" />
            <el-table-column prop="teacherName" label="授课教师" width="100" />
            <el-table-column prop="credit" label="学分" width="70" align="center" />
            <el-table-column prop="category" label="类别" width="100">
              <template #default="scope">
                <el-tag :type="getCategoryType(scope.row.category)" size="small">
                  {{ scope.row.category }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="timeInfo" label="上课时间" width="200">
              <template #default="scope">
                <div v-if="scope.row.schedules && scope.row.schedules.length > 0">
                  <div v-for="(schedule, idx) in scope.row.schedules" :key="idx" class="schedule-item">
                    <el-tag size="small" type="info">
                      {{ getWeekDayName(schedule.dayOfWeek) }} 第{{ schedule.period }}节 {{ schedule.classroom }}
                    </el-tag>
                  </div>
                </div>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'selected' ? 'success' : 'info'" size="small">
                  {{ scope.row.status === 'selected' ? '进行中' : scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="180" align="center">
              <template #default="scope">
                <div style="display: flex; gap: 8px; justify-content: center;">
                  <el-button size="small" @click="viewCourseDetail(scope.row)" :icon="View">查看</el-button>
                  <el-button size="small" type="danger" @click="dropCourse(scope.row)" :icon="Delete" 
                             :loading="dropLoading && currentCourseId === scope.row.id">退课</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 优化后的选课对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      title="选择课程" 
      width="1000px" 
      :close-on-click-modal="false"
      class="select-course-dialog">
      
      <!-- 搜索和筛选 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索课程名称、教师或课程代码"
          prefix-icon="Search"
          clearable
          style="width: 300px; margin-right: 12px"
        />
        <el-select v-model="filterCategory" placeholder="课程类别" clearable style="width: 150px; margin-right: 12px">
          <el-option label="全部" value="" />
          <el-option label="专业必修" value="专业必修" />
          <el-option label="专业选修" value="专业选修" />
          <el-option label="公共必修" value="公共必修" />
        </el-select>
        <el-select v-model="filterTeacher" placeholder="授课教师" clearable style="width: 150px">
          <el-option label="全部" value="" />
          <el-option v-for="teacher in teacherList" :key="teacher" :label="teacher" :value="teacher" />
        </el-select>
      </div>
      
      <!-- 课程卡片网格 -->
      <div class="course-grid" v-loading="selectDialogLoading">
        <div 
          v-for="course in filteredAvailableCourses" 
          :key="course.id" 
          class="course-card"
          :class="{ 'full': course.enrolled >= course.capacity, 'selected': isSelected(course.id) }">
          
          <div class="course-card-header">
            <div class="course-title">{{ course.name }}</div>
            <el-tag :type="getCategoryType(course.category)" size="small">{{ course.category }}</el-tag>
          </div>
          
          <div class="course-card-body">
            <div class="course-info-row">
              <span class="info-label">课程代码:</span>
              <span class="info-value">{{ course.courseCode }}</span>
            </div>
            <div class="course-info-row">
              <span class="info-label">授课教师:</span>
              <span class="info-value">{{ course.teacherName }}</span>
            </div>
            <div class="course-info-row">
              <span class="info-label">学分:</span>
              <span class="info-value">{{ course.credit }}</span>
            </div>
            <div class="course-info-row">
              <span class="info-label">上课时间:</span>
              <span class="info-value">
                <div v-if="course.schedules && course.schedules.length > 0" class="schedule-tags">
                  <el-tag 
                    v-for="(schedule, idx) in course.schedules.slice(0, 2)" 
                    :key="idx" 
                    size="small" 
                    type="info"
                    style="margin-right: 4px; margin-bottom: 4px;">
                    {{ getWeekDayName(schedule.dayOfWeek) }} 第{{ schedule.period }}节
                  </el-tag>
                  <span v-if="course.schedules.length > 2" style="font-size: 12px; color: var(--text-secondary);">
                    +{{ course.schedules.length - 2 }}
                  </span>
                </div>
                <span v-else style="color: var(--text-secondary);">待安排</span>
              </span>
            </div>
            <div class="course-info-row">
              <span class="info-label">选课情况:</span>
              <span class="info-value">
                <el-progress 
                  :percentage="Math.round(course.enrolled / course.capacity * 100)" 
                  :color="getProgressColor(course.enrolled / course.capacity)"
                  :show-text="false"
                  style="width: 60px; display: inline-block; margin-right: 8px" />
                {{ course.enrolled }} / {{ course.capacity }}
              </span>
            </div>
            <div v-if="course.description" class="course-desc">
              <span class="info-label">简介:</span>
              <span class="info-value">{{ course.description }}</span>
            </div>
          </div>
          
          <div class="course-card-footer">
            <el-button 
              type="primary" 
              size="small"
              :disabled="course.enrolled >= course.capacity || isSelected(course.id)"
              :loading="selectLoading && currentCourseId === course.id"
              @click="handleSelectCourse(course)"
              style="width: 100%">
              <span v-if="isSelected(course.id)">✓ 已选</span>
              <span v-else-if="course.enrolled >= course.capacity">已满</span>
              <span v-else>+ 选课</span>
            </el-button>
          </div>
        </div>
        
        <!-- 无结果提示 -->
        <el-empty v-if="filteredAvailableCourses.length === 0" description="没有找到符合条件的课程" />
      </div>
    </el-dialog>
    
    <!-- 课程详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="课程详情" width="700px">
      <el-descriptions :column="2" border v-if="currentCourse">
        <el-descriptions-item label="课程名称" :span="2">
          <b>{{ currentCourse.courseName || currentCourse.name }}</b>
        </el-descriptions-item>
        <el-descriptions-item label="课程代码">{{ currentCourse.courseCode }}</el-descriptions-item>
        <el-descriptions-item label="学分">{{ currentCourse.credit }}</el-descriptions-item>
        <el-descriptions-item label="授课教师">{{ currentCourse.teacherName }}</el-descriptions-item>
        <el-descriptions-item label="课程类别">
          <el-tag :type="getCategoryType(currentCourse.category)">{{ currentCourse.category }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="选课情况" :span="2">
          {{ currentCourse.enrolled }} / {{ currentCourse.capacity }}
          <el-progress 
            :percentage="Math.round(currentCourse.enrolled / currentCourse.capacity * 100)" 
            style="margin-top: 8px" />
        </el-descriptions-item>
        <el-descriptions-item label="课程描述" :span="2">
          {{ currentCourse.description || '暂无描述' }}
        </el-descriptions-item>
        <el-descriptions-item label="上课安排" :span="2" v-if="currentCourse.schedules && currentCourse.schedules.length > 0">
          <div v-for="(schedule, idx) in currentCourse.schedules" :key="idx" style="margin-bottom: 8px">
            <el-tag type="info">
              {{ getWeekDayName(schedule.dayOfWeek) }} 第{{ schedule.period }}节 ({{ getPeriodTime(schedule.period) }})
              | {{ schedule.classroom }}
              | 第{{ schedule.startWeek }}-{{ schedule.endWeek }}周
            </el-tag>
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, View, Delete } from '@element-plus/icons-vue'
import { getMyCourses, getAllCourses, enrollCourse, withdrawCourse } from '@/api/course'
import { getScheduleByCourseId } from '@/api/schedule'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const activeTab = ref('schedule') // 默认显示课表
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const searchKeyword = ref('')
const filterCategory = ref('')
const filterTeacher = ref('')
const myCourses = ref([])
const availableCourses = ref([])
const mySchedules = ref([]) // 我的课表数据
const currentCourse = ref(null)
const currentCourseId = ref(null)
const loading = ref(false)
const selectDialogLoading = ref(false)
const selectLoading = ref(false)
const dropLoading = ref(false)

// 星期和时间段定义
const weekDays = [
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 }
]

const periods = [
  { value: 1, time: '08:00-09:40' },
  { value: 2, time: '10:00-11:40' },
  { value: 3, time: '14:00-15:40' },
  { value: 4, time: '16:00-17:40' },
  { value: 5, time: '19:00-20:40' }
]

// 教师列表(用于筛选)
const teacherList = computed(() => {
  const teachers = new Set()
  availableCourses.value.forEach(course => {
    if (course.teacherName) teachers.add(course.teacherName)
  })
  return Array.from(teachers)
})

// 过滤后的可选课程
const filteredAvailableCourses = computed(() => {
  let courses = availableCourses.value
  
  // 搜索关键词过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    courses = courses.filter(course => 
      course.name?.toLowerCase().includes(keyword) || 
      course.teacherName?.toLowerCase().includes(keyword) ||
      course.courseCode?.toLowerCase().includes(keyword)
    )
  }
  
  // 类别过滤
  if (filterCategory.value) {
    courses = courses.filter(course => course.category === filterCategory.value)
  }
  
  // 教师过滤
  if (filterTeacher.value) {
    courses = courses.filter(course => course.teacherName === filterTeacher.value)
  }
  
  return courses
})

// 判断课程是否已选
const isSelected = (courseId) => {
  return myCourses.value.some(course => course.id === courseId)
}

// 获取课程类别标签类型
const getCategoryType = (category) => {
  const typeMap = {
    '专业必修': 'danger',
    '专业选修': 'warning',
    '公共必修': 'success',
    '公共选修': 'info'
  }
  return typeMap[category] || 'info'
}

// 获取进度条颜色
const getProgressColor = (percentage) => {
  if (percentage >= 0.9) return '#f56c6c'
  if (percentage >= 0.7) return '#e6a23c'
  return '#67c23a'
}

// 获取星期名称
const getWeekDayName = (dayOfWeek) => {
  const days = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']
  return days[dayOfWeek] || ''
}

// 获取时间段文本
const getPeriodTime = (period) => {
  const periodObj = periods.find(p => p.value === period)
  return periodObj ? periodObj.time : ''
}

// 根据星期和节次获取课程
const getCourseByTime = (dayOfWeek, period) => {
  return mySchedules.value.find(
    schedule => schedule.dayOfWeek === dayOfWeek && schedule.period === period
  )
}

// 加载我的课程
const loadMyCourses = async () => {
  loading.value = true
  try {
    const studentId = userStore.userInfo.id || 6 // 默认张三
    const res = await getMyCourses(studentId)
    if (res.code === 200) {
      myCourses.value = res.data || []
      // 加载每门课程的课表信息
      await loadMySchedules()
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

// 加载我的课表
const loadMySchedules = async () => {
  try {
    const schedules = []
    for (const course of myCourses.value) {
      try {
        const res = await getScheduleByCourseId(course.id)
        if (res.code === 200 && res.data) {
          const courseSchedules = Array.isArray(res.data) ? res.data : [res.data]
          courseSchedules.forEach(schedule => {
            schedules.push({
              ...schedule,
              courseName: course.name,
              teacherName: course.teacherName
            })
          })
          // 将课表信息附加到课程对象
          course.schedules = courseSchedules
        }
      } catch (error) {
        console.error(`加载课程 ${course.name} 的课表失败:`, error)
      }
    }
    mySchedules.value = schedules
  } catch (error) {
    console.error('加载课表失败:', error)
  }
}

// 加载可选课程
const loadAvailableCourses = async () => {
  selectDialogLoading.value = true
  try {
    const res = await getAllCourses()
    if (res.code === 200) {
      availableCourses.value = res.data || []
      // 为每个课程加载课表信息
      await loadAvailableCoursesSchedules()
    }
  } catch (error) {
    console.error('加载可选课程失败:', error)
    ElMessage.error('加载可选课程失败')
  } finally {
    selectDialogLoading.value = false
  }
}

// 加载可选课程的课表
const loadAvailableCoursesSchedules = async () => {
  try {
    for (const course of availableCourses.value) {
      try {
        const res = await getScheduleByCourseId(course.id)
        if (res.code === 200 && res.data) {
          const courseSchedules = Array.isArray(res.data) ? res.data : [res.data]
          // 将课表信息附加到课程对象
          course.schedules = courseSchedules
        }
      } catch (error) {
        console.error(`加载课程 ${course.name} 的课表失败:`, error)
      }
    }
  } catch (error) {
    console.error('加载可选课程课表失败:', error)
  }
}

// 查看课程详情
const viewCourseDetail = (course) => {
  currentCourse.value = course
  detailDialogVisible.value = true
}

// 从课表查看课程详情
const viewScheduleCourseDetail = (dayOfWeek, period) => {
  const schedule = getCourseByTime(dayOfWeek, period)
  if (!schedule) return
  
  // 从myCourses中找到对应的完整课程信息
  const course = myCourses.value.find(c => c.name === schedule.courseName)
  if (course) {
    viewCourseDetail(course)
  } else {
    ElMessage.warning('无法获取课程详情')
  }
}

// 检查时间冲突
const checkScheduleConflict = (newCourse) => {
  if (!newCourse.schedules || newCourse.schedules.length === 0) {
    return { hasConflict: false }
  }
  
  for (const myCourse of myCourses.value) {
    if (!myCourse.schedules || myCourse.schedules.length === 0) continue
    
    for (const mySchedule of myCourse.schedules) {
      for (const newSchedule of newCourse.schedules) {
        // 检查星期和节次是否冲突
        if (mySchedule.dayOfWeek === newSchedule.dayOfWeek && 
            mySchedule.period === newSchedule.period) {
          return {
            hasConflict: true,
            conflictCourse: myCourse.name,
            conflictTime: `${getWeekDayName(mySchedule.dayOfWeek)} 第${mySchedule.period}节`
          }
        }
      }
    }
  }
  
  return { hasConflict: false }
}

// 选课
const handleSelectCourse = async (course) => {
  if (selectLoading.value) return
  
  // 检查时间冲突
  const conflictCheck = checkScheduleConflict(course)
  if (conflictCheck.hasConflict) {
    ElMessage.warning({
      message: `时间冲突！《${course.name}》与已选课程《${conflictCheck.conflictCourse}》在 ${conflictCheck.conflictTime} 时间冲突`,
      duration: 4000
    })
    return
  }
  
  currentCourseId.value = course.id
  selectLoading.value = true
  try {
    const studentId = userStore.userInfo.id || 6
    const res = await enrollCourse(course.id, studentId)
    if (res.code === 200) {
      ElMessage.success({
        message: `选课成功！已选择《${course.name}》`,
        duration: 3000
      })
      dialogVisible.value = false
      // 刷新数据
      await Promise.all([loadMyCourses(), loadAvailableCourses()])
    } else {
      ElMessage.error({
        message: res.message || '选课失败',
        duration: 3000
      })
    }
  } catch (error) {
    console.error(error)
    ElMessage.error({
      message: error.message || '选课失败，请稍后重试',
      duration: 3000
    })
  } finally {
    selectLoading.value = false
    currentCourseId.value = null
  }
}

// 退课
const dropCourse = (course) => {
  if (dropLoading.value) return
  
  ElMessageBox.confirm(
    `确定要退出课程《${course.name || course.courseName}》吗？`, 
    '退课确认', 
    {
      confirmButtonText: '确定退课',
      cancelButtonText: '取消',
      type: 'warning',
      center: true
    }
  ).then(async () => {
    currentCourseId.value = course.id
    dropLoading.value = true
    try {
      const studentId = userStore.userInfo.id || 6
      const res = await withdrawCourse(course.id, studentId)
      if (res.code === 200) {
        ElMessage.success({
          message: '退课成功',
          duration: 3000
        })
        // 刷新数据
        await Promise.all([loadMyCourses(), loadAvailableCourses()])
      } else {
        ElMessage.error({
          message: res.message || '退课失败',
          duration: 3000
        })
      }
    } catch (error) {
      console.error(error)
      ElMessage.error({
        message: error.message || '退课失败，请稍后重试',
        duration: 3000
      })
    } finally {
      dropLoading.value = false
      currentCourseId.value = null
    }
  }).catch(() => {
    // 用户取消
  })
}

onMounted(() => {
  loadMyCourses()
  loadAvailableCourses()
})
</script>

<style scoped>
.courses-page {
  padding: 0;
}

.course-tabs {
  background: var(--bg-float);
  padding: 0;
}

.course-tabs :deep(.el-tabs__header) {
  margin: 0;
  padding: 0 20px;
  background: var(--bg-float);
}

.course-tabs :deep(.el-tabs__item) {
  color: var(--text-secondary);
  font-size: 15px;
  padding: 0 30px;
  height: 50px;
  line-height: 50px;
}

.course-tabs :deep(.el-tabs__item.is-active) {
  color: var(--accent-blue);
  font-weight: 600;
}

.schedule-card {
  margin-top: 0;
  background: var(--bg-float);
  border: 1px solid var(--border);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
}

/* 课表样式 */
.schedule-table-wrapper {
  overflow-x: auto;
  margin-bottom: 20px;
}

.schedule-table {
  width: 100%;
  border-collapse: collapse;
  background: var(--bg-float);
  min-width: 900px;
}

.schedule-table th {
  background: linear-gradient(135deg, #7aa2f7d7 0%, #478bebc1 100%);
  color: #1a1b26;
  padding: 16px 8px;
  text-align: center;
  font-weight: 600;
  font-size: 15px;
  border: 1px solid #414868;
  box-shadow: 0 2px 8px rgba(122, 162, 247, 0.3);
}

.schedule-table td {
  border: 1px solid #414868;
  padding: 8px;
  text-align: center;
  height: 100px;
  vertical-align: top;
  background: #1a1b26;
}

.time-col {
  background: linear-gradient(135deg, #24283b 0%, #1f2335 100%);
  width: 120px;
  font-weight: 500;
  border-right: 2px solid #565f89 !important;
}

.period-num {
  font-size: 14px;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.period-time {
  font-size: 12px;
  color: var(--text-secondary);
}

.course-cell {
  background: var(--bg-float);
  transition: all 0.3s;
  cursor: default;
}

.course-cell.has-course {
  background: linear-gradient(135deg, #292e42 0%, #1f2335 100%);
  cursor: pointer;
  border: 1px solid #565f89;
  transition: all 0.3s ease;
}

.course-cell.has-course:hover {
  background: linear-gradient(135deg, #414868 0%, #545c7e 100%);
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(122, 162, 247, 0.4);
  border-color: #7aa2f7;
}

.course-info {
  padding: 8px;
  text-align: left;
}

.course-name {
  font-size: 14px;
  font-weight: 600;
  color: #7dcfff;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-shadow: 0 1px 3px rgba(125, 207, 255, 0.3);
}

.course-location,
.course-teacher {
  font-size: 12px;
  color: #a9b1d6;
  margin-top: 2px;
}

.schedule-legend {
  margin-top: 16px;
}

/* 课程卡片网格 */
.search-bar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  max-height: 600px;
  overflow-y: auto;
  padding: 12px;
  background: linear-gradient(135deg, rgba(33, 150, 243, 0.08), rgba(56, 189, 248, 0.05));
  border: 1px solid var(--border);
  border-radius: 14px;
}

.course-card {
  background: rgba(15, 23, 42, 0.65);
  border: 1px solid rgba(148, 163, 184, 0.4);
  border-radius: 12px;
  padding: 16px;
  transition: all 0.3s;
  cursor: pointer;
  color: var(--text-primary);
  backdrop-filter: blur(8px);
}

.course-card:hover {
  border-color: var(--accent-blue);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
  transform: translateY(-4px);
}

.course-card.full {
  opacity: 0.8;
  background: rgba(71, 85, 105, 0.6);
}

.course-card.selected {
  border-color: var(--accent-green);
  background: linear-gradient(140deg, rgba(16, 185, 129, 0.32), rgba(5, 150, 105, 0.25));
  box-shadow: 0 12px 24px rgba(16, 185, 129, 0.3);
}

.course-card.selected .course-title,
.course-card.selected .info-value {
  color: #ecfdf5;
}

.course-card.selected .info-label {
  color: rgba(236, 253, 245, 0.8);
}

.course-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
  gap: 8px;
}

.course-title {
  font-size: 16px;
  font-weight: 600;
  color: #e2e8f0;
  flex: 1;
  line-height: 1.4;
}

.course-card-body {
  margin-bottom: 12px;
}

.course-info-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
}

.info-label {
  color: rgba(148, 163, 184, 0.9);
  min-width: 90px;
  font-weight: 500;
}

.info-value {
  color: var(--text-primary);
  flex: 1;
}

.course-desc {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed var(--border);
  display: flex;
  gap: 8px;
}

.course-desc .info-value {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.schedule-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
}

.course-card-footer {
  margin-top: 12px;
}

.schedule-item {
  margin-bottom: 4px;
}

/* 对话框样式 */
.select-course-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, var(--accent-blue) 0%, #5470c6 100%);
  padding: 20px;
  margin: 0;
}

.select-course-dialog :deep(.el-dialog__title) {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
}

.select-course-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #fff;
  font-size: 20px;
}

.select-course-dialog :deep(.el-dialog) {
  background: radial-gradient(circle at top, rgba(59, 130, 246, 0.18), rgba(15, 23, 42, 0.92));
  border: 1px solid rgba(99, 102, 241, 0.4);
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.65);
}

.select-course-dialog :deep(.el-dialog__body) {
  background: transparent;
  padding: 24px 28px 32px;
}

/* Element Plus组件主题适配 */
.courses-page :deep(.el-card) {
  background: var(--bg-float);
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
}

.courses-page :deep(.el-card__header) {
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border);
  color: var(--text-primary);
  padding: 16px 20px;
}

.courses-page :deep(.el-table) {
  background: transparent;
  color: var(--text-primary);
}

.courses-page :deep(.el-table th) {
  background: var(--bg-secondary);
  color: var(--text-primary);
  border-color: var(--border);
}

.courses-page :deep(.el-table td) {
  border-color: var(--border);
}

.courses-page :deep(.el-table tr:hover > td) {
  background: var(--bg-highlight) !important;
}

.courses-page :deep(.el-dialog) {
  background: var(--bg-float);
  border: 1px solid var(--border);
}

.courses-page :deep(.el-input__wrapper) {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  box-shadow: none;
}

.courses-page :deep(.el-input__inner) {
  color: var(--text-primary);
}

.courses-page :deep(.el-select .el-input__wrapper) {
  background: var(--bg-secondary);
}

.courses-page :deep(.el-progress__text) {
  color: var(--text-primary);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .course-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}

@media (max-width: 768px) {
  .course-grid {
    grid-template-columns: 1fr;
  }
  
  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-bar > * {
    width: 100% !important;
  }
  
  .schedule-table {
    font-size: 12px;
  }
  
  .course-name {
    font-size: 12px;
  }
}
</style>
