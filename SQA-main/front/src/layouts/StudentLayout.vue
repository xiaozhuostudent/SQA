<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="sidebar">
      <div class="logo">
        <h3>学生端</h3>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#16161e"
        text-color="#a9b1d6"
        active-text-color="#7dcfff"
      >
        <el-menu-item index="/student/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/student/courses">
          <el-icon><Reading /></el-icon>
          <span>我的课程</span>
        </el-menu-item>
        <el-menu-item index="/student/homework">
          <el-icon><Document /></el-icon>
          <span>课程作业</span>
        </el-menu-item>
        <el-menu-item index="/student/experiment">
          <el-icon><DataAnalysis /></el-icon>
          <span>实验管理</span>
        </el-menu-item>
        <el-menu-item index="/student/exam">
          <el-icon><EditPen /></el-icon>
          <span>在线考试</span>
        </el-menu-item>
        <el-menu-item index="/student/practice">
          <el-icon><Notebook /></el-icon>
          <span>题库刷题</span>
        </el-menu-item>
        <el-menu-item index="/student/discussion">
          <el-icon><ChatDotRound /></el-icon>
          <span>讨论区</span>
        </el-menu-item>
        <el-menu-item index="/student/livestream">
          <el-icon><VideoCamera /></el-icon>
          <span>课程直播</span>
        </el-menu-item>
        <el-menu-item index="/student/resources">
          <el-icon><Folder /></el-icon>
          <span>学习资源</span>
        </el-menu-item>
        <el-menu-item index="/student/ai-assistant">
          <el-icon><ChatDotRound /></el-icon>
          <span>智能助学</span>
        </el-menu-item>
        <el-menu-item index="/student/profile">
          <el-icon><User /></el-icon>
          <span>个人信息</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-right">
          <span class="username">{{ userStore.userInfo.realName || '学生' }}</span>
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
  overflow: hidden;
  background: var(--bg-primary);
}

.sidebar {
  overflow: hidden;
  background: var(--bg-secondary);
  border-right: 1px solid var(--border);
  overflow-x: hidden;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent-cyan);
  background: var(--bg-primary);
  font-size: 18px;
  font-weight: 700;
  border-bottom: 1px solid var(--border);
}

.header {
  background: var(--bg-float);
  box-shadow: var(--shadow-sm);
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 24px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.username {
  color: var(--text-primary);
  font-weight: 600;
  font-size: 14px;
}

.main-content {
  padding: 12px;
  background: var(--bg-primary);
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}
</style>
