package yun.pioneer_back.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisUtil
{
    private final RedisTemplate<String, Object> redisTemplate;

    // 데이터 저장 (유효기간 x)
    @Transactional
    public void set(String key, Object value)
    {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        ops.set(key, value);
    }

    // 데이터 저장 (유효기간 o)
    @Transactional
    public void set(String key, Object value, Duration duration)
    {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        ops.set(key, value, duration);
    }

    // 데이터 조회
    @Transactional(readOnly = true)
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 데이터 삭제
    @Transactional
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
