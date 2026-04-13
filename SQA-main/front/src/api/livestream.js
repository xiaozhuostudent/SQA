import request from '@/utils/request'

// 获取所有直播列表
export const getAllLiveStreams = () => {
  return request({
    url: '/livestream/list',
    method: 'get'
  })
}

// 获取直播详情
export const getLiveStreamById = (id) => {
  return request({
    url: `/livestream/${id}`,
    method: 'get'
  })
}

// 获取教师的直播列表
export const getTeacherLiveStreams = (teacherId) => {
  return request({
    url: `/livestream/teacher/${teacherId}`,
    method: 'get'
  })
}

// 根据状态获取直播列表
export const getLiveStreamsByStatus = (status) => {
  return request({
    url: `/livestream/status/${status}`,
    method: 'get'
  })
}

// 获取课程的直播列表
export const getCourseLiveStreams = (courseId) => {
  return request({
    url: `/livestream/course/${courseId}`,
    method: 'get'
  })
}

// 创建直播
export const createLiveStream = (data) => {
  return request({
    url: '/livestream/create',
    method: 'post',
    data
  })
}

// 更新直播信息
export const updateLiveStream = (data) => {
  return request({
    url: '/livestream/update',
    method: 'put',
    data
  })
}

// 开始直播
export const startLiveStream = (id) => {
  return request({
    url: `/livestream/start/${id}`,
    method: 'post'
  })
}

// 结束直播
export const endLiveStream = (id) => {
  return request({
    url: `/livestream/end/${id}`,
    method: 'post'
  })
}

// 进入直播间
export const joinLiveStream = (id) => {
  return request({
    url: `/livestream/join/${id}`,
    method: 'post'
  })
}

// 离开直播间
export const leaveLiveStream = (id) => {
  return request({
    url: `/livestream/leave/${id}`,
    method: 'post'
  })
}

// 删除直播
export const deleteLiveStream = (id) => {
  return request({
    url: `/livestream/${id}`,
    method: 'delete'
  })
}
