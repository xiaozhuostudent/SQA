package cn.edu.zjut.back.dto;

import lombok.Data;
import java.util.List;

/**
 * AI推荐请求DTO
 */
@Data
public class AIRecommendationRequest {
    private Long studentId;
    private List<WrongAnswer> wrongAnswers;
    private Statistics statistics;
    
    @Data
    public static class WrongAnswer {
        private Long questionId;
        private String content;
        private String questionType;
        private String studentAnswer;
        private String correctAnswer;
        private String analysis;
    }
    
    @Data
    public static class Statistics {
        private int correctCount;
        private int wrongCount;
        private String accuracy;
        private List<WeakType> weakTypes;
        
        @Data
        public static class WeakType {
            private String type;
            private String typeName;
            private String accuracy;
            private int wrongCount;
        }
    }
}