import request from '@/utils/request'

export const getAnalysisReport = (liveStreamId) => {
  return request({
    url: `/live-analysis/report/${liveStreamId}`,
    method: 'get'
  })
}

export const generateAnalysisReport = (liveStreamId) => {
  return request({
    url: `/live-analysis/report/${liveStreamId}`,
    method: 'post'
  })
}
