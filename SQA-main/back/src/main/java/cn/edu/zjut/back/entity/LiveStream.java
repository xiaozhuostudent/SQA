package cn.edu.zjut.back.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 直播实体类
 */
@Data
public class LiveStream {
    private Long id;
    private String title;               // 直播标题
    private String description;         // 直播描述
    private Long teacherId;             // 教师ID
    private String teacherName;         // 教师姓名
    private Long courseId;              // 关联课程ID（可选）
    private String courseName;          // 课程名称
    private String status;              // 状态: scheduled(预定), live(直播中), ended(已结束)
    private String streamKey;           // 推流密钥
    private String streamUrl;           // 推流地址
    private String playUrl;             // 播放地址
    private String coverImage;          // 封面图片
    private LocalDateTime scheduledTime; // 预定开始时间
    private LocalDateTime startTime;    // 实际开始时间
    private LocalDateTime endTime;      // 结束时间
    private Integer viewerCount;        // 当前观看人数
    private Integer totalViews;         // 累计观看次数
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}