package net.codejava.utea.customer.config;

import net.codejava.utea.customer.repository.CartRepository;
import net.codejava.utea.repository.CustomerRepository;
import net.codejava.utea.service.impl.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final CustomerRepository customerRepo;
    private final CartRepository cartRepo;

    public GlobalModelAttributes(CustomerRepository customerRepo, CartRepository cartRepo) {
        this.customerRepo = customerRepo;
        this.cartRepo = cartRepo;
    }

    @ModelAttribute("cartCount")
    public Integer cartCount(@AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) return 0;
        var c = customerRepo.findByAccount_Id(user.getId()).orElse(null);
        if (c == null) return 0;
        return cartRepo.findByCustomer(c).stream().mapToInt(i -> i.getQuantity()).sum();
    }
}
