package cn.edu.zjut.back.controller;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.entity.LiveAnalysisReport;
import cn.edu.zjut.back.service.LiveAnalysisService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/live-analysis")
@CrossOrigin
public class LiveAnalysisController {

    private final LiveAnalysisService liveAnalysisService;

    public LiveAnalysisController(LiveAnalysisService liveAnalysisService) {
        this.liveAnalysisService = liveAnalysisService;
    }

    @GetMapping("/report/{liveStreamId}")
    public Result<LiveAnalysisReport> getReport(@PathVariable Long liveStreamId) {
        try {
            LiveAnalysisReport report = liveAnalysisService.getReport(liveStreamId);
            if (report == null) {
                return Result.success("暂未生成分析报告", null);
            }
            return Result.success(report);
        } catch (Exception e) {
            return Result.error("获取分析报告失败: " + e.getMessage());
        }
    }

    @PostMapping("/report/{liveStreamId}")
    public Result<LiveAnalysisReport> generateReport(@PathVariable Long liveStreamId) {
        try {
            LiveAnalysisReport report = liveAnalysisService.generateReport(liveStreamId);
            return Result.success(report);
        } catch (Exception e) {
            return Result.error("生成分析报告失败: " + e.getMessage());
        }
    }
}
