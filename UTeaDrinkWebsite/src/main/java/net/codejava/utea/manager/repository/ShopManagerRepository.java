//package net.codejava.utea.manager.repository;
//
//import net.codejava.utea.manager.entity.ShopManager;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.Optional;
//
//@Repository
//public interface ShopManagerRepository extends JpaRepository<ShopManager, Long> {
//
//    // Sửa: manager là field name trong entity, manager.id để truy cập User ID
//    Optional<ShopManager> findByManager_Id(Long managerId);
//
//    Optional<ShopManager> findByShop_Id(Long shopId);
//
//    boolean existsByManager_Id(Long managerId);
//}
//

package net.codejava.utea.manager.repository;

import net.codejava.utea.manager.entity.ShopManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopManagerRepository extends JpaRepository<ShopManager, Long> {

    // Sửa: manager là field name trong entity, manager.id để truy cập User ID
    Optional<ShopManager> findByManager_Id(Long managerId);

    Optional<ShopManager> findByShop_Id(Long shopId);

    boolean existsByManager_Id(Long managerId);

    @Query("""
                        select sm
                        from ShopManager sm
                        left join fetch sm.manager m
                        where sm.shop.id = :shopId
                        """)
    Optional<ShopManager> findByShopIdWithManager(@Param("shopId") Long shopId);

    // Kiểm tra 1 manager đang quản lý shop nào khác (khác currentShopId)
    @Query("""
                select count(sm) > 0 from ShopManager sm
                where sm.manager.id = :managerId
                and (:currentShopId is null or sm.shop.id <> :currentShopId)
        """)
    boolean existsByManagerIdBusy(@Param("managerId") Long managerId,
                                  @Param("currentShopId") Long currentShopId);

    // Lấy map shopId -> managerName cho trang danh sách
    @Query("""
                        select sm.shop.id, m.fullName
                        from ShopManager sm join sm.manager m
                        where sm.shop.id in :ids
                        """)
    List<Object[]> findManagerNamesByShopIds(@Param("ids") List<Long> shopIds);

    Optional<ShopManager> findFirstByShop_Id(Long shopId);
}
