package net.codejava.utea.promotion.repository;

import net.codejava.utea.promotion.dto.VoucherRow;
import net.codejava.utea.promotion.entity.Voucher;
import net.codejava.utea.promotion.entity.enums.PromoScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    @Query("""
           select v from Voucher v
           where v.status = 'ACTIVE'
             and (v.activeFrom is null or v.activeFrom <= :now)
             and (v.activeTo   is null or v.activeTo   >= :now)
             and (v.usageLimit is null or v.usedCount is null or v.usedCount < v.usageLimit)
           """)
    List<Voucher> findActiveNow(LocalDateTime now);

    @Query("""
           select v from Voucher v
           where v.code = :code
             and v.status = 'ACTIVE'
             and (v.activeFrom is null or v.activeFrom <= :now)
             and (v.activeTo   is null or v.activeTo   >= :now)
             and (v.usageLimit is null or v.usedCount is null or v.usedCount < v.usageLimit)
           """)
    Optional<Voucher> findByCodeActiveNow(String code, LocalDateTime now);

    // ✅ Tìm và trả về các Voucher dưới dạng VoucherRow với filter (admin)
    @Query(value = """
            select new net.codejava.utea.promotion.dto.VoucherRow(
                v.id, v.code, v.scope,
                case when v.shop is not null then v.shop.name else '-' end,
                v.status, v.activeFrom, v.activeTo, v.usageLimit, v.usedCount
            )
            from Voucher v left join v.shop s
            where (:scope is null or v.scope = :scope)
              and (:kw is null or :kw = '' or lower(v.code) like lower(concat('%', :kw, '%')))
            """,
            countQuery = """
            select count(v) from Voucher v
            where (:scope is null or v.scope = :scope)
              and (:kw is null or :kw = '' or lower(v.code) like lower(concat('%', :kw, '%')))
            """)
    Page<VoucherRow> searchRows(
            @Param("scope") PromoScope scope,
            @Param("kw") String kw,
            Pageable pageable);
}