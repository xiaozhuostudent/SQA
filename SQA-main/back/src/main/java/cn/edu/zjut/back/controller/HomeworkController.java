package cn.edu.zjut.back.controller;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.entity.*;
import cn.edu.zjut.back.service.HomeworkService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 作业控制器 - 支持题库选题
 */
@RestController
@RequestMapping("/api/homework")
public class HomeworkController {
    
    private final HomeworkService homeworkService;
    
    public HomeworkController(HomeworkService homeworkService) {
        this.homeworkService = homeworkService;
    }
    
    /**
     * 创建作业（包含题目） - 教师
     * @param request {homework: Homework对象, questions: HomeworkQuestion数组, teacherId: 教师ID}
     */
    @PostMapping("/create")
    public Result<Homework> createHomework(@RequestBody Map<String, Object> request) {
        Homework homework = parseHomework(request.get("homework"));
        List<HomeworkQuestion> questions = parseHomeworkQuestions(request.get("questions"));
        Long teacherId = Long.valueOf(request.get("teacherId").toString());
        
        return homeworkService.createHomework(homework, questions, teacherId);
    }
    
    /**
     * 获取作业详情（包含题目）
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getHomeworkDetail(@PathVariable Long id) {
        return homeworkService.getHomeworkDetail(id);
    }
    
    /**
     * 获取课程作业列表
     */
    @GetMapping("/course/{courseId}")
    public Result<List<Homework>> getCourseHomeworks(@PathVariable Long courseId) {
        return homeworkService.getCourseHomeworks(courseId);
    }
    
    /**
     * 获取教师创建的作业列表
     */
    @GetMapping("/teacher/{teacherId}")
    public Result<List<Homework>> getTeacherHomeworks(@PathVariable Long teacherId) {
        return homeworkService.getTeacherHomeworks(teacherId);
    }
    
    /**
     * 获取学生的作业列表（所有选课的作业）
     */
    @GetMapping("/student/{studentId}")
    public Result<List<Homework>> getStudentHomeworks(@PathVariable Long studentId) {
        return homeworkService.getStudentHomeworks(studentId);
    }
    
    /**
     * 获取学生的作业提交记录
     */
    @GetMapping("/student/{studentId}/submissions")
    public Result<List<Map<String, Object>>> getStudentSubmissions(@PathVariable Long studentId) {
        return homeworkService.getStudentSubmissions(studentId);
    }
    
    /**
     * 更新作业
     * @param request {homework: Homework对象, questions: HomeworkQuestion数组, teacherId: 教师ID}
     */
    @PutMapping("/update")
    public Result<String> updateHomework(@RequestBody Map<String, Object> request) {
        Homework homework = parseHomework(request.get("homework"));
        List<HomeworkQuestion> questions = parseHomeworkQuestions(request.get("questions"));
        Long teacherId = Long.valueOf(request.get("teacherId").toString());
        
        return homeworkService.updateHomework(homework, questions, teacherId);
    }
    
    /**
     * 发布作业
     */
    @PostMapping("/{id}/publish")
    public Result<String> publishHomework(@PathVariable Long id, @RequestParam Long teacherId) {
        return homeworkService.publishHomework(id, teacherId);
    }
    
    /**
     * 删除作业
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteHomework(@PathVariable Long id, @RequestParam Long teacherId) {
        return homeworkService.deleteHomework(id, teacherId);
    }
    
    /**
     * 学生开始作业
     */
    @PostMapping("/{id}/start")
    public Result<StudentHomework> startHomework(@PathVariable Long id, @RequestParam Long studentId) {
        return homeworkService.startHomework(id, studentId);
    }
    
    /**
     * 提交作业答案
     * @param request {studentHomeworkId: ID, answers: StudentHomeworkAnswer数组}
     */
    @PostMapping("/submit")
    public Result<String> submitHomework(@RequestBody Map<String, Object> request) {
        Long studentHomeworkId = Long.valueOf(request.get("studentHomeworkId").toString());
        List<StudentHomeworkAnswer> answers = parseStudentAnswers(request.get("answers"));
        
        return homeworkService.submitHomework(studentHomeworkId, answers);
    }
    
