import request from '@/utils/request'

// ========== 教师端API ==========

// 创建作业（包含题目）
export const createHomework = (homework, questions, teacherId) => {
  return request({
    url: '/homework/create',
    method: 'post',
    data: { homework, questions, teacherId }
  })
}

// 获取作业详情（包含题目）
export const getHomeworkDetail = (homeworkId) => {
  return request({
    url: `/homework/${homeworkId}`,
    method: 'get'
  })
}

// 获取课程作业列表
export const getCourseHomeworks = (courseId) => {
  return request({
    url: `/homework/course/${courseId}`,
    method: 'get'
  })
}

// 获取教师创建的作业列表
export const getTeacherHomeworks = (teacherId) => {
  return request({
    url: `/homework/teacher/${teacherId}`,
    method: 'get'
  })
}

// 更新作业
export const updateHomework = (homework, questions, teacherId) => {
  return request({
    url: '/homework/update',
    method: 'put',
    data: { homework, questions, teacherId }
  })
}

// 发布作业
export const publishHomework = (homeworkId, teacherId) => {
  return request({
    url: `/homework/${homeworkId}/publish`,
    method: 'post',
    params: { teacherId }
  })
}

// 删除作业
export const deleteHomework = (homeworkId, teacherId) => {
  return request({
    url: `/homework/${homeworkId}`,
    method: 'delete',
    params: { teacherId }
  })
}

// 获取作业提交列表（教师）
export const getHomeworkSubmissions = (homeworkId) => {
  return request({
    url: `/homework/${homeworkId}/submissions`,
    method: 'get'
  })
}

// 获取作业统计
export const getHomeworkStats = (homeworkId) => {
  return request({
    url: `/homework/${homeworkId}/stats`,
    method: 'get'
  })
}

// 教师批改作业
export const gradeHomework = (studentHomeworkId, grades, teacherComment) => {
  return request({
    url: '/homework/grade',
    method: 'post',
    data: { studentHomeworkId, grades, teacherComment }
  })
}


// ========== 学生端API ==========

// 获取学生的作业列表（所有选课的作业）
export const getStudentHomeworks = (studentId) => {
  return request({
    url: `/homework/student/${studentId}`,
    method: 'get'
  })
}

// 学生开始作业
export const startHomework = (homeworkId, studentId) => {
  return request({
    url: `/homework/${homeworkId}/start`,
    method: 'post',
    params: { studentId }
  })
}

// 学生提交作业答案
export const submitHomework = (studentHomeworkId, answers) => {
  return request({
    url: '/homework/submit',
    method: 'post',
    data: { studentHomeworkId, answers }
  })
}

// 获取学生的作业提交记录
export const getStudentSubmissions = (studentId) => {
  return request({
    url: `/homework/student/${studentId}/submissions`,
    method: 'get'
  })
}
