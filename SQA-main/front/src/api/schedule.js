import request from '@/utils/request'

// 获取排班列表
export const getScheduleList = (params) => {
  return request({
    url: '/schedule/list',
    method: 'get',
    params
  })
}

// 创建排班
export const createSchedule = (data) => {
  return request({
    url: '/schedule/create',
    method: 'post',
    data
  })
}

// 更新排班
export const updateSchedule = (id, data) => {
  return request({
    url: `/schedule/update/${id}`,
    method: 'put',
    data
  })
}

// 删除排班
export const deleteSchedule = (id) => {
  return request({
    url: `/schedule/delete/${id}`,
    method: 'delete'
  })
}

// 检查排班冲突
export const checkScheduleConflict = (data) => {
  return request({
    url: '/schedule/check-conflict',
    method: 'post',
    data
  })
}

// 获取教室列表
export const getClassroomList = () => {
  return request({
    url: '/schedule/classrooms',
    method: 'get'
  })
}

// 获取时间段列表
export const getTimeSlotList = () => {
  return request({
    url: '/schedule/timeslots',
    method: 'get'
  })
}

// 根据课程ID获取课表
export const getScheduleByCourseId = (courseId) => {
  return request({
    url: `/schedule/course/${courseId}`,
    method: 'get'
  })
}
