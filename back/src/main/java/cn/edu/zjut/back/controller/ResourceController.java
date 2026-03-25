package cn.edu.zjut.back.controller;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.entity.Resource;
import cn.edu.zjut.back.service.ResourceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 教学资源控制器
 */
@RestController
@RequestMapping("/api/resource")
public class ResourceController {
    
    private final ResourceService resourceService;
    
    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }
    
    /**
     * 获取资源列表(分页)
     * @param page 页码，默认1
     * @param pageSize 每页数量，默认10
     * @param type 资源类型，可选：all, document, video, code, other
     * @param keyword 搜索关键词
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getResourceList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(required = false) String keyword) {
        return resourceService.getResourceList(page, pageSize, type, keyword);
    }
    
    /**
     * 获取所有资源（管理员用）
     */
    @GetMapping("/all")
    public Result<List<Resource>> getAllResources() {
        return resourceService.getAllResources();
    }
    
    /**
     * 获取资源详情
     */
    @GetMapping("/detail/{id}")
    public Result<Resource> getResourceDetail(@PathVariable Long id) {
        return resourceService.getResourceDetail(id);
    }
    
    /**
     * 获取课程的资源列表
     */
    @GetMapping("/course/{courseId}")
    public Result<List<Resource>> getCourseResources(@PathVariable Long courseId) {
        return resourceService.getCourseResources(courseId);
    }
    
    /**
     * 增加下载次数
     */
    @PostMapping("/download/{id}")
    public Result<String> incrementDownloadCount(@PathVariable Long id) {
        return resourceService.incrementDownloadCount(id);
    }
    
    /**
     * 获取教师上传的资源列表
     */
    @GetMapping("/teacher/{teacherId}")
    public Result<List<Resource>> getTeacherResources(@PathVariable Long teacherId) {
        return resourceService.getTeacherResources(teacherId);
    }
    
    /**
     * 上传资源
     */
    @PostMapping("/upload")
    public Result<Resource> uploadResource(@RequestBody Resource resource) {
        return resourceService.uploadResource(resource);
    }
    
    /**
     * 更新资源信息
     */
    @PutMapping("/update/{id}")
    public Result<String> updateResource(@PathVariable Long id, @RequestBody Resource resource) {
        resource.setId(id);
        return resourceService.updateResource(resource);
    }
    
    /**
     * 删除资源
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> deleteResource(@PathVariable Long id) {
        return resourceService.deleteResource(id);
    }
}
