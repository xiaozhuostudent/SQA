package cn.edu.zjut.back.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * 作业提交DTO
 */
@Data
public class HomeworkSubmitDTO {
    @NotNull(message = "作业ID不能为空")
    private Long homeworkId;
    
    private String content;
    
    private String attachmentUrl;
}
