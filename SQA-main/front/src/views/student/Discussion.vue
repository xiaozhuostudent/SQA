<template>
  <div class="discussion-container">
    <el-card class="header-card">
      <template #header>
        <div class="card-header">
          <h2>💬 课程讨论</h2>
          <el-button type="primary" @click="showCreateDialog">发起讨论</el-button>
        </div>
      </template>

      <!-- 卡片式布局 -->
      <div v-loading="loading" class="discussion-grid">
        <el-card 
          v-for="item in discussionList" 
          :key="item.id"
          class="discussion-card" 
          shadow="hover"
          @click="viewDiscussion(item)"
        >
          <div class="card-content">
            <!-- 头部区域 -->
            <div class="card-header-area">
              <el-tag :type="getTopicTypeColor(item.topicType)" size="small" class="type-tag">
                {{ getTopicTypeText(item.topicType) }}
              </el-tag>
              <el-tag v-if="item.isPinned" type="warning" size="small">📌 置顶</el-tag>
              <el-tag v-else-if="item.isResolved" type="success" size="small">✓ 已解决</el-tag>
              <el-tag v-else type="info" size="small">💬 进行中</el-tag>
            </div>

            <!-- 标题区域 -->
            <h3 class="card-title">{{ item.title }}</h3>

            <!-- 内容预览 -->
            <p class="card-preview">{{ item.content?.substring(0, 100) }}{{ item.content?.length > 100 ? '...' : '' }}</p>

            <!-- 底部信息区域 -->
            <div class="card-footer-area">
              <div class="author-info">
                <span class="author-name">👤 {{ item.authorName }}</span>
                <span class="create-time">🕒 {{ item.createTime }}</span>
              </div>
              <div class="stats-info">
                <span class="stat-item">👀 {{ item.viewCount || 0 }}</span>
                <span class="stat-item">💬 {{ item.replyCount || 0 }}</span>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 空状态 -->
        <div v-if="!loading && discussionList.length === 0" class="empty-state">
          <el-empty description="暂无讨论，点击右上角发起新讨论" />
        </div>
      </div>

      <el-pagination
        v-if="discussionList.length > 0"
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        style="margin-top: 20px; justify-content: center;"
      />
    </el-card>

    <!-- 创建讨论对话框 -->
    <el-dialog v-model="createDialogVisible" title="发起讨论" width="600px">
      <el-form :model="discussionForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="discussionForm.title" placeholder="请输入讨论标题" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="discussionForm.topicType" placeholder="请选择">
            <el-option label="❓ 提问" value="question" />
            <el-option label="💡 讨论" value="discussion" />
            <el-option label="📢 分享" value="sharing" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="discussionForm.content" type="textarea" :rows="6" placeholder="请输入讨论内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createDiscussion">发布</el-button>
      </template>
    </el-dialog>

    <!-- 讨论详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="讨论详情" width="80%" top="5vh">
      <div v-if="currentDiscussion" class="discussion-detail">
        <div class="discussion-header">
          <h3>{{ currentDiscussion.title }}</h3>
          <div class="discussion-meta">
            <el-tag :type="getTopicTypeColor(currentDiscussion.topicType)" size="small">
              {{ getTopicTypeText(currentDiscussion.topicType) }}
            </el-tag>
            <span class="author">{{ currentDiscussion.authorName }}</span>
            <span class="time">{{ currentDiscussion.createTime }}</span>
            <span class="stats">👀 {{ currentDiscussion.viewCount }} 💬 {{ currentDiscussion.replyCount }}</span>
          </div>
        </div>
        <div class="discussion-content">
          {{ currentDiscussion.content }}
        </div>

        <el-divider content-position="left">
          <h4>回复列表 ({{ replyList.length }})</h4>
        </el-divider>

        <div v-for="reply in replyList" :key="reply.id" class="reply-item">
          <div class="reply-header">
            <div class="reply-author">
              <strong>{{ reply.authorName || '匿名用户' }}</strong>
              <el-tag v-if="reply.authorRole === 'teacher'" type="warning" size="small" style="margin-left: 8px">教师</el-tag>
              <el-tag v-else-if="reply.authorRole === 'admin'" type="danger" size="small" style="margin-left: 8px">管理员</el-tag>
              <el-tag v-else-if="reply.authorRole === 'student'" type="primary" size="small" style="margin-left: 8px">学生</el-tag>
            </div>
            <span class="reply-time">{{ reply.createTime }}</span>
          </div>
          <div class="reply-content">{{ reply.content }}</div>
          <div class="reply-actions">
            <el-button size="small" text @click="likeReply(reply)">
              👍 {{ reply.likeCount || 0 }}
            </el-button>
            <el-button v-if="userStore.userInfo?.id && reply.authorId === userStore.userInfo.id" 
              size="small" type="danger" text @click="deleteReply(reply)">
              🗑️ 删除
            </el-button>
            <el-tag v-if="reply.isAccepted" type="success" size="small">已采纳</el-tag>
          </div>
        </div>

        <el-divider />

        <el-form>
          <el-form-item label="我的回复">
            <el-input v-model="replyContent" type="textarea" :rows="4" placeholder="输入你的回复..." />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitReply">提交回复</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import discussionApi from '@/api/discussion'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const discussionList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const createDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentDiscussion = ref(null)
