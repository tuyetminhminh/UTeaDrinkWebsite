package net.codejava.utea.customer.service;

import net.codejava.utea.customer.dto.CheckoutRequest;
import net.codejava.utea.entity.Customer;
import net.codejava.utea.customer.entity.Order;

import java.util.function.UnaryOperator;

public interface OrderService {
    Order createFromCart(Customer customer, UnaryOperator<Order.OrderBuilder> orderConfigurer, CheckoutRequest req);
}