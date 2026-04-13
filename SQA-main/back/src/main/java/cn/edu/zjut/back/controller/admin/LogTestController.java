package cn.edu.zjut.back.controller.admin;

import cn.edu.zjut.back.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 日志测试控制器 - 用于测试日志功能
 */
@RestController
@RequestMapping("/api/admin/log-test")
@CrossOrigin
public class LogTestController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 测试直接插入日志
     */
    @GetMapping("/insert")
    public Result testInsert() {
        try {
            String sql = "INSERT INTO tb_admin_operation_log " +
                        "(admin_id, admin_name, operation, module, request_url, ip_address) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
            
            int rows = jdbcTemplate.update(sql,
                1,
                "测试管理员",
                "测试插入",
                "test",
                "/api/admin/log-test/insert",
                "127.0.0.1"
            );
            
            return Result.success("插入成功，影响行数: " + rows);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("插入失败: " + e.getMessage());
        }
    }

    /**
     * 查询最新日志
     */
    @GetMapping("/latest")
    public Result getLatest() {
        try {
            String sql = "SELECT * FROM tb_admin_operation_log ORDER BY id DESC LIMIT 10";
            List<Map<String, Object>> logs = jdbcTemplate.queryForList(sql);
            return Result.success(logs);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取总记录数
     */
    @GetMapping("/count")
    public Result getCount() {
        try {
            String sql = "SELECT COUNT(*) FROM tb_admin_operation_log";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            return Result.success(Map.of("count", count));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}
