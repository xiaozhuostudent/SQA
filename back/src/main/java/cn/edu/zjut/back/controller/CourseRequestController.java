package cn.edu.zjut.back.controller;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.entity.CourseRequest;
import cn.edu.zjut.back.service.CourseRequestService;
import cn.edu.zjut.back.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * 课程请求控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/course-request")
public class CourseRequestController {
    
    @Autowired
    private CourseRequestService courseRequestService;
    
    @Autowired
    private RedisUtil redisUtil;
    
    /**
     * 上传文件到Redis（临时存储）
     */
    @PostMapping("/upload-file")
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }
            
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            
            // 只允许上传pdf和word文档
            if (!fileExtension.equalsIgnoreCase(".pdf") && 
                !fileExtension.equalsIgnoreCase(".doc") && 
                !fileExtension.equalsIgnoreCase(".docx")) {
                return Result.error("只支持上传PDF和Word文档");
            }
            
            // 限制文件大小为10MB
            if (file.getSize() > 10 * 1024 * 1024) {
                return Result.error("文件大小不能超过10MB");
            }
            
            // 将文件转为Base64存入Redis
            byte[] fileBytes = file.getBytes();
            String base64Content = Base64.getEncoder().encodeToString(fileBytes);
            
            // 生成唯一的文件key
            String fileKey = "course:request:file:" + UUID.randomUUID().toString();
            
            // 存入Redis，保留7天
            redisUtil.set(fileKey, base64Content, 7 * 24 * 60 * 60);
            
            Map<String, String> result = Map.of(
                "fileKey", fileKey,
                "fileName", originalFilename
            );
            
            log.info("文件上传成功: {}, key: {}", originalFilename, fileKey);
            return Result.success("文件上传成功", result);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 下载/预览文件
     */
    @GetMapping("/download-file")
    public Result<Map<String, String>> downloadFile(@RequestParam String fileKey) {
        try {
            Object content = redisUtil.get(fileKey);
            if (content == null) {
                return Result.error("文件不存在或已过期");
            }
            
            Map<String, String> result = Map.of(
                "content", content.toString(),
                "fileKey", fileKey
            );
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取文件失败", e);
            return Result.error("获取文件失败");
        }
    }
    
    /**
     * 提交课程请求
     */
    @PostMapping("/submit")
    public Result<CourseRequest> submitRequest(@RequestBody CourseRequest request) {
        return courseRequestService.submitRequest(request);
    }
    
    /**
     * 获取教师的请求列表
     */
    @GetMapping("/teacher-requests")
    public Result<List<CourseRequest>> getTeacherRequests(@RequestParam Long teacherId) {
        return courseRequestService.getTeacherRequests(teacherId);
    }
    
    /**
     * 获取待审核的请求列表
     */
    @GetMapping("/pending")
    public Result<List<CourseRequest>> getPendingRequests() {
        return courseRequestService.getPendingRequests();
    }
    
    /**
     * 获取所有请求（分页）
     */
    @GetMapping("/all")
    public Result<Map<String, Object>> getAllRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String requestType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return courseRequestService.getAllRequests(status, requestType, page, pageSize);
    }
    
    /**
     * 审核请求
     */
    @PostMapping("/review")
    public Result<String> reviewRequest(@RequestBody Map<String, Object> params) {
        try {
            // 参数校验
            if (!params.containsKey("requestId")) {
                return Result.error("缺少请求ID");
            }
            if (!params.containsKey("action")) {
                return Result.error("缺少操作类型");
            }
            if (!params.containsKey("adminId")) {
                return Result.error("缺少管理员ID");
            }
            if (!params.containsKey("adminName")) {
                return Result.error("缺少管理员姓名");
            }
            
            Long requestId;
            try {
                requestId = Long.valueOf(params.get("requestId").toString());
            } catch (NumberFormatException e) {
                return Result.error("请求ID格式错误");
            }
            
            String action = params.get("action").toString();
            
            Long adminId;
            try {
                adminId = Long.valueOf(params.get("adminId").toString());
            } catch (NumberFormatException e) {
                return Result.error("管理员ID格式错误");
            }
            
            String adminName = params.get("adminName").toString();
            String comment = params.getOrDefault("comment", "").toString();
            
            return courseRequestService.reviewRequest(requestId, action, adminId, adminName, comment);
        } catch (Exception e) {
            log.error("审核请求失败", e);
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }
}
