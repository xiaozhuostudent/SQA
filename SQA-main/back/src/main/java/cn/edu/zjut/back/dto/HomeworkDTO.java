package cn.edu.zjut.back.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 作业创建/编辑DTO
 */
@Data
public class HomeworkDTO {
    private Long id;
    
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    
    @NotBlank(message = "作业标题不能为空")
    private String title;
    
    private String description;
    
    @NotNull(message = "截止时间不能为空")
    private LocalDateTime deadline;
    
    @NotNull(message = "总分不能为空")
    private Integer totalScore;
    
    private String attachmentUrl;
}
