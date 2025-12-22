package yun.pioneer_back.common.entity.rdbms;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public class BaseEntity
{
    // 생성 시점
    @NotNull
    @CreatedDate
    private LocalDateTime createdAt;

    // 수정 시점
    @NotNull
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
