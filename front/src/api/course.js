import request from '@/utils/request'

// 获取课程列表
export const getCourseList = (params) => {
  return request({
    url: '/course/list',
    method: 'get',
    params
  })
}

// 获取课程详情
export const getCourseDetail = (id) => {
  return request({
    url: `/course/detail/${id}`,
    method: 'get'
  })
}

// 创建课程
export const createCourse = (data, teacherId) => {
  return request({
    url: '/course/create',
    method: 'post',
    params: { teacherId },
    data
  })
}

// 更新课程
export const updateCourse = (data, teacherId) => {
  return request({
    url: '/course/update',
    method: 'put',
    params: { teacherId },
    data
  })
}

// 删除课程
export const deleteCourse = (courseId, teacherId) => {
  return request({
    url: `/course/delete/${courseId}`,
    method: 'delete',
    params: { teacherId }
  })
}

// 学生选课
export const selectCourse = (courseId) => {
  return request({
    url: '/course/select',
    method: 'post',
    data: { courseId }
  })
}

// 学生退课
export const dropCourse = (courseId) => {
  return request({
    url: '/course/drop',
    method: 'post',
    data: { courseId }
  })
}

// 获取我的课程
export const getMyCourses = (studentId) => {
  return request({
    url: '/course/my-courses',
    method: 'get',
    params: { studentId }
  })
}

// 获取教师课程
export const getTeacherCourses = (teacherId) => {
  return request({
    url: '/course/teacher-courses',
    method: 'get',
    params: { teacherId }
  })
}

// 获取所有课程
export const getAllCourses = () => {
  return request({
    url: '/course/all',
    method: 'get'
  })
}

// 选课
export const enrollCourse = (courseId, studentId) => {
  return request({
    url: `/course/enroll/${courseId}`,
    method: 'post',
    params: { studentId }
  })
}

// 退课
export const withdrawCourse = (courseId, studentId) => {
  return request({
    url: `/course/withdraw/${courseId}`,
    method: 'post',
    params: { studentId }
  })
}

// 获取所有课程（管理员用）
export const getAllCoursesForAdmin = () => {
  return request({
    url: '/course/all-for-admin',
    method: 'get'
  })
}

// 更新课程选课开放状态（管理员）
export const updateCourseSelectionStatus = (courseId, isOpen) => {
  return request({
    url: `/course/selection-status/${courseId}`,
    method: 'put',
    params: { isOpen }
  })
}

// 批量更新课程选课开放状态（管理员）
export const batchUpdateCourseSelectionStatus = (courseIds, isOpen) => {
  return request({
    url: '/course/batch-selection-status',
    method: 'put',
    params: { isOpen },
    data: courseIds
  })
}
