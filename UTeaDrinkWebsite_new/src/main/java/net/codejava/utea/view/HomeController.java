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
        try {
            // Load banners từ shop đầu tiên (hoặc có thể lấy theo context)
            // Ở đây giả sử shopId = 1, bạn có thể điều chỉnh logic này
            Long defaultShopId = 1L;
            List<ShopBannerDTO> banners = shopService.getActiveBanners(defaultShopId);
            model.addAttribute("banners", banners);
            model.addAttribute("hasBanners", !banners.isEmpty());
        } catch (Exception e) {
            model.addAttribute("banners", List.of());
            model.addAttribute("hasBanners", false);
        }
        return "home/customer-home";
    }
}

