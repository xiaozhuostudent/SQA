package cn.edu.zjut.back.service;

import cn.edu.zjut.back.entity.Course;
import cn.edu.zjut.back.entity.Discussion;
import cn.edu.zjut.back.entity.DiscussionReply;
import cn.edu.zjut.back.entity.User;
import cn.edu.zjut.back.mapper.CourseMapper;
import cn.edu.zjut.back.mapper.DiscussionMapper;
import cn.edu.zjut.back.mapper.DiscussionReplyMapper;
import cn.edu.zjut.back.mapper.UserMapper;
import cn.edu.zjut.back.utils.JwtUtil;
import cn.edu.zjut.back.utils.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * 讨论服务类
 */
@Service
public class DiscussionService {
    
    @Autowired
    private DiscussionMapper discussionMapper;
    
    @Autowired
    private DiscussionReplyMapper replyMapper;
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private CourseMapper courseMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    // Redis 缓存键
    private static final String DISCUSSION_VIEW_COUNT_KEY = "discussion:view:";
    private static final String REPLY_LIKE_COUNT_KEY = "reply:like:";
    private static final String DISCUSSION_LIST_KEY = "discussion:list:course:";
    private static final int CACHE_EXPIRE_SECONDS = 1800; // 30分钟
    
    /**
     * 创建讨论
     */
    public Discussion createDiscussion(Discussion discussion) {
        // 根据课程ID获取课程名称
        if (discussion.getCourseId() != null) {
            Course course = courseMapper.findById(discussion.getCourseId());
            if (course != null) {
                discussion.setCourseName(course.getName());
            }
        }
        
        // 获取当前用户角色
        User currentUser = getCurrentUser();
        System.out.println("[讨论服务] 获取到的当前用户: " + (currentUser != null ? currentUser.getUsername() + ", 角色: " + currentUser.getRole() : "null"));
        if (currentUser != null) {
            discussion.setAuthorRole(currentUser.getRole());
            System.out.println("[讨论服务] 设置 authorRole: " + discussion.getAuthorRole());
        } else {
            System.err.println("[讨论服务] 警告：无法获取当前用户，authorRole将为空");
        }
        
        discussion.setStatus("active");
        discussion.setViewCount(0);
        discussion.setReplyCount(0);
        
        System.out.println("[讨论服务] 准备插入讨论 - courseId: " + discussion.getCourseId() 
            + ", courseName: " + discussion.getCourseName()
            + ", authorId: " + discussion.getAuthorId()
            + ", authorName: " + discussion.getAuthorName()
            + ", authorRole: " + discussion.getAuthorRole());
        
        discussionMapper.insert(discussion);
        
        // 清除课程讨论列表缓存
        redisUtil.delete(DISCUSSION_LIST_KEY + discussion.getCourseId());
        
        return discussion;
    }
    
    /**
     * 获取当前登录用户
     */
    private User getCurrentUser() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                System.err.println("[讨论服务] RequestAttributes 为 null");
                return null;
            }
            HttpServletRequest request = attributes.getRequest();
            String token = request.getHeader("Authorization");
            
            System.out.println("[讨论服务] Authorization header: " + (token != null ? "存在" : "不存在"));
            
            if (token == null || !token.startsWith("Bearer ")) {
                System.err.println("[讨论服务] Token 为空或格式不正确");
                return null;
            }
            
            token = token.substring(7);
            Long userId = jwtUtil.getUserIdFromToken(token);
            System.out.println("[讨论服务] 从 Token 解析到用户ID: " + userId);
            
            User user = userMapper.findByIdWithDetails(userId);
            System.out.println("[讨论服务] 查询到用户: " + (user != null ? user.getUsername() + ", 角色: " + user.getRole() : "null"));
            return user;
        } catch (Exception e) {
            System.err.println("[讨论服务] 获取当前用户失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 获取课程讨论列表
     */
    public List<Discussion> getDiscussionsByCourse(Long courseId) {
        String cacheKey = DISCUSSION_LIST_KEY + courseId;
        
        // 尝试从缓存获取
        @SuppressWarnings("unchecked")
        List<Discussion> cachedList = (List<Discussion>) redisUtil.get(cacheKey);
        if (cachedList != null) {
            return cachedList;
        }
        
        // 从数据库查询
        List<Discussion> list = discussionMapper.findByCourseId(courseId);
        
        // 存入缓存
        if (list != null && !list.isEmpty()) {
            redisUtil.set(cacheKey, list, CACHE_EXPIRE_SECONDS);
        }
        
        return list;
    }
    
    /**
     * 获取讨论详情(增加浏览量)
     */
    @Transactional
    public Discussion getDiscussionDetail(Long id) {
        // 使用 Redis 计数器增加浏览量
        String viewCountKey = DISCUSSION_VIEW_COUNT_KEY + id;
        Long viewCount = redisUtil.increment(viewCountKey, 1);
        
        // 每 10 次浏览同步一次到数据库
        if (viewCount % 10 == 0) {
            discussionMapper.incrementViewCount(id);
        }
        
        Discussion discussion = discussionMapper.findById(id);
        // 返回实时浏览量
        if (discussion != null) {
            discussion.setViewCount(viewCount.intValue());
        }
        
        return discussion;
    }
    
    /**
     * 创建回复
     */
    @Transactional
    public DiscussionReply createReply(DiscussionReply reply) {
        // 获取当前用户角色
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            reply.setAuthorRole(currentUser.getRole());
        }
        
        reply.setStatus("active");
        reply.setLikeCount(0);
        replyMapper.insert(reply);
        
        // 增加讨论的回复数
        discussionMapper.incrementReplyCount(reply.getDiscussionId());
        
        // 清除课程讨论列表缓存
        Discussion discussion = discussionMapper.findById(reply.getDiscussionId());
        if (discussion != null) {
            redisUtil.delete(DISCUSSION_LIST_KEY + discussion.getCourseId());
        }
        
        return reply;
    }
    
    /**
     * 获取讨论的所有回复
     */
    public List<DiscussionReply> getReplies(Long discussionId) {
        return replyMapper.findByDiscussionId(discussionId);
    }
    
    /**
     * 点赞回复
     */
    public void likeReply(Long replyId) {
        // 使用 Redis 计数器增加点赞数
        String likeCountKey = REPLY_LIKE_COUNT_KEY + replyId;
        Long likeCount = redisUtil.increment(likeCountKey, 1);
        
        // 每 5 次点赞同步一次到数据库
        if (likeCount % 5 == 0) {
            replyMapper.incrementLikeCount(replyId);
        }
    }
    
    /**
     * 采纳回复
     */
    @Transactional
    public void acceptReply(Long discussionId, Long replyId) {
        DiscussionReply reply = new DiscussionReply();
        reply.setId(replyId);
        reply.setIsAccepted(true);
        replyMapper.update(reply);
        
        // 标记讨论为已解决
        Discussion discussion = discussionMapper.findById(discussionId);
        discussion.setIsResolved(true);
        discussionMapper.update(discussion);
    }
    
    /**
     * 删除回复
     */
    @Transactional
    public void deleteReply(Long discussionId, Long replyId) {
        // 删除回复
        replyMapper.deleteById(replyId);
        
        // 减少讨论的回复数
        discussionMapper.decrementReplyCount(discussionId);
    }
}
