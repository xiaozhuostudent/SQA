<template>
  <div class="livestream-page">
    <div class="page-header">
      <h2>我的直播</h2>
      <el-button type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon>
        创建直播
      </el-button>
    </div>

    <!-- 直播状态标签页 -->
    <el-tabs v-model="activeTab" @tab-change="loadLiveStreams">
      <el-tab-pane label="全部" name="all"></el-tab-pane>
      <el-tab-pane label="预定中" name="scheduled"></el-tab-pane>
      <el-tab-pane label="直播中" name="live"></el-tab-pane>
      <el-tab-pane label="已结束" name="ended"></el-tab-pane>
    </el-tabs>

    <!-- 直播列表 -->
    <div v-loading="loading" class="livestream-list">
      <el-empty v-if="liveStreams.length === 0" description="暂无直播"></el-empty>
      
      <el-card v-for="stream in liveStreams" :key="stream.id" class="stream-card" shadow="hover">
        <div class="stream-cover">
          <img v-if="stream.coverImage" :src="stream.coverImage" alt="封面" />
          <div v-else class="default-cover">
            <el-icon size="64"><VideoCamera /></el-icon>
          </div>
          <el-tag :type="getStatusType(stream.status)" class="status-tag">
            {{ getStatusText(stream.status) }}
          </el-tag>
        </div>
        
        <div class="stream-content">
          <h3>{{ stream.title }}</h3>
          <p class="description">{{ stream.description }}</p>
          
          <div class="stream-info">
            <div class="info-item">
              <el-icon><User /></el-icon>
              <span>{{ stream.courseName || '无关联课程' }}</span>
            </div>
            <div class="info-item">
              <el-icon><Clock /></el-icon>
              <span>{{ formatDateTime(stream.scheduledTime) }}</span>
            </div>
            <div class="info-item">
              <el-icon><View /></el-icon>
              <span>{{ stream.viewerCount || 0 }} 人在线 / {{ stream.totalViews || 0 }} 累计观看</span>
            </div>
          </div>

          <div class="stream-actions">
            <el-button v-if="stream.status === 'scheduled'" type="success" @click="startLive(stream)">
              <el-icon><VideoPlay /></el-icon>
              开始直播
            </el-button>
            <el-button v-if="stream.status === 'live'" type="danger" @click="endLive(stream)">
              <el-icon><VideoPause /></el-icon>
              结束直播
            </el-button>
            <el-button v-if="stream.status === 'live'" type="primary" @click="enterLiveRoom(stream)">
              <el-icon><Monitor /></el-icon>
              进入直播间
            </el-button>
            <el-button v-if="stream.status === 'ended'" type="primary" @click="viewWordCloud(stream)">
              <el-icon><PieChart /></el-icon>
              分析
            </el-button>
            <el-button @click="editStream(stream)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" text @click="deleteStream(stream)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 创建/编辑直播对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="直播标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入直播标题" />
        </el-form-item>
        
        <el-form-item label="直播描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入直播描述"
          />
        </el-form-item>
        
        <el-form-item label="关联课程">
          <el-select v-model="formData.courseId" placeholder="请选择课程（可选）" clearable filterable>
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="预定时间" prop="scheduledTime">
          <el-date-picker
            v-model="formData.scheduledTime"
            type="datetime"
            placeholder="选择直播时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        
        <el-form-item label="封面图片">
          <el-input v-model="formData.coverImage" placeholder="请输入封面图片URL（可选）" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, VideoCamera, User, Clock, View, VideoPlay, VideoPause,
  Monitor, Edit, Delete, PieChart
} from '@element-plus/icons-vue'
import * as livestreamApi from '@/api/livestream'
import * as courseApi from '@/api/course'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const activeTab = ref('all')
const liveStreams = ref([])
const courses = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('创建直播')
const submitting = ref(false)
const formRef = ref(null)

const formData = reactive({
  id: null,
  title: '',
  description: '',
  courseId: null,
  courseName: '',
  scheduledTime: '',
  coverImage: ''
})

const rules = {
  title: [{ required: true, message: '请输入直播标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入直播描述', trigger: 'blur' }],
  scheduledTime: [{ required: true, message: '请选择直播时间', trigger: 'change' }]
}

// 修复日期选择器的格式
const fixDatePickerFormat = (dateStr) => {
  if (!dateStr) return null;
  // 确保日期字符串格式为 yyyy-MM-dd HH:mm:ss
  if (typeof dateStr === 'string' && dateStr.includes('T')) {
    return dateStr.replace('T', ' ').substring(0, 19);
  }
  return dateStr;
};

onMounted(() => {
  loadLiveStreams()
  loadCourses()
})

const loadLiveStreams = async () => {
  loading.value = true
  try {
    let res
    if (activeTab.value === 'all') {
      res = await livestreamApi.getTeacherLiveStreams(userStore.userInfo.id)
    } else {
      res = await livestreamApi.getLiveStreamsByStatus(activeTab.value)
      // 过滤出当前教师的直播
      if (res.code === 200 && res.data) {
        res.data = res.data.filter(stream => stream.teacherId === userStore.userInfo.id)
      }
    }
    
    if (res.code === 200) {
      liveStreams.value = res.data || []
    } else {
      ElMessage.error(res.message || '加载直播列表失败')
    }
  } catch (error) {
    console.error('加载直播列表失败:', error)
    ElMessage.error('加载直播列表失败')
  } finally {
    loading.value = false
  }
}

