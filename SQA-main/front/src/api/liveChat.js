import request from '@/utils/request'

// 获取聊天历史记录
export const getChatMessages = (liveStreamId) => {
  return request({
    url: `/live-chat/messages/${liveStreamId}`,
    method: 'get'
  })
}

// 生成词云
export const generateWordCloud = (liveStreamId) => {
  return request({
    url: `/live-chat/wordcloud/generate/${liveStreamId}`,
    method: 'post'
  })
}

// 获取词云数据
export const getWordCloud = (liveStreamId) => {
  return request({
    url: `/live-chat/wordcloud/${liveStreamId}`,
    method: 'get'
  })
}

// 删除聊天数据
export const deleteChatData = (liveStreamId) => {
  return request({
    url: `/live-chat/${liveStreamId}`,
    method: 'delete'
  })
}
