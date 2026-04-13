package cn.edu.zjut.back.service;

import cn.edu.zjut.back.entity.*;
import cn.edu.zjut.back.mapper.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 考试服务类
 */
@Service
public class ExamService {
    
    @Autowired
    private ExamPaperMapper examPaperMapper;
    
    @Autowired
    private QuestionBankMapper questionBankMapper;
    
    @Autowired
    private StudentExamMapper studentExamMapper;
    
    @Autowired
    private StudentAnswerMapper studentAnswerMapper;
    
    @Autowired
    private ExamQuestionMapper examQuestionMapper;
    
    // 服务器上的资源路径（生产环境）
    // 本地开发环境会使用 storage/exam/ 目录
    private static final String EXAM_RESOURCE_PATH_PRODUCTION = "/www/wwwroot/default/resources/exam/";
    private static final String EXAM_RESOURCE_PATH_LOCAL = "storage/exam/";
    
    // 根据环境选择路径
    private String getExamResourcePath() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("mac") || osName.contains("windows")) {
            // 本地开发环境
            return EXAM_RESOURCE_PATH_LOCAL;
        } else {
            // Linux 生产环境
            return EXAM_RESOURCE_PATH_PRODUCTION;
        }
    }
    
    /**
     * 创建试卷
     */
    @Transactional
    public ExamPaper createExamPaper(ExamPaper examPaper) {
        // Ensure passScore has a sensible default to satisfy NOT NULL constraint
        if (examPaper.getPassScore() == null) {
            Integer total = examPaper.getTotalScore();
            int defaultPass = (total != null && total > 0) ? (int) Math.round(total * 0.6) : 60;
            examPaper.setPassScore(defaultPass);
        }
        examPaperMapper.insert(examPaper);
        return examPaper;
    }
    
    /**
     * 获取试卷列表
     */
    public List<ExamPaper> getExamPapersByCourse(Long courseId) {
        return examPaperMapper.findByCourseId(courseId);
    }
    
    /**
     * 根据创建者ID获取试卷列表
     */
    public List<ExamPaper> getExamPapersByCreator(Long creatorId) {
        return examPaperMapper.findByCreatorId(creatorId);
    }
    
    /**
     * 学生开始考试
     */
    @Transactional
    public StudentExam startExam(Long examPaperId, Long studentId, String studentName, 
                                  String studentNumber, String ipAddress) {
        // 检查试卷是否存在
        ExamPaper examPaper = examPaperMapper.findById(examPaperId);
        if (examPaper == null) {
            throw new RuntimeException("试卷不存在");
        }
        
        // 检查试卷状态是否为已发布
        if (!"published".equals(examPaper.getStatus())) {
            throw new RuntimeException("试卷未发布，无法开始考试");
        }
        
        // 检查考试时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(examPaper.getStartTime())) {
            throw new RuntimeException("考试尚未开始");
        }
        if (now.isAfter(examPaper.getEndTime())) {
            throw new RuntimeException("考试已结束");
        }
        
        // 检查是否已有考试记录
        List<StudentExam> existingRecords = studentExamMapper.findByExamAndStudentAll(examPaperId, studentId);
        
        // 创建新的考试记录，每次都创建新记录而不是覆盖旧记录
        StudentExam studentExam = new StudentExam();
        studentExam.setExamPaperId(examPaperId);
        studentExam.setStudentId(studentId);
        studentExam.setStudentName(studentName);
        studentExam.setStudentNumber(studentNumber);
        studentExam.setAttemptNumber(existingRecords.size() + 1); // 设置尝试次数
        studentExam.setStartTime(LocalDateTime.now());
        studentExam.setStatus("in_progress");
        studentExam.setIpAddress(ipAddress);
        
        studentExamMapper.insert(studentExam);
        return studentExam;
    }
    
    /**
     * 提交考试答案
     */
    @Transactional
    public void submitExam(Long studentExamId, List<StudentAnswer> answers) {
        StudentExam studentExam = studentExamMapper.findById(studentExamId);
        if (studentExam == null || !"in_progress".equals(studentExam.getStatus())) {
            throw new RuntimeException("考试记录不存在或状态错误");
        }
        
        // 保存答案
        for (StudentAnswer answer : answers) {
            answer.setStudentExamId(studentExamId);
            answer.setAttemptNumber(studentExam.getAttemptNumber()); // 设置尝试次数
            answer.setAnswerTime(LocalDateTime.now());
            
            // 总是插入新答案记录而不是更新已有记录
            studentAnswerMapper.insert(answer);
        }
        
        // 获取试卷信息
        ExamPaper paper = examPaperMapper.findById(studentExam.getExamPaperId());
        int duration = paper != null ? paper.getDuration() : 90;
        int passScore = paper != null ? paper.getPassScore() : 60;
        int paperTotalScore = paper != null ? paper.getTotalScore() : 100;
        
        // 计算试卷所有题目的分值总和
        int questionsTotalScore = calculateQuestionsTotalScore(studentExam.getExamPaperId());
        
        // 自动批改客观题（返回答对题目的原始分值）
        int objectiveRawScore = autoGradeObjectiveQuestions(studentExamId);
        
        // 计算主观题原始得分（返回答对题目的原始分值）
        int subjectiveRawScore = calculateSubjectiveScore(studentExamId);
        
        // 按比例计算实际得分
        int objectiveScore = 0;
        int subjectiveScore = 0;
        int totalScore = 0;
        
        if (questionsTotalScore > 0) {
            // 客观题实际得分 = (答对分值 / 题目总分值) × 试卷总分
            objectiveScore = Math.round((float) objectiveRawScore / questionsTotalScore * paperTotalScore);
            // 主观题实际得分 = (答对分值 / 题目总分值) × 试卷总分
            subjectiveScore = Math.round((float) subjectiveRawScore / questionsTotalScore * paperTotalScore);
            totalScore = objectiveScore + subjectiveScore;
        }
        
        // 更新考试记录
        studentExam.setSubmitTime(LocalDateTime.now());
        studentExam.setStatus("submitted");
        studentExam.setObjectiveScore(objectiveScore);
        studentExam.setSubjectiveScore(subjectiveScore);
        studentExam.setTotalScore(totalScore);
        studentExam.setDuration(duration);
        studentExam.setPassScore(passScore);
        studentExamMapper.update(studentExam);
    }
    
    /**
     * 计算试卷所有题目的分值总和
     */
    private int calculateQuestionsTotalScore(Long examPaperId) {
        List<ExamQuestion> examQuestions = examQuestionMapper.findByExamPaperId(examPaperId);
        int total = 0;
        for (ExamQuestion eq : examQuestions) {
            QuestionBank question = questionBankMapper.findById(eq.getQuestionId());
            if (question != null) {
                total += question.getScore();
            }
        }
        return total;
    }
    
    /**
     * 自动批改客观题
     * @return 返回答对题目的原始分值总和（用于比例计算）
     */
    private int autoGradeObjectiveQuestions(Long studentExamId) {
        List<StudentAnswer> answers = studentAnswerMapper.findByStudentExamId(studentExamId);
        int totalScore = 0;
        
        for (StudentAnswer answer : answers) {
            QuestionBank question = questionBankMapper.findById(answer.getQuestionId());
            if (question != null && isObjectiveQuestion(question.getQuestionType())) {
                boolean isCorrect = question.getAnswer().equals(answer.getStudentAnswer());
                answer.setIsCorrect(isCorrect);
                // 保存原始分值（用于记录）
                answer.setScore(isCorrect ? question.getScore() : 0);
                studentAnswerMapper.update(answer);
                
                if (isCorrect) {
                    totalScore += question.getScore();
                }
            }
        }
        
        return totalScore;
    }
    
    private boolean isObjectiveQuestion(String questionType) {
        return "single_choice".equals(questionType) || 
               "multiple_choice".equals(questionType) || 
               "true_false".equals(questionType);
    }
    
    /**
     * 计算主观题得分
     * @return 返回主观题原始分值总和（初始为0，需要教师批改后才有分）
     */
    private int calculateSubjectiveScore(Long studentExamId) {
        List<StudentAnswer> answers = studentAnswerMapper.findByStudentExamId(studentExamId);
        int totalScore = 0;
        
        for (StudentAnswer answer : answers) {
            QuestionBank question = questionBankMapper.findById(answer.getQuestionId());
            // 检查是否为主观题（填空题、简答题、编程题）
            if (question != null && isSubjectiveQuestion(question.getQuestionType())) {
                // 主观题得分需要教师批改，初始为0分
                answer.setScore(0);
                studentAnswerMapper.update(answer);
            }
        }
        
        return totalScore;
    }
    
    private boolean isSubjectiveQuestion(String questionType) {
        return "fill_blank".equals(questionType) || 
               "short_answer".equals(questionType) || 
               "programming".equals(questionType);
    }
    
    /**
     * 获取学生考试记录
     */
    public List<StudentExam> getStudentExams(Long studentId) {
        List<StudentExam> exams = studentExamMapper.findByStudentId(studentId);
        
        // 为每个考试记录补充试卷信息（考试名称、课程名称、考试时长和及格分）
        for (StudentExam exam : exams) {
            ExamPaper paper = examPaperMapper.findById(exam.getExamPaperId());
            if (paper != null) {
                exam.setTitle(paper.getTitle());
                exam.setCourseName(paper.getCourseName());
                exam.setDuration(paper.getDuration());
                exam.setPassScore(paper.getPassScore());
            }
        }
        
        return exams;
    }
    
    /**
     * 获取学生答题详情
     */
    public List<StudentAnswer> getStudentAnswers(Long studentExamId) {
        return studentAnswerMapper.findByStudentExamId(studentExamId);
    }
    
    /**
     * 获取学生答题详情（包含题目信息）
     */
    public List<cn.edu.zjut.back.dto.StudentAnswerDetail> getStudentAnswerDetails(Long studentExamId) {
        List<StudentAnswer> answers = studentAnswerMapper.findByStudentExamId(studentExamId);
        List<cn.edu.zjut.back.dto.StudentAnswerDetail> details = new ArrayList<>();
        
        for (StudentAnswer answer : answers) {
            cn.edu.zjut.back.dto.StudentAnswerDetail detail = new cn.edu.zjut.back.dto.StudentAnswerDetail();
            
            // 复制答案信息
            detail.setId(answer.getId());
            detail.setStudentExamId(answer.getStudentExamId());
            detail.setQuestionId(answer.getQuestionId());
            detail.setQuestionOrder(answer.getQuestionOrder());
            detail.setStudentAnswer(answer.getStudentAnswer());
            detail.setIsCorrect(answer.getIsCorrect());
            detail.setAnswerTime(answer.getAnswerTime());
            detail.setScoreObtained(answer.getScore()); // 从数据库中获取实际得分
            
            // 获取题目信息
            QuestionBank question = questionBankMapper.findById(answer.getQuestionId());
            if (question != null) {
                detail.setContent(question.getContent());
                detail.setQuestionType(question.getQuestionType());
                detail.setOptions(question.getOptions());
                detail.setCorrectAnswer(question.getAnswer());
                detail.setAnalysis(question.getExplanation());
                detail.setScore(question.getScore());
            }
            
            details.add(detail);
        }
        
        return details;
    }
    
    /**
     * 创建题目
     */
    public QuestionBank createQuestion(QuestionBank question) {
        if (question.getStatus() == null) question.setStatus("active");
        if (question.getIsVisible() == null) question.setIsVisible(1);
        if (question.getUsageCount() == null) question.setUsageCount(0);
        if (question.getScore() == null) question.setScore(5);
        if (question.getDifficulty() == null) question.setDifficulty("medium");
        questionBankMapper.insert(question);
        return question;
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
    public List<QuestionBank> getAllQuestions(String userRole) {
        // 如果是学生，只返回可见的题目
        if ("student".equals(userRole)) {
            return questionBankMapper.findAllVisibleForStudent();
        }
        // 教师和管理员返回所有题目
        return questionBankMapper.findAll();
    }
    
    /**
     * 获取题库列表（带分页和筛选）
     * @param params 查询参数
     * @param userRole 用户角色 (student, teacher, admin)
     */
    public Map<String, Object> getQuestionsWithPage(Map<String, Object> params, String userRole) {
        // 根据角色获取题目列表
        List<QuestionBank> allQuestions;
        if ("student".equals(userRole)) {
            allQuestions = questionBankMapper.findAllVisibleForStudent();
        } else {
            allQuestions = questionBankMapper.findAll();
        }
        
        // 应用筛选条件
        List<QuestionBank> filteredQuestions = new ArrayList<>();
        for (QuestionBank q : allQuestions) {
            boolean match = true;
            
            // 课程筛选
            if (params.containsKey("courseId") && params.get("courseId") != null) {
                Long courseId = Long.valueOf(params.get("courseId").toString());
                if (!courseId.equals(q.getCourseId())) {
                    match = false;
                }
            }
            
            // 题型筛选
            if (params.containsKey("type") && params.get("type") != null) {
                Object typeObj = params.get("type");
                String type = typeObj instanceof List ? ((List<?>) typeObj).get(0).toString() : typeObj.toString();
                if (!type.isEmpty() && !type.equals(q.getQuestionType())) {
                    match = false;
                }
            }
            
            // 难度筛选
            if (params.containsKey("difficulty") && params.get("difficulty") != null) {
                Object difficultyObj = params.get("difficulty");
                String difficulty = difficultyObj instanceof List ? ((List<?>) difficultyObj).get(0).toString() : difficultyObj.toString();
                if (!difficulty.isEmpty() && !difficulty.equals(q.getDifficulty())) {
                    match = false;
                }
            }
            
            // 关键词筛选
            if (params.containsKey("keyword") && params.get("keyword") != null) {
                Object keywordObj = params.get("keyword");
                String keyword = keywordObj instanceof List ? ((List<?>) keywordObj).get(0).toString() : keywordObj.toString();
                if (!keyword.isEmpty() && !q.getContent().contains(keyword)) {
                    match = false;
                }
            }
            
            if (match) {
                filteredQuestions.add(q);
            }
        }
        
        // 分页处理
        int page = Integer.parseInt(params.get("page").toString());
        int size = Integer.parseInt(params.get("size").toString());
        int total = filteredQuestions.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        
        List<QuestionBank> pagedQuestions = new ArrayList<>();
        if (start < total) {
            pagedQuestions = filteredQuestions.subList(start, end);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", pagedQuestions);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        
        return result;
    }
    
    /**
     * 获取题库列表（指定课程）
     * @param courseId 课程ID
     * @param userRole 用户角色 (student, teacher, admin)
     */
    public List<QuestionBank> getQuestionsByCourse(Long courseId, String userRole) {
        if ("student".equals(userRole)) {
            return questionBankMapper.findByCourseIdVisibleForStudent(courseId);
        }
        return questionBankMapper.findByCourseId(courseId);
    }
    
    /**
     * 获取试卷题目列表
     */
    public List<QuestionBank> getExamQuestions(Long examPaperId) {
        return questionBankMapper.findByExamPaperId(examPaperId);
    }
    
    /**
     * 根据ID获取学生考试记录
     */
    public StudentExam getStudentExamById(Long studentExamId) {
        StudentExam exam = studentExamMapper.findById(studentExamId);
        if (exam != null) {
            // 补充试卷信息（不覆盖学生实际得分）
            ExamPaper paper = examPaperMapper.findById(exam.getExamPaperId());
            if (paper != null) {
                exam.setTitle(paper.getTitle());
                exam.setCourseName(paper.getCourseName());
                exam.setDuration(paper.getDuration());
                exam.setPassScore(paper.getPassScore());
            }
        }
        return exam;
    }
    
    /**
     * 添加题目到试卷
     */
    @Transactional
    public void addQuestionsToExam(Long examPaperId, List<Long> questionIds) {
        int order = examQuestionMapper.countByExamPaperId(examPaperId) + 1;
        
        for (Long questionId : questionIds) {
            QuestionBank question = questionBankMapper.findById(questionId);
            if (question != null) {
                ExamQuestion examQuestion = new ExamQuestion();
                examQuestion.setExamPaperId(examPaperId);
                examQuestion.setQuestionId(questionId);
                examQuestion.setQuestionOrder(order++);
                examQuestion.setQuestionScore(question.getScore());
                examQuestion.setIsRequired(true);
                examQuestionMapper.insert(examQuestion);
            }
        }
    }
    
    /**
     * 从试卷中移除题目
     */
    @Transactional
    public void removeQuestionFromExam(Long examPaperId, Long questionId) {
        examQuestionMapper.deleteQuestion(examPaperId, questionId);
        // 重新排序
        reorderExamQuestions(examPaperId);
    }
    
    /**
     * 重新排序试卷题目
     */
    private void reorderExamQuestions(Long examPaperId) {
        List<ExamQuestion> questions = examQuestionMapper.findByExamPaperId(examPaperId);
        for (int i = 0; i < questions.size(); i++) {
            questions.get(i).setQuestionOrder(i + 1);
            examQuestionMapper.update(questions.get(i));
        }
    }
    
    /**
     * 发布试卷 - 生成JSON文件
     */
    @Transactional
    public String publishExamPaper(Long examPaperId) throws IOException {
        System.out.println("=== 开始发布试卷 ID: " + examPaperId + " ===");
        
        ExamPaper paper = examPaperMapper.findById(examPaperId);
        if (paper == null) {
            throw new RuntimeException("试卷不存在");
        }
        System.out.println("找到试卷: " + paper.getTitle());
        
        // 获取试卷关联的题目
        List<ExamQuestion> examQuestions = examQuestionMapper.findByExamPaperId(examPaperId);
        System.out.println("试卷包含题目数量: " + examQuestions.size());
        
        if (examQuestions.isEmpty()) {
            throw new RuntimeException("试卷没有题目，无法发布");
        }
        
        // 构建完整的题目信息
        List<Map<String, Object>> questionList = new ArrayList<>();
        int totalScore = 0;
        
        for (ExamQuestion eq : examQuestions) {
            QuestionBank question = questionBankMapper.findById(eq.getQuestionId());
            if (question != null) {
                Map<String, Object> questionMap = new HashMap<>();
                questionMap.put("id", question.getId());
                questionMap.put("questionType", question.getQuestionType());
                questionMap.put("content", question.getContent());
                questionMap.put("options", question.getOptions());
                questionMap.put("answer", question.getAnswer());
                questionMap.put("explanation", question.getExplanation());
                questionMap.put("score", eq.getQuestionScore());
                questionMap.put("difficulty", question.getDifficulty());
                questionMap.put("order", eq.getQuestionOrder());
                questionList.add(questionMap);
                totalScore += eq.getQuestionScore();
            }
        }
        
        System.out.println("题目总分: " + totalScore);
        
        // 构建试卷JSON
        Map<String, Object> examData = new HashMap<>();
        examData.put("paperId", paper.getId());
        examData.put("title", paper.getTitle());
        examData.put("description", paper.getDescription());
        examData.put("courseId", paper.getCourseId());
        examData.put("courseName", paper.getCourseName());
        examData.put("duration", paper.getDuration());
        examData.put("totalScore", totalScore);
        examData.put("passScore", paper.getPassScore());
        examData.put("shuffleQuestions", paper.getShuffleQuestions());
        examData.put("shuffleOptions", paper.getShuffleOptions());
        examData.put("showAnswer", paper.getShowAnswer());
        examData.put("questions", questionList);
        examData.put("publishTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // 生成文件名: exam_{paperId}_{timestamp}.json
        String timestamp = System.currentTimeMillis() + "";
        String filename = "exam_" + examPaperId + "_" + timestamp + ".json";
        
        // 根据环境选择路径和URL
        String resourcePath = getExamResourcePath();
        String fileUrl;
        if (resourcePath.equals(EXAM_RESOURCE_PATH_LOCAL)) {
            // 本地环境
            fileUrl = "http://localhost:8080/resources/exam/" + filename;
            System.out.println("本地开发环境，文件路径: " + resourcePath);
        } else {
            // 生产环境
            fileUrl = "http://120.26.212.210/resources/exam/" + filename;
            System.out.println("生产环境，文件路径: " + resourcePath);
        }
        
        // 写入JSON文件
        File directory = new File(resourcePath);
        if (!directory.exists()) {
            System.out.println("创建目录: " + directory.getAbsolutePath());
            boolean created = directory.mkdirs();
            System.out.println("目录创建" + (created ? "成功" : "失败"));
        }
        
        File jsonFile = new File(resourcePath + filename);
        System.out.println("JSON文件完整路径: " + jsonFile.getAbsolutePath());
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, examData);
        System.out.println("JSON文件写入成功");
        
        // 更新试卷状态和URL（保留教师设定的totalScore，不使用题目总分覆盖）
        paper.setStatus("published");
        paper.setPaperUrl(fileUrl);
        // 不修改 paper.setTotalScore()，保持教师创建试卷时设定的总分
        examPaperMapper.update(paper);
        
        System.out.println("试卷状态更新为已发布，URL: " + fileUrl);
        System.out.println("=== 发布试卷完成 ===");
        
        return fileUrl;
    }
    
    /**
     * 更新试卷
     */
    @Transactional
    public ExamPaper updateExamPaper(ExamPaper examPaper) {
        examPaperMapper.update(examPaper);
        return examPaper;
    }
    
    /**
     * 删除试卷
     */
    @Transactional
    public void deleteExamPaper(Long examPaperId) {
        // 删除试卷关联的题目
        examQuestionMapper.deleteByExamPaperId(examPaperId);
        // 删除试卷
        examPaperMapper.delete(examPaperId);
    }
    
    /**
     * 获取试卷提交记录
     */
    public List<StudentExam> getExamSubmissions(Long examPaperId) {
        return studentExamMapper.findByExamPaperId(examPaperId);
    }
    
    /**
     * 获取完整的考试结果详情（包含答题记录和题目信息）
     */
    public cn.edu.zjut.back.dto.ExamResultDetail getExamResultDetail(Long studentExamId) {
        // 1. 获取学生考试记录
        StudentExam studentExam = studentExamMapper.findById(studentExamId);
        if (studentExam == null) {
            throw new RuntimeException("考试记录不存在");
        }
        
        // 2. 获取试卷信息
        ExamPaper paper = examPaperMapper.findById(studentExam.getExamPaperId());
        if (paper == null) {
            throw new RuntimeException("试卷不存在");
        }
        
        // 3. 获取答题记录
        List<StudentAnswer> answers = studentAnswerMapper.findByStudentExamId(studentExamId);
        
        // 4. 构建结果DTO
        cn.edu.zjut.back.dto.ExamResultDetail result = new cn.edu.zjut.back.dto.ExamResultDetail();
        
        // 设置考试记录信息
        result.setStudentExamId(studentExam.getId());
        result.setExamPaperId(studentExam.getExamPaperId());
        result.setStudentId(studentExam.getStudentId());
        result.setStudentName(studentExam.getStudentName());
        result.setStudentNumber(studentExam.getStudentNumber());
        result.setAttemptNumber(studentExam.getAttemptNumber());
        result.setStartTime(studentExam.getStartTime());
        result.setSubmitTime(studentExam.getSubmitTime());
        result.setStatus(studentExam.getStatus());
        result.setIpAddress(studentExam.getIpAddress());
        
        // 设置试卷信息
        result.setExamTitle(paper.getTitle());
        result.setCourseName(paper.getCourseName());
        result.setDuration(paper.getDuration());
        result.setTotalScore(paper.getTotalScore());
        result.setPassScore(paper.getPassScore());
        
        // 设置成绩信息
        result.setStudentTotalScore(studentExam.getTotalScore());
        result.setObjectiveScore(studentExam.getObjectiveScore());
        result.setSubjectiveScore(studentExam.getSubjectiveScore());
        
        // 设置批改信息
        result.setGraderId(studentExam.getGraderId());
        result.setGraderName(studentExam.getGraderName());
        result.setGradeTime(studentExam.getGradeTime());
        result.setFeedback(studentExam.getFeedback());
        
        // 5. 构建答题详情列表
        List<cn.edu.zjut.back.dto.ExamResultDetail.AnswerDetail> answerDetails = new ArrayList<>();
        for (StudentAnswer answer : answers) {
            QuestionBank question = questionBankMapper.findById(answer.getQuestionId());
            if (question != null) {
                cn.edu.zjut.back.dto.ExamResultDetail.AnswerDetail detail = 
                    new cn.edu.zjut.back.dto.ExamResultDetail.AnswerDetail();
                
                // 答题记录信息
                detail.setAnswerId(answer.getId());
                detail.setQuestionOrder(answer.getQuestionOrder());
                detail.setStudentAnswer(answer.getStudentAnswer());
                detail.setIsCorrect(answer.getIsCorrect());
                detail.setScore(answer.getScore());
                detail.setAnswerTime(answer.getAnswerTime());
                
                // 题目信息
                detail.setQuestionId(question.getId());
                detail.setContent(question.getContent());
                detail.setQuestionType(question.getQuestionType());
                detail.setOptions(question.getOptions());
                detail.setCorrectAnswer(question.getAnswer());
                detail.setAnalysis(question.getExplanation());
                detail.setQuestionScore(question.getScore());
                
                answerDetails.add(detail);
            }
        }
        result.setAnswers(answerDetails);
        
        return result;
    }
    
    /**
     * 删除题目
     */
    @Transactional
    public void deleteQuestion(Long questionId) {
        // 删除题目关联的试卷关系
        examQuestionMapper.deleteByQuestionId(questionId);
        // 删除题目
        questionBankMapper.delete(questionId);
    }
}