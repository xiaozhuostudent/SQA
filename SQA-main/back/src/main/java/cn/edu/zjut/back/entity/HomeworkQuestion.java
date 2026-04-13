package cn.edu.zjut.back.entity;

import lombok.Data;

/**
 * 作业题目关联实体类 (tb_homework_question)
 */
@Data
public class HomeworkQuestion {
    private Long id;
    private Long homeworkId;
    private Long questionId;
    private Integer questionOrder;
    private Integer questionScore;
}
