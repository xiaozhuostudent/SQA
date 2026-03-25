import request from '@/utils/request'

// 导出命名函数，便于教师端和学生端共用
export const getQuestions = (params = {}) => {
  return request.get('/exam/question/list', { params })
}

export const getQuestionBankByCourse = (courseId) => {
  return request.get(`/exam/question/list/${courseId}`)
}

export const createQuestion = (data) => {
  return request.post('/exam/question/create', data)
}

export const updateQuestion = (id, data) => {
  return request.put(`/exam/question/update/${id}`, data)
}

export const deleteQuestion = (id) => {
  return request.delete(`/exam/question/delete/${id}`);
}

// 批量导入题目（JSON格式）
export const batchImportQuestions = (data) => {
  return request.post('/questions/batch-import', data)
}

export default {
  // 获取课程题库列表
  getQuestions(courseId, params = {}) {
    if (courseId) {
      return request.get(`/exam/question/list/${courseId}`, { params })
    }
    return request.get('/exam/question/list', { params })
  },

  // 获取随机题目
  getRandomQuestion(params) {
    return request.get('/exam/question/random', { params })
  },

  // 创建题目
  createQuestion(data) {
    return request.post('/exam/question/create', data)
  },

  // 更新题目
  updateQuestion(id, data) {
    return request.put(`/exam/question/update/${id}`, data)
  },

  // 删除题目
  deleteQuestion(id) {
    return request.delete(`/exam/question/delete/${id}`)
  },

  // 导入题目
  importQuestions(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/exam/question/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}
