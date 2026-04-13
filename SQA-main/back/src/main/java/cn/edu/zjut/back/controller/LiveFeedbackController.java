package cn.edu.zjut.back.controller;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.entity.LiveFeedback;
import cn.edu.zjut.back.service.LiveFeedbackService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/live-feedback")
@CrossOrigin
public class LiveFeedbackController {

    private final LiveFeedbackService liveFeedbackService;

    public LiveFeedbackController(LiveFeedbackService liveFeedbackService) {
        this.liveFeedbackService = liveFeedbackService;
    }

    @PostMapping
    public Result<LiveFeedback> submitFeedback(@RequestBody LiveFeedback feedback) {
        try {
            if (feedback.getLiveStreamId() == null) {
                return Result.error("直播ID不能为空");
            }
            if (feedback.getUserId() == null) {
                return Result.error("用户ID不能为空");
            }
            if (feedback.getContent() == null || feedback.getContent().isBlank()) {
                return Result.error("反馈内容不能为空");
            }
            LiveFeedback saved = liveFeedbackService.submitFeedback(feedback);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.error("提交反馈失败: " + e.getMessage());
        }
    }

    @GetMapping("/{liveStreamId}")
    public Result<List<LiveFeedback>> getFeedback(@PathVariable Long liveStreamId) {
        try {
            List<LiveFeedback> feedbackList = liveFeedbackService.getFeedbackByLiveStream(liveStreamId);
            return Result.success(feedbackList);
        } catch (Exception e) {
            return Result.error("获取反馈失败: " + e.getMessage());
        }
    }
}
