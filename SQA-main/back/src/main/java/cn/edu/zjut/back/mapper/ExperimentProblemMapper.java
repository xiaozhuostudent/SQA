package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.ExperimentProblem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ExperimentProblemMapper {

    @Select("SELECT id, experiment_id, title, description, difficulty, score, " +
            "max_cpu_time_ms as timeLimit, max_memory_kb / 1024 as memoryLimit, " +
            "problem_order as sortOrder, creator_id as createdBy, create_time, update_time " +
            "FROM tb_experiment_problem WHERE id = #{id}")
    ExperimentProblem findById(Long id);

    @Select("SELECT id, experiment_id, title, description, difficulty, score, " +
            "max_cpu_time_ms as timeLimit, max_memory_kb / 1024 as memoryLimit, " +
            "problem_order as sortOrder, creator_id as createdBy, create_time, update_time " +
            "FROM tb_experiment_problem WHERE experiment_id = #{experimentId} ORDER BY problem_order ASC")
    List<ExperimentProblem> findByExperimentId(Long experimentId);

    @Insert("INSERT INTO tb_experiment_problem(experiment_id, title, description, difficulty, score, max_cpu_time_ms, max_memory_kb, problem_order, creator_id, create_time, update_time) " +
            "VALUES(#{experimentId}, #{title}, #{description}, #{difficulty}, #{score}, #{timeLimit}, #{memoryLimit} * 1024, #{sortOrder}, #{createdBy}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ExperimentProblem problem);

    @Update("UPDATE tb_experiment_problem SET title=#{title}, description=#{description}, difficulty=#{difficulty}, score=#{score}, " +
            "max_cpu_time_ms=#{timeLimit}, max_memory_kb=#{memoryLimit} * 1024, problem_order=#{sortOrder}, update_time=NOW() " +
            "WHERE id=#{id}")
    int update(ExperimentProblem problem);

    @Delete("DELETE FROM tb_experiment_problem WHERE id = #{id}")
    int deleteById(Long id);
    
    @Delete("DELETE FROM tb_experiment_problem WHERE experiment_id = #{experimentId}")
    int deleteByExperimentId(Long experimentId);
}
