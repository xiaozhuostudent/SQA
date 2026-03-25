<template>
  <div class="teachers-management">
    <el-card>
      <!-- 搜索和操作栏 -->
      <div class="toolbar">
        <div class="search-box">
          <el-input
            v-model="query.keyword"
            placeholder="搜索用户名、姓名、邮箱"
            style="width: 300px;"
            clearable
            @clear="loadTeachers"
          >
            <template #append>
              <el-button icon="Search" @click="loadTeachers" />
            </template>
          </el-input>
          
          <el-select v-model="query.gender" placeholder="性别" clearable style="width: 120px; margin-left: 10px;" @change="loadTeachers">
            <el-option label="男" value="male" />
            <el-option label="女" value="female" />
          </el-select>
        </div>
        
        <div class="actions">
          <el-button type="primary" icon="Plus" @click="showAddDialog">添加教师</el-button>
          <el-button type="success" icon="Upload" @click="showImportDialog">批量导入</el-button>
          <el-button type="info" icon="Download" @click="downloadTemplate">下载模板</el-button>
        </div>
      </div>

      <!-- 教师列表表格 -->
      <el-table :data="teachers" v-loading="loading" style="width: 100%; margin-top: 20px;">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.gender === 'male'" type="primary" size="small">男</el-tag>
            <el-tag v-else-if="row.gender === 'female'" type="danger" size="small">女</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="department" label="院系" width="150" />
        <el-table-column prop="title" label="职称" width="120">
          <template #default="{ row }">
            {{ titleMap[row.title] || row.title }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'active'" type="success" size="small">正常</el-tag>
            <el-tag v-else type="danger" size="small">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="230" fixed="right">
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
        @size-change="loadTeachers"
        @current-change="loadTeachers"
      />
    </el-card>

    <!-- 添加/编辑教师对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑教师' : '添加教师'"
      width="600px"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" :disabled="isEdit" placeholder="请输入用户名" />
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
        <el-form-item label="工号" prop="teacherNumber">
          <el-input v-model="formData.teacherNumber" placeholder="请输入工号" />
        </el-form-item>
        <el-form-item label="院系" prop="department">
          <el-input v-model="formData.department" placeholder="请输入院系" />
        </el-form-item>
        <el-form-item label="职称" prop="title">
          <el-select v-model="formData.title" placeholder="请选择职称" style="width: 100%;">
            <el-option label="教授" value="professor" />
            <el-option label="副教授" value="associate_professor" />
            <el-option label="讲师" value="lecturer" />
            <el-option label="助教" value="assistant" />
          </el-select>
        </el-form-item>
        <el-form-item label="研究方向" prop="researchField">
          <el-input v-model="formData.researchField" type="textarea" :rows="3" placeholder="请输入研究方向" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量导入对话框 -->
    <el-dialog v-model="importDialogVisible" title="批量导入教师" width="500px">
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

    <!-- 授课统计对话框 -->
    <el-dialog v-model="statsDialogVisible" title="授课统计" width="500px">
      <div v-if="currentStats" class="stats-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="教师姓名">{{ currentStats.teacherName }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentStats.username }}</el-descriptions-item>
          <el-descriptions-item label="授课数量">{{ currentStats.courseCount }} 门</el-descriptions-item>
          <el-descriptions-item label="学生总数">{{ currentStats.studentCount }} 人</el-descriptions-item>
          <el-descriptions-item label="活跃学生">{{ currentStats.activeStudentCount }} 人</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import {
  getTeacherList,
  addTeacher,
  updateTeacher,
  deleteTeacher,
  resetTeacherPassword,
  getTeacherStats,
  batchImportTeachers,
  downloadTeacherTemplate
} from '@/api/admin'

// 职称中英文映射
const titleMap = {
  'professor': '教授',
  'associate_professor': '副教授',
  'lecturer': '讲师',
  'assistant': '助教'
}

const loading = ref(false)
const teachers = ref([])
const total = ref(0)

const query = reactive({
  page: 1,
  pageSize: 10,
  keyword: '',
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
  teacherNumber: '',
  department: '',
  title: '',
  researchField: ''
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

const statsDialogVisible = ref(false)
const currentStats = ref(null)

// 加载教师列表
const loadTeachers = async () => {
  loading.value = true
  try {
    const res = await getTeacherList(query)
    teachers.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载教师列表失败')
  } finally {
    loading.value = false
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
    teacherNumber: '',
    department: '',
    title: '',
    researchField: ''
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
      await updateTeacher(formData.id, formData)
      ElMessage.success('教师信息更新成功')
    } else {
      await addTeacher(formData)
      ElMessage.success('教师添加成功')
    }
    dialogVisible.value = false
    loadTeachers()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 删除教师
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除教师 ${row.realName} 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteTeacher(row.id)
      ElMessage.success('删除成功')
      loadTeachers()
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
      await resetTeacherPassword(row.id, value || '123456')
      ElMessage.success('密码重置成功')
    } catch (error) {
      ElMessage.error('密码重置失败')
    }
  }).catch(() => {})
}

// 查看授课统计
const viewStats = async (row) => {
  try {
    currentStats.value = await getTeacherStats(row.id)
    statsDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取统计数据失败')
  }
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
    const res = await batchImportTeachers(fileList.value[0].raw)
    importResult.value = res.data
    ElMessage.success('导入完成')
    loadTeachers()
  } catch (error) {
    ElMessage.error('导入失败：' + error.message)
  } finally {
    importing.value = false
  }
}

// 下载模板
const downloadTemplate = async () => {
  try {
    const response = await fetch('/api/admin/teachers/download-template')
    if (!response.ok) throw new Error('下载失败')
    
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'teacher-template.xlsx'
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
  loadTeachers()
})
</script>

<style scoped>
.teachers-management {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-box {
  display: flex;
  align-items: center;
}

.actions {
  display: flex;
  gap: 10px;
}

.stats-content {
  padding: 20px 0;
}
</style>
