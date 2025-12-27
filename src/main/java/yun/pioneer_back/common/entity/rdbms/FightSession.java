package yun.pioneer_back.common.entity.rdbms;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"roomId"})
        }
)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder(access = AccessLevel.PRIVATE)
public class FightSession extends BaseEntity
{
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저 A
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_a")
    private User userA;

    // 유저 B
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_b")
    private User userB;

    // 승자
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "winner")
    private User winner;

    // 방 UUID
    @NotNull
    @Column(length = 255)
    private String roomId;

    // 세션 상태
    @NotNull
    @Enumerated(EnumType.STRING)
    private FightStatus status;

    // 유저 A의 선택
    @NotNull
    @Enumerated(EnumType.STRING)
    private FightChoice userAChoice;

    // 유저 B의 선택
    @NotNull
    @Enumerated(EnumType.STRING)
    private FightChoice userBChoice;

    // 매칭 생성
    public static FightSession create(User userA, User userB)
    {
        return FightSession.builder()
                .userA(userA)
                .userB(userB)
                .winner(null)
                .roomId(UUID.randomUUID().toString())
                .status(FightStatus.MATCHED)
                .userAChoice(FightChoice.SCISSORS)
                .userBChoice(FightChoice.SCISSORS)
                .build();
    }
}
