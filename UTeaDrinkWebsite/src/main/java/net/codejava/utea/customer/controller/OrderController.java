// net.codejava.utea.customer.controller.OrderController
package net.codejava.utea.customer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class OrderController {

    @GetMapping("/customer/orders/thank-you")
    public String thankYou(@RequestParam("order") String orderCode, Model model){
        model.addAttribute("orderCode", orderCode);
        return "customer/thank-you";
    }
}
