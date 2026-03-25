package cn.edu.zjut.back.dto;

import lombok.Data;
import java.util.List;

/**
 * AI推荐响应DTO
 */
@Data
public class AIRecommendationResponse {
    private List<Recommendation> recommendations;
    private List<RelatedQuestion> relatedQuestions;
    
    @Data
    public static class Recommendation {
        private String title;
        private String content;
    }
    
    @Data
    public static class RelatedQuestion {
        private Long questionId;
        private String content;
        private String questionType;
        private String difficulty;
    }
}