import request from '@/utils/request'

// 提交课程请求（创建或删除）
export const submitCourseRequest = (data) => {
  return request({
    url: '/course-request/submit',
    method: 'post',
    data
  })
}

// 上传请求附件
export const uploadRequestFile = (formData) => {
  return request({
    url: '/course-request/upload-file',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 下载/预览请求附件
export const downloadRequestFile = (fileKey) => {
  return request({
    url: '/course-request/download-file',
    method: 'get',
    params: { fileKey }
  })
}

// 获取教师的请求列表
export const getTeacherRequests = (teacherId) => {
  return request({
    url: '/course-request/teacher-requests',
    method: 'get',
    params: { teacherId }
  })
}

// 获取待审核的请求列表
export const getPendingRequests = () => {
  return request({
    url: '/course-request/pending',
    method: 'get'
  })
}

// 获取所有请求（分页）
export const getAllRequests = (params) => {
  return request({
    url: '/course-request/all',
    method: 'get',
    params
  })
}

// 审核请求
export const reviewRequest = (data) => {
  return request({
    url: '/course-request/review',
    method: 'post',
    data
  })
}
