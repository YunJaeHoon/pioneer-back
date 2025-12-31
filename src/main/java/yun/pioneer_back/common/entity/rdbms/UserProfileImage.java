package yun.pioneer_back.common.entity.rdbms;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum UserProfileImage
{
    BASIC_1("기본 이미지 1"),
    BASIC_2("기본 이미지 2"),
    BASIC_3("기본 이미지 3"),
    BASIC_4("기본 이미지 4"),
    BASIC_5("기본 이미지 5");

    private final String description;
}
