package cn.edu.zjut.back.utils;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 轻量内存缓存工具。
 * <p>
 * 该实现保留原有 RedisUtil 的方法签名，避免业务代码大范围修改，
 * 但底层不再依赖 Redis 服务。
 */
@Component
public class RedisUtil {

    private static final class CacheEntry {
        private Object value;
        private long expireAtMillis; // -1 表示永久

        private CacheEntry(Object value, long expireAtMillis) {
            this.value = value;
            this.expireAtMillis = expireAtMillis;
        }

        private boolean isExpired() {
            return expireAtMillis > 0 && System.currentTimeMillis() > expireAtMillis;
        }
    }

    private final ConcurrentHashMap<String, CacheEntry> store = new ConcurrentHashMap<>();

    private void removeIfExpired(String key) {
        CacheEntry entry = store.get(key);
        if (entry != null && entry.isExpired()) {
            store.remove(key);
        }
    }

    private CacheEntry getEntry(String key) {
        if (key == null) {
            return null;
        }
        removeIfExpired(key);
        return store.get(key);
    }

    private long toExpireAtMillis(long time, TimeUnit unit) {
        if (time <= 0) {
            return -1;
        }
        return System.currentTimeMillis() + unit.toMillis(time);
    }

    private String wildcardToRegex(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return ".*";
        }
        StringBuilder builder = new StringBuilder();
        builder.append('^');
        for (char ch : pattern.toCharArray()) {
            switch (ch) {
                case '*':
                    builder.append(".*");
                    break;
                case '?':
                    builder.append('.');
                    break;
                case '.':
                case '$':
                case '^':
                case '{':
                case '}':
                case '(': 
                case ')':
                case '|':
                case '+':
                case '[':
                case ']':
                case '\\':
                    builder.append('\\').append(ch);
                    break;
                default:
                    builder.append(ch);
            }
        }
        builder.append('$');
        return builder.toString();
    }

    // =============================common============================
    public boolean expire(String key, long time) {
        try {
            CacheEntry entry = getEntry(key);
            if (entry == null) {
                return false;
            }
            if (time > 0) {
                entry.expireAtMillis = toExpireAtMillis(time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public long getExpire(String key) {
        CacheEntry entry = getEntry(key);
        if (entry == null) {
            return -2;
        }
        if (entry.expireAtMillis < 0) {
            return -1;
        }
        long remainingMillis = entry.expireAtMillis - System.currentTimeMillis();
        return Math.max(0, TimeUnit.MILLISECONDS.toSeconds(remainingMillis));
    }

    public boolean hasKey(String key) {
        try {
            return getEntry(key) != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public void delete(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                store.remove(key[0]);
            } else {
                store.keySet().removeAll((Collection<String>) Arrays.asList(key));
            }
        }
    }

    public void deleteByPattern(String pattern) {
        String regex = wildcardToRegex(pattern);
        Pattern compiled = Pattern.compile(regex);
        Set<String> matched = new HashSet<>();
        for (String key : store.keySet()) {
            if (compiled.matcher(key).matches()) {
                matched.add(key);
            }
        }
        if (!matched.isEmpty()) {
            store.keySet().removeAll(matched);
        }
    }

    public Set<String> keys(String pattern) {
        String regex = wildcardToRegex(pattern);
        Pattern compiled = Pattern.compile(regex);
        Set<String> matched = new LinkedHashSet<>();
        for (String key : new ArrayList<>(store.keySet())) {
            if (compiled.matcher(key).matches() && hasKey(key)) {
                matched.add(key);
            }
        }
        return matched;
    }

    public void clear() {
        store.clear();
    }

    // ============================String=============================
    public Object get(String key) {
        CacheEntry entry = getEntry(key);
        return entry == null ? null : entry.value;
    }

    public boolean set(String key, Object value) {
        try {
            store.put(key, new CacheEntry(value, -1));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                store.put(key, new CacheEntry(value, toExpireAtMillis(time, TimeUnit.SECONDS)));
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public long increment(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return store.compute(key, (cacheKey, oldEntry) -> {
            long current = 0L;
            long expireAt = -1L;
            if (oldEntry != null && !oldEntry.isExpired()) {
                expireAt = oldEntry.expireAtMillis;
                Object oldValue = oldEntry.value;
                if (oldValue instanceof Number) {
                    current = ((Number) oldValue).longValue();
                } else if (oldValue != null) {
                    try {
                        current = Long.parseLong(oldValue.toString());
                    } catch (NumberFormatException ignored) {
                        current = 0L;
                    }
                }
            }
            return new CacheEntry(current + delta, expireAt);
        }).value instanceof Number ? ((Number) store.get(key).value).longValue() : 0L;
    }

    public long decrement(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return increment(key, -delta);
    }

    // ================================Map=================================
    public Object hGet(String key, String item) {
        CacheEntry entry = getEntry(key);
        if (entry == null || !(entry.value instanceof Map)) {
            return null;
        }
        return ((Map<?, ?>) entry.value).get(item);
    }

    @SuppressWarnings("unchecked")
    public Map<Object, Object> hGetAll(String key) {
        CacheEntry entry = getEntry(key);
        if (entry == null || !(entry.value instanceof Map)) {
            return Collections.emptyMap();
        }
        return new HashMap<>((Map<Object, Object>) entry.value);
    }

    public boolean hSet(String key, Map<String, Object> map) {
        try {
            store.put(key, new CacheEntry(new HashMap<>(map), -1));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hSet(String key, Map<String, Object> map, long time) {
        try {
            store.put(key, new CacheEntry(new HashMap<>(map), time > 0 ? toExpireAtMillis(time, TimeUnit.SECONDS) : -1));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hSet(String key, String item, Object value) {
        try {
            CacheEntry entry = getEntry(key);
            Map<String, Object> map;
            long expireAt = -1L;
            if (entry != null && entry.value instanceof Map) {
                map = new HashMap<>((Map<String, Object>) entry.value);
                expireAt = entry.expireAtMillis;
            } else {
                map = new HashMap<>();
            }
            map.put(item, value);
            store.put(key, new CacheEntry(map, expireAt));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void hDelete(String key, Object... item) {
        CacheEntry entry = getEntry(key);
        if (entry == null || !(entry.value instanceof Map) || item == null || item.length == 0) {
            return;
        }
        Map<Object, Object> map = new HashMap<>((Map<Object, Object>) entry.value);
        for (Object field : item) {
            map.remove(field);
        }
        store.put(key, new CacheEntry(map, entry.expireAtMillis));
    }

    // ============================set=============================
    public Set<Object> sGet(String key) {
        try {
            CacheEntry entry = getEntry(key);
            if (entry == null || !(entry.value instanceof Set)) {
                return null;
            }
            return new LinkedHashSet<>((Set<Object>) entry.value);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public long sAdd(String key, Object... values) {
        try {
            CacheEntry entry = getEntry(key);
            Set<Object> set;
            long expireAt = -1L;
            if (entry != null && entry.value instanceof Set) {
                set = new LinkedHashSet<>((Set<Object>) entry.value);
                expireAt = entry.expireAtMillis;
            } else {
                set = new LinkedHashSet<>();
            }
            if (values != null) {
                set.addAll(Arrays.asList(values));
            }
            store.put(key, new CacheEntry(set, expireAt));
            return set.size();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean sIsMember(String key, Object value) {
        try {
            CacheEntry entry = getEntry(key);
            return entry != null && entry.value instanceof Set && ((Set<?>) entry.value).contains(value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public long sGetSetSize(String key) {
        try {
            CacheEntry entry = getEntry(key);
            return entry != null && entry.value instanceof Set ? ((Set<?>) entry.value).size() : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ===============================list=================================
    public List<Object> lGet(String key, long start, long end) {
        try {
            CacheEntry entry = getEntry(key);
            if (entry == null || !(entry.value instanceof List)) {
                return null;
            }
            List<Object> list = new ArrayList<>((List<Object>) entry.value);
            int size = list.size();
            int fromIndex = (int) Math.max(0, start);
            int toIndex = end < 0 ? size : (int) Math.min(size, end + 1);
            if (fromIndex >= size || fromIndex >= toIndex) {
                return Collections.emptyList();
            }
            return new ArrayList<>(list.subList(fromIndex, toIndex));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean lPush(String key, Object value) {
        try {
            CacheEntry entry = getEntry(key);
            LinkedList<Object> list;
            long expireAt = -1L;
            if (entry != null && entry.value instanceof List) {
                list = new LinkedList<>((List<Object>) entry.value);
                expireAt = entry.expireAtMillis;
            } else {
                list = new LinkedList<>();
            }
            list.addLast(value);
            store.put(key, new CacheEntry(list, expireAt));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean lPush(String key, Object value, long time) {
        try {
            lPush(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean lPushAll(String key, List<Object> value) {
        try {
            CacheEntry entry = getEntry(key);
            LinkedList<Object> list;
            long expireAt = -1L;
            if (entry != null && entry.value instanceof List) {
                list = new LinkedList<>((List<Object>) entry.value);
                expireAt = entry.expireAtMillis;
            } else {
                list = new LinkedList<>();
            }
            if (value != null) {
                list.addAll(value);
            }
            store.put(key, new CacheEntry(list, expireAt));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Object lPop(String key) {
        try {
            CacheEntry entry = getEntry(key);
            if (entry == null || !(entry.value instanceof List)) {
                return null;
            }
            LinkedList<Object> list = new LinkedList<>((List<Object>) entry.value);
            Object value = list.pollLast();
            store.put(key, new CacheEntry(list, entry.expireAtMillis));
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
