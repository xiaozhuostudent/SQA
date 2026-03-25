/**
 * 资源URL处理工具
 * 用于统一处理资源文件的URL
 */

/**
 * 获取资源的完整访问URL
 * @param {string} fileUrl - 数据库中存储的资源URL(可能是相对路径或完整URL)
 * @returns {string} 完整的资源访问URL
 */
export const getResourceUrl = (fileUrl) => {
  if (!fileUrl) return ''
  
  // 如果已经是完整URL(http://或https://开头),直接返回
  if (fileUrl.startsWith('http://') || fileUrl.startsWith('https://')) {
    return fileUrl
  }
  
  // 如果是相对路径,需要拼接服务器地址
  // 开发环境和生产环境使用不同的基础URL
  const baseURL = import.meta.env.VITE_RESOURCE_BASE_URL || 'http://120.26.212.210:8888'
  
  // 确保路径以/开头
  const path = fileUrl.startsWith('/') ? fileUrl : `/${fileUrl}`
  
  return `${baseURL}${path}`
}

/**
 * 获取文件扩展名
 * @param {string} filename - 文件名
 * @returns {string} 文件扩展名(小写)
 */
export const getFileExtension = (filename) => {
  if (!filename || typeof filename !== 'string') return ''
  const lastDotIndex = filename.lastIndexOf('.')
  if (lastDotIndex === -1) return ''
  return filename.slice(lastDotIndex + 1).toLowerCase()
}

/**
 * 格式化文件大小
 * @param {number} bytes - 文件大小(字节)
 * @returns {string} 格式化后的文件大小
 */
export const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
}

/**
 * 判断文件是否可以预览
 * @param {string} filename - 文件名
 * @returns {boolean} 是否可预览
 */
export const canPreview = (filename) => {
  if (!filename || typeof filename !== 'string') return false
  const ext = getFileExtension(filename)
  const previewableExts = ['mp4', 'webm', 'ogg', 'pptx', 'docx', 'pdf', 'jpg', 'jpeg', 'png', 'gif', 'svg']
  return previewableExts.includes(ext)
}

/**
 * 获取文档预览URL
 * @param {string} fileUrl - 资源文件URL
 * @param {string} filename - 文件名
 * @returns {string} 预览URL
 */
export const getDocumentPreviewUrl = (fileUrl, filename) => {
  if (!fileUrl) return ''
  
  // 获取完整URL
  let fullUrl = getResourceUrl(fileUrl)
  
  // 处理中文文件名的URL编码
  // URL格式: http://120.26.212.210/resources/ppt/Java程序设计课件.pptx
  // 需要对文件名部分进行编码
  const urlParts = fullUrl.split('/')
  const encodedParts = urlParts.map((part, index) => {
    // 前3个部分是协议和域名 (http:, '', 120.26.212.210)
    if (index < 3) return part
    // 其他部分进行URI编码
    return encodeURIComponent(part)
  })
  fullUrl = encodedParts.join('/')
  
  const ext = getFileExtension(filename)
  
  if (ext === 'pdf') {
    // PDF直接预览
    return fullUrl
  } else if (['pptx', 'docx', 'xlsx', 'ppt', 'doc', 'xls'].includes(ext)) {
    // Office文档使用微软在线预览服务
    // 整个URL再次编码作为参数
    const encodedUrl = encodeURIComponent(fullUrl)
    return `https://view.officeapps.live.com/op/embed.aspx?src=${encodedUrl}`
  } else {
    return fullUrl
  }
}

/**
 * 获取文件类型对应的图标
 * @param {string} type - 资源类型
 * @returns {string} 图标名称
 */
export const getFileIcon = (type) => {
  const iconMap = {
    document: 'Document',
    video: 'VideoPlay',
    code: 'Tickets',
    other: 'Files'
  }
  return iconMap[type] || 'Document'
}

/**
 * 下载资源文件
 * @param {string} fileUrl - 资源URL
 * @param {string} filename - 文件名
 */
export const downloadResource = (fileUrl, filename) => {
  try {
    if (!fileUrl || !filename) {
      console.error('下载失败: fileUrl 或 filename 为空')
      return false
    }
    
    const fullUrl = getResourceUrl(fileUrl)
    
    // 创建隐藏的a标签进行下载
    const link = document.createElement('a')
    link.href = fullUrl
    link.download = filename || 'download'
    link.target = '_blank'
    link.style.display = 'none'
    
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    return true
  } catch (error) {
    console.error('下载失败:', error)
    return false
  }
}

export default {
  getResourceUrl,
  getFileExtension,
  formatFileSize,
  canPreview,
  getDocumentPreviewUrl,
  getFileIcon,
  downloadResource
}
