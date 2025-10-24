package net.codejava.utea.promotion.repository;

import net.codejava.utea.promotion.entity.Voucher;
import net.codejava.utea.promotion.entity.enums.PromoScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    
    Optional<Voucher> findByCode(String code);
    
    List<Voucher> findByStatus(String status);
    
    List<Voucher> findByScope(PromoScope scope);
    
    @Query("""
      select v from Voucher v
      where v.code = :code
        and v.status = 'ACTIVE'
        and (v.activeFrom is null or v.activeFrom <= CURRENT_TIMESTAMP)
        and (v.activeTo is null or v.activeTo >= CURRENT_TIMESTAMP)
    """)
    Optional<Voucher> findByCodeActiveNow(@Param("code") String code);
}

