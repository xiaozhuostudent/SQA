package cn.edu.zjut.back.controller;

import cn.edu.zjut.back.entity.LiveChatMessage;
import cn.edu.zjut.back.service.LiveChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * WebSocket消息处理控制器
 */
@Controller
public class LiveChatWebSocketController {
    
    @Autowired
    private LiveChatMessageService liveChatMessageService;
    
    /**
     * 处理聊天消息
     * 客户端发送到: /app/chat/{liveStreamId}
     * 服务器广播到: /topic/live/{liveStreamId}
     */
    @MessageMapping("/chat/{liveStreamId}")
    @SendTo("/topic/live/{liveStreamId}")
    public LiveChatMessage handleChatMessage(
            @DestinationVariable Long liveStreamId,
            LiveChatMessage message) {
        
        // 设置直播ID
        message.setLiveStreamId(liveStreamId);
        
        // 保存到数据库
        liveChatMessageService.saveMessage(message);
        
        // 广播给所有订阅该直播间的用户
        return message;
    }
}
