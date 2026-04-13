package cn.edu.zjut.back.service;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.entity.Resource;

import java.util.List;
import java.util.Map;

/**
 * 教学资源服务接口
 */
public interface ResourceService {
    
    /**
     * 获取资源列表(分页)
     */
    Result<Map<String, Object>> getResourceList(Integer page, Integer pageSize, String type, String keyword);
    
    /**
     * 获取所有资源
     */
    Result<List<Resource>> getAllResources();
    
    /**
     * 获取资源详情
     */
    Result<Resource> getResourceDetail(Long id);
    
    /**
     * 获取课程的资源列表
     */
    Result<List<Resource>> getCourseResources(Long courseId);
    
    /**
     * 增加下载次数
     */
    Result<String> incrementDownloadCount(Long id);
    
    /**
     * 获取教师上传的资源列表
     */
    Result<List<Resource>> getTeacherResources(Long teacherId);
    
    /**
     * 上传资源
     */
    Result<Resource> uploadResource(Resource resource);
    
    /**
     * 更新资源信息
     */
    Result<String> updateResource(Resource resource);
    
    /**
     * 删除资源
     */
    Result<String> deleteResource(Long id);
    
    /**
     * 审核资源
     */
    Result<String> approveResource(Long id);
    
    /**
     * 拒绝资源
     */
    Result<String> rejectResource(Long id, String reason);
    
    /**
     * 批量审核
     */
    Result<String> batchApprove(List<Long> ids);
    
    /**
     * 批量删除
     */
    Result<String> batchDelete(List<Long> ids);
    
    /**
     * 获取存储统计
     */
    Result<Map<String, Object>> getStorageStats();
    
    /**
     * 获取资源统计
     */
    Result<Map<String, Object>> getResourceStats();
}
