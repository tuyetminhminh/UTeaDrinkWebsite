//package net.codejava.utea.controller;
//
//import lombok.RequiredArgsConstructor;
//import net.codejava.utea.customer.entity.Order;
//import net.codejava.utea.customer.service.repository.OrderRepository;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//@Controller
//@RequestMapping("/customer/orders")
//@RequiredArgsConstructor
//public class OrderController {
//
//    private final OrderRepository orderRepo;
//
//    @GetMapping("/{orderCode}")
//    public String detail(@PathVariable String orderCode, Model model) {
//        Order order = orderRepo.findByOrderCode(orderCode)
//                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn: " + orderCode));
//        model.addAttribute("order", order);
//        return "customer/order-detail";
//    }
//}
//// src/main/java/net/codejava/utea/controller/OrderController.java
package net.codejava.utea.customer.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.entity.Customer;
import net.codejava.utea.customer.entity.Order;
import net.codejava.utea.repository.CustomerRepository;
import net.codejava.utea.customer.repository.OrderRepository;
import net.codejava.utea.service.impl.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/customer/orders") // nhớ có dấu '/'
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepo;
    private final CustomerRepository customerRepo;

    private Customer currentCustomer(CustomUserDetails user) {
        return customerRepo.findByAccount_Id(user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
    }

    // ✅ Danh sách đơn của tôi
    @GetMapping
    public String myOrders(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        var customer = currentCustomer(user);
        List<Order> orders = orderRepo.findByCustomerOrderByCreatedAtDesc(customer);
        model.addAttribute("orders", orders);
        return "customer/orders"; // templates/customer/orders.html
    }

    // Chi tiết đơn
    @GetMapping("/{orderCode}")
    public String detail(@PathVariable String orderCode, Model model) {
        Order order = orderRepo.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn: " + orderCode));
        model.addAttribute("order", order);
        return "customer/order-detail";
    }
}
