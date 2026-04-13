<template>
  <div class="students-management">
    <el-card>
      <!-- 搜索和操作栏 -->
      <div class="toolbar">
        <div class="search-box">
          <el-input
            v-model="query.keyword"
            placeholder="搜索用户名、姓名、邮箱"
            style="width: 300px;"
            clearable
            @clear="loadStudents"
          >
            <template #append>
              <el-button icon="Search" @click="loadStudents" />
            </template>
          </el-input>
          
          <el-select v-model="query.major" placeholder="专业" clearable style="width: 150px; margin-left: 10px;" @change="loadStudents">
            <el-option v-for="major in majors" :key="major" :label="major" :value="major" />
          </el-select>
          
          <el-select v-model="query.className" placeholder="班级" clearable style="width: 150px; margin-left: 10px;" @change="loadStudents">
            <el-option v-for="cls in classes" :key="cls" :label="cls" :value="cls" />
          </el-select>
          
          <el-select v-model="query.gender" placeholder="性别" clearable style="width: 120px; margin-left: 10px;" @change="loadStudents">
            <el-option label="男" value="male" />
            <el-option label="女" value="female" />
          </el-select>
        </div>
        
        <div class="actions">
          <el-button type="primary" icon="Plus" @click="showAddDialog">添加学生</el-button>
          <el-button type="success" icon="Upload" @click="showImportDialog">批量导入</el-button>
          <el-button type="info" icon="Download" @click="downloadTemplate">下载模板</el-button>
        </div>
      </div>

      <!-- 学生列表表格 -->
      <el-table :data="students" v-loading="loading" style="width: 100%; margin-top: 20px;">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="username" label="用户名/学号" width="130" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.gender === 'male'" type="primary" size="small">男</el-tag>
            <el-tag v-else-if="row.gender === 'female'" type="danger" size="small">女</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="major" label="专业" width="150" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="grade" label="年级" width="100">
          <template #default="{ row }">
            {{ gradeMap[row.grade] || row.grade }}
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'active'" type="success" size="small">正常</el-tag>
            <el-tag v-else type="danger" size="small">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="resetPassword(row)">重置密码</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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
        style="margin-top: 20px; justify-content: flex-end;"
        @size-change="loadStudents"
        @current-change="loadStudents"
      />
    </el-card>

    <!-- 添加/编辑学生对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑学生' : '添加学生'"
      width="600px"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户名/学号" prop="username">
          <el-input v-model="formData.username" :disabled="isEdit" placeholder="请输入用户名或学号" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="formData.password" type="password" placeholder="请输入密码，默认123456" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="formData.gender">
            <el-radio label="male">男</el-radio>
            <el-radio label="female">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="学号" prop="studentNumber">
          <el-input v-model="formData.studentNumber" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="专业" prop="major">
          <el-input v-model="formData.major" placeholder="请输入专业" />
        </el-form-item>
        <el-form-item label="班级" prop="className">
          <el-input v-model="formData.className" placeholder="请输入班级" />
        </el-form-item>
        <el-form-item label="入学年份" prop="enrollmentYear">
          <el-date-picker
            v-model="formData.enrollmentYear"
            type="year"
            placeholder="选择入学年份"
            style="width: 100%;"
            format="YYYY"
            value-format="YYYY"
          />
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-select v-model="formData.grade" placeholder="请选择年级" style="width: 100%;">
            <el-option label="大一" :value="1" />
            <el-option label="大二" :value="2" />
            <el-option label="大三" :value="3" />
            <el-option label="大四" :value="4" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量导入对话框 -->
    <el-dialog v-model="importDialogVisible" title="批量导入学生" width="500px">
      <el-upload
        drag
        :auto-upload="false"
        :on-change="handleFileChange"
        :file-list="fileList"
        accept=".xlsx,.xls"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只能上传 xlsx/xls 文件，请先下载模板填写数据
          </div>
        </template>
      </el-upload>
      
      <div v-if="importResult" style="margin-top: 20px;">
        <el-alert
          :title="`导入完成：成功 ${importResult.success} 条，失败 ${importResult.failed} 条`"
          :type="importResult.failed > 0 ? 'warning' : 'success'"
          :closable="false"
        />
        <div v-if="importResult.errors && importResult.errors.length > 0" style="margin-top: 10px; max-height: 200px; overflow-y: auto;">
          <div v-for="(error, index) in importResult.errors" :key="index" style="color: #F56C6C; font-size: 12px;">
            {{ error }}
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleImport" :loading="importing">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import {
  getStudentList,
  addStudent,
  updateStudent,
  deleteStudent,
  resetStudentPassword,
  batchImportStudents,
  getAllMajors,
  getAllClasses
} from '@/api/admin'

