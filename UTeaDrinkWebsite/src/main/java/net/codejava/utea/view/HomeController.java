package net.codejava.utea.view;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.manager.dto.ShopBannerDTO;
import net.codejava.utea.manager.service.ShopService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

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
            model.addAttribute("shopId", defaultShopId);
        } catch (Exception e) {
            model.addAttribute("banners", List.of());
            model.addAttribute("hasBanners", false);
            model.addAttribute("shopId", 1L);
        }
        return "home/customer-home";
    }
    
    /**
     * API: Lấy sections với products cho customer
     */
    @GetMapping("/api/public/shops/{shopId}/sections")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getShopSections(@PathVariable Long shopId) {
        try {
            List<Map<String, Object>> sections = shopService.getActiveSectionsWithProducts(shopId);
            return ResponseEntity.ok(sections);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }
}

