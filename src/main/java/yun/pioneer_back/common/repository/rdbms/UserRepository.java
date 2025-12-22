package yun.pioneer_back.common.repository.rdbms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yun.pioneer_back.common.entity.rdbms.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>
{
    // 이메일로 조회
    Optional<User> findByEmail(String email);

    // 닉네임으로 조회
    Optional<User> findByNickname(String nickname);
}
