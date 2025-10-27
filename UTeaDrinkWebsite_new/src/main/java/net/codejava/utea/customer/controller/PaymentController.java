// net.codejava.utea.customer.controller.PaymentController
package net.codejava.utea.customer.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.order.service.OrderService;
import net.codejava.utea.payment.entity.enums.PaymentStatus;
import net.codejava.utea.payment.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
@RequestMapping("/customer/pay")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentTransactionRepository payRepo;
    private final OrderService orderService;

    @GetMapping("/{orderCode}")
    public String qr(@PathVariable String orderCode,
                     @RequestParam String method,
                     @RequestParam(value="error", required=false) String err,
                     org.springframework.ui.Model model){
        model.addAttribute("orderCode", orderCode);
        model.addAttribute("method", method);
        model.addAttribute("error", err);
        return "customer/pay-qr";
    }

    @PostMapping("/{orderCode}/success")
    public RedirectView success(@PathVariable String orderCode){
        payRepo.findAll().stream()
                .filter(p->orderCode.equals(p.getOrderCode()))
                .forEach(p->{ p.setStatus(PaymentStatus.PAID); payRepo.save(p); });
        orderService.markPaid(orderCode);

        var uri = UriComponentsBuilder
                .fromPath("/customer/orders/thank-you")
                .queryParam("order", orderCode)
                .build().toUriString();
        return new RedirectView(uri, true);
    }

    @PostMapping("/{orderCode}/fail")
    public RedirectView fail(@PathVariable String orderCode,
                             @RequestParam(defaultValue = "MOMO") String method){
        var uri = UriComponentsBuilder
                .fromPath("/customer/pay/{orderCode}")
                .queryParam("method", method)
                .queryParam("error", "PAY_FAILED")
                .buildAndExpand(orderCode)
                .toUriString();
        return new RedirectView(uri, true);
    }
}
