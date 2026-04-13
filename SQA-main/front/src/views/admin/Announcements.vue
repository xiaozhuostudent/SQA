<template>
  <div class="announcements-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>公告管理</span>
          <el-button type="primary" @click="showAddDialog">发布公告</el-button>
        </div>
      </template>

      <!-- 搜索筛选 -->
      <el-form :inline="true" class="filter-form">
        <el-form-item label="关键词">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索标题或内容"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="queryParams.type" placeholder="全部" clearable style="width: 120px">
            <el-option label="系统通知" value="system" />
            <el-option label="课程通知" value="course" />
            <el-option label="考试通知" value="exam" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="queryParams.priority" placeholder="全部" clearable style="width: 120px">
            <el-option label="低" value="low" />
            <el-option label="中" value="medium" />
            <el-option label="高" value="high" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标">
          <el-select v-model="queryParams.targetRole" placeholder="全部" clearable style="width: 120px">
            <el-option label="所有人" value="all" />
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="草稿" value="draft" />
            <el-option label="已发布" value="published" />
            <el-option label="已归档" value="archived" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadAnnouncementList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 公告列表 -->
      <el-table :data="announcementList" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'system'" type="primary">系统通知</el-tag>
            <el-tag v-else-if="row.type === 'course'" type="success">课程通知</el-tag>
            <el-tag v-else-if="row.type === 'exam'" type="warning">考试通知</el-tag>
            <el-tag v-else type="info">其他</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="优先级" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.priority === 'high'" type="danger">高</el-tag>
            <el-tag v-else-if="row.priority === 'medium'" type="warning">中</el-tag>
            <el-tag v-else type="info">低</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="目标" width="100">
          <template #default="{ row }">
            <span v-if="row.targetRole === 'all'">所有人</span>
            <span v-else-if="row.targetRole === 'student'">学生</span>
            <el-tag v-else-if="row.targetRole === 'teacher'">教师</el-tag>
            <span v-else>管理员</span>
          </template>
        </el-table-column>
        <el-table-column prop="publisherName" label="发布人" width="100" />
        <el-table-column label="发布时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'published'" type="success">已发布</el-tag>
            <el-tag v-else-if="row.status === 'draft'" type="info">草稿</el-tag>
            <el-tag v-else type="warning">已归档</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewAnnouncement(row)">查看</el-button>
            <el-button link type="primary" size="small" @click="editAnnouncement(row)">编辑</el-button>
            <el-button 
              v-if="row.status === 'draft'" 
              link 
              type="success" 
              size="small" 
              @click="handlePublish(row.id)"
            >发布</el-button>
            <el-button 
              v-if="row.status === 'published'" 
              link 
              type="warning" 
              size="small" 
              @click="handleArchive(row.id)"
            >归档</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
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
        @size-change="loadAnnouncementList"
        @current-change="loadAnnouncementList"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 添加/编辑公告对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="resetForm"
    >
      <el-form :model="announcementForm" label-width="100px">
        <el-form-item label="标题" required>
          <el-input
            v-model="announcementForm.title"
            placeholder="请输入公告标题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input
            v-model="announcementForm.content"
            type="textarea"
            :rows="8"
            placeholder="请输入公告内容"
            maxlength="5000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="announcementForm.type" placeholder="请选择类型">
            <el-option label="系统通知" value="system" />
            <el-option label="课程通知" value="course" />
            <el-option label="考试通知" value="exam" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" required>
          <el-select v-model="announcementForm.priority" placeholder="请选择优先级">
            <el-option label="低" value="low" />
            <el-option label="中" value="medium" />
            <el-option label="高" value="high" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标角色" required>
          <el-select v-model="announcementForm.targetRole" placeholder="请选择目标角色">
            <el-option label="所有人" value="all" />
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" required>
          <el-radio-group v-model="announcementForm.status">
            <el-radio value="draft">保存为草稿</el-radio>
            <el-radio value="published">立即发布</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看公告对话框 -->
    <el-dialog v-model="viewDialogVisible" title="公告详情" width="700px">
      <el-descriptions :column="2" border v-if="currentAnnouncement">
        <el-descriptions-item label="标题" :span="2">
          {{ currentAnnouncement.title }}
        </el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag v-if="currentAnnouncement.type === 'system'" type="primary">系统通知</el-tag>
          <el-tag v-else-if="currentAnnouncement.type === 'course'" type="success">课程通知</el-tag>
          <el-tag v-else-if="currentAnnouncement.type === 'exam'" type="warning">考试通知</el-tag>
          <el-tag v-else type="info">其他</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag v-if="currentAnnouncement.priority === 'high'" type="danger">高</el-tag>
          <el-tag v-else-if="currentAnnouncement.priority === 'medium'" type="warning">中</el-tag>
          <el-tag v-else type="info">低</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="目标角色">
          {{ getRoleText(currentAnnouncement.targetRole) }}
        </el-descriptions-item>
        <el-descriptions-item label="发布人">
          {{ currentAnnouncement.publisherName }}
        </el-descriptions-item>
        <el-descriptions-item label="发布时间" :span="2">
          {{ formatDateTime(currentAnnouncement.publishTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="currentAnnouncement.status === 'published'" type="success">已发布</el-tag>
          <el-tag v-else-if="currentAnnouncement.status === 'draft'" type="info">草稿</el-tag>
          <el-tag v-else type="warning">已归档</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="内容" :span="2">
          <div style="white-space: pre-wrap; line-height: 1.6">
            {{ currentAnnouncement.content }}
          </div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAnnouncementList,
  getAnnouncementDetail,
  createAnnouncement,
  updateAnnouncement,
  publishAnnouncement,
  archiveAnnouncement,
  deleteAnnouncement
} from '@/api/admin'

const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const dialogTitle = ref('发布公告')
const isEdit = ref(false)
const submitting = ref(false)

const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  type: '',
  priority: '',
  targetRole: '',
  status: ''
})

