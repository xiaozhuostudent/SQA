package cn.edu.zjut.back.controller;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.entity.*;
import cn.edu.zjut.back.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 考试Controller
 */
@RestController
@RequestMapping("/api/exam")
@CrossOrigin
public class ExamController {
    
    @Autowired
    private ExamService examService;
    
    /**
     * 创建试卷
     */
    @PostMapping("/paper/create")
    public Result<ExamPaper> createExamPaper(@RequestBody ExamPaper examPaper) {
        try {
            ExamPaper created = examService.createExamPaper(examPaper);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error("创建试卷失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取课程试卷列表
     */
    @GetMapping("/paper/list/{courseId}")
    public Result<List<ExamPaper>> getExamPapers(@PathVariable Long courseId) {
        try {
            List<ExamPaper> papers = examService.getExamPapersByCourse(courseId);
            return Result.success(papers);
        } catch (Exception e) {
            return Result.error("获取试卷列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据创建者获取试卷列表
     */
    @GetMapping("/paper/listByCreator/{creatorId}")
    public Result<List<ExamPaper>> getExamPapersByCreator(@PathVariable Long creatorId) {
        try {
            List<ExamPaper> papers = examService.getExamPapersByCreator(creatorId);
            return Result.success(papers);
        } catch (Exception e) {
            return Result.error("获取试卷列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 学生开始考试
     */
    @PostMapping("/start")
    public Result<StudentExam> startExam(@RequestBody Map<String, Object> params) {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            Long examPaperId = Long.valueOf(params.get("examPaperId").toString());
            Long studentId = Long.valueOf(params.get("studentId").toString());
            String studentName = params.get("studentName").toString();
            String studentNumber = params.get("studentNumber").toString();
            String ipAddress = request.getRemoteAddr();
            
            StudentExam exam = examService.startExam(examPaperId, studentId, 
                studentName, studentNumber, ipAddress);
            return Result.success(exam);
        } catch (Exception e) {
            return Result.error("开始考试失败: " + e.getMessage());
        }
    }
    
    /**
     * 提交考试
     */
    @PostMapping("/submit")
    public Result<String> submitExam(@RequestBody Map<String, Object> params) {
        try {
            Long studentExamId = Long.valueOf(params.get("studentExamId").toString());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> answerList = (List<Map<String, Object>>) params.get("answers");
            
            List<StudentAnswer> answers = new java.util.ArrayList<>();
            for (Map<String, Object> item : answerList) {
                StudentAnswer answer = new StudentAnswer();
                answer.setQuestionId(Long.valueOf(item.get("questionId").toString()));
                answer.setQuestionOrder(Integer.valueOf(item.get("questionOrder").toString()));
                Object studentAnswerObj = item.get("studentAnswer");
                String studentAnswer = (studentAnswerObj != null) ? studentAnswerObj.toString() : "";
                answer.setStudentAnswer(studentAnswer);
                answers.add(answer);
            }
            
            examService.submitExam(studentExamId, answers);
            return Result.success("提交成功");
        } catch (Exception e) {
            return Result.error("提交考试失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取学生考试记录
     */
    @GetMapping("/student/list/{studentId}")
    public Result<List<StudentExam>> getStudentExams(@PathVariable Long studentId) {
        try {
            List<StudentExam> exams = examService.getStudentExams(studentId);
            return Result.success(exams);
        } catch (Exception e) {
            return Result.error("获取考试记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取试卷题目
     */
    @GetMapping("/questions/{examPaperId}")
    public Result<List<QuestionBank>> getExamQuestions(@PathVariable Long examPaperId) {
        try {
            List<QuestionBank> questions = examService.getExamQuestions(examPaperId);
            return Result.success(questions);
        } catch (Exception e) {
            return Result.error("获取试卷题目失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取考试结果
     */
    @GetMapping("/result/{studentExamId}")
    public Result<StudentExam> getExamResult(@PathVariable Long studentExamId) {
        try {
            StudentExam result = examService.getStudentExamById(studentExamId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取考试结果失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取答题详情
     */
    @GetMapping("/answers/{studentExamId}")
    public Result<List<cn.edu.zjut.back.dto.StudentAnswerDetail>> getAnswers(@PathVariable Long studentExamId) {
        try {
            List<cn.edu.zjut.back.dto.StudentAnswerDetail> answers = examService.getStudentAnswerDetails(studentExamId);
            return Result.success(answers);
        } catch (Exception e) {
            return Result.error("获取答题详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取完整的考试结果详情（新接口）
     */
    @GetMapping("/result/detail/{studentExamId}")
    public Result<cn.edu.zjut.back.dto.ExamResultDetail> getExamResultDetail(@PathVariable Long studentExamId) {
        try {
            cn.edu.zjut.back.dto.ExamResultDetail detail = examService.getExamResultDetail(studentExamId);
            return Result.success(detail);
        } catch (Exception e) {
            return Result.error("获取考试结果详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建题目
     */
    @PostMapping("/question/create")
    public Result<QuestionBank> createQuestion(@RequestBody QuestionBank question) {
        try {
            QuestionBank created = examService.createQuestion(question);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error("创建题目失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取题库列表（所有课程，支持分页和筛选）
     */
    @GetMapping("/question/list")
    public Result<Map<String, Object>> getAllQuestions(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String keyword,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "student") String userRole) {
        try {
            // 如果没有分页参数，返回所有数据
            if (page == null || size == null) {
                List<QuestionBank> questions = examService.getAllQuestions(userRole);
                Map<String, Object> result = new java.util.HashMap<>();
                result.put("list", questions);
                result.put("total", questions.size());
                return Result.success(result);
            }
            
            // 带分页和筛选
            Map<String, Object> params = new java.util.HashMap<>();
            params.put("page", page);
            params.put("size", size);
            if (type != null && !type.isEmpty()) params.put("type", type);
            if (courseId != null) params.put("courseId", courseId);
            if (difficulty != null && !difficulty.isEmpty()) params.put("difficulty", difficulty);
            if (keyword != null && !keyword.isEmpty()) params.put("keyword", keyword);
            
            Map<String, Object> result = examService.getQuestionsWithPage(params, userRole);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取题库失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取题库列表（指定课程）
     */
    @GetMapping("/question/list/{courseId}")
    public Result<List<QuestionBank>> getQuestions(
            @PathVariable Long courseId,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "student") String userRole) {
        try {
            List<QuestionBank> questions = examService.getQuestionsByCourse(courseId, userRole);
            return Result.success(questions);
        } catch (Exception e) {
            return Result.error("获取题库失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加题目到试卷
     */
    @PostMapping("/paper/{examPaperId}/questions")
    public Result<String> addQuestionsToExam(@PathVariable Long examPaperId, 
                                             @RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<?> rawQuestionIds = (List<?>) params.get("questionIds");
            // Convert to Long to handle both Integer and Long from JSON
            List<Long> questionIds = new java.util.ArrayList<>();
            for (Object id : rawQuestionIds) {
                if (id instanceof Number) {
                    questionIds.add(((Number) id).longValue());
                }
            }
            examService.addQuestionsToExam(examPaperId, questionIds);
            return Result.success("添加成功");
        } catch (Exception e) {
            return Result.error("添加题目失败: " + e.getMessage());
        }
    }
    
    /**
     * 从试卷移除题目
     */
    @DeleteMapping("/paper/{examPaperId}/question/{questionId}")
    public Result<String> removeQuestionFromExam(@PathVariable Long examPaperId, 
                                                 @PathVariable Long questionId) {
        try {
            examService.removeQuestionFromExam(examPaperId, questionId);
            return Result.success("移除成功");
        } catch (Exception e) {
            return Result.error("移除题目失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除题目（备用路径）
     */
    @DeleteMapping("/question/delete/{questionId}")
    public Result<String> deleteQuestion(@PathVariable Long questionId) {
        try {
            examService.deleteQuestion(questionId);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除题目失败: " + e.getMessage());
        }
    }
    
    /**
     * 发布试卷
     */
    @PostMapping("/paper/publish/{id}")
    public Result<String> publishExamPaper(@PathVariable Long id) {
        try {
            String fileUrl = examService.publishExamPaper(id);
            return Result.success(fileUrl);
        } catch (Exception e) {
            return Result.error("发布试卷失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新试卷
     */
    @PutMapping("/paper/update/{id}")
    public Result<ExamPaper> updateExamPaper(@PathVariable Long id, 
                                             @RequestBody ExamPaper examPaper) {
        try {
            examPaper.setId(id);
            System.out.println("更新试卷请求 - ID: " + id);
            System.out.println("试卷数据: " + examPaper);
            ExamPaper updated = examService.updateExamPaper(examPaper);
            return Result.success(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新试卷失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除试卷
     */
    @DeleteMapping("/paper/delete/{id}")
    public Result<String> deleteExamPaper(@PathVariable Long id) {
        try {
            examService.deleteExamPaper(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除试卷失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取试卷提交记录
     */
    @GetMapping("/submissions/{examPaperId}")
    public Result<List<StudentExam>> getSubmissions(@PathVariable Long examPaperId) {
        try {
            List<StudentExam> submissions = examService.getExamSubmissions(examPaperId);
            return Result.success(submissions);
        } catch (Exception e) {
            return Result.error("获取提交记录失败: " + e.getMessage());
        }
    }
}
