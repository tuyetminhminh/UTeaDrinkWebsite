package net.codejava.utea.customer.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.service.ProductService;
import net.codejava.utea.catalog.service.ProductVariantService;
import net.codejava.utea.common.security.CustomUserDetails;
import net.codejava.utea.customer.dto.ProductDTO;
import net.codejava.utea.customer.entity.CartItem;
import net.codejava.utea.customer.service.CartService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/customer/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final ProductService productService;
    private final ProductVariantService variantService;

    @GetMapping
    @Transactional
    public String viewCart(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        // Lấy danh sách sản phẩm từ giỏ hàng
        List<CartItem> items = cartService.listItems(user.getUser());

        // Chuyển đổi sang DTO để truyền về view
        List<ProductDTO> productDTOs = new ArrayList<>();
        for (CartItem item : items) {
            ProductDTO dto = new ProductDTO(
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getProduct().getImages().isEmpty() ? "/images/default.png" : item.getProduct().getImages().get(0).getUrl()
            );
            productDTOs.add(dto);
        }

        // Truyền dữ liệu vào model
        model.addAttribute("items", productDTOs);
        model.addAttribute("subtotal", cartService.getSelectedSubtotal(user.getUser()));
        return "customer/cart";  // View giỏ hàng
    }

    @PostMapping("/add")
    public String addToCart(@AuthenticationPrincipal CustomUserDetails user,
                            @RequestParam Long productId,
                            @RequestParam(required = false) Long variantId,
                            @RequestParam(defaultValue = "1") int quantity,
                            RedirectAttributes ra) {
        cartService.addItem(user.getUser(), productId, variantId, quantity);
        ra.addFlashAttribute("success", "Đã thêm sản phẩm vào giỏ hàng!");
        return "redirect:/customer/cart";
    }

    @PostMapping("/update-qty")
    public String updateQty(@AuthenticationPrincipal CustomUserDetails user,
                            @RequestParam Long itemId,
                            @RequestParam int qty) {
        cartService.updateQty(user.getUser(), itemId, qty);
        return "redirect:/customer/cart";
    }

    @PostMapping("/toggle")
    public String toggle(@AuthenticationPrincipal CustomUserDetails user,
                         @RequestParam Long itemId,
                         @RequestParam boolean selected) {
        cartService.toggleSelect(user.getUser(), itemId, selected);
        return "redirect:/customer/cart";
    }

    @PostMapping("/remove")
    public String removeItem(@AuthenticationPrincipal CustomUserDetails user,
                             @RequestParam Long itemId) {
        cartService.removeItem(user.getUser(), itemId);
        return "redirect:/customer/cart";
    }
}
