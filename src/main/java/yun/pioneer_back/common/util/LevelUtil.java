package yun.pioneer_back.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yun.pioneer_back.common.configuration.LevelConfig;

@Service
@RequiredArgsConstructor
public class LevelUtil
{
    private final LevelConfig levelConfig;

    // 다음 레벨로 가기 위한 경험치 계산
    public int getRequiredExp(int level)
    {
        return levelConfig.getRules().stream()
                .filter(rule -> rule.getMin() <= level && level <= rule.getMax())
                .findFirst()
                .orElseThrow()
                .getRequiredExp();
    }
}
