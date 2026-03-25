import request from '@/utils/request'

export default {
  // 获取讨论列表
  getDiscussions(courseId = 1) {
    return request({
      url: `/discussion/list/${courseId}`,
      method: 'get'
    })
  },

  // 获取讨论详情
  getDiscussionDetail(id) {
    return request({
      url: `/discussion/detail/${id}`,
      method: 'get'
    })
  },

  // 创建讨论
  createDiscussion(data) {
    return request({
      url: '/discussion/create',
      method: 'post',
      data
    })
  },

  // 创建回复
  createReply(data) {
    return request({
      url: '/discussion/reply/create',
      method: 'post',
      data
    })
  },

  // 获取回复列表
  getReplies(discussionId) {
    return request({
      url: `/discussion/reply/list/${discussionId}`,
      method: 'get'
    })
  },

  // 点赞回复
  likeReply(replyId) {
    return request({
      url: `/discussion/reply/like/${replyId}`,
      method: 'post'
    })
  },

  // 采纳回复
  acceptReply(discussionId, replyId) {
    return request({
      url: `/discussion/reply/accept/${discussionId}/${replyId}`,
      method: 'post'
    })
  },

  // 删除回复
  deleteReply(discussionId, replyId) {
    return request({
      url: `/discussion/reply/delete/${discussionId}/${replyId}`,
      method: 'delete'
    })
  }
}
