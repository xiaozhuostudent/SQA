package cn.edu.zjut.back.service;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.common.ResultCode;
import cn.edu.zjut.back.dto.ExperimentDTO;
import cn.edu.zjut.back.dto.ExperimentProblemDTO;
import cn.edu.zjut.back.dto.ProblemSampleDTO;
import cn.edu.zjut.back.entity.*;
import cn.edu.zjut.back.mapper.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ExperimentService {

    private final ExperimentMapper experimentMapper;
    private final ExperimentProblemMapper experimentProblemMapper;
    private final ProblemSampleMapper problemSampleMapper;
    private final CourseMapper courseMapper;
    private final ExperimentSubmissionMapper experimentSubmissionMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final ExperimentProblemSubmissionMapper experimentProblemSubmissionMapper;

    public ExperimentService(ExperimentMapper experimentMapper, 
                           ExperimentProblemMapper experimentProblemMapper, 
                           ProblemSampleMapper problemSampleMapper, 
                           CourseMapper courseMapper,
                           ExperimentSubmissionMapper experimentSubmissionMapper,
                           EnrollmentMapper enrollmentMapper,
                           UserMapper userMapper,
                           ObjectMapper objectMapper,
                           ExperimentProblemSubmissionMapper experimentProblemSubmissionMapper) {
        this.experimentMapper = experimentMapper;
        this.experimentProblemMapper = experimentProblemMapper;
        this.problemSampleMapper = problemSampleMapper;
        this.courseMapper = courseMapper;
        this.experimentSubmissionMapper = experimentSubmissionMapper;
        this.enrollmentMapper = enrollmentMapper;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
        this.experimentProblemSubmissionMapper = experimentProblemSubmissionMapper;
    }

    @Transactional
    public Result createExperiment(ExperimentDTO dto, Long userId) {
        Course course = courseMapper.findById(dto.getCourseId());
        if (course == null) {
            return Result.error(ResultCode.COURSE_NOT_FOUND);
        }

        Experiment experiment = new Experiment();
        BeanUtils.copyProperties(dto, experiment);
        experiment.setCourseName(course.getName());
        
        experimentMapper.insert(experiment);

        if (dto.getProblems() != null) {
            int order = 1;
            for (ExperimentProblemDTO pDto : dto.getProblems()) {
                ExperimentProblem problem = new ExperimentProblem();
                BeanUtils.copyProperties(pDto, problem);
                problem.setExperimentId(experiment.getId());
                problem.setCreatedBy(userId);
                // 如果前端没有传 sortOrder，则自动生成
                if (problem.getSortOrder() == null) {
                    problem.setSortOrder(order++);
                }
                experimentProblemMapper.insert(problem);

                if (pDto.getSamples() != null) {
                    for (ProblemSampleDTO sDto : pDto.getSamples()) {
                        ProblemSample sample = new ProblemSample();
                        BeanUtils.copyProperties(sDto, sample);
                        sample.setProblemId(problem.getId());
                        problemSampleMapper.insert(sample);
                    }
                }
            }
        }

        return Result.success(experiment);
    }

    public Result getExperiment(Long id) {
        Experiment experiment = experimentMapper.findById(id);
        if (experiment == null) {
            return Result.error(ResultCode.EXPERIMENT_NOT_FOUND);
        }
        
        // 获取实验的所有题目
        List<ExperimentProblem> problems = experimentProblemMapper.findByExperimentId(id);
        
        // 为每个题目获取样例
        List<ExperimentProblemDTO> problemDTOs = new ArrayList<>();
        for (ExperimentProblem problem : problems) {
            ExperimentProblemDTO dto = new ExperimentProblemDTO();
            BeanUtils.copyProperties(problem, dto);
            
            // 获取题目的样例
            List<ProblemSample> samples = problemSampleMapper.findByProblemId(problem.getId());
            List<ProblemSampleDTO> sampleDTOs = new ArrayList<>();
            for (ProblemSample sample : samples) {
                ProblemSampleDTO sampleDTO = new ProblemSampleDTO();
                BeanUtils.copyProperties(sample, sampleDTO);
                sampleDTOs.add(sampleDTO);
            }
            dto.setSamples(sampleDTOs);
            problemDTOs.add(dto);
        }
        
        // 构建返回的DTO
        ExperimentDTO experimentDTO = new ExperimentDTO();
        BeanUtils.copyProperties(experiment, experimentDTO);
        experimentDTO.setProblems(problemDTOs);
        
        return Result.success(experimentDTO);
    }
    
    public Result getExperimentProblems(Long experimentId, Long userId) {
        List<ExperimentProblem> problems = experimentProblemMapper.findByExperimentId(experimentId);
        
        // 如果有userId，查询学生对每道题的提交状态
        if (userId != null) {
            // 将用户ID转换为学生ID
            Long studentId = userMapper.getStudentIdByUserId(userId);
            
            List<Map<String, Object>> problemsWithStatus = new ArrayList<>();
            for (ExperimentProblem problem : problems) {
                Map<String, Object> problemMap = new HashMap<>();
                problemMap.put("id", problem.getId());
                problemMap.put("experimentId", problem.getExperimentId());
                problemMap.put("title", problem.getTitle());
                problemMap.put("description", problem.getDescription());
                problemMap.put("inputFormat", problem.getInputFormat());
                problemMap.put("outputFormat", problem.getOutputFormat());
                problemMap.put("samples", problem.getSamples());
                problemMap.put("difficulty", problem.getDifficulty());
                problemMap.put("score", problem.getScore());
                problemMap.put("timeLimit", problem.getTimeLimit());
                problemMap.put("memoryLimit", problem.getMemoryLimit());
                
                // 使用学生ID查询最新提交状态
                if (studentId != null) {
                    ExperimentProblemSubmission latestSubmission = experimentProblemSubmissionMapper
                        .findLatestByProblemAndStudent(problem.getId(), studentId);
                    
                    if (latestSubmission != null) {
                        problemMap.put("status", latestSubmission.getStatus());
                        problemMap.put("passRate", latestSubmission.getPassRate());
                    } else {
                        problemMap.put("status", "not_submitted");
                        problemMap.put("passRate", 0);
                    }
                } else {
                    problemMap.put("status", "not_submitted");
                    problemMap.put("passRate", 0);
                }
                
                problemsWithStatus.add(problemMap);
            }
            return Result.success(problemsWithStatus);
        }
        
        return Result.success(problems);
    }

    public Result getProblemDetails(Long problemId) {
        ExperimentProblem problem = experimentProblemMapper.findById(problemId);
        if (problem == null) {
            return Result.error(ResultCode.PROBLEM_NOT_FOUND);
        }
        // TODO: Should we return samples here? Maybe only public ones.
        return Result.success(problem);
    }

    public List<Experiment> getExperimentsByCourseId(Long courseId) {
        return experimentMapper.findByCourseId(courseId);
    }

    public List<Experiment> getAllExperiments() {
        return experimentMapper.findAll();
    }
    
    /**
     * 获取实验的提交统计信息
     * @param experimentId 实验ID
     * @return 包含提交人数和总人数的统计信息
     */
    public Result getExperimentSubmissionStats(Long experimentId) {
        Experiment experiment = experimentMapper.findById(experimentId);
        if (experiment == null) {
            return Result.error(ResultCode.EXPERIMENT_NOT_FOUND);
        }
        
        // TODO: 实现统计逻辑
        return Result.success(null);
    }
    
    /**
     * 删除实验
     */
    @Transactional
    public void deleteExperiment(Long experimentId) {
        // 删除实验相关的题目
        experimentProblemMapper.deleteByExperimentId(experimentId);
        // 删除实验
        experimentMapper.deleteById(experimentId);
    }
    
    /**
     * 更新实验
     */
    @Transactional
    public Result updateExperiment(Long experimentId, ExperimentDTO dto, Long userId) {
        Experiment experiment = experimentMapper.findById(experimentId);
        if (experiment == null) {
            return Result.error(ResultCode.EXPERIMENT_NOT_FOUND);
        }
        
        // 保存原来的 courseName
        String originalCourseName = experiment.getCourseName();
        
        // 如果课程ID改变了，需要更新课程名称
        if (dto.getCourseId() != null && !dto.getCourseId().equals(experiment.getCourseId())) {
            Course course = courseMapper.findById(dto.getCourseId());
            if (course == null) {
                return Result.error(ResultCode.COURSE_NOT_FOUND);
            }
            originalCourseName = course.getName();
        }
        
        // 更新基本信息
        BeanUtils.copyProperties(dto, experiment);
        experiment.setId(experimentId);
        
        // 确保 courseName 不为 null
        if (experiment.getCourseName() == null) {
            experiment.setCourseName(originalCourseName);
        }
        
        experimentMapper.update(experiment);
        
        // 删除旧的题目和样例
        List<ExperimentProblem> oldProblems = experimentProblemMapper.findByExperimentId(experimentId);
        for (ExperimentProblem oldProblem : oldProblems) {
            problemSampleMapper.deleteByProblemId(oldProblem.getId());
        }
        experimentProblemMapper.deleteByExperimentId(experimentId);
        
        // 添加新的题目和样例
        if (dto.getProblems() != null) {
            int order = 1;
            for (ExperimentProblemDTO pDto : dto.getProblems()) {
                ExperimentProblem problem = new ExperimentProblem();
                BeanUtils.copyProperties(pDto, problem);
                problem.setExperimentId(experimentId);
                problem.setCreatedBy(userId);
                if (problem.getSortOrder() == null) {
                    problem.setSortOrder(order++);
                }
                experimentProblemMapper.insert(problem);

                if (pDto.getSamples() != null) {
                    for (ProblemSampleDTO sDto : pDto.getSamples()) {
                        ProblemSample sample = new ProblemSample();
                        BeanUtils.copyProperties(sDto, sample);
                        sample.setProblemId(problem.getId());
                        problemSampleMapper.insert(sample);
                    }
                }
            }
        }
        
        return Result.success(experiment);
    }
    
    /**
     * 获取实验的提交统计信息
     */
    public Map<String, Object> getSubmissionStats(Long experimentId) {
        Map<String, Object> stats = new HashMap<>();
        
        Experiment experiment = experimentMapper.findById(experimentId);
        if (experiment == null) {
            stats.put("submittedCount", 0);
            stats.put("totalStudents", 0);
            return stats;
        }
        
        // 获取已提交人数
        int submittedCount = experimentSubmissionMapper.countByExperimentId(experimentId);
        
        // 获取课程总人数（选课学生数）
        int totalStudents = enrollmentMapper.countByCourseId(experiment.getCourseId());
        
        stats.put("submittedCount", submittedCount);
        stats.put("totalStudents", totalStudents);
        
        return stats;
    }
    
    /**
     * 获取实验的所有提交报告
     */
    public Result getExperimentSubmissions(Long experimentId) {
        Experiment experiment = experimentMapper.findById(experimentId);
        if (experiment == null) {
            return Result.error(ResultCode.EXPERIMENT_NOT_FOUND);
        }
        
        List<ExperimentSubmission> submissions = experimentSubmissionMapper.findByExperimentId(experimentId);
        return Result.success(submissions);
    }
    
    /**
     * 检查学生是否已提交实验报告
     */
    public boolean hasStudentSubmitted(Long experimentId, Long studentId) {
        ExperimentSubmission submission = experimentSubmissionMapper.findByExperimentIdAndStudentId(experimentId, studentId);
        return submission != null;
    }
    
    /**
     * 学生提交实验报告
     */
    @Transactional
    public Result submitReport(Long experimentId, Long studentId, String content, List<Map<String, Object>> fileList) {
        try {
            System.out.println("=== 提交实验报告 ===");
            System.out.println("实验ID: " + experimentId);
            System.out.println("学生ID: " + studentId);
            System.out.println("内容: " + content);
            
            // 验证实验是否存在
            Experiment experiment = experimentMapper.findById(experimentId);
            if (experiment == null) {
                return Result.error(ResultCode.EXPERIMENT_NOT_FOUND);
            }
            
            // 获取学生信息
            User student = userMapper.findById(studentId);
            if (student == null) {
                return Result.error("学生不存在");
            }
            
            // 检查是否已经提交过
            ExperimentSubmission existing = experimentSubmissionMapper.findByExperimentIdAndStudentId(experimentId, studentId);
            
            LocalDateTime now = LocalDateTime.now();
            System.out.println("当前时间: " + now);
            
            ExperimentSubmission submission;
            if (existing != null) {
                // 更新已有提交
                System.out.println("更新已有提交，ID: " + existing.getId());
                submission = existing;
                submission.setContent(content);
                if (fileList != null) {
                    submission.setFiles(objectMapper.writeValueAsString(fileList));
                }
                submission.setSubmitTime(now);
                submission.setStatus("submitted");
                experimentSubmissionMapper.update(submission);
            } else {
                // 创建新提交
                System.out.println("创建新提交");
                submission = new ExperimentSubmission();
                submission.setExperimentId(experimentId);
                submission.setStudentId(studentId);
                submission.setStudentName(student.getRealName());
                submission.setStudentNumber(student.getUsername());
                submission.setContent(content);
                if (fileList != null) {
                    submission.setFiles(objectMapper.writeValueAsString(fileList));
                }
                submission.setSubmitTime(now);
                submission.setStatus("submitted");
                experimentSubmissionMapper.insert(submission);
                System.out.println("插入后的ID: " + submission.getId());
            }
            
            System.out.println("提交时间: " + submission.getSubmitTime());
            return Result.success("提交成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("提交失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取实验的学生完成情况统计
     */
    public Result getStudentStats(Long experimentId) {
        try {
            // 获取实验详情和题目
            Experiment experiment = experimentMapper.findById(experimentId);
            if (experiment == null) {
                return Result.error(ResultCode.EXPERIMENT_NOT_FOUND);
            }
            
            System.out.println("=== 获取学生完成情况 ===");
            System.out.println("实验ID: " + experimentId);
            System.out.println("课程ID: " + experiment.getCourseId());
            
            List<ExperimentProblem> problems = experimentProblemMapper.findByExperimentId(experimentId);
            System.out.println("题目数量: " + problems.size());
            
            // 获取课程的所有学生（包含studentId字段）
            List<User> enrolledStudents = enrollmentMapper.findStudentsByCourseId(experiment.getCourseId());
            System.out.println("选课学生数量: " + enrolledStudents.size());
            
            // 获取所有提交记录
            List<ExperimentProblemSubmission> submissions = experimentProblemSubmissionMapper.findByExperimentId(experimentId);
            System.out.println("提交记录数量: " + submissions.size());
            
            // 合并学生列表：使用学生ID作为key
            Map<Long, User> studentMap = new java.util.LinkedHashMap<>();
            
            // 1. 先添加选课学生（用studentId作为key）
            for (User student : enrolledStudents) {
                if (student.getStudentId() != null) {
                    studentMap.put(student.getStudentId(), student);
                    System.out.println("添加选课学生: " + student.getRealName() + " (学生ID: " + student.getStudentId() + ", 用户ID: " + student.getId() + ")");
                }
            }
            
            // 2. 从提交记录中获取学生ID
            Set<Long> submittedStudentIds = submissions.stream()
                .map(ExperimentProblemSubmission::getStudentId)
                .collect(java.util.stream.Collectors.toSet());
            
            // 3. 添加提交过但不在选课列表中的学生
            for (Long studentId : submittedStudentIds) {
                if (!studentMap.containsKey(studentId)) {
                    // 通过学生ID反查用户ID
                    Long userId = userMapper.getUserIdByStudentId(studentId);
                    if (userId != null) {
                        User student = userMapper.findById(userId);
                        if (student != null) {
                            student.setStudentId(studentId);  // 设置学生ID
                            studentMap.put(studentId, student);
                            System.out.println("添加有提交记录但未选课的学生: " + student.getRealName() + " (学生ID: " + studentId + ", 用户ID: " + userId + ")");
                        }
                    }
                }
            }
            
            List<User> students = new ArrayList<>(studentMap.values());
            System.out.println("总学生数量: " + students.size());
            
            // 构建学生统计数据
            List<Map<String, Object>> studentStats = new ArrayList<>();
            for (User student : students) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("studentId", student.getStudentId());  // 返回学生ID
                stat.put("studentName", student.getRealName());
                
                // 构建题目完成情况
                Map<Long, Map<String, Object>> problemStatus = new HashMap<>();
                int passedCount = 0;
                
                for (ExperimentProblem problem : problems) {
                    // 使用学生ID查找最新提交
                    ExperimentProblemSubmission latestSubmission = experimentProblemSubmissionMapper
                        .findLatestByProblemAndStudent(problem.getId(), student.getStudentId());
                    
                    Map<String, Object> status = new HashMap<>();
                    if (latestSubmission != null) {
                        status.put("status", latestSubmission.getStatus());
                        status.put("passRate", latestSubmission.getPassRate());
                        status.put("attempts", submissions.stream()
                            .filter(s -> s.getProblemId().equals(problem.getId()) && s.getStudentId().equals(student.getStudentId()))
                            .count());
                        
                        if ("passed".equals(latestSubmission.getStatus())) {
                            passedCount++;
                        }
                    } else {
                        status.put("status", "not_submitted");
                        status.put("passRate", 0);
                        status.put("attempts", 0);
                    }
                    
                    problemStatus.put(problem.getId(), status);
                }
                
                stat.put("problems", problemStatus);
                
                // 计算完成度
                int completionRate = problems.size() > 0 ? (passedCount * 100 / problems.size()) : 0;
                stat.put("completionRate", completionRate);
                
                studentStats.add(stat);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("problems", problems);
            result.put("studentStats", studentStats);
            
            System.out.println("返回数据: problems数量=" + problems.size() + ", studentStats数量=" + studentStats.size());
            
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }
}