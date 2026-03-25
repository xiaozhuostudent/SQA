import request from '@/utils/request'

// 获取资源列表
export const getResourceList = (params) => {
  return request({
    url: '/resource/list',
    method: 'get',
    params
  })
}

// 获取资源详情
export const getResourceDetail = (id) => {
  return request({
    url: `/resource/detail/${id}`,
    method: 'get'
  })
}

// 上传资源
export const uploadResource = (data) => {
  return request({
    url: '/resource/upload',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 更新资源信息
export const updateResource = (id, data) => {
  return request({
    url: `/resource/update/${id}`,
    method: 'put',
    data
  })
}

// 删除资源
export const deleteResource = (id) => {
  return request({
    url: `/resource/delete/${id}`,
    method: 'delete'
  })
}

// 下载资源
export const downloadResource = (id) => {
  return request({
    url: `/resource/download/${id}`,
    method: 'get',
    responseType: 'blob'
  })
}

// 获取资源分类
export const getResourceCategories = () => {
  return request({
    url: '/resource/categories',
    method: 'get'
  })
}
