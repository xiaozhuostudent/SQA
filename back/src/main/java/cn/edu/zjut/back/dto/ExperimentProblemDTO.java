package cn.edu.zjut.back.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExperimentProblemDTO {
    private Long id; // Optional for update
    private String title;
    private String description;
    private String difficulty;
    private Integer score;
    private Integer timeLimit;
    private Integer memoryLimit;
    private Integer sortOrder;
    private List<ProblemSampleDTO> samples;
}
