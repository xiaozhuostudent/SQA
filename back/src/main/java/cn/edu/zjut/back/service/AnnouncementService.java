package cn.edu.zjut.back.service;

import cn.edu.zjut.back.dto.AnnouncementQueryDTO;
import cn.edu.zjut.back.entity.Announcement;
import cn.edu.zjut.back.mapper.AnnouncementMapper;
import cn.edu.zjut.back.utils.RedisUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公告通知服务
 */
@Service
public class AnnouncementService {

    private final AnnouncementMapper announcementMapper;
    private final RedisUtil redisUtil;
    
    // Redis 缓存键
    private static final String ANNOUNCEMENT_CACHE_KEY = "announcement:";
    private static final String ANNOUNCEMENTS_BY_ROLE_KEY = "announcements:role:";
    private static final int CACHE_EXPIRE_SECONDS = 1800; // 30分钟

    public AnnouncementService(AnnouncementMapper announcementMapper, RedisUtil redisUtil) {
        this.announcementMapper = announcementMapper;
        this.redisUtil = redisUtil;
    }

    /**
     * 分页查询公告
     */
    public Map<String, Object> queryAnnouncements(AnnouncementQueryDTO query) {
        int offset = (query.getPage() - 1) * query.getSize();
        List<Announcement> list = announcementMapper.queryAnnouncements(query, offset);
        long total = announcementMapper.countAnnouncements(query);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", query.getPage());
        result.put("size", query.getSize());
        return result;
    }

    /**
     * 根据角色获取已发布的公告（首页展示）
     */
    public List<Announcement> getPublishedAnnouncementsByRole(String role, int limit) {
        String cacheKey = ANNOUNCEMENTS_BY_ROLE_KEY + role + ":" + limit;
        
        // 尝试从缓存获取
        @SuppressWarnings("unchecked")
        List<Announcement> cachedList = (List<Announcement>) redisUtil.get(cacheKey);
        if (cachedList != null) {
            return cachedList;
        }
        
        // 从数据库查询
        List<Announcement> list = announcementMapper.getPublishedAnnouncementsByRole(role, limit);
        
        // 存入缓存
        if (list != null && !list.isEmpty()) {
            redisUtil.set(cacheKey, list, CACHE_EXPIRE_SECONDS);
        }
        
        return list;
    }

    /**
     * 根据ID查询公告
     */
    public Announcement getById(Long id) {
        String cacheKey = ANNOUNCEMENT_CACHE_KEY + id;
        
        // 尝试从缓存获取
        Announcement cached = (Announcement) redisUtil.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // 从数据库查询
        Announcement announcement = announcementMapper.findById(id);
        
        // 存入缓存
        if (announcement != null) {
            redisUtil.set(cacheKey, announcement, CACHE_EXPIRE_SECONDS);
        }
        
        return announcement;
    }

    /**
     * 创建公告
     */
    @Transactional
    public Long createAnnouncement(Announcement announcement) {
        announcementMapper.insert(announcement);
        // 清除角色相关的公告列表缓存
        clearAnnouncementCaches();
        return announcement.getId();
    }

    /**
     * 更新公告
     */
    @Transactional
    public void updateAnnouncement(Announcement announcement) {
        announcementMapper.update(announcement);
        // 清除该公告的缓存
        redisUtil.delete(ANNOUNCEMENT_CACHE_KEY + announcement.getId());
        // 清除角色相关的公告列表缓存
        clearAnnouncementCaches();
    }

    /**
     * 发布公告
     */
    @Transactional
    public void publishAnnouncement(Long id) {
        announcementMapper.publish(id);
        // 清除该公告的缓存
        redisUtil.delete(ANNOUNCEMENT_CACHE_KEY + id);
        // 清除角色相关的公告列表缓存
        clearAnnouncementCaches();
    }

    /**
     * 归档公告
     */
    @Transactional
    public void archiveAnnouncement(Long id) {
        announcementMapper.archive(id);
        // 清除该公告的缓存
        redisUtil.delete(ANNOUNCEMENT_CACHE_KEY + id);
        // 清除角色相关的公告列表缓存
        clearAnnouncementCaches();
    }

    /**
     * 删除公告
     */
    @Transactional
    public void deleteAnnouncement(Long id) {
        announcementMapper.delete(id);
        // 清除该公告的缓存
        redisUtil.delete(ANNOUNCEMENT_CACHE_KEY + id);
        // 清除角色相关的公告列表缓存
        clearAnnouncementCaches();
    }
    
    /**
     * 清除公告相关缓存
     */
    private void clearAnnouncementCaches() {
        // 清除所有角色的公告列表缓存
        redisUtil.deleteByPattern(ANNOUNCEMENTS_BY_ROLE_KEY + "*");
    }
}
