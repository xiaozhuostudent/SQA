package cn.edu.zjut.back.service.impl;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.entity.Resource;
import cn.edu.zjut.back.entity.Course;
import cn.edu.zjut.back.mapper.ResourceMapper;
import cn.edu.zjut.back.mapper.CourseMapper;
import cn.edu.zjut.back.service.ResourceService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 教学资源服务实现类
 */
@Service
public class ResourceServiceImpl implements ResourceService {
    
    private final ResourceMapper resourceMapper;
    private final CourseMapper courseMapper;
    
    public ResourceServiceImpl(ResourceMapper resourceMapper, CourseMapper courseMapper) {
        this.resourceMapper = resourceMapper;
        this.courseMapper = courseMapper;
    }
    
    @Override
    public Result<Map<String, Object>> getResourceList(Integer page, Integer pageSize, String type, String keyword) {
        try {
            // 计算偏移量
            int offset = (page - 1) * pageSize;
            
            // 查询资源列表
            List<Resource> resources = resourceMapper.selectResourceList(type, keyword, offset, pageSize);
            
            // 查询总数
            long total = resourceMapper.countResourceList(type, keyword);
            
            // 构建返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("records", resources);
            result.put("total", total);
            result.put("size", pageSize);
            result.put("current", page);
            result.put("pages", (total + pageSize - 1) / pageSize);
            
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取资源列表失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<List<Resource>> getAllResources() {
        try {
            // 查询所有资源，不分页
            List<Resource> resources = resourceMapper.selectResourceList("all", null, 0, 10000);
            return Result.success(resources);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取所有资源失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<Resource> getResourceDetail(Long id) {
        try {
            Resource resource = resourceMapper.selectById(id);
            if (resource == null) {
                return Result.error("资源不存在");
            }
            return Result.success(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取资源详情失败");
        }
    }
    
    @Override
    public Result<List<Resource>> getCourseResources(Long courseId) {
        try {
            List<Resource> resources = resourceMapper.selectByCourseId(courseId);
            return Result.success(resources);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取课程资源失败");
        }
    }
    
    @Override
    public Result<String> incrementDownloadCount(Long id) {
        try {
            Resource resource = resourceMapper.selectById(id);
            if (resource == null) {
                return Result.error("资源不存在");
            }
            
            int rows = resourceMapper.incrementDownloadCount(id);
            if (rows > 0) {
                return Result.success("下载次数已更新");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新下载次数失败");
        }
    }
    
    @Override
    public Result<List<Resource>> getTeacherResources(Long teacherId) {
        try {
            // 1. 先查询该教师的所有课程
            List<Course> courses = courseMapper.findByTeacherId(teacherId);
            
            if (courses == null || courses.isEmpty()) {
                // 如果没有课程，返回空列表
                return Result.success(List.of());
            }
            
            // 2. 提取课程ID列表
            List<Long> courseIds = courses.stream()
                    .map(Course::getId)
                    .collect(Collectors.toList());
            
            // 3. 根据课程ID列表查询资源
            List<Resource> resources = resourceMapper.selectByCourseIds(courseIds);
            
            return Result.success(resources);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取教师资源失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<Resource> uploadResource(Resource resource) {
        try {
            // 设置默认状态为待审核
            if (resource.getStatus() == null) {
                resource.setStatus("approved"); // 直接通过,也可以设置为pending待审核
            }
            
            int rows = resourceMapper.insert(resource);
            if (rows > 0) {
                return Result.success(resource);
            } else {
                return Result.error("上传失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("上传资源失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<String> updateResource(Resource resource) {
        try {
            Resource existingResource = resourceMapper.selectById(resource.getId());
            if (existingResource == null) {
                return Result.error("资源不存在");
            }
            
            int rows = resourceMapper.update(resource);
            if (rows > 0) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新资源失败");
        }
    }
    
    @Override
    public Result<String> deleteResource(Long id) {
        try {
            Resource resource = resourceMapper.selectById(id);
            if (resource == null) {
                return Result.error("资源不存在");
            }
            
            int rows = resourceMapper.deleteById(id);
            if (rows > 0) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除资源失败");
        }
    }
    
    @Override
    public Result<String> approveResource(Long id) {
        try {
            Resource resource = resourceMapper.selectById(id);
            if (resource == null) {
                return Result.error("资源不存在");
            }
            
            resource.setStatus("approved");
            int rows = resourceMapper.update(resource);
            if (rows > 0) {
                return Result.success("审核通过");
            } else {
                return Result.error("审核失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("审核资源失败");
        }
    }
    
    @Override
    public Result<String> rejectResource(Long id, String reason) {
        try {
            Resource resource = resourceMapper.selectById(id);
            if (resource == null) {
                return Result.error("资源不存在");
            }
            
            resource.setStatus("rejected");
            int rows = resourceMapper.update(resource);
            if (rows > 0) {
                return Result.success("已拒绝");
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("拒绝资源失败");
        }
    }
    
    @Override
    public Result<String> batchApprove(List<Long> ids) {
        try {
            int count = 0;
            for (Long id : ids) {
                Resource resource = resourceMapper.selectById(id);
                if (resource != null) {
                    resource.setStatus("approved");
                    int rows = resourceMapper.update(resource);
                    if (rows > 0) {
                        count++;
                    }
                }
            }
            return Result.success("已审核 " + count + " 个资源");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("批量审核失败");
        }
    }
    
    @Override
    public Result<String> batchDelete(List<Long> ids) {
        try {
            int count = 0;
            for (Long id : ids) {
                int rows = resourceMapper.deleteById(id);
                if (rows > 0) {
                    count++;
                }
            }
            return Result.success("已删除 " + count + " 个资源");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("批量删除失败");
        }
    }
    
    @Override
    public Result<Map<String, Object>> getStorageStats() {
        try {
            // 这里可以从数据库统计实际使用情况
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", 1000); // 总容量 GB
            stats.put("used", 456); // 已使用 GB
            stats.put("available", 544); // 可用 GB
            return Result.success(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取存储统计失败");
        }
    }
    
    @Override
    public Result<Map<String, Object>> getResourceStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 统计各类资源数量
            stats.put("document", resourceMapper.countByType("document"));
            stats.put("video", resourceMapper.countByType("video"));
            stats.put("code", resourceMapper.countByType("code"));
            stats.put("other", resourceMapper.countByType("other"));
            stats.put("total", resourceMapper.countResourceList("all", null));
            
            return Result.success(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取资源统计失败");
        }
    }
}
