package yun.pioneer_back.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"email"}),
                @UniqueConstraint(columnNames = {"nickname"})
        }
)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class User extends BaseEntity
{
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이메일
    @NotNull
    @Email
    @Column(length = 255)
    private String email;

    // 비밀번호
    @NotNull
    @Column(length = 255)
    private String password;

    // 닉네임
    @NotNull
    @Column(length = 50)
    private String nickname;

    // 사용자 권한
    @NotNull
    @Enumerated(EnumType.STRING)
    private UserRole role;

    // refresh token
    @Column(length = 255)
    private String refreshToken;

    // refresh token 갱신
    public void renewRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
