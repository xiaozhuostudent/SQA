package cn.edu.zjut.back.controller;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.entity.LiveChatMessage;
import cn.edu.zjut.back.entity.LiveWordCloud;
import cn.edu.zjut.back.service.LiveChatMessageService;
import cn.edu.zjut.back.service.LiveFeedbackService;
import cn.edu.zjut.back.service.LiveAnalysisService;
import cn.edu.zjut.back.service.LiveWordCloudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 直播聊天REST控制器
 */
@RestController
@RequestMapping("/api/live-chat")
@CrossOrigin
public class LiveChatController {
    
    @Autowired
    private LiveChatMessageService liveChatMessageService;
    
    @Autowired
    private LiveWordCloudService liveWordCloudService;

    @Autowired
    private LiveFeedbackService liveFeedbackService;

    @Autowired
    private LiveAnalysisService liveAnalysisService;
    
    /**
     * 获取直播的聊天历史记录
     */
    @GetMapping("/messages/{liveStreamId}")
    public Result<List<LiveChatMessage>> getChatMessages(@PathVariable Long liveStreamId) {
        try {
            List<LiveChatMessage> messages = liveChatMessageService.getMessagesByLiveStreamId(liveStreamId);
            return Result.success(messages);
        } catch (Exception e) {
            return Result.error("获取聊天记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成词云
     */
    @PostMapping("/wordcloud/generate/{liveStreamId}")
    public Result<LiveWordCloud> generateWordCloud(@PathVariable Long liveStreamId) {
        try {
            LiveWordCloud wordCloud = liveWordCloudService.generateWordCloud(liveStreamId);
            if (wordCloud == null) {
                return Result.error("生成词云失败，没有聊天记录");
            }
            return Result.success(wordCloud);
        } catch (Exception e) {
            return Result.error("生成词云失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取词云数据
     */
    @GetMapping("/wordcloud/{liveStreamId}")
    public Result<LiveWordCloud> getWordCloud(@PathVariable Long liveStreamId) {
        try {
            LiveWordCloud wordCloud = liveWordCloudService.getWordCloud(liveStreamId);
            if (wordCloud == null) {
                return Result.error("词云数据不存在");
            }
            return Result.success(wordCloud);
        } catch (Exception e) {
            return Result.error("获取词云数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除聊天记录和词云
     */
    @DeleteMapping("/{liveStreamId}")
    public Result<String> deleteChatData(@PathVariable Long liveStreamId) {
        try {
            liveChatMessageService.deleteMessages(liveStreamId);
            liveWordCloudService.deleteWordCloud(liveStreamId);
            liveFeedbackService.deleteByLiveStream(liveStreamId);
            liveAnalysisService.deleteReport(liveStreamId);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}
