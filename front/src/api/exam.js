import request from '@/utils/request'

// 学生端API
export const getExamPapers = (courseId) => {
  return request({
    url: `/exam/paper/list/${courseId}`,
    method: 'get'
  })
}

// 根据创建者获取试卷列表
export const getExamPapersByCreator = (creatorId) => {
  return request({
    url: `/exam/paper/listByCreator/${creatorId}`,
    method: 'get'
  })
}

export const getStudentExams = (studentId) => {
  return request({
    url: `/exam/student/list/${studentId}`,
    method: 'get'
  })
}

export const startExam = (data) => {
  return request({
    url: '/exam/start',
    method: 'post',
    data
  })
}

export const getExamQuestions = (examPaperId) => {
  return request({
    url: `/exam/questions/${examPaperId}`,
    method: 'get'
  })
}

export const submitExam = (data) => {
  return request({
    url: '/exam/submit',
    method: 'post',
    data
  })
}

export const getExamResult = (studentExamId) => {
  return request({
    url: `/exam/result/${studentExamId}`,
    method: 'get'
  })
}

export const getAnswers = (studentExamId) => {
  return request({
    url: `/exam/answers/${studentExamId}`,
    method: 'get'
  })
}

// 获取完整的考试结果详情（新接口）
export const getExamResultDetail = (studentExamId) => {
  return request({
    url: `/exam/result/detail/${studentExamId}`,
    method: 'get'
  })
}

// 教师端API
export const createExamPaper = (data) => {
  return request({
    url: '/exam/paper/create',
    method: 'post',
    data
  })
}

export const updateExamPaper = (id, data) => {
  return request({
    url: `/exam/paper/update/${id}`,
    method: 'put',
    data
  })
}

export const deleteExamPaper = (id) => {
  return request({
    url: `/exam/paper/delete/${id}`,
    method: 'delete'
  })
}

export const publishExamPaper = (id) => {
  return request({
    url: `/exam/paper/publish/${id}`,
    method: 'post'
  })
}

export const getSubmissions = (examPaperId) => {
  return request({
    url: `/exam/submissions/${examPaperId}`,
    method: 'get'
  })
}

// 题库管理API
export const createQuestion = (data) => {
  return request({
    url: '/exam/question/create',
    method: 'post',
    data
  })
}

export const getAllQuestions = (params) => {
  return request({
    url: '/exam/question/list',
    method: 'get',
    params
  })
}

export const getQuestionsByCourse = (courseId) => {
  return request({
    url: `/exam/question/list/${courseId}`,
    method: 'get'
  })
}

export const addQuestionsToExam = (examPaperId, questionIds) => {
  return request({
    url: `/exam/paper/${examPaperId}/questions`,
    method: 'post',
    data: { questionIds }
  })
}

export const removeQuestionFromExam = (examPaperId, questionId) => {
  return request({
    url: `/exam/paper/${examPaperId}/question/${questionId}`,
    method: 'delete'
  })
}

// 批量导入题目
export const batchImportQuestions = (data) => {
  return request({
    url: '/questions/batch-import',
    method: 'post',
    data
  })
}

export default {
  getExamPapers,
  getExamPapersByCreator,
  getStudentExams,
  startExam,
  getExamQuestions,
  submitExam,
  getExamResult,
  getAnswers,
  getExamResultDetail,
  createExamPaper,
  updateExamPaper,
  deleteExamPaper,
  publishExamPaper,
  getSubmissions,
  createQuestion,
  getAllQuestions,
  getQuestionsByCourse,
  addQuestionsToExam,
  removeQuestionFromExam,
  batchImportQuestions
}
