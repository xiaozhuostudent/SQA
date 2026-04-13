package cn.edu.zjut.back.service;

import cn.edu.zjut.back.entity.LearningProgress;
import cn.edu.zjut.back.mapper.LearningProgressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学习进度服务类
 */
@Service
public class LearningProgressService {
    
    @Autowired
    private LearningProgressMapper progressMapper;
    
    /**
     * 更新学习进度
     */
    public void updateProgress(Long studentId, Long courseId, Long resourceId, 
                                String resourceType, Integer progressPercent, 
                                Integer durationSeconds, String lastPosition) {
        LearningProgress progress = progressMapper.findByStudentAndResource(studentId, resourceId);
        
        if (progress == null) {
            // 创建新记录
            progress = new LearningProgress();
            progress.setStudentId(studentId);
            progress.setCourseId(courseId);
            progress.setResourceId(resourceId);
            progress.setResourceType(resourceType);
            progress.setProgressPercent(progressPercent);
            progress.setDurationSeconds(durationSeconds);
            progress.setLastPosition(lastPosition);
            progress.setIsCompleted(progressPercent >= 100);
            if (progress.getIsCompleted()) {
                progress.setCompleteTime(LocalDateTime.now());
            }
            progressMapper.insert(progress);
        } else {
            // 更新现有记录
            progress.setProgressPercent(progressPercent);
            progress.setDurationSeconds(progress.getDurationSeconds() + durationSeconds);
            progress.setLastPosition(lastPosition);
            progress.setIsCompleted(progressPercent >= 100);
            if (progress.getIsCompleted() && progress.getCompleteTime() == null) {
                progress.setCompleteTime(LocalDateTime.now());
            }
            progressMapper.update(progress);
        }
    }
    
    /**
     * 获取学生课程进度
     */
    public List<LearningProgress> getStudentCourseProgress(Long studentId, Long courseId) {
        return progressMapper.findByStudentAndCourse(studentId, courseId);
    }
    
    /**
     * 获取资源进度
     */
    public LearningProgress getResourceProgress(Long studentId, Long resourceId) {
        return progressMapper.findByStudentAndResource(studentId, resourceId);
    }
}
