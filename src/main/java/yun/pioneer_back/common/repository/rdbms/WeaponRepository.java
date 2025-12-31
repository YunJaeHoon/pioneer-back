package yun.pioneer_back.common.repository.rdbms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import yun.pioneer_back.common.entity.rdbms.Weapon;

@Repository
public interface WeaponRepository extends JpaRepository<Weapon, Long>
{
    @Query("select w from Weapon w where w.name = '리볼버'")
    Weapon findBasicWeapon();
}
