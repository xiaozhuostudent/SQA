import request from '@/utils/request'

/**
 * 获取教师仪表板统计数据
 */
export const getTeacherDashboard = () => {
  return request({
    url: '/teacher/statistics/dashboard',
    method: 'get'
  })
}

/**
 * 获取学生仪表板统计数据
 */
export const getStudentDashboard = () => {
  return request({
    url: '/student/statistics/dashboard',
    method: 'get'
  })
}
