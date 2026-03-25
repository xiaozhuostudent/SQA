package cn.edu.zjut.back.controller;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.entity.LiveStream;
import cn.edu.zjut.back.service.LiveStreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * 直播Controller
 */
@RestController
@RequestMapping("/api/livestream")
@CrossOrigin
public class LiveStreamController {
    
    @Autowired
    private LiveStreamService liveStreamService;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${livestream.srs-api-url:http://localhost:1985}")
    private String srsApiUrl;
    
    /**
     * 获取SRS服务器状态（系统监控）
     */
    @GetMapping("/monitor/status")
    @SuppressWarnings("unchecked")
    public Result<Map<String, Object>> getSrsStatus() {
        try {
            String url = srsApiUrl + "/api/v1/summaries";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            // 包装响应数据以符合前端期望的格式
            Map<String, Object> wrapper = new java.util.HashMap<>();
            wrapper.put("code", 0);
            wrapper.put("data", response);
            return Result.success(wrapper);
        } catch (Exception e) {
            e.printStackTrace(); // 打印错误堆栈以便调试
            return Result.error("获取SRS状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有直播列表
     */
    @GetMapping("/list")
    public Result<List<LiveStream>> getAllLiveStreams() {
        try {
            List<LiveStream> liveStreams = liveStreamService.getAllLiveStreams();
            return Result.success(liveStreams);
        } catch (Exception e) {
            return Result.error("获取直播列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取直播详情
     */
    @GetMapping("/{id}")
    public Result<LiveStream> getLiveStreamById(@PathVariable Long id) {
        try {
            LiveStream liveStream = liveStreamService.getLiveStreamById(id);
            if (liveStream == null) {
                return Result.error("直播不存在");
            }
            return Result.success(liveStream);
        } catch (Exception e) {
            return Result.error("获取直播详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取教师的直播列表
     */
    @GetMapping("/teacher/{teacherId}")
    public Result<List<LiveStream>> getTeacherLiveStreams(@PathVariable Long teacherId) {
        try {
            List<LiveStream> liveStreams = liveStreamService.getTeacherLiveStreams(teacherId);
            return Result.success(liveStreams);
        } catch (Exception e) {
            return Result.error("获取教师直播列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据状态获取直播列表
     */
    @GetMapping("/status/{status}")
    public Result<List<LiveStream>> getLiveStreamsByStatus(@PathVariable String status) {
        try {
            List<LiveStream> liveStreams = liveStreamService.getLiveStreamsByStatus(status);
            return Result.success(liveStreams);
        } catch (Exception e) {
            return Result.error("获取直播列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取课程的直播列表
     */
    @GetMapping("/course/{courseId}")
    public Result<List<LiveStream>> getCourseLiveStreams(@PathVariable Long courseId) {
        try {
            List<LiveStream> liveStreams = liveStreamService.getCourseLiveStreams(courseId);
            return Result.success(liveStreams);
        } catch (Exception e) {
            return Result.error("获取课程直播列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建直播
     */
    @PostMapping("/create")
    public Result<LiveStream> createLiveStream(@RequestBody LiveStream liveStream) {
        try {
            LiveStream created = liveStreamService.createLiveStream(liveStream);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error("创建直播失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新直播信息
     */
    @PutMapping("/update")
    public Result<String> updateLiveStream(@RequestBody LiveStream liveStream) {
        try {
            boolean success = liveStreamService.updateLiveStream(liveStream);
            return success ? Result.success("更新成功") : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error("更新直播失败: " + e.getMessage());
        }
    }
    
    /**
     * 开始直播
     */
    @PostMapping("/start/{id}")
    public Result<String> startLiveStream(@PathVariable Long id) {
        try {
            boolean success = liveStreamService.startLiveStream(id);
            return success ? Result.success("直播已开始") : Result.error("开始直播失败");
        } catch (Exception e) {
            return Result.error("开始直播失败: " + e.getMessage());
        }
    }
    
    /**
     * 结束直播
     */
    @PostMapping("/end/{id}")
    public Result<String> endLiveStream(@PathVariable Long id) {
        try {
            boolean success = liveStreamService.endLiveStream(id);
            return success ? Result.success("直播已结束") : Result.error("结束直播失败");
        } catch (Exception e) {
            return Result.error("结束直播失败: " + e.getMessage());
        }
    }
    
    /**
     * 进入直播间（增加观看人数）
     */
    @PostMapping("/join/{id}")
    public Result<String> joinLiveStream(@PathVariable Long id) {
        try {
            liveStreamService.incrementViewer(id);
            return Result.success("已进入直播间");
        } catch (Exception e) {
            return Result.error("进入直播间失败: " + e.getMessage());
        }
    }
    
    /**
     * 离开直播间（减少观看人数）
     */
    @PostMapping("/leave/{id}")
    public Result<String> leaveLiveStream(@PathVariable Long id) {
        try {
            liveStreamService.decrementViewer(id);
            return Result.success("已离开直播间");
        } catch (Exception e) {
            return Result.error("离开直播间失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除直播
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteLiveStream(@PathVariable Long id) {
        try {
            boolean success = liveStreamService.deleteLiveStream(id);
            return success ? Result.success("删除成功") : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error("删除直播失败: " + e.getMessage());
        }
    }
}
