import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { ElMessage } from 'element-plus'

class LiveChatWebSocket {
  constructor() {
    this.client = null
    this.liveStreamId = null
    this.messageCallback = null
    this.connected = false
  }

  /**
   * 连接WebSocket
   */
  connect(liveStreamId, onMessageReceived) {
    this.liveStreamId = liveStreamId
    this.messageCallback = onMessageReceived

    // 创建STOMP客户端
    this.client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      
      connectHeaders: {},
      
      debug: (str) => {
        console.log('[WebSocket]', str)
      },
      
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      
      onConnect: () => {
        console.log('WebSocket连接成功')
        this.connected = true
        
        // 订阅直播间消息
        this.client.subscribe(`/topic/live/${liveStreamId}`, (message) => {
          const chatMessage = JSON.parse(message.body)
          if (this.messageCallback) {
            this.messageCallback(chatMessage)
          }
        })
        
        ElMessage.success('已连接到聊天室')
      },
      
      onStompError: (frame) => {
        console.error('WebSocket错误:', frame)
        this.connected = false
        ElMessage.error('聊天室连接失败')
      },
      
      onWebSocketClose: () => {
        console.log('WebSocket连接关闭')
        this.connected = false
      }
    })

    // 激活连接
    this.client.activate()
  }

  /**
   * 发送消息
   */
  sendMessage(message) {
    if (!this.connected || !this.client) {
      ElMessage.warning('聊天室未连接')
      return false
    }

    try {
      this.client.publish({
        destination: `/app/chat/${this.liveStreamId}`,
        body: JSON.stringify(message)
      })
      return true
    } catch (error) {
      console.error('发送消息失败:', error)
      ElMessage.error('发送消息失败')
      return false
    }
  }

  /**
   * 断开连接
   */
  disconnect() {
    if (this.client) {
      this.client.deactivate()
      this.connected = false
      this.client = null
      console.log('WebSocket已断开')
    }
  }

  /**
   * 是否已连接
   */
  isConnected() {
    return this.connected
  }
}

export default LiveChatWebSocket
