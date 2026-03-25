import request from '@/utils/request'

/**
 * 获取管理员操作日志
 */
export function getAdminLogs(params) {
  return request({
    url: '/admin/log/admin',
    method: 'get',
    params
  })
}

/**
 * 获取教师操作日志
 */
export function getTeacherLogs(params) {
  return request({
    url: '/admin/log/teacher',
    method: 'get',
    params
  })
}

/**
 * 获取学生操作日志
 */
export function getStudentLogs(params) {
  return request({
    url: '/admin/log/student',
    method: 'get',
    params
  })
}

/**
 * 获取日志统计数据
 */
export function getLogStatistics(params) {
  return request({
    url: '/admin/log/statistics',
    method: 'get',
    params
  })
}

/**
 * 导出所有日志
 */
export function exportAllLogs(params) {
  return request({
    url: '/admin/log/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

/**
 * 清空日志（仅保留最近指定天数）
 */
export function clearLogs(params) {
  return request({
    url: '/admin/log/clear',
    method: 'delete',
    params
  })
}
