package net.codejava.utea.order.service;

import net.codejava.utea.common.entity.User;
import net.codejava.utea.customer.dto.CheckoutRequest;
import net.codejava.utea.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {

    Order createFromCart(User user, java.util.function.Consumer<Order.OrderBuilder> extra, CheckoutRequest req);

    List<Order> myOrders(User user);

    Page<Order> searchForShop(Long shopId, String status, Pageable pageable);

    void changeStatus(String orderCode, String newStatus, Long actorId);

    void markPaid(String orderCode);




    // Overload có đối soát số tiền & lưu mã giao dịch
    void markPaid(String orderCode, String paymentTxnId, BigDecimal paidAmount);
}