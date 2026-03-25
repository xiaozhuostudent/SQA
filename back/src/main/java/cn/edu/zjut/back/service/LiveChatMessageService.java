package cn.edu.zjut.back.service;

import cn.edu.zjut.back.entity.LiveChatMessage;
import cn.edu.zjut.back.mapper.LiveChatMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 直播聊天消息服务
 */
@Service
public class LiveChatMessageService {
    
    @Autowired
    private LiveChatMessageMapper liveChatMessageMapper;

    private static final List<String> SENSITIVE_WORDS = Arrays.asList(
            "傻逼", "煞笔", "妈的", "操你", "草你", "艹", "垃圾", "狗屎",
            "滚开", "滚蛋", "fuck", "shit", "wtf", "sb", "sex", "cao"
    );

    private static final String MASK = "***";
    
    /**
     * 保存聊天消息
     */
    public LiveChatMessage saveMessage(LiveChatMessage message) {
        if (message.getCreatedAt() == null) {
            message.setCreatedAt(LocalDateTime.now());
        }
        message.setContent(sanitizeContent(message.getContent()));
        liveChatMessageMapper.insert(message);
        return message;
    }

    private String sanitizeContent(String content) {
        if (content == null || content.isBlank()) {
            return content;
        }
        String sanitized = content;
        for (String word : SENSITIVE_WORDS) {
            String regex = Pattern.quote(word);
            sanitized = sanitized.replaceAll("(?i)" + regex, MASK);
        }
        return sanitized;
    }
    
    /**
     * 获取直播的所有聊天消息
     */
    public List<LiveChatMessage> getMessagesByLiveStreamId(Long liveStreamId) {
        return liveChatMessageMapper.findByLiveStreamId(liveStreamId);
    }
    
    /**
     * 获取消息数量
     */
    public int getMessageCount(Long liveStreamId) {
        return liveChatMessageMapper.countByLiveStreamId(liveStreamId);
    }
    
    /**
     * 删除直播的所有消息
     */
    public boolean deleteMessages(Long liveStreamId) {
        return liveChatMessageMapper.deleteByLiveStreamId(liveStreamId) > 0;
    }
}
