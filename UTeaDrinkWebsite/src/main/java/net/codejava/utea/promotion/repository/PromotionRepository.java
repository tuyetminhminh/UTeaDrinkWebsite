//package net.codejava.utea.promotion.repository;
//
//import net.codejava.utea.promotion.entity.Promotion;
//import net.codejava.utea.promotion.entity.enums.PromoScope;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//public interface PromotionRepository extends JpaRepository<Promotion, Long> {
//
//    @Query("""
//           select p from Promotion p
//           where p.status = 'ACTIVE'
//             and (p.activeFrom is null or p.activeFrom <= :now)
//             and (p.activeTo   is null or p.activeTo   >= :now)
//           """)
//    List<Promotion> findActiveNow(LocalDateTime now);
//
//    // ✅ XOÁ @Query — để JPA tự sinh
//    List<Promotion> findByShop_IdAndStatus(Long shopId, String status);
//
//    // ✅ Lấy tất cả promotion của shop (không filter status)
//    List<Promotion> findByShop_Id(Long shopId);
//
//    // ✅ Thêm cho DataInitializer
//    List<Promotion> findByScope(PromoScope scope);
//}


package net.codejava.utea.promotion.repository;

import net.codejava.utea.promotion.dto.PromotionRow;
import net.codejava.utea.promotion.entity.Promotion;
import net.codejava.utea.promotion.entity.enums.PromoScope;
import net.codejava.utea.promotion.entity.enums.PromoType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    @Query("""
           select p from Promotion p
           where p.status = 'ACTIVE'
             and (p.activeFrom is null or p.activeFrom <= :now)
             and (p.activeTo   is null or p.activeTo   >= :now)
           """)
    List<Promotion> findActiveNow(LocalDateTime now);

    // ✅ XOÁ @Query — để JPA tự sinh
    List<Promotion> findByShop_IdAndStatus(Long shopId, String status);

    // ✅ Lấy tất cả promotion của shop (không filter status)
    List<Promotion> findByShop_Id(Long shopId);

    // ✅ Thêm cho DataInitializer
    List<Promotion> findByScope(PromoScope scope);

    // Tìm và trả về các Promotion dưới dạng PromotionRow với filter
    @Query(value = """
            select new net.codejava.utea.promotion.dto.PromotionRow(
                p.id, p.title, p.scope,
                case when p.shop is not null then p.shop.name else '-' end,
                p.type, p.status, p.activeFrom, p.activeTo
            )
            from Promotion p left join p.shop s
            where (:scope is null or p.scope = :scope)
              and (:type is null or p.type = :type)
              and (:kw is null or :kw = '' or lower(p.title) like lower(concat('%', :kw, '%')))
            """,
            countQuery = """
            select count(p) from Promotion p
            where (:scope is null or p.scope = :scope)
              and (:type is null or p.type = :type)
              and (:kw is null or :kw = '' or lower(p.title) like lower(concat('%', :kw, '%')))
            """)
    Page<PromotionRow> searchRows(
            @Param("scope") PromoScope scope,
            @Param("type") PromoType type,
            @Param("kw") String kw,
            Pageable pageable);
}
