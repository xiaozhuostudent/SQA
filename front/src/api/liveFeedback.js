import request from '@/utils/request'

export const submitFeedback = (data) => {
  return request({
    url: '/live-feedback',
    method: 'post',
    data
  })
}

export const getFeedbackByLiveStream = (liveStreamId) => {
  return request({
    url: `/live-feedback/${liveStreamId}`,
    method: 'get'
  })
}
