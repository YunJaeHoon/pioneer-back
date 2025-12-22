package yun.pioneer_back.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import yun.pioneer_back.common.entity.Weapon;

@Repository
public interface WeaponRepository extends JpaRepository<Weapon, Long>
{
    @Query("select w from Weapon w where w.name = '리볼버'")
    Weapon findBasicWeapon();
}
