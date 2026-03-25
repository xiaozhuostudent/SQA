<template>
  <div class="users-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="showCreateDialog">添加用户</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="全部" style="width: 150px">
            <el-option label="全部" value="" />
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" style="width: 150px">
            <el-option label="全部" value="" />
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="inactive" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="姓名/账号" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadUsers">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 用户列表 -->
      <el-table :data="users" style="width: 100%" v-loading="loading" element-loading-text="加载用户数据中...">
        <el-table-column prop="username" label="账号" width="150" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="scope">
            <el-tag :type="getRoleType(scope.row.role)">
              {{ getRoleLabel(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="手机" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="280">
          <template #default="scope">
            <div class="action-buttons">
              <el-button 
                v-if="hasPermission('user:edit')"
                size="small" 
                type="primary"
                link
                @click="editUser(scope.row)">
                编辑
              </el-button>
              <el-button 
                v-if="hasPermission('user:edit')"
                size="small" 
                type="warning"
                link
                @click="resetPassword(scope.row)">
                重置密码
              </el-button>
              <el-button 
                v-if="hasPermission('user:edit')"
                size="small" 
                :type="getToggleButtonType(scope.row.status)"
                link
                @click="toggleStatus(scope.row)"
              >
                {{ getToggleButtonText(scope.row.status) }}
              </el-button>
              <el-button 
                v-if="hasPermission('user:delete')"
                size="small" 
                type="danger"
                link
                @click="handleDelete(scope.row)">
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadUsers"
        @size-change="loadUsers"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 创建/编辑用户对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="userForm" label-width="100px">
        <el-form-item label="账号">
          <el-input v-model="userForm.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="userForm.realName" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="userForm.role" style="width: 100%">
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="手机">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="初始密码" v-if="!isEdit">
          <el-input v-model="userForm.password" type="password" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getUserList, 
  createUser, 
  updateUser, 
  deleteUser as deleteUserApi,
  resetUserPassword,
  updateUserStatus
} from '@/api/admin'
import { hasPermission } from '@/utils/permission'

const loading = ref(false) // 页面加载状态
const dialogVisible = ref(false)
const isEdit = ref(false)

const searchForm = ref({
  role: '',
  status: '',
  keyword: ''
})

const users = ref([])
const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

const userForm = ref({
  username: '',
  realName: '',
  role: 'student',
  email: '',
  phone: '',
  password: ''
})

const dialogTitle = computed(() => isEdit.value ? '编辑用户' : '添加用户')

const getRoleType = (role) => {
  const types = { student: '', teacher: 'success', admin: 'danger' }
  return types[role] || ''
}

const getRoleLabel = (role) => {
  const labels = { student: '学生', teacher: '教师', admin: '管理员' }
  return labels[role] || role
}

const getStatusType = (status) => {
  // 明确判断status值，默认为active
  return (status === 'inactive') ? 'danger' : 'success'
}

const getStatusLabel = (status) => {
  // 明确判断status值，默认为启用
  return (status === 'inactive') ? '禁用' : '启用'
}

const getToggleButtonType = (status) => {
  // 如果是禁用状态，按钮显示为success（启用按钮）
  // 如果是启用状态，按钮显示为warning（禁用按钮）
  return (status === 'inactive') ? 'success' : 'warning'
}

const getToggleButtonText = (status) => {
  // 如果是禁用状态，按钮文字为"启用"
  // 如果是启用状态，按钮文字为"禁用"
  return (status === 'inactive') ? '启用' : '禁用'
}

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getUserList({ ...searchForm.value, ...pagination.value })
    if (res.code === 200) {
      users.value = res.data.list || res.data || []
      pagination.value.total = res.data.total || users.value.length
      
      // 调试：打印用户状态信息
      console.log('加载的用户列表:', users.value.map(u => ({
        username: u.username,
        realName: u.realName,
        status: u.status,
        statusType: typeof u.status
      })))
    }
  } catch (error) {
    console.error('加载用户失败:', error)
    ElMessage.error('加载用户失败，后端接口需要实现')
    users.value = []
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.value = { role: '', status: '', keyword: '' }
  loadUsers()
}

const showCreateDialog = () => {
  isEdit.value = false
  userForm.value = {
    username: '',
    realName: '',
    role: 'student',
    email: '',
    phone: '',
    password: ''
  }
  dialogVisible.value = true
}

const editUser = (user) => {
  isEdit.value = true
  userForm.value = { ...user }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (isEdit.value) {
      // 更新用户
      const res = await updateUser(userForm.value.id, userForm.value)
      if (res.code === 200) {
        ElMessage.success('用户信息更新成功')
        dialogVisible.value = false
        loadUsers()
      } else {
        ElMessage.error(res.message || '更新失败')
      }
    } else {
      // 创建用户
      const res = await createUser(userForm.value)
      if (res.code === 200) {
        ElMessage.success('用户创建成功')
        dialogVisible.value = false
        loadUsers()
      } else {
        ElMessage.error(res.message || '创建失败')
      }
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

const resetPassword = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置用户 ${user.realName}(${user.username}) 的密码吗？密码将被重置为：123456`, 
      '确认重置密码', 
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await resetUserPassword(user.id)
    if (res.code === 200) {
      ElMessage.success('密码已重置为：123456')
    } else {
      ElMessage.error(res.message || '重置失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置密码失败:', error)
      ElMessage.error(error.response?.data?.message || '重置密码失败')
    }
  }
}

const toggleStatus = async (user) => {
  // 明确获取当前状态，如果为空或active则视为active
  const currentStatus = (user.status === 'inactive') ? 'inactive' : 'active'
  const newStatus = currentStatus === 'active' ? 'inactive' : 'active'
  const action = newStatus === 'active' ? '启用' : '禁用'
  
  console.log('切换状态:', {
    username: user.username,
    currentStatus,
    newStatus,
    action,
    originalStatus: user.status
  })
  
  try {
    await ElMessageBox.confirm(
      `确定要${action}用户 ${user.realName}(${user.username}) 吗？${newStatus === 'inactive' ? '禁用后该用户将无法登录系统。' : ''}`, 
      `确认${action}`, 
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    loading.value = true
    const res = await updateUserStatus(user.id, newStatus)
    if (res.code === 200) {
      ElMessage.success(`${action}成功`)
      // 立即刷新列表以显示最新状态
      await loadUsers()
    } else {
      ElMessage.error(res.message || `${action}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('状态切换失败:', error)
      ElMessage.error(error.response?.data?.message || '状态切换失败')
    }
  } finally {
    loading.value = false
  }
}

