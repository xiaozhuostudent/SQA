<template>
  <div class="course-requests-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>课程请求审核</span>
          <div class="header-actions">
            <el-select v-model="filterStatus" placeholder="筛选状态" style="width: 150px" @change="loadRequests">
              <el-option label="全部" value="" />
              <el-option label="待审核" value="pending" />
              <el-option label="已批准" value="approved" />
              <el-option label="已拒绝" value="rejected" />
            </el-select>
            <el-select v-model="filterType" placeholder="筛选类型" style="width: 150px; margin-left: 10px" @change="loadRequests">
              <el-option label="全部" value="" />
              <el-option label="创建课程" value="create" />
              <el-option label="删除课程" value="delete" />
            </el-select>
          </div>
        </div>
      </template>

      <el-table :data="requests" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="requestType" label="申请类型" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.requestType === 'create' ? 'success' : 'danger'">
              {{ scope.row.requestType === 'create' ? '创建课程' : '删除课程' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="teacherName" label="申请教师" width="120" />
        <el-table-column prop="courseName" label="课程名称" width="180" />
        <el-table-column prop="courseCode" label="课程代码" width="120" />
        <el-table-column prop="reason" label="申请理由" show-overflow-tooltip min-width="200" />
        <el-table-column prop="fileName" label="附件" width="100">
          <template #default="scope">
            <el-button v-if="scope.row.fileKey" size="small" text @click="viewFile(scope.row)">
              查看
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag 
              :type="scope.row.status === 'approved' ? 'success' : 
                     scope.row.status === 'rejected' ? 'danger' : 'warning'"
            >
              {{ scope.row.status === 'approved' ? '已批准' : 
                 scope.row.status === 'rejected' ? '已拒绝' : '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button v-if="scope.row.status === 'pending'" 
                      size="small" 
                      type="success" 
                      @click="handleReview(scope.row, 'approve')">
              批准
            </el-button>
            <el-button v-if="scope.row.status === 'pending'" 
                      size="small" 
                      type="danger" 
                      @click="handleReview(scope.row, 'reject')">
              拒绝
            </el-button>
            <el-button size="small" @click="viewDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadRequests"
        @current-change="loadRequests"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 审核对话框 -->
    <el-dialog v-model="reviewDialogVisible" :title="reviewAction === 'approve' ? '批准申请' : '拒绝申请'" width="600px">
      <el-alert
        :title="reviewAction === 'approve' ? '确认批准此申请？' : '确认拒绝此申请？'"
        :type="reviewAction === 'approve' ? 'success' : 'warning'"
        :closable="false"
        style="margin-bottom: 20px"
      />
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="申请类型">
          <el-tag :type="currentRequest.requestType === 'create' ? 'success' : 'danger'">
            {{ currentRequest.requestType === 'create' ? '创建课程' : '删除课程' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请教师">{{ currentRequest.teacherName }}</el-descriptions-item>
        <el-descriptions-item label="课程名称">{{ currentRequest.courseName }}</el-descriptions-item>
        <el-descriptions-item label="课程代码">{{ currentRequest.courseCode }}</el-descriptions-item>
        <el-descriptions-item label="申请理由" :span="2">{{ currentRequest.reason }}</el-descriptions-item>
      </el-descriptions>

      <el-form :model="reviewForm" label-width="100px" style="margin-top: 20px">
        <el-form-item label="审核意见">
          <el-input
            v-model="reviewForm.comment"
            type="textarea"
            :rows="4"
            :placeholder="reviewAction === 'approve' ? '选填，可以留下批准意见' : '必填，请说明拒绝原因'"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button 
          :type="reviewAction === 'approve' ? 'success' : 'danger'" 
          @click="submitReview"
          :loading="reviewing"
        >
          确认{{ reviewAction === 'approve' ? '批准' : '拒绝' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="申请详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="申请ID">{{ currentRequest.id }}</el-descriptions-item>
        <el-descriptions-item label="申请类型">
          <el-tag :type="currentRequest.requestType === 'create' ? 'success' : 'danger'">
            {{ currentRequest.requestType === 'create' ? '创建课程' : '删除课程' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请教师">{{ currentRequest.teacherName }}</el-descriptions-item>
        <el-descriptions-item label="教师ID">{{ currentRequest.teacherId }}</el-descriptions-item>
        <el-descriptions-item label="课程名称">{{ currentRequest.courseName }}</el-descriptions-item>
        <el-descriptions-item label="课程代码">{{ currentRequest.courseCode }}</el-descriptions-item>
        
        <template v-if="currentRequest.requestType === 'create'">
          <el-descriptions-item label="学分">{{ currentRequest.credit }}</el-descriptions-item>
          <el-descriptions-item label="容量">{{ currentRequest.capacity }}</el-descriptions-item>
          <el-descriptions-item label="学期">{{ currentRequest.semester }}</el-descriptions-item>
          <el-descriptions-item label="类别">{{ currentRequest.category }}</el-descriptions-item>
          <el-descriptions-item label="课程简介" :span="2">
            {{ currentRequest.description || '无' }}
          </el-descriptions-item>
        </template>
        
        <el-descriptions-item label="申请理由" :span="2">
          {{ currentRequest.reason }}
        </el-descriptions-item>
        
        <el-descriptions-item label="附件" :span="2">
          <el-button v-if="currentRequest.fileKey" size="small" @click="viewFile(currentRequest)">
            {{ currentRequest.fileName }}
          </el-button>
          <span v-else>无</span>
        </el-descriptions-item>
        
        <el-descriptions-item label="状态">
          <el-tag 
            :type="currentRequest.status === 'approved' ? 'success' : 
                   currentRequest.status === 'rejected' ? 'danger' : 'warning'"
          >
            {{ currentRequest.status === 'approved' ? '已批准' : 
               currentRequest.status === 'rejected' ? '已拒绝' : '待审核' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentRequest.createTime }}</el-descriptions-item>
        
        <template v-if="currentRequest.status !== 'pending'">
          <el-descriptions-item label="审核管理员">{{ currentRequest.adminName }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ currentRequest.reviewTime }}</el-descriptions-item>
          <el-descriptions-item label="审核意见" :span="2">
            {{ currentRequest.adminComment || '无' }}
          </el-descriptions-item>
        </template>
      </el-descriptions>
    </el-dialog>

    <!-- 文件预览对话框 -->
    <el-dialog v-model="fileDialogVisible" title="附件预览" width="800px">
      <div v-if="fileContent" class="file-preview">
        <el-alert
          :title="`文件名：${currentFile.fileName}`"
          type="info"
          :closable="false"
          style="margin-bottom: 15px"
        />
        <div class="file-info">
          <p>文件已存储在Redis中，有效期7天</p>
          <p>如需下载，请复制base64内容后转换</p>
        </div>
        <el-input
          v-model="fileContent"
          type="textarea"
          :rows="15"
          readonly
          placeholder="文件内容（Base64编码）"
        />
      </div>
      <div v-else class="file-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <p>加载中...</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getAllRequests, reviewRequest, downloadRequestFile } from '@/api/courseRequest'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loading = ref(false)
const reviewing = ref(false)
const requests = ref([])
const currentRequest = ref({})
const currentFile = ref({})
const fileContent = ref('')

const filterStatus = ref('')
const filterType = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const reviewDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const fileDialogVisible = ref(false)
const reviewAction = ref('approve')

const reviewForm = ref({
  comment: ''
})

// 加载请求列表
const loadRequests = async () => {
  loading.value = true
  try {
    const params = {
      status: filterStatus.value,
      requestType: filterType.value,
      page: currentPage.value,
      pageSize: pageSize.value
    }
    
    const res = await getAllRequests(params)
    if (res.code === 200) {
      requests.value = res.data.list || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (error) {
    console.error('加载请求列表失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 处理审核
const handleReview = (request, action) => {
  currentRequest.value = request
  reviewAction.value = action
  reviewForm.value.comment = ''
  reviewDialogVisible.value = true
}

// 提交审核
const submitReview = async () => {
  if (reviewAction.value === 'reject' && !reviewForm.value.comment) {
    ElMessage.warning('请填写拒绝原因')
    return
  }
  
  reviewing.value = true
  try {
    const data = {
      requestId: currentRequest.value.id,
      action: reviewAction.value === 'approve' ? 'approved' : 'rejected',
      adminId: userStore.userInfo?.id,
      adminName: userStore.userInfo?.realName || userStore.userInfo?.username || '未知管理员',
      comment: reviewForm.value.comment
    }
    
    const res = await reviewRequest(data)
    if (res.code === 200) {
      ElMessage.success(reviewAction.value === 'approve' ? '已批准申请' : '已拒绝申请')
      reviewDialogVisible.value = false
      await loadRequests()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('操作失败')
  } finally {
    reviewing.value = false
  }
}

// 查看详情
const viewDetail = (request) => {
  currentRequest.value = request
  detailDialogVisible.value = true
}

// 查看文件
const viewFile = async (request) => {
  currentFile.value = request
  fileContent.value = ''
  fileDialogVisible.value = true
  
  try {
    const res = await downloadRequestFile(request.fileKey)
    if (res.code === 200) {
      fileContent.value = res.data.content
    } else {
      ElMessage.error('文件加载失败')
      fileDialogVisible.value = false
    }
  } catch (error) {
    console.error('加载文件失败:', error)
    ElMessage.error('文件加载失败')
    fileDialogVisible.value = false
  }
}

onMounted(() => {
  loadRequests()
})
</script>

<style scoped>
.course-requests-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.file-preview {
  padding: 10px;
}

.file-info {
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 15px;
}

.file-info p {
  margin: 5px 0;
  font-size: 14px;
  color: #606266;
}

.file-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.file-loading .el-icon {
  font-size: 40px;
  color: #409eff;
  margin-bottom: 15px;
}

.file-loading p {
  font-size: 14px;
  color: #909399;
}
</style>
