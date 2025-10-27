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
     * Trang chủ customer với banner động và sections
     */
    @GetMapping("/customer/home")
    public String customerHome(Model model) {
        Long shopId = shopService.getDefaultShopId().orElse(1L);
        List<ShopBannerDTO> banners = List.of();
        java.util.List<java.util.Map<String, Object>> sections = List.of();
        
        try {
            banners = shopService.getActiveBanners(shopId);
            sections = shopService.getActiveSectionsWithProducts(shopId);
        } catch (Exception e) {
            System.err.println("❌ Error loading banners/sections: " + e.getMessage());
            e.printStackTrace();
        }

        model.addAttribute("shopId", shopId);
        model.addAttribute("banners", banners);
        model.addAttribute("hasBanners", !banners.isEmpty());
        model.addAttribute("sections", sections);
        model.addAttribute("hasSections", !sections.isEmpty());
        return "home/customer-home";
    }

}

