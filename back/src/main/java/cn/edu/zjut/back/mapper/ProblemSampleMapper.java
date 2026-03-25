package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.ProblemSample;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProblemSampleMapper {

    @Select("SELECT id, experiment_problem_id as problemId, input_data as input, output_data as output, " +
            "NOT is_example as isHidden, create_time " +
            "FROM tb_problem_sample WHERE experiment_problem_id = #{problemId}")
    List<ProblemSample> findByProblemId(Long problemId);

    @Insert("INSERT INTO tb_problem_sample(experiment_problem_id, input_data, output_data, is_example, create_time) " +
            "VALUES(#{problemId}, #{input}, #{output}, NOT #{isHidden}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ProblemSample sample);

    @Delete("DELETE FROM tb_problem_sample WHERE experiment_problem_id = #{problemId}")
    int deleteByProblemId(Long problemId);
}
