package cn.edu.zjut.back.controller;

import cn.edu.zjut.back.entity.QuestionBank;
import cn.edu.zjut.back.entity.Course;
import cn.edu.zjut.back.service.QuestionBankService;
import cn.edu.zjut.back.service.CourseService;
import cn.edu.zjut.back.utils.JwtUtil;
import cn.edu.zjut.back.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 题库管理控制器
 */
@RestController
@RequestMapping("/api/questions")
public class QuestionBankController {
    private final QuestionBankService questionBankService;
    private final CourseService courseService;
    private final JwtUtil jwtUtil;

    public QuestionBankController(QuestionBankService questionBankService, CourseService courseService, JwtUtil jwtUtil) {
        this.questionBankService = questionBankService;
        this.courseService = courseService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 创建题目（教师专用）
     */
    @PostMapping("/create")
    public Result<QuestionBank> createQuestion(@RequestBody QuestionBank question, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Map<String, Object> claims = jwtUtil.parseToken(token);
            String userRole = (String) claims.get("role");
            Long userId = Long.valueOf(claims.get("userId").toString());
            String username = (String) claims.get("username");

            if (!"teacher".equals(userRole) && !"admin".equals(userRole)) {
                return Result.error("只有教师和管理员可以创建题目");
            }

            // 设置创建者信息
            question.setCreatorId(userId);
            question.setCreatorName(username);
            // options 非 JSON 时转为 JSON 数组
            if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                try { new com.fasterxml.jackson.databind.ObjectMapper().readTree(question.getOptions()); }
                catch (Exception e2) {
                    java.util.List<String> list = new java.util.ArrayList<>();
                    for (String l : question.getOptions().split("\n")) { if (!l.trim().isEmpty()) list.add(l.trim()); }
                    question.setOptions(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(list));
                }
            }
            // difficulty 数字转字符串
            if (question.getDifficulty() != null) {
                try { int d = Integer.parseInt(question.getDifficulty());
                    question.setDifficulty(d <= 1 ? "easy" : d <= 2 ? "medium" : "hard");
                } catch (NumberFormatException ignored) {}
            }
            QuestionBank created = questionBankService.createQuestion(question);
            return Result.success(created);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("创建题目失败: " + e.getMessage());
        }
    }

    /**
     * 获取题库列表
     */
    @GetMapping("/list")
    public Result<List<QuestionBank>> getQuestionList(
            @RequestParam(required = false) Long courseId,
            HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Map<String, Object> claims = jwtUtil.parseToken(token);
            String userRole = (String) claims.get("role");

            List<QuestionBank> questions;
            if (courseId != null) {
                questions = questionBankService.getQuestionsByCourse(courseId, userRole);
            } else {
                questions = questionBankService.getAllQuestions(userRole);
            }
            return Result.success(questions);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取题库失败: " + e.getMessage());
        }
    }

    /**
     * 根据题型获取题目
     */
    @GetMapping("/by-type/{questionType}")
    public Result<List<QuestionBank>> getQuestionsByType(
            @PathVariable String questionType,
            HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Map<String, Object> claims = jwtUtil.parseToken(token);
            String userRole = (String) claims.get("role");

            List<QuestionBank> questions = questionBankService.getQuestionsByType(questionType, userRole);
            return Result.success(questions);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取题目失败: " + e.getMessage());
        }
    }

    /**
     * 获取题目详情
     */
    @GetMapping("/{id}")
    public Result<QuestionBank> getQuestionById(@PathVariable Long id) {
        try {
            QuestionBank question = questionBankService.getQuestionById(id);
            if (question == null) {
                return Result.error("题目不存在");
            }
            return Result.success(question);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取题目详情失败: " + e.getMessage());
        }
    }

    /**
     * 更新题目
     */
    @PutMapping("/{id}")
    public Result<String> updateQuestion(
            @PathVariable Long id,
            @RequestBody QuestionBank question,
            HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Map<String, Object> claims = jwtUtil.parseToken(token);
            String userRole = (String) claims.get("role");
            Long userId = Long.valueOf(claims.get("userId").toString());

            if (!"teacher".equals(userRole) && !"admin".equals(userRole)) {
                return Result.error("只有教师和管理员可以修改题目");
            }

            QuestionBank existing = questionBankService.getQuestionById(id);
            if (existing == null) {
                return Result.error("题目不存在");
            }

            // 教师只能修改自己创建的题目
            if ("teacher".equals(userRole) && !userId.equals(existing.getCreatorId())) {
                return Result.error("只能修改自己创建的题目");
            }

            question.setId(id);
            // options 非 JSON 时转为 JSON 数组
            if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                try { new com.fasterxml.jackson.databind.ObjectMapper().readTree(question.getOptions()); }
                catch (Exception e2) {
                    java.util.List<String> list = new java.util.ArrayList<>();
                    for (String l : question.getOptions().split("\n")) { if (!l.trim().isEmpty()) list.add(l.trim()); }
                    question.setOptions(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(list));
                }
            }
            // difficulty 数字转字符串
            if (question.getDifficulty() != null) {
                try { int d = Integer.parseInt(question.getDifficulty());
                    question.setDifficulty(d <= 1 ? "easy" : d <= 2 ? "medium" : "hard");
                } catch (NumberFormatException ignored) {}
            }
            questionBankService.updateQuestion(question);
            return Result.success("更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新题目失败: " + e.getMessage());
        }
    }

    /**
     * 删除题目
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteQuestion(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Map<String, Object> claims = jwtUtil.parseToken(token);
            String userRole = (String) claims.get("role");
            Long userId = Long.valueOf(claims.get("userId").toString());

            if (!"teacher".equals(userRole) && !"admin".equals(userRole)) {
                return Result.error("只有教师和管理员可以删除题目");
            }

            QuestionBank existing = questionBankService.getQuestionById(id);
            if (existing == null) {
                return Result.error("题目不存在");
            }

            // 教师只能删除自己创建的题目
            if ("teacher".equals(userRole) && !userId.equals(existing.getCreatorId())) {
                return Result.error("只能删除自己创建的题目");
            }

            questionBankService.deleteQuestion(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除题目失败: " + e.getMessage());
        }
    }

    /**
     * 批量导入题目（教师和管理员专用）
     */
    @PostMapping("/batch-import")
    public Result<Map<String, Object>> batchImport(@RequestBody List<QuestionBank> questions, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Map<String, Object> claims = jwtUtil.parseToken(token);
            String userRole = (String) claims.get("role");
            Long userId = Long.valueOf(claims.get("userId").toString());
            String username = (String) claims.get("username");

            if (!"teacher".equals(userRole) && !"admin".equals(userRole)) {
                return Result.error("只有教师和管理员可以批量导入题目");
            }

            // 为每个题目设置创建者信息
            for (QuestionBank question : questions) {
                question.setCreatorId(userId);
                question.setCreatorName(username);
            }

            Map<String, Object> result = questionBankService.batchImport(questions);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("批量导入失败: " + e.getMessage());
        }
    }

    /**
     * 获取教师的课程列表（用于题库管理的课程下拉框）
     */
    @GetMapping("/teacher-courses")
    public Result<List<Course>> getTeacherCourses(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Map<String, Object> claims = jwtUtil.parseToken(token);
            String userRole = (String) claims.get("role");
            Long userId = Long.valueOf(claims.get("userId").toString());

            if (!"teacher".equals(userRole) && !"admin".equals(userRole)) {
                return Result.error("只有教师和管理员可以访问");
            }

            // 教师获取自己的课程，管理员获取所有课程
            List<Course> courses;
            if ("admin".equals(userRole)) {
                courses = courseService.getAllCoursesForAdmin().getData();
            } else {
                courses = courseService.getTeacherCourses(userId).getData();
            }
            
            return Result.success(courses);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取课程列表失败: " + e.getMessage());
        }
    }
}