const loading = ref(false)
const students = ref([])
const total = ref(0)
const majors = ref([])
const classes = ref([])

// 年级映射：数字 -> 中文
const gradeMap = {
  1: '大一',
  2: '大二',
  3: '大三',
  4: '大四'
}

const query = reactive({
  page: 1,
  pageSize: 10,
  keyword: '',
  major: '',
  className: '',
  gender: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const submitting = ref(false)

const formData = reactive({
  username: '',
  password: '123456',
  realName: '',
  gender: 'male',
  email: '',
  phone: '',
  studentNumber: '',
  major: '',
  className: '',
  enrollmentYear: new Date().getFullYear().toString(),
  grade: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }]
}

const importDialogVisible = ref(false)
const importing = ref(false)
const fileList = ref([])
const importResult = ref(null)

// 加载学生列表
const loadStudents = async () => {
  loading.value = true
  try {
    const res = await getStudentList(query)
    students.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载学生列表失败')
  } finally {
    loading.value = false
  }
}

// 加载专业和班级列表
const loadFilters = async () => {
  try {
    majors.value = await getAllMajors()
    classes.value = await getAllClasses()
  } catch (error) {
    console.error('加载筛选项失败', error)
  }
}

// 显示添加对话框
const showAddDialog = () => {
  isEdit.value = false
  Object.assign(formData, {
    username: '',
    password: '123456',
    realName: '',
    gender: 'male',
    email: '',
    phone: '',
    studentNumber: '',
    major: '',
    className: '',
    enrollmentYear: new Date().getFullYear().toString(),
    grade: 1
  })
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (row) => {
  isEdit.value = true
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  
  try {
    if (isEdit.value) {
      await updateStudent(formData.id, formData)
      ElMessage.success('学生信息更新成功')
    } else {
      await addStudent(formData)
      ElMessage.success('学生添加成功')
    }
    dialogVisible.value = false
    loadStudents()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 删除学生
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除学生 ${row.realName} 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteStudent(row.id)
      ElMessage.success('删除成功')
      loadStudents()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 重置密码
const resetPassword = (row) => {
  ElMessageBox.prompt('请输入新密码（留空则重置为123456）', '重置密码', {
    inputValue: '123456'
  }).then(async ({ value }) => {
    try {
      await resetStudentPassword(row.id, value || '123456')
      ElMessage.success('密码重置成功')
    } catch (error) {
      ElMessage.error('密码重置失败')
    }
  }).catch(() => {})
}

// 显示导入对话框
const showImportDialog = () => {
  fileList.value = []
  importResult.value = null
  importDialogVisible.value = true
}

// 文件选择
const handleFileChange = (file) => {
  fileList.value = [file]
}

// 执行导入
const handleImport = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请选择要导入的文件')
    return
  }
  
  importing.value = true
  try {
    const res = await batchImportStudents(fileList.value[0].raw)
    importResult.value = res.data
    ElMessage.success('导入完成')
    loadStudents()
    loadFilters() // 重新加载筛选项
  } catch (error) {
    ElMessage.error('导入失败：' + error.message)
  } finally {
    importing.value = false
  }
}

// 下载模板
const downloadTemplate = async () => {
  try {
    const response = await fetch('/api/admin/students/download-template')
    if (!response.ok) throw new Error('下载失败')
    
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'student-template.xlsx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('模板下载成功')
  } catch (error) {
    ElMessage.error('模板下载失败')
  }
}

onMounted(() => {
  loadStudents()
  loadFilters()
})
</script>

<style scoped>
.students-management {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.search-box {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.actions {
  display: flex;
  gap: 10px;
}
</style>
