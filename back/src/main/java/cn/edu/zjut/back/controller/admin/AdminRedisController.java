package cn.edu.zjut.back.controller.admin;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 内存缓存管理控制器
 */
@RestController
@RequestMapping("/api/admin/cache")
@CrossOrigin
public class AdminRedisController {

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 获取缓存信息
     */
    @GetMapping("/info")
    public Result getRedisInfo(@RequestParam(required = false) String section) {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("mode", "in-memory");
            result.put("section", section);
            result.put("totalKeys", redisUtil.keys("*").size());
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取缓存信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取缓存统计信息（用于前端可视化）
     */
    @GetMapping("/stats")
    public Result getRedisStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("mode", "in-memory");
            stats.put("totalKeys", redisUtil.keys("*").size());
            stats.put("sampleKeys", redisUtil.keys("*").stream().limit(20).toList());
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取缓存统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取键空间信息
     */
    @GetMapping("/keyspace")
    public Result getKeyspaceInfo() {
        try {
            Map<String, Object> keyspaceInfo = new HashMap<>();
            keyspaceInfo.put("mode", "in-memory");
            keyspaceInfo.put("totalKeys", redisUtil.keys("*").size());
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
            return Result.success(Map.of("ping", "PONG", "mode", "in-memory"));
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
            Set<String> keys = redisUtil.keys(pattern);
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
            redisUtil.clear();
            return Result.success("清空数据库成功");
        } catch (Exception e) {
            return Result.error("清空缓存失败: " + e.getMessage());
        }
    }
}
