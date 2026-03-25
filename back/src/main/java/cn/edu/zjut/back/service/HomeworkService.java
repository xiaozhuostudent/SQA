package cn.edu.zjut.back.service;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.common.ResultCode;
import cn.edu.zjut.back.dto.HomeworkDTO;
import cn.edu.zjut.back.dto.HomeworkGradeDTO;
import cn.edu.zjut.back.dto.HomeworkSubmitDTO;
import cn.edu.zjut.back.entity.*;
import cn.edu.zjut.back.mapper.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 作业服务 - 支持从题库选择题目
 */
@Service
@Slf4j
public class HomeworkService {
    
    private final HomeworkMapper homeworkMapper;
    private final HomeworkQuestionMapper homeworkQuestionMapper;
    private final StudentHomeworkMapper studentHomeworkMapper;
    private final StudentHomeworkAnswerMapper studentHomeworkAnswerMapper;
    private final QuestionBankMapper questionBankMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    // AI评分服务地址
    private static final String AI_GRADING_SERVICE_URL = "http://localhost:5052/api/ai/grade-subjective";
    
    public HomeworkService(HomeworkMapper homeworkMapper, 
                          HomeworkQuestionMapper homeworkQuestionMapper,
                          StudentHomeworkMapper studentHomeworkMapper,
                          StudentHomeworkAnswerMapper studentHomeworkAnswerMapper,
                          QuestionBankMapper questionBankMapper,
                          CourseMapper courseMapper, 
                          UserMapper userMapper,
                          RestTemplate restTemplate,
                          ObjectMapper objectMapper) {
        this.homeworkMapper = homeworkMapper;
        this.homeworkQuestionMapper = homeworkQuestionMapper;
        this.studentHomeworkMapper = studentHomeworkMapper;
        this.studentHomeworkAnswerMapper = studentHomeworkAnswerMapper;
        this.questionBankMapper = questionBankMapper;
        this.courseMapper = courseMapper;
        this.userMapper = userMapper;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * 创建作业（包含题目） - 教师
     */
    @Transactional
    public Result<Homework> createHomework(Homework homework, List<HomeworkQuestion> questions, Long teacherId) {
        Course course = courseMapper.findById(homework.getCourseId());
        if (course == null) {
            return Result.error(ResultCode.COURSE_NOT_FOUND);
        }
        
        if (!course.getTeacherId().equals(teacherId)) {
            return Result.error(ResultCode.FORBIDDEN);
        }
        
        // 设置创建者信息
        User teacher = userMapper.findById(teacherId);
        homework.setCreatorId(teacherId);
        homework.setCreatorName(teacher.getRealName());
        homework.setCourseName(course.getName());
        homework.setStatus("draft");
        
        // 计算总分
        Integer totalScore = questions.stream()
            .map(HomeworkQuestion::getQuestionScore)
            .reduce(0, Integer::sum);
        homework.setTotalScore(totalScore);
        
        // 插入作业
        homeworkMapper.insert(homework);
        
        // 插入题目关联
        int order = 1;
        for (HomeworkQuestion question : questions) {
            question.setHomeworkId(homework.getId());
            question.setQuestionOrder(order++);
            homeworkQuestionMapper.insert(question);
        }
        
        return Result.success(homework);
    }
    
    /**
     * 获取作业详情（包含题目列表）
     */
    public Result<Map<String, Object>> getHomeworkDetail(Long homeworkId) {
        Homework homework = homeworkMapper.findById(homeworkId);
        if (homework == null) {
            return Result.error(ResultCode.HOMEWORK_NOT_FOUND);
        }
        
        List<HomeworkQuestion> questions = homeworkQuestionMapper.findByHomeworkId(homeworkId);
        
        // 获取题目详细信息
        List<Map<String, Object>> questionDetails = new ArrayList<>();
        for (HomeworkQuestion hq : questions) {
            QuestionBank question = questionBankMapper.findById(hq.getQuestionId());
            if (question != null) {
                Map<String, Object> detail = new HashMap<>();
                detail.put("homeworkQuestionId", hq.getId());
                detail.put("questionOrder", hq.getQuestionOrder());
                detail.put("score", hq.getQuestionScore()); // 使用score字段名
                // 扁平化question字段
                detail.put("id", question.getId());
                detail.put("content", question.getContent());
                detail.put("questionType", question.getQuestionType());
                detail.put("options", question.getOptions());
                detail.put("answer", question.getAnswer());
                detail.put("difficulty", question.getDifficulty());
                questionDetails.add(detail);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("homework", homework);
        result.put("questions", questionDetails);
        return Result.success(result);
    }
    
    /**
     * 获取课程作业列表
     */
    public Result<List<Homework>> getCourseHomeworks(Long courseId) {
        List<Homework> homeworks = homeworkMapper.findByCourseId(courseId);
        return Result.success(homeworks);
    }
    
    /**
     * 获取教师创建的作业列表
     */
    public Result<List<Homework>> getTeacherHomeworks(Long teacherId) {
        List<Homework> homeworks = homeworkMapper.findByCreatorId(teacherId);
        return Result.success(homeworks);
    }
    
    /**
     * 获取学生的作业列表（所有选课的已发布作业）
     */
    public Result<List<Homework>> getStudentHomeworks(Long studentId) {
        // 直接查询学生选课的所有已发布作业
        List<Homework> homeworks = homeworkMapper.findByStudentId(studentId);
        return Result.success(homeworks);
    }
    
    /**
     * 获取学生的作业提交记录
     */
    public Result<List<Map<String, Object>>> getStudentSubmissions(Long studentId) {
        List<StudentHomework> submissions = studentHomeworkMapper.findByStudentId(studentId);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (StudentHomework sh : submissions) {
            // 获取作业信息
            Homework homework = homeworkMapper.findById(sh.getHomeworkId());
            if (homework == null) continue;
            
            // 获取答案列表（带题目信息）
            List<StudentHomeworkAnswer> answers = studentHomeworkAnswerMapper.findByStudentHomeworkId(sh.getId());
            List<Map<String, Object>> answerDetails = new ArrayList<>();
            for (StudentHomeworkAnswer answer : answers) {
                QuestionBank question = questionBankMapper.findById(answer.getQuestionId());
                if (question != null) {
                    // 从HomeworkQuestion获取该题在作业中的分值
                    HomeworkQuestion hq = homeworkQuestionMapper.findByHomeworkIdAndQuestionId(
                        sh.getHomeworkId(), 
                        question.getId()
                    );
                    BigDecimal questionScore;
                    if (hq != null && hq.getQuestionScore() != null) {
                        questionScore = BigDecimal.valueOf(hq.getQuestionScore());
                    } else if (question.getScore() != null) {
                        questionScore = BigDecimal.valueOf(question.getScore());
                    } else {
                        questionScore = BigDecimal.ZERO;
                    }
                    
                    Map<String, Object> answerDetail = new HashMap<>();
                    answerDetail.put("id", answer.getId());
                    answerDetail.put("questionId", answer.getQuestionId());
                    answerDetail.put("studentAnswer", answer.getStudentAnswer());
                    answerDetail.put("score", answer.getScore());
                    answerDetail.put("isCorrect", answer.getIsCorrect());
                    answerDetail.put("teacherComment", answer.getTeacherComment());
                    answerDetail.put("aiScore", answer.getAiScore());
                    answerDetail.put("aiFeedback", parseAiFeedback(answer.getAiFeedback()));
                    // 添加题目信息
                    Map<String, Object> questionInfo = new HashMap<>();
                    questionInfo.put("id", question.getId());
                    questionInfo.put("content", question.getContent());
                    questionInfo.put("questionType", question.getQuestionType());
                    questionInfo.put("answer", question.getAnswer());
                    questionInfo.put("options", question.getOptions());
                    questionInfo.put("score", questionScore); // 使用作业中的分值
                    answerDetail.put("question", questionInfo);
                    answerDetails.add(answerDetail);
                }
            }
            
            Map<String, Object> item = new HashMap<>();
            item.put("homeworkId", homework.getId());
            item.put("title", homework.getTitle());
            item.put("courseName", homework.getCourseName());
            item.put("totalScore", homework.getTotalScore());
            item.put("submitTime", sh.getSubmitTime());
            item.put("score", sh.getScore());
            item.put("status", sh.getStatus());
            item.put("isLate", sh.getIsLate());
            item.put("teacherComment", sh.getTeacherComment());
            item.put("answers", answerDetails);
            
            result.add(item);
        }
        
        return Result.success(result);
    }
    
    /**
     * 更新作业
     */
    @Transactional
    public Result<String> updateHomework(Homework homework, List<HomeworkQuestion> questions, Long teacherId) {
        Homework existing = homeworkMapper.findById(homework.getId());
        if (existing == null) {
            return Result.error(ResultCode.HOMEWORK_NOT_FOUND);
        }
        
        if (!existing.getCreatorId().equals(teacherId)) {
            return Result.error(ResultCode.FORBIDDEN);
        }
        
        // 计算总分
        if (questions != null && !questions.isEmpty()) {
            Integer totalScore = questions.stream()
                .map(HomeworkQuestion::getQuestionScore)
                .reduce(0, Integer::sum);
            homework.setTotalScore(totalScore);
            
            // 删除旧的题目关联
            homeworkQuestionMapper.deleteByHomeworkId(homework.getId());
            
            // 插入新的题目关联
            int order = 1;
            for (HomeworkQuestion question : questions) {
                question.setHomeworkId(homework.getId());
                question.setQuestionOrder(order++);
                homeworkQuestionMapper.insert(question);
            }
        }
        
        homeworkMapper.update(homework);
        return Result.success("作业更新成功");
    }
    
    /**
     * 发布作业
     */
    public Result<String> publishHomework(Long homeworkId, Long teacherId) {
        Homework homework = homeworkMapper.findById(homeworkId);
        if (homework == null) {
            return Result.error(ResultCode.HOMEWORK_NOT_FOUND);
        }
        
        if (!homework.getCreatorId().equals(teacherId)) {
            return Result.error(ResultCode.FORBIDDEN);
        }
        
        homework.setStatus("published");
        homeworkMapper.update(homework);
        return Result.success("作业发布成功");
    }
    
    /**
     * 删除作业
     */
    @Transactional
    public Result<String> deleteHomework(Long homeworkId, Long teacherId) {
        Homework homework = homeworkMapper.findById(homeworkId);
        if (homework == null) {
            return Result.error(ResultCode.HOMEWORK_NOT_FOUND);
        }
        
        if (!homework.getCreatorId().equals(teacherId)) {
            return Result.error(ResultCode.FORBIDDEN);
        }
        
        // 删除题目关联
        homeworkQuestionMapper.deleteByHomeworkId(homeworkId);
        
        // 删除作业
        homeworkMapper.delete(homeworkId);
        return Result.success("作业删除成功");
    }
    
    /**
     * 学生开始作业
     */
    @Transactional
    public Result<StudentHomework> startHomework(Long homeworkId, Long studentId) {
        Homework homework = homeworkMapper.findById(homeworkId);
        if (homework == null) {
            return Result.error(ResultCode.HOMEWORK_NOT_FOUND);
        }
        
        if (!"published".equals(homework.getStatus())) {
            return Result.error(ResultCode.FORBIDDEN);
        }
        
        // 检查是否已有记录
        StudentHomework existing = studentHomeworkMapper.findByHomeworkAndStudent(homeworkId, studentId);
        if (existing != null && "submitted".equals(existing.getStatus())) {
            return Result.error(ResultCode.FORBIDDEN);
        }
        
        if (existing == null) {
            // 创建新记录
            User student = userMapper.findById(studentId);
            StudentHomework studentHomework = new StudentHomework();
            studentHomework.setHomeworkId(homeworkId);
            studentHomework.setStudentId(studentId);
            studentHomework.setStudentName(student.getRealName());
            studentHomework.setStudentNumber(student.getUsername()); // 假设username是学号
            studentHomework.setStartTime(LocalDateTime.now());
            studentHomework.setStatus("in_progress");
            
            studentHomeworkMapper.insert(studentHomework);
            return Result.success(studentHomework);
        }
        
        return Result.success(existing);
    }
    
    /**
     * 提交作业答案
     */
    @Transactional
    public Result<String> submitHomework(Long studentHomeworkId, List<StudentHomeworkAnswer> answers) {
        StudentHomework studentHomework = studentHomeworkMapper.findById(studentHomeworkId);
        if (studentHomework == null) {
            return Result.error(ResultCode.HOMEWORK_NOT_FOUND);
        }
        
        if ("submitted".equals(studentHomework.getStatus())) {
            return Result.error(ResultCode.FORBIDDEN);
        }
        
        Homework homework = homeworkMapper.findById(studentHomework.getHomeworkId());
        
        // 检查是否迟交
        boolean isLate = LocalDateTime.now().isAfter(homework.getDeadline());
        
        // 保存答案
        for (StudentHomeworkAnswer answer : answers) {
            answer.setStudentHomeworkId(studentHomeworkId);
            answer.setAiScore(null);
            answer.setAiFeedback(null);
            studentHomeworkAnswerMapper.insert(answer);
        }

        Map<Long, HomeworkQuestion> homeworkQuestionMap = buildHomeworkQuestionMap(homework.getId());
        BigDecimal objectiveScore = autoGradeObjectiveQuestions(studentHomeworkId, homeworkQuestionMap);
        BigDecimal subjectiveScore = autoGradeSubjectiveQuestions(studentHomeworkId, homeworkQuestionMap);
        BigDecimal totalScore = objectiveScore.add(subjectiveScore);
        
        // 更新学生作业记录
        studentHomework.setSubmitTime(LocalDateTime.now());
        studentHomework.setStatus("submitted");
        studentHomework.setScore(totalScore);
        studentHomework.setIsLate(isLate);
        
        // 如果迟交且有扣分
        if (isLate && homework.getAllowLateSubmission() && homework.getLatePenalty() != null) {
            BigDecimal penalty = totalScore.multiply(new BigDecimal(homework.getLatePenalty())).divide(new BigDecimal(100));
            studentHomework.setScore(totalScore.subtract(penalty));
        }
        
        studentHomeworkMapper.update(studentHomework);
        return Result.success("作业提交成功");
    }
    
    /**
     * 自动批改客观题
     */
    private BigDecimal autoGradeObjectiveQuestions(Long studentHomeworkId, Map<Long, HomeworkQuestion> homeworkQuestionMap) {
        List<StudentHomeworkAnswer> answers = studentHomeworkAnswerMapper.findByStudentHomeworkId(studentHomeworkId);
        BigDecimal totalScore = BigDecimal.ZERO;
        
        for (StudentHomeworkAnswer answer : answers) {
            QuestionBank question = questionBankMapper.findById(answer.getQuestionId());
            if (question != null && isObjectiveQuestion(question.getQuestionType())) {
                HomeworkQuestion hq = homeworkQuestionMap.get(answer.getQuestionId());
                Integer questionScore = hq != null ? hq.getQuestionScore() : null;
                if (questionScore == null && question.getScore() != null) {
                    questionScore = question.getScore();
                }
                
                boolean isCorrect = checkAnswer(question.getQuestionType(), question.getAnswer(), answer.getStudentAnswer());
                answer.setIsCorrect(isCorrect);
                
                if (questionScore != null) {
                    BigDecimal gained = isCorrect ? new BigDecimal(questionScore) : BigDecimal.ZERO;
                    answer.setScore(gained);
                    if (isCorrect) {
                        totalScore = totalScore.add(gained);
                    }
                } else {
                    answer.setScore(BigDecimal.ZERO);
                }
                
                studentHomeworkAnswerMapper.update(answer);
            }
        }
        
        return totalScore;
    }

    private BigDecimal autoGradeSubjectiveQuestions(Long studentHomeworkId, Map<Long, HomeworkQuestion> homeworkQuestionMap) {
        List<StudentHomeworkAnswer> answers = studentHomeworkAnswerMapper.findByStudentHomeworkId(studentHomeworkId);
        BigDecimal totalScore = BigDecimal.ZERO;

        for (StudentHomeworkAnswer answer : answers) {
            QuestionBank question = questionBankMapper.findById(answer.getQuestionId());
            if (question == null || isObjectiveQuestion(question.getQuestionType())) {
                continue;
            }

            if (answer.getStudentAnswer() == null || answer.getStudentAnswer().trim().isEmpty()) {
                continue;
            }

            HomeworkQuestion hq = homeworkQuestionMap.get(answer.getQuestionId());
            Integer questionScore = hq != null ? hq.getQuestionScore() : null;
            if (questionScore == null && question.getScore() != null) {
                questionScore = question.getScore();
            }

            int totalScoreForAi = questionScore != null ? questionScore : 10;

            try {
                Map<String, Object> aiData = invokeAiService(
                    question.getContent(),
                    question.getAnswer(),
                    answer.getStudentAnswer(),
                    totalScoreForAi,
                    question.getQuestionType()
                );
                applyAiResult(answer, aiData, totalScoreForAi);
                studentHomeworkAnswerMapper.update(answer);
                if (answer.getScore() != null) {
                    totalScore = totalScore.add(answer.getScore());
                }
            } catch (Exception ex) {
                log.error("自动AI评分失败, answerId={}, message={}", answer.getId(), ex.getMessage());
            }
        }

        return totalScore;
    }
    
    /**
     * 判断是否为客观题
     */
    private boolean isObjectiveQuestion(String questionType) {
        return "single_choice".equals(questionType) || 
               "multiple_choice".equals(questionType) || 
               "true_false".equals(questionType) ||
               "fill_blank".equals(questionType);
    }
    
    /**
     * 检查答案是否正确
     * 对于多选题，会忽略选项顺序进行比较
     */
    private boolean checkAnswer(String questionType, String correctAnswer, String studentAnswer) {
        if (correctAnswer == null || studentAnswer == null) {
            return false;
        }
        
        // 单选题、判断题和填空题：直接比较（忽略首尾空格）
        if ("single_choice".equals(questionType) || "true_false".equals(questionType) || "fill_blank".equals(questionType)) {
            return correctAnswer.trim().equals(studentAnswer.trim());
        }
        
        // 多选题：需要排序后比较（忽略选项顺序）
        if ("multiple_choice".equals(questionType)) {
            String[] correctOptions = correctAnswer.split(",");
            String[] studentOptions = studentAnswer.split(",");
            
            // 去除空格并排序
            java.util.Arrays.sort(correctOptions);
            java.util.Arrays.sort(studentOptions);
            
            // 比较排序后的数组
            return java.util.Arrays.equals(correctOptions, studentOptions);
        }
        
        return false;
    }
    
    /**
     * 教师批改作业（主观题）
     */
    @Transactional
    public Result<String> gradeHomework(Long studentHomeworkId, List<Map<String, Object>> grades, String teacherComment) {
        StudentHomework studentHomework = studentHomeworkMapper.findById(studentHomeworkId);
        if (studentHomework == null) {
            return Result.error(ResultCode.HOMEWORK_NOT_FOUND);
        }
        
        // 先更新主观题分数
        for (Map<String, Object> gradeInfo : grades) {
            Long answerId = Long.valueOf(gradeInfo.get("answerId").toString());
            BigDecimal score = new BigDecimal(gradeInfo.get("score").toString());
            String comment = (String) gradeInfo.get("comment");
            
            StudentHomeworkAnswer answer = studentHomeworkAnswerMapper.findById(answerId);
            if (answer != null) {
                answer.setScore(score);
                answer.setTeacherComment(comment);
                studentHomeworkAnswerMapper.update(answer);
            }
        }
        
        // 重新计算总分：遍历所有答案，累加每道题的得分
        List<StudentHomeworkAnswer> allAnswers = studentHomeworkAnswerMapper.findByStudentHomeworkId(studentHomeworkId);
        BigDecimal totalScore = BigDecimal.ZERO;
        for (StudentHomeworkAnswer answer : allAnswers) {
            if (answer.getScore() != null) {
                totalScore = totalScore.add(answer.getScore());
            }
        }
        
        // 更新学生作业记录
        studentHomework.setScore(totalScore);
        studentHomework.setStatus("graded");
        studentHomework.setTeacherComment(teacherComment);
        studentHomeworkMapper.update(studentHomework);
        
        return Result.success("批改成功");
    }
    
    /**
     * 获取作业提交列表（教师）
     */
    public Result<List<Map<String, Object>>> getHomeworkSubmissions(Long homeworkId) {
        List<StudentHomework> submissions = studentHomeworkMapper.findSubmittedByHomeworkId(homeworkId);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (StudentHomework sh : submissions) {
            Map<String, Object> item = new HashMap<>();
            // 扁平化studentHomework字段
            item.put("id", sh.getId());
            item.put("studentId", sh.getStudentId());
            item.put("studentName", sh.getStudentName());
            item.put("studentNumber", sh.getStudentNumber());
            item.put("submitTime", sh.getSubmitTime());
            item.put("score", sh.getScore());
            item.put("status", sh.getStatus());
            item.put("isLate", sh.getIsLate());
            item.put("teacherComment", sh.getTeacherComment());
            
            // 获取答案列表（带题目信息）
            List<StudentHomeworkAnswer> answers = studentHomeworkAnswerMapper.findByStudentHomeworkId(sh.getId());
            List<Map<String, Object>> answerDetails = new ArrayList<>();
            for (StudentHomeworkAnswer answer : answers) {
                QuestionBank question = questionBankMapper.findById(answer.getQuestionId());
                if (question != null) {
                    Map<String, Object> answerDetail = new HashMap<>();
                    answerDetail.put("id", answer.getId());
                    answerDetail.put("questionId", answer.getQuestionId());
                    answerDetail.put("studentAnswer", answer.getStudentAnswer());
                    answerDetail.put("score", answer.getScore());
                    answerDetail.put("isCorrect", answer.getIsCorrect());
                    answerDetail.put("teacherComment", answer.getTeacherComment());
                    answerDetail.put("aiScore", answer.getAiScore());
                    answerDetail.put("aiFeedback", parseAiFeedback(answer.getAiFeedback()));
                    // 添加题目信息
                    Map<String, Object> questionInfo = new HashMap<>();
                    questionInfo.put("id", question.getId());
                    questionInfo.put("content", question.getContent());
                    questionInfo.put("questionType", question.getQuestionType());
                    questionInfo.put("answer", question.getAnswer());
                    questionInfo.put("score", question.getScore());
                    answerDetail.put("question", questionInfo);
                    answerDetails.add(answerDetail);
                }
            }
            item.put("answers", answerDetails);
            
            // 保留原始对象供需要时使用
            item.put("studentHomework", sh);
            
            result.add(item);
        }
        
        return Result.success(result);
    }
    
    /**
     * 获取作业统计信息
     */
    public Result<Map<String, Object>> getHomeworkStats(Long homeworkId) {
        List<StudentHomework> allRecords = studentHomeworkMapper.findByHomeworkId(homeworkId);
        
        int total = allRecords.size();
        long submitted = allRecords.stream().filter(s -> 
            "submitted".equals(s.getStatus()) || "graded".equals(s.getStatus())
        ).count();
        long ungraded = allRecords.stream().filter(s -> 
            "submitted".equals(s.getStatus())
        ).count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("submitted", submitted);
        stats.put("ungraded", ungraded);
        stats.put("submissionRate", total > 0 ? (double) submitted / total * 100 : 0);
        
        return Result.success(stats);
    }
    
    /**
     * AI自动评分主观题
     */
    @Transactional
    public Result<Map<String, Object>> aiGradeSubjective(
            Long answerId,
            String questionContent,
            String standardAnswer,
            String studentAnswer,
            Integer totalScore,
            String questionType) {
        
        try {
            Map<String, Object> aiData = invokeAiService(
                questionContent,
                standardAnswer,
                studentAnswer,
                totalScore,
                questionType
            );

            StudentHomeworkAnswer answer = studentHomeworkAnswerMapper.findById(answerId);
            if (answer == null) {
                return Result.error("答案不存在");
            }

            applyAiResult(answer, aiData, totalScore);
            studentHomeworkAnswerMapper.update(answer);
            recalculateStudentHomeworkScore(answer.getStudentHomeworkId());

            Map<String, Object> result = new HashMap<>();
            result.put("answerId", answerId);
            result.put("score", answer.getAiScore());
            result.put("comment", aiData.get("comment"));
            result.put("aiFeedback", aiData);

            return Result.success(result);
        } catch (Exception e) {
            log.error("AI评分调用失败", e);
            return Result.error("AI评分调用失败: " + e.getMessage());
        }
    }

    private Map<Long, HomeworkQuestion> buildHomeworkQuestionMap(Long homeworkId) {
        return homeworkQuestionMapper.findByHomeworkId(homeworkId).stream()
            .collect(Collectors.toMap(HomeworkQuestion::getQuestionId, hq -> hq, (a, b) -> a));
    }

    private Map<String, Object> parseAiFeedback(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("解析AI反馈失败: {}", e.getMessage());
            return Map.of("comment", json);
        }
    }

    private String serializeAiFeedback(Map<String, Object> data) throws JsonProcessingException {
        return data == null ? null : objectMapper.writeValueAsString(data);
    }

    private BigDecimal extractScore(Map<String, Object> aiData, Integer totalScore) {
        if (aiData == null) {
            return BigDecimal.ZERO;
        }
        Object scoreObj = aiData.get("score");
        BigDecimal aiScore = BigDecimal.ZERO;
        if (scoreObj instanceof Number number) {
            aiScore = BigDecimal.valueOf(number.doubleValue());
        } else if (scoreObj != null) {
            try {
                aiScore = new BigDecimal(scoreObj.toString());
            } catch (NumberFormatException ignored) {
                aiScore = BigDecimal.ZERO;
            }
        }

        if (totalScore != null) {
            BigDecimal max = new BigDecimal(totalScore);
            if (aiScore.compareTo(max) > 0) {
                aiScore = max;
            }
        }

        if (aiScore.compareTo(BigDecimal.ZERO) < 0) {
            aiScore = BigDecimal.ZERO;
        }
        return aiScore;
    }

    private void applyAiResult(StudentHomeworkAnswer answer, Map<String, Object> aiData, Integer totalScore) throws JsonProcessingException {
        BigDecimal aiScore = extractScore(aiData, totalScore);
        answer.setScore(aiScore);
        answer.setAiScore(aiScore);
        answer.setAiFeedback(serializeAiFeedback(aiData));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> invokeAiService(String questionContent,
                                                String standardAnswer,
                                                String studentAnswer,
                                                Integer totalScore,
                                                String questionType) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("questionContent", questionContent);
        requestBody.put("standardAnswer", standardAnswer);
        requestBody.put("studentAnswer", studentAnswer);
        requestBody.put("totalScore", totalScore);
        requestBody.put("questionType", questionType);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
            AI_GRADING_SERVICE_URL,
            request,
            Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> responseBody = response.getBody();
            if (Boolean.TRUE.equals(responseBody.get("success"))) {
                return (Map<String, Object>) responseBody.get("data");
            }
            throw new IllegalStateException("AI评分失败: " + responseBody.get("message"));
        }
        throw new IllegalStateException("AI评分服务响应异常");
    }

    private void recalculateStudentHomeworkScore(Long studentHomeworkId) {
        List<StudentHomeworkAnswer> allAnswers = studentHomeworkAnswerMapper.findByStudentHomeworkId(studentHomeworkId);
        BigDecimal total = BigDecimal.ZERO;
        for (StudentHomeworkAnswer answer : allAnswers) {
            if (answer.getScore() != null) {
                total = total.add(answer.getScore());
            }
        }
        StudentHomework record = studentHomeworkMapper.findById(studentHomeworkId);
        if (record != null) {
            record.setScore(total);
            studentHomeworkMapper.update(record);
        }
    }
}
