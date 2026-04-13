package cn.edu.zjut.back.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 题目样例实体类
 */
@Data
public class ProblemSample {
    private Long id;
    private Long problemId;
    private String input;
    private String output;
    private Boolean isHidden;
    private LocalDateTime createTime;
}
