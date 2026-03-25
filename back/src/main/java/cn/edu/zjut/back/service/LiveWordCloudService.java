package cn.edu.zjut.back.service;

import cn.edu.zjut.back.entity.LiveChatMessage;
import cn.edu.zjut.back.entity.LiveWordCloud;
import cn.edu.zjut.back.mapper.LiveWordCloudMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 直播词云服务 - 使用Jieba分词提取关键词
 */
@Service
public class LiveWordCloudService {
    
    @Autowired
    private LiveWordCloudMapper liveWordCloudMapper;
    
    @Autowired
    private LiveChatMessageService liveChatMessageService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JiebaSegmenter segmenter = new JiebaSegmenter();
    private Set<String> stopWords = new HashSet<>();
    
    public LiveWordCloudService() {
        // 加载停用词
        loadStopWords();
    }
    
    /**
     * 加载停用词表
     */
    private void loadStopWords() {
        try {
            ClassPathResource resource = new ClassPathResource("stopwords.txt");
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
            );
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    stopWords.add(line);
                }
            }
            reader.close();
            System.out.println("停用词加载成功，共 " + stopWords.size() + " 个");
        } catch (Exception e) {
            System.err.println("停用词加载失败: " + e.getMessage());
            // 添加默认停用词
            stopWords.addAll(Arrays.asList("的", "了", "和", "是", "在", "有", "我", "你", "他", 
                "她", "它", "们", "这", "那", "哪", "什么", "怎么", "吗", "呢", "吧", "啊"));
        }
    }
    /**
     * 生成词云数据 - 使用Jieba分词提取关键词
     */
    public LiveWordCloud generateWordCloud(Long liveStreamId) {
        // 获取所有聊天消息
        List<LiveChatMessage> messages = liveChatMessageService.getMessagesByLiveStreamId(liveStreamId);
        
        if (messages.isEmpty()) {
            return null;
        }
        
        // 统计词频 - 使用Jieba分词
        Map<String, Integer> wordFrequency = new HashMap<>();
        int totalWords = 0;
        
        for (LiveChatMessage message : messages) {
            String content = message.getContent();
            if (content == null || content.trim().isEmpty()) {
                continue;
            }
            
            // 使用Jieba进行分词
            List<SegToken> tokens = segmenter.process(content, JiebaSegmenter.SegMode.SEARCH);
            
            for (SegToken token : tokens) {
                String word = token.word
                        .replaceAll("[\\s]+", "")               // 去掉空白
                        .replaceAll("[^\\p{IsHan}A-Za-z0-9]", "") // 去掉符号
                        .trim();
                if (word.isEmpty()) {
                    continue;
                }
                // 英文统一转小写，避免大小写重复
                if (word.matches("[A-Za-z0-9]+")) {
                    word = word.toLowerCase(Locale.ROOT);
                }
                
                // 过滤条件：
                // 1. 长度2-8个字符，避免整句
                // 2. 不在停用词表中
                // 3. 不是纯数字
                // 4. 不是纯标点符号
                if (word.length() >= 2 
                    && word.length() <= 8
                    && !stopWords.contains(word) 
                    && !word.matches("\\d+")
                    && !word.matches("\\p{Punct}+")) {
                    
                    wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
                    totalWords++;
                }
            }
        }
        
        // 如果没有有效词语，返回null
        if (wordFrequency.isEmpty()) {
            return null;
        }
        
        // 按频率排序，取前10个高频词
        List<Map<String, Object>> wordList = wordFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(10)
                .map(entry -> {
                    Map<String, Object> wordItem = new HashMap<>();
                    wordItem.put("word", entry.getKey());
                    wordItem.put("count", entry.getValue());
                    wordItem.put("weight", entry.getValue());
                    return wordItem;
                })
                .collect(Collectors.toList());
        
        // 转换为JSON
        String wordDataJson;
        try {
            wordDataJson = objectMapper.writeValueAsString(wordList);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
        // 查看是否已存在
        LiveWordCloud existingWordCloud = liveWordCloudMapper.findByLiveStreamId(liveStreamId);
        
        LiveWordCloud wordCloud = new LiveWordCloud();
        wordCloud.setLiveStreamId(liveStreamId);
        wordCloud.setWordData(wordDataJson);
        wordCloud.setTotalMessages(messages.size());
        wordCloud.setTotalWords(totalWords);
        
        if (existingWordCloud != null) {
            // 更新
            liveWordCloudMapper.update(wordCloud);
            wordCloud.setId(existingWordCloud.getId());
            wordCloud.setGeneratedAt(existingWordCloud.getGeneratedAt());
        } else {
            // 新增
            wordCloud.setGeneratedAt(LocalDateTime.now());
            liveWordCloudMapper.insert(wordCloud);
        }
        
        return wordCloud;
    }
    
    /**
     * 获取词云数据
     */
    public LiveWordCloud getWordCloud(Long liveStreamId) {
        return liveWordCloudMapper.findByLiveStreamId(liveStreamId);
    }
    
    /**
     * 删除词云数据
     */
    public boolean deleteWordCloud(Long liveStreamId) {
        return liveWordCloudMapper.deleteByLiveStreamId(liveStreamId) > 0;
    }
}
