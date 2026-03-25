package cn.edu.zjut.back.service;

import cn.edu.zjut.back.entity.QuestionBank;
import cn.edu.zjut.back.mapper.QuestionBankMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * 题库服务类
 */
@Service
public class QuestionBankService {
    private final QuestionBankMapper questionBankMapper;

    public QuestionBankService(QuestionBankMapper questionBankMapper) {
        this.questionBankMapper = questionBankMapper;
    }

    /**
     * 创建题目
     */
    public QuestionBank createQuestion(QuestionBank question) {
        // 设置默认值
        if (question.getStatus() == null) {
            question.setStatus("active");
        }
        if (question.getIsVisible() == null) {
            question.setIsVisible(1);
        }
        if (question.getUsageCount() == null) {
            question.setUsageCount(0);
        }
        if (question.getScore() == null) {
            question.setScore(5);
        }
        if (question.getDifficulty() == null) {
            question.setDifficulty("medium");
        }

        questionBankMapper.insert(question);
        return question;
    }

    /**
     * 获取所有题目（根据角色过滤）
     */
    public List<QuestionBank> getAllQuestions(String userRole) {
        if ("student".equals(userRole)) {
            return questionBankMapper.findAllVisibleForStudent();
        }
        return questionBankMapper.findAll();
    }

    /**
     * 根据课程ID获取题目
     */
    public List<QuestionBank> getQuestionsByCourse(Long courseId, String userRole) {
        if ("student".equals(userRole)) {
            return questionBankMapper.findByCourseIdVisibleForStudent(courseId);
        }
        return questionBankMapper.findByCourseId(courseId);
    }

    /**
     * 根据题型获取题目
     */
    public List<QuestionBank> getQuestionsByType(String questionType, String userRole) {
        List<QuestionBank> questions = questionBankMapper.findByQuestionType(questionType);
        if ("student".equals(userRole)) {
            return questions.stream()
                    .filter(q -> q.getIsVisible() == 1)
                    .toList();
        }
        return questions;
    }

    /**
     * 根据ID获取题目
     */
    public QuestionBank getQuestionById(Long id) {
        return questionBankMapper.findById(id);
    }

    /**
     * 更新题目
     */
    public void updateQuestion(QuestionBank question) {
        questionBankMapper.update(question);
    }

    /**
     * 删除题目
     */
    public void deleteQuestion(Long id) {
        questionBankMapper.delete(id);
    }

    /**
     * 根据试卷ID获取题目
     */
    public List<QuestionBank> getQuestionsByExamPaper(Long examPaperId) {
        return questionBankMapper.findByExamPaperId(examPaperId);
    }

    /**
     * 批量导入题目
     */
    public Map<String, Object> batchImport(List<QuestionBank> questions) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < questions.size(); i++) {
            try {
                QuestionBank question = questions.get(i);
                
                // 验证必填字段
                if (question.getCourseId() == null) {
                    throw new IllegalArgumentException("课程ID不能为空");
                }
                if (question.getContent() == null || question.getContent().trim().isEmpty()) {
                    throw new IllegalArgumentException("题目内容不能为空");
                }
                if (question.getAnswer() == null || question.getAnswer().trim().isEmpty()) {
                    throw new IllegalArgumentException("答案不能为空");
                }
                
                // 转换难度值：1->easy, 2->medium, 3->hard
                String difficulty = question.getDifficulty();
                if (difficulty != null) {
                    if ("1".equals(difficulty)) {
                        question.setDifficulty("easy");
                    } else if ("2".equals(difficulty)) {
                        question.setDifficulty("medium");
                    } else if ("3".equals(difficulty)) {
                        question.setDifficulty("hard");
                    } else if (!"easy".equals(difficulty) && !"medium".equals(difficulty) && !"hard".equals(difficulty)) {
                        question.setDifficulty("medium"); // 默认值
                    }
                }
                
                // 转换options字段为JSON数组格式
                String options = question.getOptions();
                if (options != null && !options.trim().isEmpty()) {
                    // 如果是以分号分隔的字符串（如：A. xxx;B. yyy;C. zzz），转换为JSON数组
                    if (!options.trim().startsWith("[")) {
                        String[] parts = options.split(";");
                        StringBuilder jsonArray = new StringBuilder("[");
                        for (int j = 0; j < parts.length; j++) {
                            if (j > 0) jsonArray.append(", ");
                            jsonArray.append("\"").append(parts[j].trim()).append("\"");
                        }
                        jsonArray.append("]");
                        question.setOptions(jsonArray.toString());
                    }
                } else {
                    // 如果options为空，设置为null（对于判断题、简答题等）
                    question.setOptions(null);
                }
                
                // 调用创建方法，它会设置默认值
                createQuestion(question);
                successCount++;
            } catch (Exception e) {
                failCount++;
                errors.add("第" + (i + 1) + "题: " + e.getMessage());
            }
        }

        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("total", questions.size());
        result.put("errors", errors);
        return result;
    }
}
