package cn.edu.zjut.back.service;

import cn.edu.zjut.back.entity.LiveStream;
import cn.edu.zjut.back.mapper.LiveStreamMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

/**
 * 直播服务类
 */
@Service
public class LiveStreamService {
    
    @Autowired
    private LiveStreamMapper liveStreamMapper;
    
    @Autowired
    private LiveWordCloudService liveWordCloudService;

    @Value("${livestream.stream-host:}")
    private String configuredStreamHost;

    @Value("${livestream.rtmp-port:8000}")
    private int rtmpPort;

    @Value("${livestream.http-port:8088}")
    private int httpPort;

    @Value("${livestream.app-name:live}")
    private String streamApp;

    private volatile String cachedStreamHost;
    
    /**
     * 获取所有直播
     */
    public List<LiveStream> getAllLiveStreams() {
        return liveStreamMapper.findAll();
    }
    
    /**
     * 根据ID获取直播
     */
    public LiveStream getLiveStreamById(Long id) {
        return liveStreamMapper.findById(id);
    }
    
    /**
     * 获取教师的直播列表
     */
    public List<LiveStream> getTeacherLiveStreams(Long teacherId) {
        return liveStreamMapper.findByTeacherId(teacherId);
    }
    
    /**
     * 获取指定状态的直播列表
     */
    public List<LiveStream> getLiveStreamsByStatus(String status) {
        return liveStreamMapper.findByStatus(status);
    }
    
    /**
     * 获取课程的直播列表
     */
    public List<LiveStream> getCourseLiveStreams(Long courseId) {
        return liveStreamMapper.findByCourseId(courseId);
    }
    
    /**
     * 创建直播
     */
    public LiveStream createLiveStream(LiveStream liveStream) {
        // 生成推流密钥
        String streamKey = UUID.randomUUID().toString().replace("-", "");
        liveStream.setStreamKey(streamKey);
        
        String host = resolveStreamHost();
        String streamUrl = String.format("rtmp://%s:%d/%s/%s", host, rtmpPort, streamApp, streamKey);
        String playUrl = String.format("http://%s:%d/%s/%s.m3u8", host, httpPort, streamApp, streamKey);
        liveStream.setStreamUrl(streamUrl);
        liveStream.setPlayUrl(playUrl);
        
        // 初始化状态和统计数据
        liveStream.setStatus("scheduled");
        liveStream.setViewerCount(0);
        liveStream.setTotalViews(0);
        
        // 确保日期字段正确设置
        if (liveStream.getScheduledTime() == null) {
            liveStream.setScheduledTime(LocalDateTime.now());
        }
        
        liveStreamMapper.insert(liveStream);
        return liveStream;
    }
    
    /**
     * 更新直播信息
     */
    public boolean updateLiveStream(LiveStream liveStream) {
        return liveStreamMapper.update(liveStream) > 0;
    }
    
    /**
     * 开始直播
     */
    public boolean startLiveStream(Long id) {
        return liveStreamMapper.updateStatus(id, "live", LocalDateTime.now()) > 0;
    }
    
    /**
     * 结束直播
     */
    public boolean endLiveStream(Long id) {
        boolean result = liveStreamMapper.endLive(id) > 0;
        
        // 直播结束后自动生成词云
        if (result) {
            try {
                liveWordCloudService.generateWordCloud(id);
            } catch (Exception e) {
                // 词云生成失败不影响直播结束
                e.printStackTrace();
            }
        }
        
        return result;
    }
    
    /**
     * 更新观看统计
     */
    public boolean updateViewerStats(Long id, Integer viewerCount, Integer totalViews) {
        return liveStreamMapper.updateViewerStats(id, viewerCount, totalViews) > 0;
    }
    
    /**
     * 删除直播
     */
    public boolean deleteLiveStream(Long id) {
        return liveStreamMapper.delete(id) > 0;
    }
    
    /**
     * 增加观看人数
     */
    public void incrementViewer(Long id) {
        LiveStream liveStream = liveStreamMapper.findById(id);
        if (liveStream != null) {
            int newViewerCount = (liveStream.getViewerCount() != null ? liveStream.getViewerCount() : 0) + 1;
            int newTotalViews = (liveStream.getTotalViews() != null ? liveStream.getTotalViews() : 0) + 1;
            liveStreamMapper.updateViewerStats(id, newViewerCount, newTotalViews);
        }
    }
    
    /**
     * 减少观看人数
     */
    public void decrementViewer(Long id) {
        LiveStream liveStream = liveStreamMapper.findById(id);
        if (liveStream != null && liveStream.getViewerCount() != null && liveStream.getViewerCount() > 0) {
            int newViewerCount = liveStream.getViewerCount() - 1;
            liveStreamMapper.updateViewerStats(id, newViewerCount, liveStream.getTotalViews());
        }
    }

    private String resolveStreamHost() {
        if (StringUtils.hasText(cachedStreamHost)) {
            return cachedStreamHost;
        }
        if (StringUtils.hasText(configuredStreamHost)) {
            cachedStreamHost = configuredStreamHost;
            return cachedStreamHost;
        }
        // 在Docker容器中，直接使用localhost
        cachedStreamHost = "localhost";
        return cachedStreamHost;
    }

    private String detectLocalIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface == null || !networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
            InetAddress fallback = InetAddress.getLocalHost();
            if (fallback != null) {
                return fallback.getHostAddress();
            }
        } catch (Exception ignored) {
        }
        return "localhost";
    }
}