package cn.edu.zjut.back.controller;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.entity.LearningProgress;
import cn.edu.zjut.back.service.LearningProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 学习进度Controller
 */
@RestController
@RequestMapping("/api/progress")
@CrossOrigin
public class LearningProgressController {
    
    @Autowired
    private LearningProgressService progressService;
    
    /**
     * 更新学习进度
     */
    @PostMapping("/update")
    public Result<String> updateProgress(@RequestBody Map<String, Object> params) {
        try {
            Long studentId = Long.valueOf(params.get("studentId").toString());
            Long courseId = Long.valueOf(params.get("courseId").toString());
            Long resourceId = Long.valueOf(params.get("resourceId").toString());
            String resourceType = params.get("resourceType").toString();
            Integer progressPercent = Integer.valueOf(params.get("progressPercent").toString());
            Integer durationSeconds = Integer.valueOf(params.get("durationSeconds").toString());
            String lastPosition = params.get("lastPosition") != null ? 
                params.get("lastPosition").toString() : null;
            
            progressService.updateProgress(studentId, courseId, resourceId, resourceType, 
                progressPercent, durationSeconds, lastPosition);
            return Result.success("进度更新成功");
        } catch (Exception e) {
            return Result.error("进度更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取学生课程进度
     */
    @GetMapping("/course/{studentId}/{courseId}")
    public Result<List<LearningProgress>> getCourseProgress(@PathVariable Long studentId, @PathVariable Long courseId) {
        try {
            List<LearningProgress> progress = progressService.getStudentCourseProgress(studentId, courseId);
            return Result.success(progress);
        } catch (Exception e) {
            return Result.error("获取进度失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取资源进度
     */
    @GetMapping("/resource/{studentId}/{resourceId}")
    public Result<LearningProgress> getResourceProgress(@PathVariable Long studentId, @PathVariable Long resourceId) {
        try {
            LearningProgress progress = progressService.getResourceProgress(studentId, resourceId);
            
            return Result.success(progress);
        } catch (Exception e) {
            return Result.error("获取资源进度失败: " + e.getMessage());
        }
    }
}
