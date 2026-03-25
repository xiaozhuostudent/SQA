package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.ExperimentProblemSubmission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ExperimentProblemSubmissionMapper {
    
    /**
     * 根据实验ID和学生ID获取所有提交记录
     */
    List<ExperimentProblemSubmission> findByExperimentIdAndStudentId(
        @Param("experimentId") Long experimentId,
        @Param("studentId") Long studentId
    );
    
    /**
     * 根据实验ID获取所有提交记录
     */
    List<ExperimentProblemSubmission> findByExperimentId(@Param("experimentId") Long experimentId);
    
    /**
     * 获取学生最新的题目提交记录
     */
    ExperimentProblemSubmission findLatestByProblemAndStudent(
        @Param("problemId") Long problemId,
        @Param("studentId") Long studentId
    );
    
    /**
     * 获取学生对某题目的所有提交记录
     */
    List<ExperimentProblemSubmission> findByProblemIdAndStudentId(
        @Param("problemId") Long problemId,
        @Param("studentId") Long studentId
    );
    
    /**
     * 插入提交记录
     */
    void insert(ExperimentProblemSubmission submission);
    
    /**
     * 更新提交记录
     */
    void update(ExperimentProblemSubmission submission);
}