const replyList = ref([])
const replyContent = ref('')

const discussionForm = ref({
  title: '',
  topicType: 'question',
  content: '',
  courseId: 1 // 示例课程ID
})

const loadDiscussions = async () => {
  loading.value = true
  try {
    const res = await discussionApi.getDiscussions()
    discussionList.value = res.data || []
    
    // 同步每个讨论的实际回复数
    for (const discussion of discussionList.value) {
      try {
        const repliesRes = await discussionApi.getReplies(discussion.id)
        discussion.replyCount = repliesRes.data?.length || 0
      } catch (err) {
        console.error(`获取讨论${discussion.id}的回复数失败:`, err)
      }
    }
    
    total.value = discussionList.value.length
  } catch (error) {
    console.error('加载讨论列表失败:', error)
    ElMessage.error('加载讨论列表失败')
  } finally {
    loading.value = false
  }
}

const showCreateDialog = () => {
  discussionForm.value = {
    title: '',
    topicType: 'question',
    content: '',
    courseId: 1
  }
  createDialogVisible.value = true
}

const createDiscussion = async () => {
  if (!discussionForm.value.title) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!discussionForm.value.content) {
    ElMessage.warning('请输入内容')
    return
  }
  
  try {
    // 学生角色固定为 'student'
    await discussionApi.createDiscussion({
      ...discussionForm.value,
      authorId: userStore.userInfo?.id || 1,
      authorName: userStore.userInfo?.realName || '学生',
      authorRole: 'student'
    })
    ElMessage.success('发布成功')
    createDialogVisible.value = false
    loadDiscussions()
  } catch (error) {
    console.error('发布失败:', error)
    ElMessage.error('发布失败')
  }
}

const viewDiscussion = async (row) => {
  currentDiscussion.value = { ...row }
  detailDialogVisible.value = true
  
  try {
    const res = await discussionApi.getReplies(row.id)
    replyList.value = res.data || []
    
    // 更新实际回复数
    currentDiscussion.value.replyCount = replyList.value.length
    
    // 同步更新列表中的回复数
    const discussionInList = discussionList.value.find(d => d.id === row.id)
    if (discussionInList) {
      discussionInList.replyCount = replyList.value.length
    }
  } catch (error) {
    console.error('加载回复失败:', error)
    replyList.value = []
    ElMessage.error('加载回复失败')
  }
}

const submitReply = async () => {
  if (!replyContent.value) {
    ElMessage.warning('请输入回复内容')
    return
  }
  try {
    await discussionApi.createReply({
      discussionId: currentDiscussion.value.id,
      content: replyContent.value,
      authorId: userStore.userInfo?.id || 1,
      authorName: userStore.userInfo?.realName || '学生',
      authorRole: 'student'
    })
    ElMessage.success('回复成功')
    replyContent.value = ''
    
    // 刷新回复列表
    const res = await discussionApi.getReplies(currentDiscussion.value.id)
    replyList.value = res.data || []
    
    // 更新回复数
    currentDiscussion.value.replyCount++
    
    // 同步更新讨论列表中的回复数
    const discussionInList = discussionList.value.find(d => d.id === currentDiscussion.value.id)
    if (discussionInList) {
      discussionInList.replyCount++
    }
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error('回复失败')
  }
}

