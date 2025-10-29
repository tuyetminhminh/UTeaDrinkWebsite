package net.codejava.utea.promotion.repository;

import net.codejava.utea.promotion.entity.CustomerVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CustomerVoucherRepository extends JpaRepository<CustomerVoucher, Long> {

    boolean existsByUser_IdAndVoucher_Id(Long userId, Long voucherId);

    @Query("""
           select cv.voucher.code from CustomerVoucher cv
           where cv.user.id = :userId and cv.state = 'ACTIVE'
           """)
    List<String> findSavedCodesByUserId(Long userId);

    Optional<CustomerVoucher> findByUser_IdAndVoucher_CodeAndState(Long userId, String code, String state);
    Optional<CustomerVoucher> findByUser_IdAndVoucher_Id(Long userId, Long voucherId);
}
