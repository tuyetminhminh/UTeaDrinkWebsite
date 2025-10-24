package net.codejava.utea.customer.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.entity.Product;
import net.codejava.utea.catalog.service.ProductService;
import net.codejava.utea.catalog.service.ProductVariantService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * Controller cho khách hàng (CUSTOMER) xem menu, xem chi tiết sản phẩm.
 * URL base: /customer
 */
@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class ProductCusController {

    private final ProductService productService;
    private final ProductVariantService variantService;

    /**
     * ✅ Trang hiển thị menu danh sách sản phẩm.
     * URL: /customer/menu
     * Query params:
     *  - categoryId: lọc theo danh mục (tùy chọn)
     *  - page: số trang (mặc định 0)
     *  - size: số lượng mỗi trang (mặc định 12)
     */
    @GetMapping("/menu")
    public String listProducts(Model model,
                               @RequestParam(required = false) Long categoryId,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Product> products = (categoryId == null)
                ? productService.getAllAvailableProductsPaged(pageable)
                : productService.getProductsByCategoryPaged(categoryId, pageable);

        model.addAttribute("products", products);
        model.addAttribute("currentCategoryId", categoryId);
        model.addAttribute("title", (categoryId == null ? "Tất cả sản phẩm" : "Danh mục được chọn"));

        return "customer/menu"; // ➜ templates/customer/menu.html
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product product = productService.findAvailableById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        // ✅ Ép load category để tránh proxy lỗi
        if (product.getCategory() != null)
            product.getCategory().getCategoryName(); // trigger load

        model.addAttribute("product", product);
        model.addAttribute("variants", variantService.findActiveByProduct(id));

        List<Product> related = product.getCategory() != null
                ? productService.getProductsByCategory(product.getCategory().getCategoryId())
                : Collections.emptyList();
        model.addAttribute("relatedProducts", related);

        return "customer/product";
    }


}
