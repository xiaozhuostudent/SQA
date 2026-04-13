package cn.edu.zjut.back.entity;

import lombok.Data;

/**
 * 试卷题目关联实体类
 */
@Data
public class ExamQuestion {
    private Long id;
    private Long examPaperId;
    private Long questionId;
    private Integer questionOrder;
    private Integer questionScore;
    private Boolean isRequired;
}
