package yun.pioneer_back.common.entity.rdbms;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum WeaponGrade
{
    NORMAL("일반"),
    UNCOMMON("고급"),
    RARE("희귀"),
    EPIC("영웅"),
    LEGENDARY("전설"),
    UNIQUE("고유");

    private final String description;
}
