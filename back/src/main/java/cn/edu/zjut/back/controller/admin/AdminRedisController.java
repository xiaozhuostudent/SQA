package cn.edu.zjut.back.controller.admin;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Redis 监控管理控制器
 */
@RestController
@RequestMapping("/api/admin/redis")
@CrossOrigin
public class AdminRedisController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 获取 Redis 服务器信息
     */
    @GetMapping("/info")
    public Result getRedisInfo(@RequestParam(required = false) String section) {
        try {
            Properties info = redisTemplate.execute((RedisCallback<Properties>) connection ->
                    connection.serverCommands().info(section));
            
            Map<String, Object> result = new HashMap<>();
            if (info != null) {
                info.forEach((key, value) -> result.put(key.toString(), value));
            }
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取 Redis 信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取 Redis 统计信息（用于前端可视化）
     */
    @GetMapping("/stats")
    public Result getRedisStats() {
        try {
            Properties info = redisTemplate.execute((RedisCallback<Properties>) connection ->
                    connection.serverCommands().info());

            Map<String, Object> stats = new HashMap<>();
            
            if (info != null) {
                // 基本信息
                stats.put("redis_version", info.getProperty("redis_version"));
                stats.put("redis_mode", info.getProperty("redis_mode"));
                stats.put("os", info.getProperty("os"));
                stats.put("uptime_in_seconds", info.getProperty("uptime_in_seconds"));
                stats.put("uptime_in_days", info.getProperty("uptime_in_days"));
                
                // 客户端信息
                stats.put("connected_clients", info.getProperty("connected_clients"));
                stats.put("blocked_clients", info.getProperty("blocked_clients"));
                
                // 内存信息
                stats.put("used_memory", info.getProperty("used_memory"));
                stats.put("used_memory_human", info.getProperty("used_memory_human"));
                stats.put("used_memory_rss", info.getProperty("used_memory_rss"));
                stats.put("used_memory_rss_human", info.getProperty("used_memory_rss_human"));
                stats.put("used_memory_peak", info.getProperty("used_memory_peak"));
                stats.put("used_memory_peak_human", info.getProperty("used_memory_peak_human"));
                stats.put("mem_fragmentation_ratio", info.getProperty("mem_fragmentation_ratio"));
                
                // 统计信息
                stats.put("total_connections_received", info.getProperty("total_connections_received"));
                stats.put("total_commands_processed", info.getProperty("total_commands_processed"));
                stats.put("instantaneous_ops_per_sec", info.getProperty("instantaneous_ops_per_sec"));
                stats.put("total_net_input_bytes", info.getProperty("total_net_input_bytes"));
                stats.put("total_net_output_bytes", info.getProperty("total_net_output_bytes"));
                
                // 键空间信息
                stats.put("db0", info.getProperty("db0"));
                
                // 持久化信息
                stats.put("rdb_changes_since_last_save", info.getProperty("rdb_changes_since_last_save"));
                stats.put("rdb_last_save_time", info.getProperty("rdb_last_save_time"));
                stats.put("rdb_last_bgsave_status", info.getProperty("rdb_last_bgsave_status"));
                stats.put("aof_enabled", info.getProperty("aof_enabled"));
                stats.put("aof_last_rewrite_time_sec", info.getProperty("aof_last_rewrite_time_sec"));
            }
            
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取 Redis 统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取键空间信息
     */
    @GetMapping("/keyspace")
    public Result getKeyspaceInfo() {
        try {
            Map<String, Object> keyspaceInfo = new HashMap<>();
            
            Properties info = redisTemplate.execute((RedisCallback<Properties>) connection ->
                    connection.serverCommands().info("keyspace"));
            
            if (info != null) {
                info.forEach((key, value) -> {
                    if (key.toString().startsWith("db")) {
                        keyspaceInfo.put(key.toString(), value);
                    }
                });
            }
            
            return Result.success(keyspaceInfo);
        } catch (Exception e) {
            return Result.error("获取键空间信息失败: " + e.getMessage());
        }
    }

    /**
     * PING 测试
     */
    @GetMapping("/ping")
    public Result ping() {
        try {
            // 简单的 ping 测试，通过获取键总数判断连接
            Long keyCount = redisTemplate.execute((RedisCallback<Long>) connection ->
                    connection.dbSize());
            return Result.success(Map.of("ping", "PONG", "dbSize", keyCount));
        } catch (Exception e) {
            return Result.error("PING 失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定模式的键列表
     */
    @GetMapping("/keys")
    public Result getKeys(@RequestParam(defaultValue = "*") String pattern,
                          @RequestParam(defaultValue = "100") int limit) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            List<String> keyList = new ArrayList<>();
            
            if (keys != null) {
                int count = 0;
                for (String key : keys) {
                    if (count >= limit) break;
                    keyList.add(key);
                    count++;
                }
            }
            
            return Result.success(Map.of(
                    "keys", keyList,
                    "total", keys != null ? keys.size() : 0,
                    "displayed", keyList.size()
            ));
        } catch (Exception e) {
            return Result.error("获取键列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取键的值
     */
    @GetMapping("/get/{key}")
    public Result getKey(@PathVariable String key) {
        try {
            if (!redisUtil.hasKey(key)) {
                return Result.error("键不存在");
            }
            
            Object value = redisUtil.get(key);
            long ttl = redisUtil.getExpire(key);
            
            return Result.success(Map.of(
                    "key", key,
                    "value", value,
                    "ttl", ttl
            ));
        } catch (Exception e) {
            return Result.error("获取键值失败: " + e.getMessage());
        }
    }

    /**
     * 删除键
     */
    @DeleteMapping("/{key}")
    public Result deleteKey(@PathVariable String key) {
        try {
            if (!redisUtil.hasKey(key)) {
                return Result.error("键不存在");
            }
            redisUtil.delete(key);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除键失败: " + e.getMessage());
        }
    }

    /**
     * 设置键值
     */
    @PostMapping("/set")
    public Result setKey(@RequestBody Map<String, Object> data) {
        try {
            String key = (String) data.get("key");
            Object value = data.get("value");
            Integer ttl = (Integer) data.get("ttl");
            
            if (key == null || value == null) {
                return Result.error("键和值不能为空");
            }
            
            if (ttl != null && ttl > 0) {
                redisUtil.set(key, value, ttl);
            } else {
                redisUtil.set(key, value);
            }
            
            return Result.success("设置成功");
        } catch (Exception e) {
            return Result.error("设置键值失败: " + e.getMessage());
        }
    }

    /**
     * 清空当前数据库
     */
    @DeleteMapping("/flushdb")
    public Result flushDb() {
        try {
            redisTemplate.execute((RedisCallback<String>) connection -> {
                connection.serverCommands().flushDb();
                return "OK";
            });
            return Result.success("清空数据库成功");
        } catch (Exception e) {
            return Result.error("清空数据库失败: " + e.getMessage());
        }
    }
}
