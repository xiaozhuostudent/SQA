package cn.edu.zjut.back.controller;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.dto.ExperimentDTO;
import cn.edu.zjut.back.entity.Experiment;
import cn.edu.zjut.back.service.ExperimentService;
import cn.edu.zjut.back.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/experiment")
public class ExperimentController {

    private final ExperimentService experimentService;
    private final JwtUtil jwtUtil;

    public ExperimentController(ExperimentService experimentService, JwtUtil jwtUtil) {
        this.experimentService = experimentService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/create")
    public Result createExperiment(@RequestBody ExperimentDTO dto, HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromRequest(request);
        if (userId == null) {
            userId = 1L; // Default admin for testing
        }
        return experimentService.createExperiment(dto, userId);
    }

    @GetMapping("/{id}")
    public Result getExperiment(@PathVariable Long id) {
        return experimentService.getExperiment(id);
    }

    @GetMapping("/{id}/problems")
    public Result getExperimentProblems(@PathVariable Long id, HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromRequest(request);
        return experimentService.getExperimentProblems(id, userId);
    }

    @GetMapping("/problem/{id}")
    public Result getProblemDetails(@PathVariable Long id) {
        return experimentService.getProblemDetails(id);
    }

    @GetMapping("/list")
    public Result getExperimentList(@RequestParam(required = false) Long courseId, HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromRequest(request);
        List<Experiment> experiments;
        if (courseId != null) {
            experiments = experimentService.getExperimentsByCourseId(courseId);
        } else {
            experiments = experimentService.getAllExperiments();
        }
        
        // 为每个实验添加统计信息
        List<Map<String, Object>> result = new ArrayList<>();
        for (Experiment exp : experiments) {
            Map<String, Object> expMap = new HashMap<>();
            expMap.put("id", exp.getId());
            expMap.put("courseId", exp.getCourseId());
            expMap.put("courseName", exp.getCourseName());
            expMap.put("title", exp.getTitle());
            expMap.put("description", exp.getDescription());
            expMap.put("requirements", exp.getRequirements());
            expMap.put("steps", exp.getSteps());
            expMap.put("startTime", exp.getStartTime());
            expMap.put("deadline", exp.getDeadline());
            expMap.put("environmentType", exp.getEnvironmentType());
            expMap.put("environmentConfig", exp.getEnvironmentConfig());
            expMap.put("resources", exp.getResources());
            
            // 添加统计信息
            Map<String, Object> stats = experimentService.getSubmissionStats(exp.getId());
            expMap.put("submittedCount", stats.get("submittedCount"));
            expMap.put("totalStudents", stats.get("totalStudents"));
            
            // 检查当前学生是否已提交（仅当有userId时）
            if (userId != null) {
                boolean hasSubmitted = experimentService.hasStudentSubmitted(exp.getId(), userId);
                expMap.put("hasSubmitted", hasSubmitted);
            }
            
            result.add(expMap);
        }
        
        return Result.success(result);
    }
    
    /**
     * 删除实验
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteExperiment(@PathVariable Long id) {
        try {
            experimentService.deleteExperiment(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除实验失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新实验
     */
    @PutMapping("/update/{id}")
    public Result updateExperiment(@PathVariable Long id, @RequestBody ExperimentDTO dto, HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            if (userId == null) {
                userId = 1L; // Default admin for testing
            }
            System.out.println("=== 更新实验请求 ===");
            System.out.println("实验ID: " + id);
            System.out.println("DTO: " + dto);
            System.out.println("courseId: " + dto.getCourseId());
            System.out.println("title: " + dto.getTitle());
            System.out.println("startTime: " + dto.getStartTime());
            System.out.println("deadline: " + dto.getDeadline());
            System.out.println("environmentConfig: " + dto.getEnvironmentConfig());
            System.out.println("resources: " + dto.getResources());
            System.out.println("problems数量: " + (dto.getProblems() != null ? dto.getProblems().size() : 0));
            
            return experimentService.updateExperiment(id, dto, userId);
        } catch (Exception e) {
            System.err.println("更新实验异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取实验的提交报告列表
     */
    @GetMapping("/{id}/submissions")
    public Result getExperimentSubmissions(@PathVariable Long id) {
        return experimentService.getExperimentSubmissions(id);
    }
    
    /**
     * 学生提交实验报告
     */
    @PostMapping("/submit-report")
    public Result submitReport(@RequestBody Map<String, Object> reportData, HttpServletRequest request) {
        try {
            Long userId = jwtUtil.getUserIdFromRequest(request);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            Long experimentId = Long.valueOf(reportData.get("experimentId").toString());
            String content = (String) reportData.get("content");
            
            // 处理文件列表
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> fileList = (List<Map<String, Object>>) reportData.get("files");
            
            return experimentService.submitReport(experimentId, userId, content, fileList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("提交失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取实验的学生完成情况统计
     */
    @GetMapping("/{id}/student-stats")
    public Result getStudentStats(@PathVariable Long id) {
        return experimentService.getStudentStats(id);
    }
}
