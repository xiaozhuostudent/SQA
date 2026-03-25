package cn.edu.zjut.back.service;

import cn.edu.zjut.back.dto.AIRecommendationRequest;
import cn.edu.zjut.back.dto.AIRecommendationResponse;
import cn.edu.zjut.back.entity.QuestionBank;
import cn.edu.zjut.back.mapper.QuestionBankMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI服务类
 */
@Service
public class AIService {
    
    @Autowired
    private QuestionBankMapper questionBankMapper;
    
    @Value("${ai.api.url:http://localhost:5051/chat}")
    private String aiApiUrl;
    
    @Autowired
    private RestTemplate aiRestTemplate;
    
    /**
     * 生成学习建议和相关题目推荐
     */
    public AIRecommendationResponse generateRecommendations(AIRecommendationRequest request) {
        AIRecommendationResponse response = new AIRecommendationResponse();
        
        // 生成学习建议
        List<AIRecommendationResponse.Recommendation> recommendations = new ArrayList<>();
        
        // 基于整体表现的建议
        AIRecommendationResponse.Recommendation overallRecommendation = new AIRecommendationResponse.Recommendation();
        overallRecommendation.setTitle("整体学习建议");
        overallRecommendation.setContent(generateOverallRecommendation(request));
        recommendations.add(overallRecommendation);
        
        // 基于错题的建议
        for (AIRecommendationRequest.WrongAnswer wrongAnswer : request.getWrongAnswers()) {
            AIRecommendationResponse.Recommendation recommendation = new AIRecommendationResponse.Recommendation();
            recommendation.setTitle("错题分析: " + wrongAnswer.getContent().substring(0, Math.min(20, wrongAnswer.getContent().length())) + "...");
            recommendation.setContent(generateWrongAnswerRecommendation(wrongAnswer));
            recommendations.add(recommendation);
        }
        
        // 基于薄弱题型的建议
        if (request.getStatistics().getWeakTypes() != null && !request.getStatistics().getWeakTypes().isEmpty()) {
            AIRecommendationResponse.Recommendation weakTypeRecommendation = new AIRecommendationResponse.Recommendation();
            weakTypeRecommendation.setTitle("薄弱题型提升建议");
            weakTypeRecommendation.setContent(generateWeakTypeRecommendation(request.getStatistics().getWeakTypes()));
            recommendations.add(weakTypeRecommendation);
        }
        
        response.setRecommendations(recommendations);
        
        // 生成相关题目推荐
        List<AIRecommendationResponse.RelatedQuestion> relatedQuestions = generateRelatedQuestions(request);
        response.setRelatedQuestions(relatedQuestions);
        
        return response;
    }
    
    /**
     * 生成整体学习建议
     */
    private String generateOverallRecommendation(AIRecommendationRequest request) {
        int wrongCount = request.getWrongAnswers().size();
        double accuracy = Double.parseDouble(request.getStatistics().getAccuracy());
        
        if (wrongCount == 0) {
            return "恭喜您答对了所有题目！为了进一步提升，建议尝试更具挑战性的题目。";
        } else if (accuracy >= 80) {
            return String.format("您答错了%d道题目，正确率为%.1f%%。表现良好，建议在保持现有学习节奏的同时，增加练习题量，特别是针对薄弱题型。", 
                               wrongCount, accuracy);
        } else if (accuracy >= 60) {
            return String.format("您答错了%d道题目，正确率为%.1f%%。及格了，但还有提升空间，建议制定详细的学习计划，重点攻克薄弱知识点。", 
                               wrongCount, accuracy);
        } else {
            return String.format("您答错了%d道题目，正确率为%.1f%%。需要加强学习，建议复习基础知识点，并寻求老师或同学的帮助。", 
                               wrongCount, accuracy);
        }
    }
    
    /**
     * 生成错题分析建议
     */
    private String generateWrongAnswerRecommendation(AIRecommendationRequest.WrongAnswer wrongAnswer) {
        StringBuilder sb = new StringBuilder();
        sb.append("题目：").append(wrongAnswer.getContent()).append("\n");
        sb.append("您的答案：").append(wrongAnswer.getStudentAnswer() != null ? wrongAnswer.getStudentAnswer() : "未作答").append("\n");
        sb.append("正确答案：").append(wrongAnswer.getCorrectAnswer()).append("\n");
        
        if (wrongAnswer.getAnalysis() != null && !wrongAnswer.getAnalysis().isEmpty()) {
            sb.append("解析：").append(wrongAnswer.getAnalysis()).append("\n");
        }
        
        sb.append("建议：请仔细复习相关知识点，理解解题思路和方法。");
        return sb.toString();
    }
    
    /**
     * 生成薄弱题型建议
     */
    private String generateWeakTypeRecommendation(List<AIRecommendationRequest.Statistics.WeakType> weakTypes) {
        StringBuilder sb = new StringBuilder();
        sb.append("检测到您在以下题型掌握较弱：");
        for (int i = 0; i < weakTypes.size(); i++) {
            if (i > 0) sb.append("、");
            sb.append(weakTypes.get(i).getTypeName());
        }
        sb.append("。建议您针对这些题型进行专项练习，可以通过查阅教材、观看教学视频或请教老师同学来加强理解。");
        return sb.toString();
    }
    
    /**
     * 生成相关题目推荐
     */
    private List<AIRecommendationResponse.RelatedQuestion> generateRelatedQuestions(AIRecommendationRequest request) {
        List<AIRecommendationResponse.RelatedQuestion> relatedQuestions = new ArrayList<>();
        
        // 收集所有错题的题型
        List<String> wrongQuestionTypes = request.getWrongAnswers().stream()
                .map(AIRecommendationRequest.WrongAnswer::getQuestionType)
                .distinct()
                .collect(Collectors.toList());
        
        // 为每个错题题型查找相关练习题
        for (String questionType : wrongQuestionTypes) {
            // 从题库中查找同类型题目作为练习题
            List<QuestionBank> practiceQuestions = questionBankMapper.findByQuestionType(questionType);
            
            // 过滤掉已经做过的题目
            List<Long> answeredQuestionIds = request.getWrongAnswers().stream()
                    .map(AIRecommendationRequest.WrongAnswer::getQuestionId)
                    .collect(Collectors.toList());
            
            List<QuestionBank> filteredQuestions = practiceQuestions.stream()
                    .filter(q -> !answeredQuestionIds.contains(q.getId()))
                    .limit(2) // 每种题型最多推荐2道题
                    .collect(Collectors.toList());
            
            // 转换为推荐题目格式
            for (QuestionBank question : filteredQuestions) {
                AIRecommendationResponse.RelatedQuestion relatedQuestion = new AIRecommendationResponse.RelatedQuestion();
                relatedQuestion.setQuestionId(question.getId());
                relatedQuestion.setContent("[练习]" + question.getContent());
                relatedQuestion.setQuestionType(question.getQuestionType());
                relatedQuestion.setDifficulty(question.getDifficulty());
                relatedQuestions.add(relatedQuestion);
            }
        }
        
        return relatedQuestions;
    }
}