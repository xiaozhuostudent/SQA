package cn.edu.zjut.back.dto;

import lombok.Data;

@Data
public class CodeSubmissionDTO {
    private Long experimentId;
    private Long problemId;
    private String language;
    private String code;
}
