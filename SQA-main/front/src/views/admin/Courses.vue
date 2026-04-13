<template>
  <div class="courses-management">
    <el-card>
      <!-- 搜索和操作栏 -->
      <div class="toolbar">
        <div class="search-box">
          <el-input
            v-model="query.keyword"
            placeholder="搜索课程名称、课程代码、教师"
            style="width: 300px;"
            clearable
            @clear="loadCourses"
          >
            <template #append>
              <el-button icon="Search" @click="loadCourses" />
            </template>
          </el-input>
          
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px; margin-left: 10px;" @change="loadCourses">
            <el-option label="已通过" value="approved" />
            <el-option label="待审核" value="pending" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
        </div>
        
        <div class="actions">
          <el-button type="primary" icon="Plus" @click="showAddDialog">添加课程</el-button>
        </div>
      </div>

      <!-- 课程列表表格 -->
      <el-table :data="courses" v-loading="loading" style="width: 100%; margin-top: 20px;">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="courseCode" label="课程代码" width="120" />
        <el-table-column prop="name" label="课程名称" width="200" />
        <el-table-column prop="teacherName" label="授课教师" width="120" />
        <el-table-column prop="semester" label="学期" width="150" />
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column prop="category" label="类别" width="120" />
        <el-table-column label="选课情况" width="120">
          <template #default="{ row }">
            {{ row.enrolled }} / {{ row.capacity }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'approved'" type="success" size="small">已通过</el-tag>
            <el-tag v-else-if="row.status === 'pending'" type="warning" size="small">待审核</el-tag>
            <el-tag v-else type="danger" size="small">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            <el-button v-if="row.status === 'pending'" type="success" size="small" @click="handleApprove(row, 'approved')">通过</el-button>
            <el-button v-if="row.status === 'pending'" type="warning" size="small" @click="handleApprove(row, 'rejected')">拒绝</el-button>
            <el-button type="info" size="small" @click="adjustCapacity(row)">调整容量</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadCourses"
        @size-change="loadCourses"
        style="margin-top: 20px; justify-content: center;"
      />
    </el-card>

    <!-- 添加/编辑课程对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑课程' : '添加课程'"
      width="700px"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="课程代码" prop="courseCode">
          <el-input v-model="formData.courseCode" :disabled="isEdit" placeholder="请输入课程代码" />
        </el-form-item>
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="授课教师ID" prop="teacherId">
          <el-input-number v-model="formData.teacherId" :min="1" placeholder="教师ID" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="教师姓名" prop="teacherName">
          <el-input v-model="formData.teacherName" placeholder="请输入教师姓名" />
        </el-form-item>
        <el-form-item label="学期" prop="semester">
          <el-input v-model="formData.semester" placeholder="例如：2025-2026-1" />
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="formData.credit" :min="0" :max="10" :precision="1" :step="0.5" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input-number v-model="formData.capacity" :min="1" :max="500" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="类别" prop="category">
          <el-select v-model="formData.category" placeholder="请选择类别" style="width: 100%;">
            <el-option label="专业必修" value="专业必修" />
            <el-option label="专业选修" value="专业选修" />
            <el-option label="通识必修" value="通识必修" />
            <el-option label="通识选修" value="通识选修" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="4" placeholder="请输入课程描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 调整容量对话框 -->
    <el-dialog v-model="capacityDialogVisible" title="调整课程容量" width="500px">
      <el-form label-width="120px">
        <el-form-item label="当前容量">
          <span>{{ currentCourse.capacity }}</span>
        </el-form-item>
        <el-form-item label="已选人数">
          <span>{{ currentCourse.enrolled }}</span>
        </el-form-item>
        <el-form-item label="新容量">
          <el-input-number v-model="newCapacity" :min="currentCourse.enrolled" :max="500" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="capacityDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCapacityChange">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCourseList,
  addCourse,
  updateCourse,
  deleteCourse,
  approveCourseStatus,
  updateCourseCapacityNew
} from '@/api/admin'

const loading = ref(false)
const courses = ref([])
const total = ref(0)

const query = reactive({
  page: 1,
  pageSize: 10,
  keyword: '',
  status: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const submitting = ref(false)

const formData = reactive({
  courseCode: '',
  name: '',
  teacherId: null,
  teacherName: '',
  semester: '',
  credit: 3.0,
  capacity: 50,
  category: '专业必修',
  description: ''
})

const rules = {
  courseCode: [{ required: true, message: '请输入课程代码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  teacherId: [{ required: true, message: '请输入教师ID', trigger: 'blur' }],
  teacherName: [{ required: true, message: '请输入教师姓名', trigger: 'blur' }],
  semester: [{ required: true, message: '请输入学期', trigger: 'blur' }],
  credit: [{ required: true, message: '请输入学分', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'blur' }],
  category: [{ required: true, message: '请选择类别', trigger: 'change' }]
}

const capacityDialogVisible = ref(false)
const currentCourse = ref({})
const newCapacity = ref(0)

// 加载课程列表
const loadCourses = async () => {
  loading.value = true
  try {
    const res = await getCourseList(query)
    courses.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载课程列表失败')
  } finally {
    loading.value = false
  }
}

// 显示添加对话框
const showAddDialog = () => {
  isEdit.value = false
  Object.assign(formData, {
    courseCode: '',
    name: '',
    teacherId: null,
    teacherName: '',
    semester: '',
    credit: 3.0,
    capacity: 50,
    category: '专业必修',
    description: ''
  })
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (row) => {
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    courseCode: row.courseCode,
    name: row.name,
    teacherId: row.teacherId,
    teacherName: row.teacherName,
    semester: row.semester,
    credit: row.credit,
    capacity: row.capacity,
    category: row.category,
    description: row.description
  })
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  
  try {
    if (isEdit.value) {
      await updateCourse(formData.id, formData)
      ElMessage.success('课程信息更新成功')
    } else {
      await addCourse(formData)
      ElMessage.success('课程添加成功')
    }
    dialogVisible.value = false
    loadCourses()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 删除课程
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除课程 ${row.name} 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCourse(row.id)
      ElMessage.success('删除成功')
      loadCourses()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }).catch(() => {})
}

// 审核课程
const handleApprove = (row, status) => {
  const action = status === 'approved' ? '通过' : '拒绝'
  ElMessageBox.confirm(`确定要${action}课程 ${row.name} 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await approveCourseStatus(row.id, status)
      ElMessage.success(`${action}成功`)
      loadCourses()
    } catch (error) {
      ElMessage.error(`${action}失败`)
    }
  }).catch(() => {})
}

// 调整容量
const adjustCapacity = (row) => {
  currentCourse.value = row
  newCapacity.value = row.capacity
  capacityDialogVisible.value = true
}

// 处理容量调整
const handleCapacityChange = async () => {
  try {
    await updateCourseCapacityNew(currentCourse.value.id, newCapacity.value)
    ElMessage.success('容量调整成功')
    capacityDialogVisible.value = false
    loadCourses()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '容量调整失败')
  }
}

onMounted(() => {
  loadCourses()
})
</script>

<style scoped>
.courses-management {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-box {
  display: flex;
  gap: 10px;
}

.actions {
  display: flex;
  gap: 10px;
}

:deep(.el-pagination) {
  display: flex;
  justify-content: center;
}
</style>
