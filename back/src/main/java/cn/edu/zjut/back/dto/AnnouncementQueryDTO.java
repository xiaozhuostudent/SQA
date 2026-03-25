package cn.edu.zjut.back.dto;

import lombok.Data;

/**
 * 公告查询参数DTO
 */
@Data
public class AnnouncementQueryDTO {
    private Integer page = 1;
    private Integer size = 10;
    private String keyword; // 搜索关键词
    private String type; // 公告类型
    private String priority; // 优先级
    private String targetRole; // 目标角色
    private String status; // 状态
}
