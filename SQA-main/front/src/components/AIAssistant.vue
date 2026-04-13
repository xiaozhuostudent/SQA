<template>
  <div class="ai-assistant-modal" v-if="visible" @click.self="closeModal">
    <div class="ai-assistant-container">
      <!-- 头部 -->
      <div class="ai-header">
        <div class="header-left">
          <i class="el-icon-s-cooperation"></i>
          <span>AI智能助手</span>
        </div>
        <div class="header-right">
          <el-button size="small" icon="el-icon-plus" @click="createNewSession">新对话</el-button>
          <el-button size="small" icon="el-icon-close" @click="closeModal">关闭</el-button>
        </div>
      </div>

      <div class="ai-body">
        <!-- 左侧历史会话列表 -->
        <div class="sessions-sidebar">
          <div class="sessions-header">历史对话</div>
          <div class="sessions-list">
            <div
              v-for="session in sessions"
              :key="session.session_id"
              class="session-item"
              :class="{ active: currentSessionId === session.session_id }"
              @click="loadSession(session.session_id)"
            >
              <div class="session-title">{{ session.session_title }}</div>
              <div class="session-preview">{{ session.first_message || '新对话' }}</div>
              <div class="session-time">{{ formatTime(session.last_time) }}</div>
            </div>
          </div>
        </div>

        <!-- 右侧聊天区域 -->
        <div class="chat-area">
          <!-- 示例问题 -->
          <div class="example-queries" v-if="messages.length === 0">
            <h4>💡 快速开始：</h4>
            <div class="example-buttons">
              <el-button
                v-for="(example, index) in exampleQueries"
                :key="index"
                size="small"
                type="success"
                plain
                @click="sendExample(example)"
              >
                {{ example }}
              </el-button>
            </div>
          </div>

          <!-- 消息列表 -->
          <div class="messages-container" ref="messagesContainer">
            <div v-for="(msg, index) in messages" :key="index" class="message-item" :class="msg.role">
              <div class="message-avatar">
                <i :class="msg.role === 'user' ? 'el-icon-user' : 'el-icon-s-cooperation'"></i>
              </div>
              <div class="message-content">
                <div class="message-text" v-html="formatMessage(msg.content)"></div>
              </div>
            </div>
            <div v-if="loading" class="message-item assistant">
              <div class="message-avatar">
                <i class="el-icon-loading"></i>
              </div>
              <div class="message-content">
                <div class="message-text">正在思考中...</div>
              </div>
            </div>
          </div>

          <!-- 输入区域 -->
          <div class="input-area">
            <el-upload
              class="upload-btn"
              action="#"
              :before-upload="handleFileUpload"
              :show-file-list="false"
              accept=".pdf,.doc,.docx,.xls,.xlsx,.jpg,.jpeg,.png,.txt"
            >
              <el-button size="small" icon="el-icon-paperclip" circle></el-button>
            </el-upload>
            <el-input
              v-model="inputMessage"
              placeholder="输入你的问题..."
              @keyup.enter.native="sendMessage"
              :disabled="loading"
            ></el-input>
            <el-button type="primary" @click="sendMessage" :loading="loading" icon="el-icon-s-promotion">
              发送
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'AIAssistant',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    role: {
      type: String,
      required: true,
      validator: val => ['student', 'teacher', 'admin'].includes(val)
    },
    userId: {
      type: [String, Number],
      default: null
    },
    username: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      sessions: [],
      currentSessionId: null,
      messages: [],
      inputMessage: '',
      loading: false,
      uploadedFile: null,
      exampleQueriesByRole: {
        student: ['我是谁？', '我选了哪些课程？'],
        teacher: ['我是谁？', '我教授哪些课程？'],
        admin: ['系统中有多少课程？', '当前有多少学生？']
      }
    }
  },
  computed: {
    exampleQueries() {
      return this.exampleQueriesByRole[this.role] || []
    },
    apiBaseUrl() {
      return 'http://localhost:5052'
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.loadSessions()
      }
    }
  },
  methods: {
    closeModal() {
      this.$emit('update:visible', false)
    },
    
    async loadSessions() {
      try {
        const response = await axios.get(`${this.apiBaseUrl}/sessions`, {
          params: {
            userId: this.userId,
            username: this.username,
            role: this.role
          }
        })
        if (response.data.success) {
          this.sessions = response.data.sessions
          if (this.sessions.length > 0 && !this.currentSessionId) {
            this.loadSession(this.sessions[0].session_id)
          }
        }
      } catch (error) {
        this.$message.error('加载历史会话失败')
        console.error(error)
      }
    },

    async createNewSession() {
      try {
        const response = await axios.post(`${this.apiBaseUrl}/sessions`, {
          userId: this.userId,
          username: this.username,
          role: this.role,
          title: '新对话'
        })
        if (response.data.success) {
          this.currentSessionId = response.data.session_id
          this.messages = []
          await this.loadSessions()
        }
      } catch (error) {
        this.$message.error('创建会话失败')
        console.error(error)
      }
    },

    async loadSession(sessionId) {
      try {
        this.currentSessionId = sessionId
        const response = await axios.get(`${this.apiBaseUrl}/history`, {
          params: {
            userId: this.userId,
            username: this.username,
            role: this.role,
            sessionId: sessionId
          }
        })
        if (response.data.success) {
          this.messages = response.data.messages
          this.$nextTick(() => {
            this.scrollToBottom()
          })
        }
      } catch (error) {
        this.$message.error('加载会话历史失败')
        console.error(error)
      }
    },

    async handleFileUpload(file) {
      const formData = new FormData()
      formData.append('file', file)
      
      try {
        const response = await axios.post(`${this.apiBaseUrl}/upload`, formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        if (response.data.success) {
          this.uploadedFile = response.data
          this.$message.success('文件上传成功')
          this.inputMessage = `[已上传文件: ${response.data.file_name}] `
        }
      } catch (error) {
        this.$message.error('文件上传失败')
        console.error(error)
      }
      return false
    },

    sendExample(example) {
      this.inputMessage = example
      this.sendMessage()
    },

    async sendMessage() {
      if (!this.inputMessage.trim() && !this.uploadedFile) return
      
      if (!this.userId) {
        this.$message.error('用户信息未加载，请刷新页面后重试')
        return
      }
      
      if (!this.currentSessionId) {
        await this.createNewSession()
      }

      const userMessage = this.inputMessage
      this.messages.push({
        role: 'user',
        content: userMessage
      })
      this.inputMessage = ''
      this.loading = true
      
      this.$nextTick(() => {
        this.scrollToBottom()
      })

      try {
        const response = await axios.post(`${this.apiBaseUrl}/chat`, {
          message: userMessage,
          role: this.role,
          userId: this.userId,
          username: this.username,
          sessionId: this.currentSessionId,
          sessionTitle: this.sessions.find(s => s.session_id === this.currentSessionId)?.session_title || '新对话'
        })
        
        if (response.data.success) {
          this.messages.push({
            role: 'assistant',
            content: response.data.response
          })
          this.uploadedFile = null
          await this.loadSessions()
        } else {
          throw new Error(response.data.error)
        }
      } catch (error) {
        this.$message.error('发送消息失败：' + (error.response?.data?.error || error.message))
        this.messages.pop() // 移除失败的用户消息
      } finally {
        this.loading = false
        this.$nextTick(() => {
          this.scrollToBottom()
        })
      }
    },

    scrollToBottom() {
      const container = this.$refs.messagesContainer
      if (container) {
        container.scrollTop = container.scrollHeight
      }
    },

    formatMessage(content) {
      if (!content) return ''
      // 简单的markdown格式化
      return content
        .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
        .replace(/\n/g, '<br>')
    },

    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      const now = new Date()
      const diff = now - date
      
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      return date.toLocaleDateString()
    }
  }
}
</script>

