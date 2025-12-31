package yun.pioneer_back.common.entity.rdbms;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum FightChoice
{
    ROCK("바위"),
    SCISSORS("가위"),
    PAPER("보");

    private final String description;
}