const announcementForm = reactive({
  id: null,
  title: '',
  content: '',
  type: 'system',
  priority: 'medium',
  targetRole: 'all',
  status: 'published',
  publisherId: null,
  publisherName: ''
})

const announcementList = ref([])
const total = ref(0)
const currentAnnouncement = ref(null)

// 加载公告列表
const loadAnnouncementList = async () => {
  try {
    const res = await getAnnouncementList(queryParams)
    const data = res.data || res
    if (data.success !== false) {
      announcementList.value = data.list || []
      total.value = data.total || 0
    } else {
      ElMessage.error(data.message || '加载失败')
    }
  } catch (error) {
    console.error('加载公告列表失败:', error)
    ElMessage.error('加载失败')
  }
}

// 重置查询
const resetQuery = () => {
  queryParams.page = 1
  queryParams.keyword = ''
  queryParams.type = ''
  queryParams.priority = ''
  queryParams.targetRole = ''
  queryParams.status = ''
  loadAnnouncementList()
}

// 显示添加对话框
const showAddDialog = () => {
  isEdit.value = false
  dialogTitle.value = '发布公告'
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  announcementForm.publisherId = userInfo.id
  announcementForm.publisherName = userInfo.realName || userInfo.username
  dialogVisible.value = true
}

// 编辑公告
const editAnnouncement = async (row) => {
  try {
    const res = await getAnnouncementDetail(row.id)
    const data = res.data || res
    if (data.success !== false) {
      isEdit.value = true
      dialogTitle.value = '编辑公告'
      Object.assign(announcementForm, data)
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('加载公告详情失败:', error)
    ElMessage.error('加载失败')
  }
}

// 查看公告
const viewAnnouncement = async (row) => {
  try {
    const res = await getAnnouncementDetail(row.id)
    const data = res.data || res
    if (data.success !== false) {
      currentAnnouncement.value = data
      viewDialogVisible.value = true
    }
  } catch (error) {
    console.error('加载公告详情失败:', error)
    ElMessage.error('加载失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!announcementForm.title) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!announcementForm.content) {
    ElMessage.warning('请输入内容')
    return
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      const res = await updateAnnouncement(announcementForm.id, announcementForm)
      const data = res.data || res
      if (data.success !== false) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        loadAnnouncementList()
      } else {
        ElMessage.error(data.message || '更新失败')
      }
    } else {
      const res = await createAnnouncement(announcementForm)
      const data = res.data || res
      if (data.success !== false) {
        ElMessage.success('创建成功')
        dialogVisible.value = false
        loadAnnouncementList()
      } else {
        ElMessage.error(data.message || '创建失败')
      }
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

// 发布公告
const handlePublish = async (id) => {
  try {
    await ElMessageBox.confirm('确定要发布这条公告吗？', '提示', {
      type: 'warning'
    })
    const res = await publishAnnouncement(id)
    const data = res.data || res
    if (data.success !== false) {
      ElMessage.success('发布成功')
      loadAnnouncementList()
    } else {
      ElMessage.error(data.message || '发布失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布失败:', error)
      ElMessage.error('发布失败')
    }
  }
}

// 归档公告
const handleArchive = async (id) => {
  try {
    await ElMessageBox.confirm('确定要归档这条公告吗？', '提示', {
      type: 'warning'
    })
    const res = await archiveAnnouncement(id)
    const data = res.data || res
    if (data.success !== false) {
      ElMessage.success('归档成功')
      loadAnnouncementList()
    } else {
      ElMessage.error(data.message || '归档失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('归档失败:', error)
      ElMessage.error('归档失败')
    }
  }
}

// 删除公告
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条公告吗？删除后无法恢复！', '提示', {
      type: 'error'
    })
    const res = await deleteAnnouncement(id)
    const data = res.data || res
    if (data.success !== false) {
      ElMessage.success('删除成功')
      loadAnnouncementList()
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
  Object.assign(announcementForm, {
    id: null,
    title: '',
    content: '',
    type: 'system',
    priority: 'medium',
    targetRole: 'all',
    status: 'published',
    publisherId: null,
    publisherName: ''
  })
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 获取角色文本
const getRoleText = (role) => {
  const map = {
    all: '所有人',
    student: '学生',
    teacher: '教师',
    admin: '管理员'
  }
  return map[role] || role
}

onMounted(() => {
  loadAnnouncementList()
})
</script>

<style scoped>
.announcements-page {
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

:deep(.el-card) {
  background-color: var(--bg-float);
  border-color: var(--border);
}

:deep(.el-card__header) {
  background-color: var(--bg-secondary);
  color: var(--text-primary);
  font-weight: 600;
}

:deep(.el-table) {
  background-color: var(--bg-float);
  color: var(--text-primary);
}

:deep(.el-table th) {
  background-color: var(--bg-secondary);
  color: var(--text-primary);
}

:deep(.el-table tr) {
  background-color: var(--bg-float);
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background-color: var(--bg-secondary);
}

:deep(.el-pagination) {
  color: var(--text-primary);
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

:deep(.el-textarea__inner) {
  background-color: var(--bg-secondary);
  color: var(--text-primary);
}
</style>
