import request from '@/utils/request'

// 获取实验列表
export const getExperimentList = (params) => {
  return request({
    url: '/experiment/list',
    method: 'get',
    params
  })
}

// 获取实验详情
export const getExperimentDetail = (id) => {
  return request({
    url: `/experiment/${id}`,
    method: 'get'
  })
}

// 教师创建实验
export const createExperiment = (data) => {
  return request({
    url: '/experiment/create',
    method: 'post',
    data
  })
}

// 教师更新实验
export const updateExperiment = (id, data) => {
  return request({
    url: `/experiment/update/${id}`,
    method: 'put',
    data
  })
}

// 教师删除实验
export const deleteExperiment = (id) => {
  return request({
    url: `/experiment/delete/${id}`,
    method: 'delete'
  })
}

// 获取实验的提交报告列表
export const getExperimentSubmissions = (experimentId) => {
  return request({
    url: `/experiment/${experimentId}/submissions`,
    method: 'get'
  })
}

// 学生提交实验报告
export const submitExperimentReport = (data) => {
  return request({
    url: '/experiment/submit-report',
    method: 'post',
    data
  })
}

// 获取实验环境信息
export const getExperimentEnvironment = (experimentId) => {
  return request({
    url: `/experiment/environment/${experimentId}`,
    method: 'get'
  })
}

// 启动实验环境
export const startExperimentEnvironment = (experimentId) => {
  return request({
    url: `/experiment/start-environment/${experimentId}`,
    method: 'post'
  })
}

// 停止实验环境
export const stopExperimentEnvironment = (experimentId) => {
  return request({
    url: `/experiment/stop-environment/${experimentId}`,
    method: 'post'
  })
}

// 获取实验题目列表
export const getExperimentProblems = (id) => {
  return request({
    url: `/experiment/${id}/problems`,
    method: 'get'
  })
}

// 获取题目详情
export const getProblemDetail = (id) => {
  return request({
    url: `/experiment/problem/${id}`,
    method: 'get'
  })
}

// 获取实验学生完成情况统计
export const getStudentStats = (experimentId) => {
  return request({
    url: `/experiment/${experimentId}/student-stats`,
    method: 'get'
  })
}
