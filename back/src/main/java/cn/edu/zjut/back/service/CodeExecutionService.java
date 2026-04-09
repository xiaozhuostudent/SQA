package cn.edu.zjut.back.service;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.entity.Experiment;
import cn.edu.zjut.back.entity.ExperimentProblem;
import cn.edu.zjut.back.entity.ExperimentProblemSubmission;
import cn.edu.zjut.back.entity.User;
import cn.edu.zjut.back.mapper.ExperimentMapper;
import cn.edu.zjut.back.mapper.ExperimentProblemMapper;
import cn.edu.zjut.back.mapper.ExperimentProblemSubmissionMapper;
import cn.edu.zjut.back.mapper.ProblemSampleMapper;
import cn.edu.zjut.back.mapper.UserMapper;
import cn.edu.zjut.back.entity.ProblemSample;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CodeExecutionService {

    private final ExperimentProblemSubmissionMapper submissionMapper;
    private final ExperimentProblemMapper problemMapper;
    private final ExperimentMapper experimentMapper;
    private final UserMapper userMapper;
    private final ProblemSampleMapper problemSampleMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String pistonExecuteApi;

    public CodeExecutionService(
            ExperimentProblemSubmissionMapper submissionMapper,
            ExperimentProblemMapper problemMapper,
            ExperimentMapper experimentMapper,
            UserMapper userMapper,
            ProblemSampleMapper problemSampleMapper,
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${piston.api.url:http://localhost:5053/api/v2/piston}") String pistonApiBase) {
        this.submissionMapper = submissionMapper;
        this.problemMapper = problemMapper;
        this.experimentMapper = experimentMapper;
        this.userMapper = userMapper;
        this.problemSampleMapper = problemSampleMapper;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.pistonExecuteApi = normalizePistonExecuteApi(pistonApiBase);
    }

    private String normalizePistonExecuteApi(String pistonApiBase) {
        String base = pistonApiBase == null ? "" : pistonApiBase.trim();
        if (base.endsWith("/execute")) {
            return base;
        }
        if (base.endsWith("/")) {
            return base + "execute";
        }
        return base + "/execute";
    }

    /**
     * 提交代码并执行判题
     * @param userId 用户ID（将被转换为学生ID）
     */
    public Result submitCode(Long experimentId, Long problemId, Long userId, String language, String code) {
        try {
            // 获取实验信息以验证语言限制
            Experiment experiment = experimentMapper.findById(experimentId);
            if (experiment == null) {
                return Result.error("实验不存在");
            }
            
            // 验证语言是否允许
            if (experiment.getAllowedLanguages() != null && !experiment.getAllowedLanguages().isEmpty()) {
                String[] allowedLanguages = experiment.getAllowedLanguages().split(",");
                boolean languageAllowed = false;
                for (String allowedLang : allowedLanguages) {
                    if (allowedLang.trim().equalsIgnoreCase(language)) {
                        languageAllowed = true;
                        break;
                    }
                }
                if (!languageAllowed) {
                    return Result.error("该实验不允许使用 " + language + " 语言，允许的语言: " + experiment.getAllowedLanguages());
                }
            }
            
            // 获取题目信息
            ExperimentProblem problem = problemMapper.findById(problemId);
            if (problem == null) {
                return Result.error("题目不存在");
            }

            // 获取学生信息
            User student = userMapper.findById(userId);
            if (student == null) {
                return Result.error("用户不存在");
            }
            
            // 将用户ID转换为学生ID
            Long studentId = userMapper.getStudentIdByUserId(userId);
            if (studentId == null) {
                return Result.error("该用户不是学生");
            }

            // 创建提交记录
            ExperimentProblemSubmission submission = new ExperimentProblemSubmission();
            submission.setExperimentId(experimentId);
            submission.setProblemId(problemId);
            submission.setStudentId(studentId);  // 使用学生ID而非用户ID
            submission.setStudentName(student.getRealName() != null ? student.getRealName() : student.getUsername());
            submission.setCode(code);
            submission.setLanguage(language);
            submission.setStatus("running");
            submission.setSubmitTime(LocalDateTime.now());

            // 执行代码
            Map<String, Object> executionResult = executeCode(code, language);
            
            // 解析测试样例：优先从样例表读取，兼容旧的 JSON 字段兜底
            List<Map<String, String>> samples = loadSamples(problem);
            
            // 运行测试
            int passedCount = 0;
            StringBuilder output = new StringBuilder();
            long startTime = System.currentTimeMillis();
            
            if (samples != null && !samples.isEmpty()) {
                for (int i = 0; i < samples.size(); i++) {
                    Map<String, String> sample = samples.get(i);
                    String input = sample.get("input");
                    String expectedOutput = sample.get("output");
                    
                    // 执行代码获取输出
                    Map<String, Object> testResult = executeCodeWithInput(code, language, input);
                    String actualOutput = (String) testResult.get("output");
                    
                    boolean passed = compareOutput(actualOutput, expectedOutput);
                    if (passed) {
                        passedCount++;
                    }
                    
                    output.append("测试样例 ").append(i + 1).append(": ")
                          .append(passed ? "通过" : "失败").append("\n");
                }
            } else {
                // 没有测试样例，直接执行代码
                String codeOutput = (String) executionResult.get("output");
                output.append("代码执行结果:\n").append(codeOutput);
                passedCount = 1; // 假设通过
            }

            long executeTime = System.currentTimeMillis() - startTime;

            // 计算通过率
            int totalSamples = samples != null && !samples.isEmpty() ? samples.size() : 1;
            int passRate = (passedCount * 100) / totalSamples;
            
            // 确定状态
            String status;
            String result;
            if (executionResult.containsKey("error") && executionResult.get("error") != null 
                && !((String)executionResult.get("error")).trim().isEmpty()) {
                status = "error";
                result = "RE"; // Runtime Error
                submission.setErrorMessage((String) executionResult.get("error"));
            } else if (passRate == 100) {
                status = "passed";
                result = "AC"; // Accepted
            } else if (passRate > 0) {
                status = "failed";
                result = "WA"; // Wrong Answer (部分通过)
            } else {
                status = "failed";
                result = "WA"; // Wrong Answer
            }

            submission.setStatus(status);
            submission.setPassRate(passRate);
            submission.setOutput(output.toString());
            submission.setExecuteTime((int)executeTime);
            
            // 保存到数据库
            submissionMapper.insert(submission);

            // 返回结果
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("submissionId", submission.getId());
            resultData.put("result", result);
            resultData.put("passRate", passRate);
            resultData.put("output", output.toString());
            resultData.put("runTimeMs", executeTime);
            resultData.put("submitTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            // 添加详细的错误信息
            if (executionResult.containsKey("error") && executionResult.get("error") != null 
                && !((String)executionResult.get("error")).trim().isEmpty()) {
                resultData.put("errorMessage", executionResult.get("error"));
            }
            
            // 添加测试用例详细结果
            if (samples != null && !samples.isEmpty()) {
                resultData.put("totalTests", samples.size());
                resultData.put("passedTests", passedCount);
                String passRateText = String.format("通过 %d/%d 个测试用例 (%d%%)", 
                                                   passedCount, samples.size(), passRate);
                resultData.put("passRateText", passRateText);
            } else {
                resultData.put("passRateText", "代码执行完成");
            }
            
            // 添加结果说明
            String resultDescription = "";
            if ("RE".equals(result)) {
                resultDescription = "代码运行时发生错误，请检查代码逻辑和语法";
            } else if ("AC".equals(result)) {
                resultDescription = "恭喜！所有测试用例通过";
            } else if ("WA".equals(result)) {
                resultDescription = "部分测试用例未通过，请检查代码逻辑";
            }
            resultData.put("resultDescription", resultDescription);

            return Result.success(resultData);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("代码执行失败: " + e.getMessage());
        }
    }

    /**
     * 获取提交历史
     * @param userId 用户ID（将被转换为学生ID）
     */
    public Result getSubmissionHistory(Long problemId, Long userId) {
        try {
            // 将用户ID转换为学生ID
            Long studentId = userMapper.getStudentIdByUserId(userId);
            if (studentId == null) {
                return Result.error("该用户不是学生");
            }
            
            // 查询该学生对该题目的所有提交记录
            List<ExperimentProblemSubmission> submissions = submissionMapper
                    .findByProblemIdAndStudentId(problemId, studentId);
            
            List<Map<String, Object>> history = new ArrayList<>();
            for (ExperimentProblemSubmission sub : submissions) {
                Map<String, Object> item = new HashMap<>();
                
                // 转换状态为显示文本
                String result = "PENDING";
                if ("passed".equals(sub.getStatus())) {
                    result = "AC";
                } else if ("failed".equals(sub.getStatus())) {
                    result = "WA";
                } else if ("error".equals(sub.getStatus())) {
                    result = "RE";
                }
                
                item.put("result", result);
                item.put("runTimeMs", sub.getExecuteTime());
                item.put("submitTime", sub.getSubmitTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                item.put("passRate", sub.getPassRate());
                item.put("language", sub.getLanguage());
                
                // 添加详细说明
                String description = "";
                if ("AC".equals(result)) {
                    description = "通过率: " + sub.getPassRate() + "%";
                } else if ("WA".equals(result)) {
                    description = "通过率: " + sub.getPassRate() + "%";
                } else if ("RE".equals(result)) {
                    description = "运行时错误";
                    if (sub.getErrorMessage() != null && !sub.getErrorMessage().trim().isEmpty()) {
                        // 只显示错误信息的前50个字符
                        String errorMsg = sub.getErrorMessage().trim();
                        if (errorMsg.length() > 50) {
                            errorMsg = errorMsg.substring(0, 50) + "...";
                        }
                        description += ": " + errorMsg;
                    }
                }
                item.put("description", description);
                
                history.add(item);
            }
            
            return Result.success(history);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取历史记录失败: " + e.getMessage());
        }
    }

    /**
     * 执行代码（使用Piston API）
     */
    private Map<String, Object> executeCode(String code, String language) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("language", mapLanguage(language));
            requestBody.put("version", "*");
            requestBody.put("files", Collections.singletonList(
                    Map.of("content", code)
            ));

            String response = restTemplate.postForObject(pistonExecuteApi, requestBody, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);

            Map<String, Object> result = new HashMap<>();
            result.put("output", jsonNode.path("run").path("stdout").asText());
            result.put("error", jsonNode.path("run").path("stderr").asText());
            result.put("runTime", jsonNode.path("run").path("runtime").asInt());

            return result;
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "代码执行服务异常: " + e.getMessage());
            return errorResult;
        }
    }

    /**
     * 执行代码并传入输入
     */
    private Map<String, Object> executeCodeWithInput(String code, String language, String input) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("language", mapLanguage(language));
            requestBody.put("version", "*");
            requestBody.put("files", Collections.singletonList(
                    Map.of("content", code)
            ));
            requestBody.put("stdin", input);

            String response = restTemplate.postForObject(pistonExecuteApi, requestBody, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);

            Map<String, Object> result = new HashMap<>();
            result.put("output", jsonNode.path("run").path("stdout").asText());
            result.put("error", jsonNode.path("run").path("stderr").asText());

            return result;
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "执行失败");
            return errorResult;
        }
    }

    /**
     * 映射语言名称
     */
    private String mapLanguage(String language) {
        Map<String, String> langMap = new HashMap<>();
        langMap.put("cpp", "c++");
        langMap.put("c", "c");
        langMap.put("java", "java");
        langMap.put("python", "python");
        langMap.put("javascript", "javascript");
        langMap.put("typescript", "typescript");
        langMap.put("go", "go");
        langMap.put("rust", "rust");
        
        return langMap.getOrDefault(language, language);
    }

    /**
     * 解析测试样例
     */
    private List<Map<String, String>> parseSamples(String samplesJson) {
        try {
            if (samplesJson == null || samplesJson.trim().isEmpty()) {
                return new ArrayList<>();
            }
            
            JsonNode jsonNode = objectMapper.readTree(samplesJson);
            List<Map<String, String>> samples = new ArrayList<>();
            
            if (jsonNode.isArray()) {
                for (JsonNode node : jsonNode) {
                    Map<String, String> sample = new HashMap<>();
                    sample.put("input", node.path("input").asText());
                    sample.put("output", node.path("output").asText());
                    samples.add(sample);
                }
            }
            
            return samples;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<Map<String, String>> loadSamples(ExperimentProblem problem) {
        List<Map<String, String>> samples = new ArrayList<>();
        if (problem == null || problem.getId() == null) {
            return samples;
        }

        try {
            List<ProblemSample> sampleEntities = problemSampleMapper.findByProblemId(problem.getId());
            if (sampleEntities != null) {
                for (ProblemSample sampleEntity : sampleEntities) {
                    Map<String, String> sample = new HashMap<>();
                    sample.put("input", sampleEntity.getInput() == null ? "" : sampleEntity.getInput());
                    sample.put("output", sampleEntity.getOutput() == null ? "" : sampleEntity.getOutput());
                    samples.add(sample);
                }
            }
        } catch (Exception e) {
            // 兼容旧逻辑：样例表读取失败时继续尝试 JSON 字段
        }

        if (samples.isEmpty()) {
            samples = parseSamples(problem.getSamples());
        }

        return samples;
    }

    /**
     * 比较输出
     */
    private boolean compareOutput(String actual, String expected) {
        if (actual == null || expected == null) {
            return false;
        }
        
        // 去除前后空白并比较
        String normalizedActual = actual.trim().replaceAll("\\s+", " ");
        String normalizedExpected = expected.trim().replaceAll("\\s+", " ");
        
        return normalizedActual.equals(normalizedExpected);
    }
}
