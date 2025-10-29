package net.codejava.utea.payment.repository;

import net.codejava.utea.payment.entity.PaymentTransaction;
import net.codejava.utea.payment.entity.enums.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findTopByOrderCodeAndMethodOrderByIdDesc(String orderCode, PaymentMethod method);
    Optional<PaymentTransaction> findByGatewayTxnCode(String gatewayTxnCode);
}
