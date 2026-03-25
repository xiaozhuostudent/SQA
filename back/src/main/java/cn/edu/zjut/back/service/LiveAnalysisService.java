package cn.edu.zjut.back.service;

import cn.edu.zjut.back.entity.LiveAnalysisReport;
import cn.edu.zjut.back.entity.LiveChatMessage;
import cn.edu.zjut.back.entity.LiveFeedback;
import cn.edu.zjut.back.mapper.LiveAnalysisReportMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LiveAnalysisService {

    private final LiveAnalysisReportMapper liveAnalysisReportMapper;
    private final LiveChatMessageService liveChatMessageService;
    private final LiveFeedbackService liveFeedbackService;
    private final RestTemplate restTemplate;

    @Value("${livestream.analysis.ai-endpoint:http://localhost:5051/chat}")
    private String aiEndpoint;

    public LiveAnalysisService(LiveAnalysisReportMapper liveAnalysisReportMapper,
                               LiveChatMessageService liveChatMessageService,
                               LiveFeedbackService liveFeedbackService,
                               RestTemplate restTemplate) {
        this.liveAnalysisReportMapper = liveAnalysisReportMapper;
        this.liveChatMessageService = liveChatMessageService;
        this.liveFeedbackService = liveFeedbackService;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public LiveAnalysisReport generateReport(Long liveStreamId) {
        List<LiveChatMessage> messages = liveChatMessageService.getMessagesByLiveStreamId(liveStreamId);
        if (messages == null) {
            messages = Collections.emptyList();
        }
        List<LiveFeedback> feedbackList = liveFeedbackService.getFeedbackByLiveStream(liveStreamId);
        if (feedbackList == null) {
            feedbackList = Collections.emptyList();
        }

        String prompt = buildPrompt(messages, feedbackList);
        String aiResult = requestReportFromAi(prompt);

        LiveAnalysisReport existing = liveAnalysisReportMapper.findByLiveStreamId(liveStreamId);
        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            existing = new LiveAnalysisReport();
            existing.setLiveStreamId(liveStreamId);
            existing.setGeneratedAt(now);
            existing.setReportContent(aiResult);
            existing.setAiProvider(aiEndpoint);
            existing.setPromptSnapshot(prompt);
            existing.setUpdatedAt(now);
            liveAnalysisReportMapper.insert(existing);
        } else {
            existing.setReportContent(aiResult);
            existing.setAiProvider(aiEndpoint);
            existing.setPromptSnapshot(prompt);
            existing.setUpdatedAt(now);
            liveAnalysisReportMapper.update(existing);
        }
        return liveAnalysisReportMapper.findByLiveStreamId(liveStreamId);
    }

    public LiveAnalysisReport getReport(Long liveStreamId) {
        return liveAnalysisReportMapper.findByLiveStreamId(liveStreamId);
    }

    public void deleteReport(Long liveStreamId) {
        liveAnalysisReportMapper.deleteByLiveStreamId(liveStreamId);
    }

    private String buildPrompt(List<LiveChatMessage> messages, List<LiveFeedback> feedbackList) {
        StringBuilder builder = new StringBuilder();
        builder.append("你是一名教学质量分析师，请根据下面的直播弹幕和学生反馈，生成一份300-400字的教学复盘报告。报告应包括：\n")
                .append("1. 学生讨论/关注的重点\n")
                .append("2. 存在的问题或困惑\n")
                .append("3. 学生反馈中值得保留或改进的建议\n")
                .append("4. 给老师的改进建议\n\n");

        if (!messages.isEmpty()) {
            builder.append("【弹幕摘录】\n");
            messages.stream()
                    .limit(80)
                    .forEach(msg -> builder.append("-")
                            .append(msg.getUserName())
                            .append(": ")
                            .append(msg.getContent())
                            .append("\n"));
        } else {
            builder.append("【弹幕摘录】暂无\n");
        }

        if (!feedbackList.isEmpty()) {
            builder.append("\n【学生反馈】\n");
            feedbackList.stream()
                    .limit(50)
                    .forEach(feedback -> builder.append("-")
                            .append(feedback.getUserName())
                            .append("(阶段:")
                            .append(feedback.getPhase())
                            .append(")：")
                            .append(feedback.getContent())
                            .append("\n"));
        } else {
            builder.append("\n【学生反馈】暂无\n");
        }

        String prompt = builder.toString();
        // 限制长度，避免请求过大
        if (prompt.length() > 6000) {
            prompt = prompt.substring(0, 6000);
        }
        return prompt;
    }

    @SuppressWarnings("unchecked")
    private String requestReportFromAi(String prompt) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("message", prompt);
        payload.put("session_id", "live-report-" + System.currentTimeMillis());

        try {
            Map<String, Object> response = restTemplate.postForObject(aiEndpoint, payload, Map.class);
            if (response != null) {
                if (response.get("reply") != null) {
                    return response.get("reply").toString();
                }
                if (response.get("answer_content") != null) {
                    return response.get("answer_content").toString();
                }
                Object output = response.get("output");
                if (output instanceof Map<?, ?> outputMap) {
                    Object text = outputMap.get("text");
                    if (text != null) {
                        return text.toString();
                    }
                }
            }
            return "AI未返回有效内容，请稍后重试。";
        } catch (Exception e) {
            return "自动生成失败：" + e.getMessage();
        }
    }
}
