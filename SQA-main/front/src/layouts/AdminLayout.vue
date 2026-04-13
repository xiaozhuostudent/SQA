<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="sidebar">
      <div class="logo">
        <div style="text-align: center;">
          <h3 style="margin-bottom: 8px;">管理员端</h3>
          <el-tag :type="getRoleTagType()" size="small">
            {{ roleName }}
          </el-tag>
        </div>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#16161e"
        text-color="#9aa5ce"
        active-text-color="#bb9af7"
      >
        <template v-for="item in visibleMenuItems" :key="item.index">
          <el-menu-item :index="item.index">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-right">
          <span class="username">{{ userStore.userInfo.realName || '管理员' }} ({{ roleName }})</span>
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
import { HomeFilled, User, Reading, Calendar, Bell, Folder, DataLine, Monitor, Coin } from '@element-plus/icons-vue'
import { getRoleName, hasPermission } from '@/utils/permission'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

// 获取当前角色名称
const roleName = computed(() => getRoleName())

// 菜单配置（添加requiredLevel字段，表示需要的最低权限等级）
const menuItems = [
  { index: '/admin/dashboard', icon: 'HomeFilled', label: '首页', permission: null, requiredLevel: 1 },
  { index: '/admin/teachers', icon: 'User', label: '教师管理', permission: 'user:view', requiredLevel: 3 },
  { index: '/admin/students', icon: 'User', label: '学生管理', permission: 'user:view', requiredLevel: 3 },
  { index: '/admin/users', icon: 'User', label: '用户管理', permission: 'user:manage', requiredLevel: 5 },
  { index: '/admin/courses', icon: 'Reading', label: '课程管理', permission: 'course:view', requiredLevel: 3 },
  { index: '/admin/course-selection-management', icon: 'Setting', label: '选课管理', permission: 'course:view', requiredLevel: 3 },
  { index: '/admin/course-requests', icon: 'DocumentChecked', label: '课程请求审核', permission: 'course:view', requiredLevel: 3 },
  { index: '/admin/schedule', icon: 'Calendar', label: '排班管理', permission: 'course:view', requiredLevel: 3 },
  { index: '/admin/announcements', icon: 'Bell', label: '公告管理', permission: 'announcement:view', requiredLevel: 3 },
  { index: '/admin/resources', icon: 'Folder', label: '资源管理', permission: 'resource:view', requiredLevel: 2 },
  { index: '/admin/statistics', icon: 'DataLine', label: '统计分析', permission: 'statistics:view', requiredLevel: 2 },
  { index: '/admin/live-monitor', icon: 'Monitor', label: '直播监控', permission: 'monitor:live', requiredLevel: 2 },
  { index: '/admin/redis-monitor', icon: 'Coin', label: 'Redis监控', permission: 'monitor:redis', requiredLevel: 4 },
  { index: '/admin/logs/admin', icon: 'Document', label: '管理员日志', permission: 'log:view', requiredLevel: 4 },
  { index: '/admin/logs/teacher', icon: 'Document', label: '教师日志', permission: 'log:view', requiredLevel: 4 },
  { index: '/admin/logs/student', icon: 'Document', label: '学生日志', permission: 'log:view', requiredLevel: 4 },
  { index: '/admin/ai-assistant', icon: 'ChatDotRound', label: '智能助管理', permission: null, requiredLevel: 1 },
  { index: '/admin/profile', icon: 'User', label: '个人信息', permission: null, requiredLevel: 1 }
]

// 根据权限和权限等级过滤可见菜单
const visibleMenuItems = computed(() => {
  const currentLevel = userStore.userInfo?.permissionLevel || 1
  
  // 特殊规则：不同角色看到的菜单
  const roleMenuMap = {
    4: ['/admin/dashboard', '/admin/redis-monitor', '/admin/logs/admin', '/admin/logs/teacher', '/admin/logs/student', '/admin/ai-assistant', '/admin/profile'], // 日志管理员
    3: ['/admin/dashboard', '/admin/teachers', '/admin/students', '/admin/schedule', '/admin/courses', '/admin/course-selection-management', '/admin/course-requests', '/admin/announcements', '/admin/statistics', '/admin/ai-assistant', '/admin/profile'], // 教务管理员
    2: ['/admin/dashboard', '/admin/resources', '/admin/statistics', '/admin/live-monitor', '/admin/announcements', '/admin/ai-assistant', '/admin/profile'] // 资源管理员
  }
  
  // 如果是超级管理员(level 5)，显示所有菜单
  if (currentLevel === 5) {
    return menuItems.filter(item => {
      if (!item.permission) return true
      return hasPermission(item.permission)
    })
  }
  
  // 如果有特殊角色规则，使用角色规则
  if (roleMenuMap[currentLevel]) {
    return menuItems.filter(item => {
      // 检查是否在该角色允许的菜单列表中
      if (!roleMenuMap[currentLevel].includes(item.index)) return false
      // 检查权限
      if (!item.permission) return true
      return hasPermission(item.permission)
    })
  }
  
  // 默认规则（访客等）
  return menuItems.filter(item => {
    if (!item.permission) return true
    return hasPermission(item.permission)
  })
})

// 获取角色标签类型
const getRoleTagType = () => {
  const level = userStore.userInfo?.permissionLevel || 1
  const typeMap = {
    5: 'danger',  // 超级管理员 - 红色
    4: 'warning', // 日志管理员 - 橙色
    3: 'success', // 教务管理员 - 绿色
    2: 'info',    // 资源管理员 - 蓝色
    1: ''         // 访客 - 默认
  }
  return typeMap[level] || ''
}

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
  min-height: 100vh;
  background-color: var(--bg-primary);
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
  color: var(--accent-purple);
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
  min-height: calc(100vh - 60px);
}
</style>
