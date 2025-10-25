package net.codejava.utea.promotion.repository;

import net.codejava.utea.promotion.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}