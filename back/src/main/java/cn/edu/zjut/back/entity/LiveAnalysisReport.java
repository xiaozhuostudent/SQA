package cn.edu.zjut.back.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 分析报告实体
 */
@Data
public class LiveAnalysisReport {
    private Long id;
    private Long liveStreamId;
    private String reportContent;
    private String aiProvider;
    private String promptSnapshot;
    private LocalDateTime generatedAt;
    private LocalDateTime updatedAt;
}