const likeReply = async (reply) => {
  try {
    await discussionApi.likeReply(reply.id)
    ElMessage.success('点赞成功')
    reply.likeCount++
  } catch (error) {
    console.error('点赞失败:', error)
    ElMessage.error('点赞失败')
  }
}

const deleteReply = async (reply) => {
  try {
    await ElMessageBox.confirm('确定要删除这条回复吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await discussionApi.deleteReply(currentDiscussion.value.id, reply.id)
    ElMessage.success('删除成功')
    
    // 从列表中移除
    const index = replyList.value.findIndex(r => r.id === reply.id)
    if (index > -1) {
      replyList.value.splice(index, 1)
    }
    
    // 更新回复数
    if (currentDiscussion.value.replyCount > 0) {
      currentDiscussion.value.replyCount--
    }
    
    // 刷新讨论列表中的回复数
    const discussionInList = discussionList.value.find(d => d.id === currentDiscussion.value.id)
    if (discussionInList && discussionInList.replyCount > 0) {
      discussionInList.replyCount--
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const getTopicTypeText = (type) => {
  const texts = {
    question: '提问',
    discussion: '讨论',
    sharing: '分享',
    notice: '通知'
  }
  return texts[type] || type
}

const getTopicTypeColor = (type) => {
  const colors = {
    question: 'warning',
    discussion: 'primary',
    sharing: 'success',
    notice: 'danger'
  }
  return colors[type] || 'info'
}

onMounted(() => {
  loadDiscussions()
})
</script>

<style scoped>
.discussion-container {
  padding: 20px;
  background: var(--bg-primary, #f5f7fa);
  min-height: 100vh;
}

.header-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary, #303133);
}

/* 卡片网格布局 */
.discussion-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  min-height: 200px;
}

.empty-state {
  grid-column: 1 / -1;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

/* 讨论卡片样式 */
.discussion-card {
  cursor: pointer;
  transition: all 0.3s ease;
  height: 100%;
  border-radius: 12px;
  border: 1px solid var(--border, #e4e7ed);
}

.discussion-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15) !important;
}

.card-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.card-header-area {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.type-tag {
  font-weight: 600;
}

.card-title {
  margin: 0 0 12px 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary, #303133);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  min-height: 50px;
}

.card-preview {
  color: var(--text-secondary, #606266);
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 16px 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  min-height: 60px;
}

.card-footer-area {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--border, #e4e7ed);
  margin-top: auto;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-size: 13px;
  color: var(--text-primary, #303133);
  font-weight: 500;
}

.create-time {
  font-size: 12px;
  color: var(--text-muted, #909399);
}

.stats-info {
  display: flex;
  gap: 12px;
  align-items: center;
}

.stat-item {
  font-size: 13px;
  color: var(--text-secondary, #606266);
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 讨论详情样式 */
.discussion-detail {
  color: var(--text-primary, #303133);
}

.discussion-header h3 {
  margin: 0 0 10px 0;
  color: var(--text-primary, #303133);
  font-size: 20px;
}

.discussion-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  color: var(--text-muted, #909399);
  font-size: 14px;
  padding: 10px 0;
  border-bottom: 1px solid var(--border, #dcdfe6);
}

.discussion-content {
  padding: 20px;
  background: var(--bg-secondary, #f9fafc);
  border-radius: 8px;
  margin: 20px 0;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}

.reply-item {
  padding: 15px;
  background: var(--bg-secondary, #f9fafc);
  border-radius: 8px;
  margin-bottom: 15px;
  transition: all 0.3s;
}

.reply-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  color: var(--text-primary, #303133);
  font-weight: 500;
}

.reply-author {
  display: flex;
  align-items: center;
}

.reply-time {
  color: var(--text-muted, #909399);
  font-size: 13px;
  font-weight: normal;
}

.reply-content {
  color: var(--text-secondary, #606266);
  line-height: 1.6;
  margin-bottom: 10px;
  white-space: pre-wrap;
  word-break: break-word;
}

.reply-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .discussion-grid {
    grid-template-columns: 1fr;
  }
}
</style>
