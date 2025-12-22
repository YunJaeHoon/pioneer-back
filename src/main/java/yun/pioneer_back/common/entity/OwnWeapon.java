package yun.pioneer_back.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"owner", "weapon"})
        }
)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class OwnWeapon extends BaseEntity
{
    @Id
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 소유자
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner")
    private User owner;

    // 무기
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "weapon")
    private Weapon weapon;
}
