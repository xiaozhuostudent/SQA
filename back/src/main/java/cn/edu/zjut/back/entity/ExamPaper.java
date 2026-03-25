package cn.edu.zjut.back.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 试卷实体类
 */
@Data
public class ExamPaper {
    private Long id;
    private Long courseId;
    private String courseName;
    private String title;
    private String description;
    private Integer totalScore;
    private Integer passScore;
    private Integer duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String examType; // online, offline
    private Boolean shuffleQuestions;
    private Boolean shuffleOptions;
    private Boolean showAnswer;
    private Boolean allowReview;
    private Boolean faceRecognitionEnabled; // 是否启用人脸识别
    private Long creatorId;
    private String creatorName;
    private String status; // draft, published, closed, archived
    private String paperUrl; // 试卷JSON文件URL
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}