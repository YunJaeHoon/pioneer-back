package yun.pioneer_back.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yun.pioneer_back.common.entity.OwnWeapon;

@Repository
public interface OwnWeaponRepository extends JpaRepository<OwnWeapon, Long>
{
}
