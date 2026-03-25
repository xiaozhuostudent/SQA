package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.Experiment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ExperimentMapper {

    @Select("SELECT * FROM tb_experiment WHERE id = #{id}")
    Experiment findById(Long id);

    @Select("SELECT * FROM tb_experiment WHERE course_id = #{courseId}")
    List<Experiment> findByCourseId(Long courseId);

    @Insert("INSERT INTO tb_experiment(course_id, course_name, title, description, requirements, steps, start_time, deadline, environment_type, environment_config, resources, create_time, update_time) " +
            "VALUES(#{courseId}, #{courseName}, #{title}, #{description}, #{requirements}, #{steps}, #{startTime}, #{deadline}, #{environmentType}, #{environmentConfig}, #{resources}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Experiment experiment);

    @Update("UPDATE tb_experiment SET course_id=#{courseId}, course_name=#{courseName}, title=#{title}, description=#{description}, requirements=#{requirements}, steps=#{steps}, " +
            "start_time=#{startTime}, deadline=#{deadline}, environment_type=#{environmentType}, environment_config=#{environmentConfig}, resources=#{resources}, update_time=NOW() " +
            "WHERE id=#{id}")
    int update(Experiment experiment);

    @Delete("DELETE FROM tb_experiment WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM tb_experiment")
    List<Experiment> findAll();
}
