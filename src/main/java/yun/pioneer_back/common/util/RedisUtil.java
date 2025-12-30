package yun.pioneer_back.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisUtil
{
    private final RedisTemplate<String, Object> redisTemplate;

    /// ============ Value ============

    // 데이터 저장 (유효기간 x)
    @Transactional
    public void valueAdd(String key, Object value)
    {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        ops.set(key, value);
    }

    // 데이터 저장 (유효기간 o)
    @Transactional
    public void valueAdd(String key, Object value, Duration duration)
    {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        ops.set(key, value, duration);
    }

    // 데이터 조회
    @Transactional(readOnly = true)
    public Object valueGet(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 데이터 삭제
    @Transactional
    public void valueDelete(String key) {
        redisTemplate.delete(key);
    }

    /// ============ ZSet ============

    // 데이터 저장 (유효기간 x)
    @Transactional
    public void zsetAdd(String key, Object value, double score)
    {
        ZSetOperations<String, Object> ops = redisTemplate.opsForZSet();
        ops.add(key, value, score);
    }

    // 데이터 저장 (유효기간 o)
    @Transactional
    public void zsetAdd(String key, Object value, double score, Duration duration)
    {
        ZSetOperations<String, Object> ops = redisTemplate.opsForZSet();
        ops.add(key, value, score);

        redisTemplate.expire(key, duration);
    }

    // 데이터 조회
    // isReverse == true : 높은 score 조회 (내림차순)
    // isReverse == false : 낮은 score 조회 (오름차순)
    // {start}번째부터 {end}번째까지 조회합니다. (index는 0부터 시작)
    @Transactional(readOnly = true)
    public Set<Object> zsetGet(String key, boolean isReverse, int start, int end)
    {
        return isReverse ?
                redisTemplate.opsForZSet().reverseRange(key, start, end) :
                redisTemplate.opsForZSet().range(key, start, end);
    }

    // score 조회
    @Transactional(readOnly = true)
    public Double zsetGetScore(String key, Object value)
    {
        return redisTemplate.opsForZSet().score(key, value);
    }

    // 데이터 삭제 (key 자체 삭제)
    @Transactional
    public void zsetDelete(String key) {
        redisTemplate.opsForZSet().remove(key);
    }

    // 데이터 삭제
    @Transactional
    public void zsetDelete(String key, Object value) {
        redisTemplate.opsForZSet().remove(key, value);
    }
}
