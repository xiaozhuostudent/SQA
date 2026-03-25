package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.ExperimentSubmission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExperimentSubmissionMapper {
    
    /**
     * 根据实验ID获取所有提交
     */
    List<ExperimentSubmission> findByExperimentId(@Param("experimentId") Long experimentId);
    
    /**
     * 根据实验ID统计提交人数
     */
    int countByExperimentId(@Param("experimentId") Long experimentId);
    
    /**
     * 根据实验ID和学生ID查询提交
     */
    ExperimentSubmission findByExperimentIdAndStudentId(
        @Param("experimentId") Long experimentId, 
        @Param("studentId") Long studentId
    );
    
    /**
     * 插入提交记录
     */
    void insert(ExperimentSubmission submission);
    
    /**
     * 更新提交记录
     */
    void update(ExperimentSubmission submission);
    
    /**
     * 删除提交记录
     */
    void deleteById(@Param("id") Long id);
    
    /**
     * 根据实验ID删除所有提交
     */
    void deleteByExperimentId(@Param("experimentId") Long experimentId);
}
