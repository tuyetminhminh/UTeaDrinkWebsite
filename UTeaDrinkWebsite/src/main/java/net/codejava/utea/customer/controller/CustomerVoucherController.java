package net.codejava.utea.customer.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.security.CustomUserDetails;
import net.codejava.utea.promotion.service.CustomerVaultQueryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controller hiển thị danh sách voucher cho khách hàng.
 * Đã BỎ tính năng "Lưu voucher" - chỉ hiển thị tất cả voucher available.
 */
@Controller
@RequestMapping("/customer/vouchers")
@RequiredArgsConstructor
public class CustomerVoucherController {

    private final CustomerVaultQueryService queryService;

    @GetMapping
    public String index(@AuthenticationPrincipal CustomUserDetails cud,
                        @RequestParam(name="q", required=false) String q,
                        @RequestParam(name="page", defaultValue="0") int page,
                        @RequestParam(name="size", defaultValue="10") int size,
                        @RequestParam(name="sort", defaultValue="newest") String sort,
                        Model model) {

        Long userId = cud.getUser().getId();
        // saved=false để luôn hiển thị tất cả voucher
        var items = queryService.list(userId, q, sort, false, PageRequest.of(page, size));

        model.addAttribute("items", items);
        model.addAttribute("q", q);
        model.addAttribute("sort", sort);

        return "customer/vouchers";
    }
}
