<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="sidebar">
      <div class="logo">
        <h3>教师端</h3>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#16161e"
        text-color="#9aa5ce"
        active-text-color="#7dcfff"
      >
        <el-menu-item index="/teacher/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/teacher/courses">
          <el-icon><Reading /></el-icon>
          <span>课程管理</span>
        </el-menu-item>
        <el-menu-item index="/teacher/homework">
          <el-icon><Document /></el-icon>
          <span>作业管理</span>
        </el-menu-item>
        <el-menu-item index="/teacher/experiment">
          <el-icon><DataAnalysis /></el-icon>
          <span>实验管理</span>
        </el-menu-item>
        <el-menu-item index="/teacher/exam">
          <el-icon><EditPen /></el-icon>
          <span>考试管理</span>
        </el-menu-item>
        <el-menu-item index="/teacher/question-bank">
          <el-icon><Collection /></el-icon>
          <span>题库管理</span>
        </el-menu-item>
        <el-menu-item index="/teacher/discussion">
          <el-icon><ChatDotRound /></el-icon>
          <span>讨论区</span>
        </el-menu-item>
        <el-menu-item index="/teacher/livestream">
          <el-icon><VideoCamera /></el-icon>
          <span>直播管理</span>
        </el-menu-item>
        <el-menu-item index="/teacher/resources">
          <el-icon><Folder /></el-icon>
          <span>教学资源</span>
        </el-menu-item>
        <el-menu-item index="/teacher/preparation">
          <el-icon><Notebook /></el-icon>
          <span>备课中心</span>
        </el-menu-item>
        <el-menu-item index="/teacher/ai-assistant">
          <el-icon><ChatDotRound /></el-icon>
          <span>智能助教</span>
        </el-menu-item>
        <el-menu-item index="/teacher/profile">
          <el-icon><User /></el-icon>
          <span>个人信息</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-right">
          <span class="username">{{ userStore.userInfo.realName || '教师' }}</span>
          <el-button type="primary" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import { Notebook } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    router.push('/login')
  })
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  background-color: var(--bg-primary);
  overflow: hidden;
}

.sidebar {
  background-color: #16161e;
  overflow-x: hidden;
  border-right: 1px solid var(--border);
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent-cyan);
  background-color: #0d0d14;
  font-weight: 700;
  letter-spacing: 0.05em;
  border-bottom: 1px solid var(--border);
}

.header {
  background: #16161e;
  box-shadow: var(--shadow-sm);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 24px;
  border-bottom: 1px solid var(--border);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.username {
  color: var(--text-primary);
  font-weight: 600;
  font-size: 15px;
}

.main-content {
  padding: 24px;
  background-color: var(--bg-primary);
  height: calc(100vh - 60px);
  overflow-y: auto;
  box-sizing: border-box;
}
</style>
