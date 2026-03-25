package cn.edu.zjut.back.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExperimentDTO {
    private Long id;
    private Long courseId;
    private String courseName;
    private String title;
    private String description;
    private String requirements;
    private String steps;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime deadline;
    
    private String environmentType;
    private String environmentConfig;
    private String resources;
    private String allowedLanguages;
    private List<ExperimentProblemDTO> problems;
}
