package cn.edu.zjut.back.controller;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.dto.AIRecommendationRequest;
import cn.edu.zjut.back.dto.AIRecommendationResponse;
import cn.edu.zjut.back.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AI相关Controller
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AIController {
    
    @Autowired
    private AIService aiService;
    
    /**
     * 获取AI学习建议和相关题目推荐
     */
    @PostMapping("/recommendations")
    public Result<AIRecommendationResponse> getAIRecommendations(@RequestBody AIRecommendationRequest request) {
        try {
            AIRecommendationResponse response = aiService.generateRecommendations(request);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("生成AI建议失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试AI接口连通性
     */
    @GetMapping("/health")
    public Result<Map<String, String>> healthCheck() {
        Map<String, String> data = new HashMap<>();
        data.put("status", "AI服务正常运行");
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return Result.success(data);
    }
}