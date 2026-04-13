package cn.edu.zjut.back.controller;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.service.CodeExecutionService;
import cn.edu.zjut.back.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/code")
public class CodeExecutionController {

    private final CodeExecutionService codeExecutionService;
    private final JwtUtil jwtUtil;

    public CodeExecutionController(CodeExecutionService codeExecutionService, JwtUtil jwtUtil) {
        this.codeExecutionService = codeExecutionService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/submit")
    public Result submitCode(@RequestBody Map<String, Object> submitData, HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        Long experimentId = Long.valueOf(submitData.get("experimentId").toString());
        Long problemId = Long.valueOf(submitData.get("problemId").toString());
        String language = (String) submitData.get("language");
        String code = (String) submitData.get("code");
        
        // 传递userId，由service层转换为studentId
        return codeExecutionService.submitCode(experimentId, problemId, userId, language, code);
    }

    @GetMapping("/history/{problemId}")
    public Result getSubmissionHistory(@PathVariable Long problemId, HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        return codeExecutionService.getSubmissionHistory(problemId, userId);
    }
}
