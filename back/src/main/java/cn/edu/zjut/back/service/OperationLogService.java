package cn.edu.zjut.back.service;

import cn.edu.zjut.back.dto.OperationLogQueryDTO;
import cn.edu.zjut.back.entity.OperationLog;
import cn.edu.zjut.back.mapper.OperationLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作日志Service
 */
@Service
public class OperationLogService {

    @Autowired
    private OperationLogMapper logMapper;

    /**
     * 分页查询操作日志
     */
    public Map<String, Object> queryLogs(OperationLogQueryDTO query) {
        List<OperationLog> logs = logMapper.queryLogs(query);
        int total = logMapper.countLogs(query);

        Map<String, Object> result = new HashMap<>();
        result.put("list", logs);
        result.put("total", total);
        result.put("page", query.getPage());
        result.put("size", query.getSize());
        return result;
    }

    /**
     * 根据ID查询日志详情
     */
    public OperationLog getLogById(Long id) {
        return logMapper.findById(id);
    }

    /**
     * 记录操作日志
     */
    @Transactional
    public void saveLog(OperationLog log) {
        logMapper.insert(log);
    }

    /**
     * 批量删除日志
     */
    @Transactional
    public void batchDeleteLogs(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            logMapper.batchDelete(ids);
        }
    }

    /**
     * 清空所有日志
     */
    @Transactional
    public void clearAllLogs() {
        logMapper.clearAll();
    }

    /**
     * 获取操作类型统计
     */
    public List<Map<String, Object>> getOperationStats() {
        return logMapper.getOperationStats();
    }

    /**
     * 获取用户操作统计
     */
    public List<Map<String, Object>> getUserStats() {
        return logMapper.getUserStats();
    }
}
