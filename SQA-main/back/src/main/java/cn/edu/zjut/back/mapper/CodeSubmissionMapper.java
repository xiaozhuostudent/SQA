package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.CodeSubmission;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CodeSubmissionMapper {

    @Select("SELECT * FROM tb_code_submission WHERE id = #{id}")
    CodeSubmission findById(Long id);

    @Select("SELECT * FROM tb_code_submission WHERE student_id = #{studentId} AND experiment_problem_id = #{problemId} ORDER BY submit_time DESC")
    List<CodeSubmission> findByStudentIdAndProblemId(@Param("studentId") Long studentId, @Param("problemId") Long problemId);

    @Insert("INSERT INTO tb_code_submission(experiment_id, experiment_problem_id, student_id, language, code, result, run_time_ms, memory_kb, stderr, piston_response, submit_time) " +
            "VALUES(#{experimentId}, #{experimentProblemId}, #{studentId}, #{language}, #{code}, #{result}, #{runTimeMs}, #{memoryKb}, #{stderr}, #{pistonResponse}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CodeSubmission submission);

    @Update("UPDATE tb_code_submission SET result=#{result}, run_time_ms=#{runTimeMs}, memory_kb=#{memoryKb}, " +
            "stderr=#{stderr}, piston_response=#{pistonResponse} WHERE id=#{id}")
    int updateResult(CodeSubmission submission);
}
