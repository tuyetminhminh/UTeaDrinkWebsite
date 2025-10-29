package net.codejava.utea.customer.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.security.CustomUserDetails;
import net.codejava.utea.manager.entity.Shop;
import net.codejava.utea.manager.repository.ShopRepository;
import net.codejava.utea.promotion.service.CustomerPromotionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller cho customer xem chương trình khuyến mãi tự động
 */
@Controller
@RequestMapping("/customer/promotions")
@RequiredArgsConstructor
public class CustomerPromotionController {

    private final CustomerPromotionService promotionService;
    private final ShopRepository shopRepo;

    @GetMapping
    public String index(@AuthenticationPrincipal CustomUserDetails cud,
                        @RequestParam(name = "q", required = false) String q,
                        @RequestParam(name = "page", defaultValue = "0") int page,
                        @RequestParam(name = "size", defaultValue = "12") int size,
                        Model model) {

        // Lấy shop đầu tiên (hoặc có thể lấy từ session/context)
        // Tạm thời lấy shop đầu tiên, có thể customize sau
        Shop shop = shopRepo.findAll().stream().findFirst().orElse(null);
        Long shopId = (shop != null) ? shop.getId() : null;

        var promotions = promotionService.getActivePromotions(shopId, q, PageRequest.of(page, size));

        model.addAttribute("items", promotions);
        model.addAttribute("q", q);
        model.addAttribute("shopName", shop != null ? shop.getName() : "Hệ thống");

        return "customer/promotions";
    }
}

