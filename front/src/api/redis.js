import request from '@/utils/request'

/**
 * 获取 Redis 统计信息
 */
export function getRedisStats() {
  return request({
    url: '/admin/redis/stats',
    method: 'get'
  })
}

/**
 * 获取 Redis 详细信息
 */
export function getRedisInfo(section = null) {
  return request({
    url: '/admin/redis/info',
    method: 'get',
    params: { section }
  })
}

/**
 * 获取键空间信息
 */
export function getKeyspaceInfo() {
  return request({
    url: '/admin/redis/keyspace',
    method: 'get'
  })
}

/**
 * PING 测试
 */
export function pingRedis() {
  return request({
    url: '/admin/redis/ping',
    method: 'get'
  })
}

/**
 * 获取键列表
 */
export function getKeys(pattern = '*', limit = 100) {
  return request({
    url: '/admin/redis/keys',
    method: 'get',
    params: { pattern, limit }
  })
}

/**
 * 获取键的值
 */
export function getKeyValue(key) {
  return request({
    url: `/admin/redis/get/${encodeURIComponent(key)}`,
    method: 'get'
  })
}

/**
 * 删除键
 */
export function deleteKeyApi(key) {
  return request({
    url: `/admin/redis/${encodeURIComponent(key)}`,
    method: 'delete'
  })
}

/**
 * 设置键值
 */
export function setKeyValue(data) {
  return request({
    url: '/admin/redis/set',
    method: 'post',
    data
  })
}

/**
 * 清空数据库
 */
export function flushDb() {
  return request({
    url: '/admin/redis/flushdb',
    method: 'delete'
  })
}
