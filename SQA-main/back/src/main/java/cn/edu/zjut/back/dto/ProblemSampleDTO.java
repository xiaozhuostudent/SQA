package cn.edu.zjut.back.dto;

import lombok.Data;

@Data
public class ProblemSampleDTO {
    private String input;
    private String output;
    private Boolean isHidden;
}
