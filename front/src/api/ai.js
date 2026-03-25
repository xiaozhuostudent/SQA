import axios from 'axios'
import request from '@/utils/request'

const AI_SERVICE_BASE = import.meta.env.VITE_AI_SERVICE_BASE_URL || '/ai-service'

const llmClient = axios.create({
  baseURL: AI_SERVICE_BASE,
  timeout: 20000
})

// 获取AI学习建议和相关题目推荐
export const getAIRecommendations = (data) => {
  return request({
    url: '/ai/recommendations',
    method: 'post',
    data
  })
}

// 测试AI接口连通性
export const checkAIHealth = () => {
  return request({
    url: '/ai/health',
    method: 'get'
  })
}

// AI 对话接口
export const chatWithAI = (data) => {
  return llmClient.post('/chat', {
    message: data.message,
    session_id: data.sessionId || data.session_id,
    sessionId: data.sessionId || data.session_id,
    exam: data.exam,
    statistics: data.statistics,
    wrongTopics: data.wrongTopics
  }).then(res => res.data)
}

export default {
  getAIRecommendations,
  checkAIHealth,
  chatWithAI
}