const handleDelete = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 ${user.realName}(${user.username}) 吗？此操作不可恢复！`, 
      '确认删除', 
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error',
        buttonSize: 'default'
      }
    )
    
    const res = await deleteUserApi(user.id)
    if (res.code === 200) {
      ElMessage.success('用户删除成功')
      loadUsers()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
      ElMessage.error(error.response?.data?.message || '删除用户失败')
    }
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.users-page {
  padding: 24px;
  background-color: var(--bg-primary);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 24px;
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

:deep(.el-table th.el-table__cell) {
  background-color: var(--bg-secondary);
  color: var(--text-primary);
}

:deep(.el-table td.el-table__cell) {
  border-color: var(--border);
}

:deep(.el-pagination) {
  margin-top: 24px;
}

:deep(.el-dialog) {
  background-color: var(--bg-float);
}

:deep(.el-dialog__header) {
  background-color: var(--bg-secondary);
}

:deep(.el-input__wrapper) {
  background-color: var(--bg-secondary);
  color: var(--text-primary);
}

:deep(.el-form-item__label) {
  color: var(--text-primary);
  font-weight: 500;
}

.action-buttons {
  display: flex;
  flex-wrap: nowrap;
  gap: 8px;
  align-items: center;
  white-space: nowrap;
}

.action-buttons .el-button {
  margin: 0 !important;
  padding: 5px 12px;
  flex-shrink: 0;
}
</style>