    /**
     * 教师批改作业
     * @param request {studentHomeworkId: ID, grades: [{answerId, score, comment}], teacherComment: 总评}
     */
    @PostMapping("/grade")
    public Result<String> gradeHomework(@RequestBody Map<String, Object> request) {
        Long studentHomeworkId = Long.valueOf(request.get("studentHomeworkId").toString());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> grades = (List<Map<String, Object>>) request.get("grades");
        String teacherComment = (String) request.get("teacherComment");
        
        return homeworkService.gradeHomework(studentHomeworkId, grades, teacherComment);
    }
    
    /**
     * 获取作业提交列表（教师）
     */
    @GetMapping("/{id}/submissions")
    public Result<List<Map<String, Object>>> getHomeworkSubmissions(@PathVariable Long id) {
        return homeworkService.getHomeworkSubmissions(id);
    }
    
    /**
     * 获取作业统计
     */
    @GetMapping("/{id}/stats")
    public Result<Map<String, Object>> getHomeworkStats(@PathVariable Long id) {
        return homeworkService.getHomeworkStats(id);
    }
    
    /**
     * AI自动评分主观题
     * @param request {answerId: 答案ID, questionContent: 题目内容, standardAnswer: 标准答案, studentAnswer: 学生答案, totalScore: 总分, questionType: 题型}
     */
    @PostMapping("/ai-grade")
    public Result<Map<String, Object>> aiGradeSubjective(@RequestBody Map<String, Object> request) {
        try {
            Long answerId = Long.valueOf(request.get("answerId").toString());
            String questionContent = (String) request.get("questionContent");
            String standardAnswer = (String) request.get("standardAnswer");
            String studentAnswer = (String) request.get("studentAnswer");
            Integer totalScore = Integer.valueOf(request.get("totalScore").toString());
            String questionType = (String) request.getOrDefault("questionType", "short_answer");
            
            return homeworkService.aiGradeSubjective(
                answerId, questionContent, standardAnswer, studentAnswer, totalScore, questionType
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("AI评分失败: " + e.getMessage());
        }
    }
    
    // ========== 辅助方法 ==========
    
    @SuppressWarnings("unchecked")
    private Homework parseHomework(Object obj) {
        Map<String, Object> map = (Map<String, Object>) obj;
        Homework homework = new Homework();
        
        if (map.get("id") != null) homework.setId(Long.valueOf(map.get("id").toString()));
        if (map.get("courseId") != null) homework.setCourseId(Long.valueOf(map.get("courseId").toString()));
        if (map.get("courseName") != null) homework.setCourseName((String) map.get("courseName"));
        if (map.get("title") != null) homework.setTitle((String) map.get("title"));
        if (map.get("description") != null) homework.setDescription((String) map.get("description"));
        if (map.get("deadline") != null) homework.setDeadline(java.time.LocalDateTime.parse(map.get("deadline").toString()));
        if (map.get("allowLateSubmission") != null) homework.setAllowLateSubmission((Boolean) map.get("allowLateSubmission"));
        if (map.get("latePenalty") != null) homework.setLatePenalty(Integer.valueOf(map.get("latePenalty").toString()));
        if (map.get("showAnswer") != null) homework.setShowAnswer((Boolean) map.get("showAnswer"));
        
        return homework;
    }
    
    @SuppressWarnings("unchecked")
    private List<HomeworkQuestion> parseHomeworkQuestions(Object obj) {
        List<Map<String, Object>> list = (List<Map<String, Object>>) obj;
        return list.stream().map(map -> {
            HomeworkQuestion hq = new HomeworkQuestion();
            if (map.get("questionId") != null) hq.setQuestionId(Long.valueOf(map.get("questionId").toString()));
            if (map.get("questionScore") != null) hq.setQuestionScore(Integer.valueOf(map.get("questionScore").toString()));
            return hq;
        }).toList();
    }
    
    @SuppressWarnings("unchecked")
    private List<StudentHomeworkAnswer> parseStudentAnswers(Object obj) {
        List<Map<String, Object>> list = (List<Map<String, Object>>) obj;
        return list.stream().map(map -> {
            StudentHomeworkAnswer answer = new StudentHomeworkAnswer();
            if (map.get("questionId") != null) answer.setQuestionId(Long.valueOf(map.get("questionId").toString()));
            if (map.get("studentAnswer") != null) answer.setStudentAnswer((String) map.get("studentAnswer"));
            return answer;
        }).toList();
    }
}
