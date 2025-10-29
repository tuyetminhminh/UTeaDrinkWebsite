package net.codejava.utea.customer.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.entity.Product;
import net.codejava.utea.catalog.entity.ProductVariant;
import net.codejava.utea.catalog.entity.enums.Size;
import net.codejava.utea.catalog.repository.ProductRepository;
import net.codejava.utea.catalog.repository.ProductVariantRepository;
import net.codejava.utea.common.security.CustomUserDetails;
import net.codejava.utea.customer.entity.CartItem;
import net.codejava.utea.customer.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/customer/cart/api")
@RequiredArgsConstructor
public class CartApiController {

    private final ProductRepository productRepo;
    private final ProductVariantRepository variantRepo;
    private final CartService cartService;

    @PostMapping(value = "/quick-add", consumes = "application/x-www-form-urlencoded;charset=UTF-8")
    public ResponseEntity<?> quickAdd(@AuthenticationPrincipal CustomUserDetails cud,
                                      @RequestParam("productId") Long productId,
                                      @RequestParam(value = "quantity", defaultValue = "1") Integer qty) {

        Map<String,Object> res = new HashMap<>();
        if (cud == null) {
            res.put("ok", false);
            res.put("reason", "NOT_LOGGED_IN");
            return ResponseEntity.status(401).body(res);
        }
        if (qty == null || qty < 1) qty = 1;

        Product p = productRepo.findById(productId).orElse(null);
        if (p == null) {
            res.put("ok", false);
            res.put("reason", "NOT_FOUND");
            return ResponseEntity.ok(res);
        }

        // Nếu bạn dùng status OUT_OF_STOCK cho toàn sản phẩm
        String status = p.getStatus() == null ? "" : p.getStatus();
        if ("OUT_OF_STOCK".equalsIgnoreCase(status)) {
            res.put("ok", false);
            res.put("reason", "OUT_OF_STOCK");
            return ResponseEntity.ok(res);
        }

        // Luật quick-add:
        // - Mặc định không topping
        // - Nếu product thuộc danh mục Bánh (id=3) => không chọn variant và topping
        Long variantId = null;
        boolean forceNoVariant = p.getCategory() != null && Objects.equals(p.getCategory().getId(), 3L);

        if (!forceNoVariant) {
            // Không truy cập p.getVariants() (LAZY). Lấy qua repository (đã sort theo giá tăng dần).
            List<ProductVariant> variants = variantRepo.findByProduct_IdOrderByPriceAsc(productId);
            if (variants != null && !variants.isEmpty()) {
                variantId = pickDefaultVariantId(variants);
            }
        }

        CartItem saved = cartService.addItem(cud.getUser(), p.getId(), variantId, qty, null);

        res.put("ok", true);
        res.put("itemId", saved != null ? saved.getId() : null);
        try {
            res.put("cartCount", cartService.listItems(cud.getUser()).size());
        } catch (Exception ignore) {}
        return ResponseEntity.ok(res);
    }

    /** Ưu tiên size M/Medium, nếu không có thì trả biến thể rẻ nhất (list đã OrderByPriceAsc). */
    private Long pickDefaultVariantId(List<ProductVariant> variants) {
        // Ưu tiên size M nếu tồn tại
        for (ProductVariant v : variants) {
            Size s = v.getSize();
            if (s == Size.M) return v.getId();
        }
        // Fallback: rẻ nhất
        return variants.get(0).getId();
    }
}