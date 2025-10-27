package net.codejava.utea.view;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.manager.dto.ShopBannerDTO;
import net.codejava.utea.manager.service.ShopService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ShopService shopService;

    /**
     * Trang chủ customer với banner động
     */
    @GetMapping("/customer/home")
    public String customerHome(Model model) {
        Long shopId = shopService.getDefaultShopId().orElse(1L); // hoặc requireDefaultShopId()
        List<ShopBannerDTO> banners = List.of();
        try {
            banners = shopService.getActiveBanners(shopId);
        } catch (Exception ignore) {}

        model.addAttribute("shopId", shopId);          // ❗ Quan trọng cho JS
        model.addAttribute("banners", banners);
        model.addAttribute("hasBanners", !banners.isEmpty());
        return "home/customer-home";
    }

}

