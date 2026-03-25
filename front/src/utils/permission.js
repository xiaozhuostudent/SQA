// 管理员权限工具类
import { useUserStore } from '@/stores/user'

// 权限等级常量
export const PERMISSION_LEVELS = {
  VIEWER: 1,        // 数据查看员
  CONTENT_ADMIN: 2, // 内容管理员
  ACADEMIC_ADMIN: 3,// 教务管理员
  SYSTEM_ADMIN: 4,  // 系统管理员
  SUPER_ADMIN: 5    // 超级管理员
}

// 角色名称映射
export const ROLE_NAMES = {
  1: '访客',
  2: '资源管理员',
  3: '教务管理员',
  4: '日志管理员',
  5: '超级管理员'
}

// 权限配置矩阵
const PERMISSION_MATRIX = {
  // 用户管理
  'user:view': [2, 3, 4, 5],
  'user:manage': [5],  // 用户管理页面访问权限（仅超级管理员）
  'user:create': [3, 5],
  'user:edit': [3, 5],
  'user:delete': [5],
  'admin:manage': [5],
  
  // 课程管理
  'course:view': [2, 3, 4, 5],
  'course:create': [3, 5],
  'course:edit': [3, 5],
  'course:delete': [3, 5],
  
  // 资源管理
  'resource:view': [2, 3, 4, 5],
  'resource:approve': [2, 3, 5],
  'resource:delete': [2, 3, 5],
  'resource:batch': [3, 5],
  
  // 公告管理
  'announcement:view': [2, 3, 4, 5],
  'announcement:create': [2, 3, 5],
  'announcement:edit': [2, 3, 5],
  'announcement:delete': [3, 5],
  
  // 系统监控
  'monitor:redis': [4, 5],      // 日志管理员和超级管理员
  'monitor:live': [2, 3, 4, 5], // 资源管理员及以上
  'log:view': [4, 5],            // 日志查看权限 - 日志管理员和超级管理员
  'log:export': [4, 5],          // 日志导出权限
  'log:delete': [5],             // 日志删除权限 - 仅超级管理员
  
  // 统计分析
  'statistics:view': [2, 3, 4, 5]
}

/**
 * 检查当前管理员是否有指定权限
 * @param {string} permission - 权限标识,如 'user:delete'
 * @returns {boolean}
 */
export function hasPermission(permission) {
  const userStore = useUserStore()
  const permissionLevel = userStore.userInfo?.permissionLevel || 1
  
  // 超级管理员拥有所有权限
  if (permissionLevel === 5) {
    return true
  }
  
  // 检查权限矩阵
  const allowedLevels = PERMISSION_MATRIX[permission]
  if (!allowedLevels) {
    console.warn(`未定义的权限: ${permission}`)
    return false
  }
  
  return allowedLevels.includes(permissionLevel)
}

/**
 * 检查是否至少有指定权限等级
 * @param {number} requiredLevel - 所需最低权限等级
 * @returns {boolean}
 */
export function hasLevel(requiredLevel) {
  const userStore = useUserStore()
  const permissionLevel = userStore.userInfo?.permissionLevel || 1
  return permissionLevel >= requiredLevel
}

/**
 * 获取当前管理员角色名称
 * @returns {string}
 */
export function getRoleName() {
  const userStore = useUserStore()
  const level = userStore.userInfo?.permissionLevel || 1
  return ROLE_NAMES[level] || '未知角色'
}

/**
 * 获取当前权限等级
 * @returns {number}
 */
export function getPermissionLevel() {
  const userStore = useUserStore()
  return userStore.userInfo?.permissionLevel || 1
}

/**
 * 检查是否是超级管理员
 * @returns {boolean}
 */
export function isSuperAdmin() {
  return getPermissionLevel() === PERMISSION_LEVELS.SUPER_ADMIN
}

/**
 * 过滤菜单项(根据权限)
 * @param {Array} menuItems - 菜单项数组
 * @returns {Array}
 */
export function filterMenuByPermission(menuItems) {
  return menuItems.filter(item => {
    if (!item.permission) return true
    return hasPermission(item.permission)
  })
}

export default {
  hasPermission,
  hasLevel,
  getRoleName,
  getPermissionLevel,
  isSuperAdmin,
  filterMenuByPermission,
  PERMISSION_LEVELS,
  ROLE_NAMES
}
