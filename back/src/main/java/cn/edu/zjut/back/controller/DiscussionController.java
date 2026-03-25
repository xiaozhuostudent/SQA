package cn.edu.zjut.back.controller;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.entity.Discussion;
import cn.edu.zjut.back.entity.DiscussionReply;
import cn.edu.zjut.back.service.DiscussionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 讨论Controller
 */
@RestController
@RequestMapping("/api/discussion")
@CrossOrigin
public class DiscussionController {
    
    @Autowired
    private DiscussionService discussionService;
    
    /**
     * 创建讨论
     */
    @PostMapping("/create")
    public Result<Discussion> createDiscussion(@RequestBody Discussion discussion) {
        try {
            Discussion created = discussionService.createDiscussion(discussion);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error("创建讨论失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取课程讨论列表
     */
    @GetMapping("/list/{courseId}")
    public Result<List<Discussion>> getDiscussions(@PathVariable Long courseId) {
        try {
            List<Discussion> discussions = discussionService.getDiscussionsByCourse(courseId);
            return Result.success(discussions);
        } catch (Exception e) {
            return Result.error("获取讨论列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取讨论详情
     */
    @GetMapping("/detail/{id}")
    public Result<Discussion> getDiscussionDetail(@PathVariable Long id) {
        try {
            Discussion discussion = discussionService.getDiscussionDetail(id);
            return Result.success(discussion);
        } catch (Exception e) {
            return Result.error("获取讨论详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建回复
     */
    @PostMapping("/reply/create")
    public Result<DiscussionReply> createReply(@RequestBody DiscussionReply reply) {
        try {
            DiscussionReply created = discussionService.createReply(reply);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error("回复失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取回复列表
     */
    @GetMapping("/reply/list/{discussionId}")
    public Result<List<DiscussionReply>> getReplies(@PathVariable Long discussionId) {
        try {
            List<DiscussionReply> replies = discussionService.getReplies(discussionId);
            return Result.success(replies);
        } catch (Exception e) {
            return Result.error("获取回复列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 点赞回复
     */
    @PostMapping("/reply/like/{replyId}")
    public Result<String> likeReply(@PathVariable Long replyId) {
        try {
            discussionService.likeReply(replyId);
            return Result.success("点赞成功");
        } catch (Exception e) {
            return Result.error("点赞失败: " + e.getMessage());
        }
    }
    
    /**
     * 采纳回复
     */
    @PostMapping("/reply/accept/{discussionId}/{replyId}")
    public Result<String> acceptReply(@PathVariable Long discussionId, @PathVariable Long replyId) {
        try {
            discussionService.acceptReply(discussionId, replyId);
            return Result.success("采纳成功");
        } catch (Exception e) {
            return Result.error("采纳失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除回复
     */
    @DeleteMapping("/reply/delete/{discussionId}/{replyId}")
    public Result<String> deleteReply(@PathVariable Long discussionId, @PathVariable Long replyId) {
        try {
            discussionService.deleteReply(discussionId, replyId);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}