const loadCourses = async () => {
  try {
    const res = await courseApi.getTeacherCourses(userStore.userInfo.id)
    if (res.code === 200) {
      courses.value = res.data || []
    }
  } catch (error) {
    console.error('加载课程列表失败:', error)
  }
}

const showCreateDialog = () => {
  dialogTitle.value = '创建直播'
  resetForm()
  dialogVisible.value = true
}

const editStream = (stream) => {
  dialogTitle.value = '编辑直播'
  formData.id = stream.id
  formData.title = stream.title
  formData.description = stream.description
  formData.courseId = stream.courseId
  // 处理日期格式
  if (stream.scheduledTime) {
    if (typeof stream.scheduledTime === 'string') {
      formData.scheduledTime = stream.scheduledTime;
    } else {
      formData.scheduledTime = new Date(stream.scheduledTime);
    }
  } else {
    formData.scheduledTime = '';
  }
  formData.coverImage = stream.coverImage
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const selectedCourse = courses.value.find(c => c.id === formData.courseId)
      
      // 正确处理日期格式
      let scheduledTimeValue = formData.scheduledTime;
      if (formData.scheduledTime && typeof formData.scheduledTime === 'string') {
        // 如果是字符串，检查是否需要调整格式
        if (formData.scheduledTime.includes('T')) {
          scheduledTimeValue = formData.scheduledTime.replace('T', ' ').substring(0, 19);
        }
      } else if (formData.scheduledTime instanceof Date) {
        // 如果是Date对象，转换为指定格式的字符串
        scheduledTimeValue = formData.scheduledTime.toISOString().slice(0, 19).replace('T', ' ');
      }
      
      // 构造正确的payload
      const payload = {
        title: formData.title,
        description: formData.description,
        teacherId: userStore.userInfo.id,
        teacherName: userStore.userInfo.realName || userStore.userInfo.name || userStore.userInfo.nickname,
        courseId: formData.courseId || null,
        courseName: selectedCourse ? selectedCourse.name : null,
        scheduledTime: scheduledTimeValue,
        coverImage: formData.coverImage || null
      };
      
      let res
      if (formData.id) {
        res = await livestreamApi.updateLiveStream(payload)
      } else {
        res = await livestreamApi.createLiveStream(payload)
      }
      
      if (res.code === 200) {
        ElMessage.success(formData.id ? '更新成功' : '创建成功')
        dialogVisible.value = false
        loadLiveStreams()
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    } catch (error) {
      console.error('提交失败:', error)
      ElMessage.error('操作失败: ' + (error.message || error.toString()))
    } finally {
      submitting.value = false
    }
  })
}

const startLive = async (stream) => {
  try {
    await ElMessageBox.confirm(
      '确定要开始这场直播吗？',
      '提示',
      { type: 'info' }
    )
    
    const res = await livestreamApi.startLiveStream(stream.id)
    if (res.code === 200) {
      ElMessage.success('直播已开始')
      loadLiveStreams()
      // 进入直播间
      enterLiveRoom(stream)
    } else {
      ElMessage.error(res.message || '开始直播失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('开始直播失败:', error)
      ElMessage.error('开始直播失败')
    }
  }
}

const endLive = async (stream) => {
  try {
    await ElMessageBox.confirm(
      '确定要结束这场直播吗？',
      '提示',
      { type: 'warning' }
    )
    
    const res = await livestreamApi.endLiveStream(stream.id)
    if (res.code === 200) {
      ElMessage.success('直播已结束')
      loadLiveStreams()
    } else {
      ElMessage.error(res.message || '结束直播失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('结束直播失败:', error)
      ElMessage.error('结束直播失败')
    }
  }
}

const enterLiveRoom = (stream) => {
  router.push({
    name: 'TeacherLiveRoom',
    params: { id: stream.id }
  })
}

const viewWordCloud = (stream) => {
  router.push({
    name: 'TeacherLiveWordCloud',
    params: { id: stream.id }
  })
}

const deleteStream = async (stream) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这场直播吗？此操作不可恢复。',
      '提示',
      { type: 'warning' }
    )
    
    const res = await livestreamApi.deleteLiveStream(stream.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadLiveStreams()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const resetForm = () => {
  formData.id = null
  formData.title = ''
  formData.description = ''
  formData.courseId = null
  formData.courseName = ''
  formData.scheduledTime = ''
  formData.coverImage = ''
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const getStatusType = (status) => {
  const types = {
    scheduled: 'info',
    live: 'success',
    ended: 'info'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    scheduled: '预定中',
    live: '直播中',
    ended: '已结束'
  }
  return texts[status] || status
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}
</script>

<style scoped>
.livestream-page {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.livestream-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.stream-card {
  display: flex;
  flex-direction: column;
}

.stream-cover {
  position: relative;
  width: 100%;
  height: 200px;
  background: var(--bg-secondary, #f5f5f5);
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stream-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.default-cover {
  color: #ccc;
}

.status-tag {
  position: absolute;
  top: 12px;
  right: 12px;
}

.stream-content {
  padding: 16px 0;
}

.stream-content h3 {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 600;
}

.description {
  color: var(--text-secondary, #666);
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.stream-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-secondary, #666);
}

.stream-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .livestream-list {
    grid-template-columns: 1fr;
  }
}
</style>
