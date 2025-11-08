package yun.pioneer_back.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yun.pioneer_back.common.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
