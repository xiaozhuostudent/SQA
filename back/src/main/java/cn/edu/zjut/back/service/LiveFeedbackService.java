package cn.edu.zjut.back.service;

import cn.edu.zjut.back.entity.LiveFeedback;
import cn.edu.zjut.back.mapper.LiveFeedbackMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LiveFeedbackService {

    private final LiveFeedbackMapper liveFeedbackMapper;

    public LiveFeedbackService(LiveFeedbackMapper liveFeedbackMapper) {
        this.liveFeedbackMapper = liveFeedbackMapper;
    }

    public LiveFeedback submitFeedback(LiveFeedback feedback) {
        if (feedback.getPhase() == null || feedback.getPhase().isBlank()) {
            feedback.setPhase("live");
        }
        if (feedback.getCreatedAt() == null) {
            feedback.setCreatedAt(LocalDateTime.now());
        }
        liveFeedbackMapper.insert(feedback);
        return feedback;
    }

    public List<LiveFeedback> getFeedbackByLiveStream(Long liveStreamId) {
        return liveFeedbackMapper.findByLiveStreamId(liveStreamId);
    }

    public void deleteByLiveStream(Long liveStreamId) {
        liveFeedbackMapper.deleteByLiveStreamId(liveStreamId);
    }
}
