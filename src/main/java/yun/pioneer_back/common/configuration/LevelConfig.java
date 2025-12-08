package yun.pioneer_back.common.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@RequiredArgsConstructor
@ConfigurationProperties(prefix = "level")
@Getter
public class LevelConfig
{
    private final List<LevelRule> rules;

    @RequiredArgsConstructor
    @Getter
    public static class LevelRule
    {
        private final int min;
        private final int max;
        private final int requiredExp;
    }
}
