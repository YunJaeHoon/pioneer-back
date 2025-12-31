package yun.pioneer_back.common.repository.rdbms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yun.pioneer_back.common.entity.rdbms.FightSession;

@Repository
public interface FightSessionRepository extends JpaRepository<FightSession, Long>
{
}
