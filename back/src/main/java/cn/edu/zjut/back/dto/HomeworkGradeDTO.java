package cn.edu.zjut.back.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * 作业批改DTO
 */
@Data
public class HomeworkGradeDTO {
    @NotNull(message = "提交ID不能为空")
    private Long submissionId;
    
    @NotNull(message = "分数不能为空")
    private Integer score;
    
    private String feedback;
}