<style scoped>
.ai-assistant-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.ai-assistant-container {
  width: 90%;
  max-width: 1200px;
  height: 80vh;
  background: linear-gradient(135deg, #1a1d2e 0%, #16213e 100%);
  border-radius: 20px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.1);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.ai-header {
  padding: 20px 24px;
  border-bottom: 1px solid rgba(125, 207, 255, 0.2);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, rgba(122, 162, 247, 0.1) 0%, rgba(125, 207, 255, 0.1) 100%);
  border-radius: 20px 20px 0 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 0.5px;
}

.header-left i {
  background: linear-gradient(135deg, #7aa2f7 0%, #7dcfff 100%);
  color: white;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.ai-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.sessions-sidebar {
  width: 280px;
  border-right: 1px solid rgba(125, 207, 255, 0.15);
  display: flex;
  flex-direction: column;
  background: rgba(22, 33, 62, 0.5);
}

.sessions-header {
  padding: 16px;
  font-weight: 700;
  font-size: 14px;
  color: #ffffff;
  border-bottom: 1px solid rgba(125, 207, 255, 0.15);
  letter-spacing: 0.5px;
}

.sessions-list {
  flex: 1;
  overflow-y: auto;
}

.sessions-list::-webkit-scrollbar {
  width: 6px;
}

.sessions-list::-webkit-scrollbar-thumb {
  background: rgba(125, 207, 255, 0.3);
  border-radius: 3px;
}

.session-item {
  padding: 14px 16px;
  cursor: pointer;
  border-bottom: 1px solid rgba(125, 207, 255, 0.05);
  transition: all 0.3s ease;
}

.session-item:hover {
  background: rgba(125, 207, 255, 0.1);
  padding-left: 20px;
}

.session-item.active {
  background: linear-gradient(90deg, rgba(122, 162, 247, 0.2) 0%, rgba(125, 207, 255, 0.2) 100%);
  border-left: 3px solid #7dcfff;
  padding-left: 17px;
}

.session-title {
  font-weight: 600;
  margin-bottom: 6px;
  color: #ffffff;
  font-size: 14px;
}

.session-preview {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

.session-time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
  margin-top: 6px;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.example-queries {
  padding: 24px;
  border-bottom: 1px solid rgba(125, 207, 255, 0.15);
  background: rgba(22, 33, 62, 0.3);
}

.example-queries h4 {
  margin: 0 0 16px 0;
  color: #ffffff;
  font-size: 16px;
  font-weight: 600;
}

.example-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.example-buttons .el-button {
  background: rgba(125, 207, 255, 0.1);
  border: 1px solid rgba(125, 207, 255, 0.3);
  color: #7dcfff;
  transition: all 0.3s ease;
}

.example-buttons .el-button:hover {
  background: rgba(125, 207, 255, 0.2);
  border-color: #7dcfff;
  transform: translateY(-2px);
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: rgba(22, 33, 62, 0.3);
}

.messages-container::-webkit-scrollbar {
  width: 8px;
}

.messages-container::-webkit-scrollbar-thumb {
  background: rgba(125, 207, 255, 0.3);
  border-radius: 4px;
}

.message-item {
  display: flex;
  margin-bottom: 24px;
  gap: 14px;
  animation: messageSlide 0.3s ease;
}

@keyframes messageSlide {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.message-item.user .message-avatar {
  background: linear-gradient(135deg, #7aa2f7 0%, #7dcfff 100%);
  color: white;
}

.message-item.assistant .message-avatar {
  background: linear-gradient(135deg, #9ece6a 0%, #73daca 100%);
  color: white;
}

.message-content {
  max-width: 70%;
  padding: 14px 18px;
  border-radius: 12px;
  line-height: 1.7;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.message-item.user .message-content {
  background: linear-gradient(135deg, #7aa2f7 0%, #7dcfff 100%);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.message-item.assistant .message-content {
  background: rgba(125, 207, 255, 0.1);
  color: #ffffff;
  border: 1px solid rgba(125, 207, 255, 0.2);
}

.input-area {
  padding: 20px 24px;
  border-top: 1px solid rgba(125, 207, 255, 0.2);
  display: flex;
  gap: 12px;
  align-items: center;
  background: rgba(22, 33, 62, 0.5);
}

.upload-btn {
  flex-shrink: 0;
}

.input-area .el-input {
  flex: 1;
}

.input-area ::v-deep .el-input__wrapper {
  background: rgba(22, 33, 62, 0.8);
  border: 1px solid rgba(125, 207, 255, 0.3);
  box-shadow: none;
}

.input-area ::v-deep .el-input__inner {
  color: #ffffff;
}

.input-area ::v-deep .el-button--primary {
  background: linear-gradient(135deg, #7aa2f7 0%, #7dcfff 100%);
  border: none;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(122, 162, 247, 0.3);
  transition: all 0.3s ease;
}

.input-area ::v-deep .el-button--primary:hover {
  background: linear-gradient(135deg, #89b4fa 0%, #89ddff 100%);
  box-shadow: 0 6px 16px rgba(125, 207, 255, 0.4);
  transform: translateY(-2px);
}
</style>
