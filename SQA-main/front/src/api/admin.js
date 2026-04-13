import request from '@/utils/request'

// 获取用户列表（管理员）
export const getUserList = (params) => {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

// 创建用户（管理员）
export const createUser = (data) => {
  return request({
    url: '/admin/user/create',
    method: 'post',
    data
  })
}

// 更新用户信息（管理员）
export const updateUser = (id, data) => {
  return request({
    url: `/admin/user/update/${id}`,
    method: 'put',
    data
  })
}

// 删除用户（管理员）
export const deleteUser = (id) => {
  return request({
    url: `/admin/user/delete/${id}`,
    method: 'delete'
  })
}

// 重置用户密码（管理员）
export const resetUserPassword = (id) => {
  return request({
    url: `/admin/user/reset-password/${id}`,
    method: 'post'
  })
}

// 获取系统统计信息
export const getSystemStatistics = () => {
  return request({
    url: '/admin/statistics',
    method: 'get'
  })
}

// 获取课程统计信息
export const getCourseStatistics = (params) => {
  return request({
    url: '/admin/statistics/courses',
    method: 'get',
    params
  })
}

// 获取用户统计信息
export const getUserStatistics = () => {
  return request({
    url: '/admin/statistics/users',
    method: 'get'
  })
}

// 更新用户状态（启用/禁用）
export const updateUserStatus = (id, status) => {
  return request({
    url: `/admin/user/status/${id}`,
    method: 'put',
    data: { status }
  })
}

// ==================== 课程管理 ====================

// 获取课程列表（分页、搜索、筛选）
export const getCourseList = (params) => {
  return request({
    url: '/admin/courses',
    method: 'get',
    params
  })
}

// 获取课程详情
export const getCourseDetail = (id) => {
  return request({
    url: `/admin/courses/${id}`,
    method: 'get'
  })
}

// 添加课程
export const addCourse = (data) => {
  return request({
    url: '/admin/courses',
    method: 'post',
    data
  })
}

// 更新课程信息
export const updateCourse = (id, data) => {
  return request({
    url: `/admin/courses/${id}`,
    method: 'put',
    data
  })
}

// 删除课程
export const deleteCourse = (id) => {
  return request({
    url: `/admin/courses/${id}`,
    method: 'delete'
  })
}

// 审核课程
export const approveCourseStatus = (id, status, reason = '') => {
  return request({
    url: `/admin/courses/${id}/approve`,
    method: 'post',
    data: { status, reason }
  })
}

// 调整课程容量
export const updateCourseCapacityNew = (id, capacity) => {
  return request({
    url: `/admin/courses/${id}/capacity`,
    method: 'put',
    data: { capacity }
  })
}

// 获取所有课程列表
export const getAllCourses = (params) => {
  return request({
    url: '/admin/courses',
    method: 'get',
    params
  })
}

// 审核课程
export const approveCourse = (id, data) => {
  return request({
    url: `/admin/course/approve/${id}`,
    method: 'post',
    data
  })
}

// 更新课程容量
export const updateCourseCapacity = (id, capacity) => {
  return request({
    url: `/admin/course/capacity/${id}`,
    method: 'put',
    data: { capacity }
  })
}

// 获取资源列表
export const getResourceList = (params) => {
  return request({
    url: '/admin/resources',
    method: 'get',
    params
  })
}

// 审核资源
export const approveResource = (id, data) => {
  return request({
    url: `/admin/resource/approve/${id}`,
    method: 'post',
    data
  })
}

// 删除资源
export const deleteResource = (id) => {
  return request({
    url: `/admin/resource/delete/${id}`,
    method: 'delete'
  })
}

// 批量删除资源
export const batchDeleteResources = (ids) => {
  return request({
    url: '/admin/resources/batch-delete',
    method: 'post',
    data: { ids }
  })
}

// 获取存储统计信息
export const getStorageStats = () => {
  return request({
    url: '/admin/storage/stats',
    method: 'get'
  })
}

// 获取综合统计信息
export const getStatistics = () => {
  return request({
    url: '/admin/statistics/all',
    method: 'get'
  })
}

// ==================== 教师管理 ====================

// 获取教师列表（分页、搜索、筛选）
export const getTeacherList = (params) => {
  return request({
    url: '/admin/teachers',
    method: 'get',
    params
  })
}

// 获取教师详情
export const getTeacherDetail = (id) => {
  return request({
    url: `/admin/teachers/${id}`,
    method: 'get'
  })
}

// 添加教师
export const addTeacher = (data) => {
  return request({
    url: '/admin/teachers',
    method: 'post',
    data
  })
}

// 更新教师信息
export const updateTeacher = (id, data) => {
  return request({
    url: `/admin/teachers/${id}`,
    method: 'put',
    data
  })
}

// 删除教师
export const deleteTeacher = (id) => {
  return request({
    url: `/admin/teachers/${id}`,
    method: 'delete'
  })
}

// 重置教师密码
export const resetTeacherPassword = (id, password = '123456') => {
  return request({
    url: `/admin/teachers/${id}/reset-password`,
    method: 'post',
    data: { password }
  })
}

// 获取教师授课统计
export const getTeacherStats = (id) => {
  return request({
    url: `/admin/teachers/${id}/stats`,
    method: 'get'
  })
}

// 批量导入教师
export const batchImportTeachers = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/admin/teachers/batch-import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 下载教师导入模板
export const downloadTeacherTemplate = () => {
  return request({
    url: '/admin/teachers/download-template',
    method: 'get'
  })
}

// ==================== 学生管理 ====================

// 获取学生列表（分页、搜索、筛选）
export const getStudentList = (params) => {
  return request({
    url: '/admin/students',
    method: 'get',
    params
  })
}

// 获取学生详情
export const getStudentDetail = (id) => {
  return request({
    url: `/admin/students/${id}`,
    method: 'get'
  })
}

// 添加学生
export const addStudent = (data) => {
  return request({
    url: '/admin/students',
    method: 'post',
    data
  })
}

// 更新学生信息
export const updateStudent = (id, data) => {
  return request({
    url: `/admin/students/${id}`,
    method: 'put',
    data
  })
}

// 删除学生
export const deleteStudent = (id) => {
  return request({
    url: `/admin/students/${id}`,
    method: 'delete'
  })
}

// 重置学生密码
export const resetStudentPassword = (id, password = '123456') => {
  return request({
    url: `/admin/students/${id}/reset-password`,
    method: 'post',
    data: { password }
  })
}

// 获取所有专业列表
export const getAllMajors = () => {
  return request({
    url: '/admin/students/majors',
    method: 'get'
  })
}

// 获取所有班级列表
export const getAllClasses = () => {
  return request({
    url: '/admin/students/classes',
    method: 'get'
  })
}

// 批量导入学生
export const batchImportStudents = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/admin/students/batch-import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 下载学生导入模板
export const downloadStudentTemplate = () => {
  return request({
    url: '/admin/students/download-template',
    method: 'get'
  })
}

// ==================== 排课管理 ====================

// 获取班级课表
export const getClassSchedule = (className, semester) => {
  return request({
    url: '/admin/schedules/class-schedule',
    method: 'get',
    params: { className, semester }
  })
}

// 查询排课列表
export const getScheduleList = (params) => {
  return request({
    url: '/admin/schedules',
    method: 'get',
    params
  })
}

// 获取排课详情
export const getScheduleDetail = (id) => {
  return request({
    url: `/admin/schedules/${id}`,
    method: 'get'
  })
}

// 添加排课
export const addSchedule = (data) => {
  return request({
    url: '/admin/schedules',
    method: 'post',
    data
  })
}

// 更新排课
export const updateSchedule = (id, data) => {
  return request({
    url: `/admin/schedules/${id}`,
    method: 'put',
    data
  })
}

// 删除排课
export const deleteSchedule = (id) => {
  return request({
    url: `/admin/schedules/${id}`,
    method: 'delete'
  })
}

// 获取所有班级列表
export const getAllScheduleClasses = () => {
  return request({
    url: '/admin/schedules/classes',
    method: 'get'
  })
}

// 获取所有学期列表
export const getAllSemesters = () => {
  return request({
    url: '/admin/schedules/semesters',
    method: 'get'
  })
}

// ==================== 公告管理 ====================

// 获取公告列表（管理员）
export const getAnnouncementList = (params) => {
  return request({
    url: '/admin/announcements',
    method: 'get',
    params
  })
}

// 获取公告详情
export const getAnnouncementDetail = (id) => {
  return request({
    url: `/admin/announcements/${id}`,
    method: 'get'
  })
}

// 创建公告
export const createAnnouncement = (data) => {
  return request({
    url: '/admin/announcements',
    method: 'post',
    data
  })
}

// 更新公告
export const updateAnnouncement = (id, data) => {
  return request({
    url: `/admin/announcements/${id}`,
    method: 'put',
    data
  })
}

// 发布公告
export const publishAnnouncement = (id) => {
  return request({
    url: `/admin/announcements/${id}/publish`,
    method: 'put'
  })
}

// 归档公告
export const archiveAnnouncement = (id) => {
  return request({
    url: `/admin/announcements/${id}/archive`,
    method: 'put'
  })
}

// 删除公告
export const deleteAnnouncement = (id) => {
  return request({
    url: `/admin/announcements/${id}`,
    method: 'delete'
  })
}

// 根据角色获取公告（首页展示）
export const getAnnouncementsByRole = (role, limit = 5) => {
  return request({
    url: `/announcements/role/${role}`,
    method: 'get',
    params: { limit }
  })
}

// ==================== 操作日志管理 ====================

// 获取操作日志列表
export const getLogList = (params) => {
  return request({
    url: '/admin/logs',
    method: 'get',
    params
  })
}

// 获取日志详情
export const getLogDetail = (id) => {
  return request({
    url: `/admin/logs/${id}`,
    method: 'get'
  })
}

// 批量删除日志
export const batchDeleteLogs = (ids) => {
  return request({
    url: '/admin/logs/batch',
    method: 'delete',
    data: { ids }
  })
}

// 清空所有日志
export const clearAllLogs = () => {
  return request({
    url: '/admin/logs/clear',
    method: 'delete'
  })
}

// 获取操作类型统计
export const getOperationStats = () => {
  return request({
    url: '/admin/logs/stats/operations',
    method: 'get'
  })
}

// 获取用户操作统计
export const getUserOperationStats = () => {
  return request({
    url: '/admin/logs/stats/users',
    method: 'get'
  })
}
