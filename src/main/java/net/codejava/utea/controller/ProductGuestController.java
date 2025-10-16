package net.codejava.utea.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.entity.Product;
import net.codejava.utea.repository.ProductCategoryRepository;
import net.codejava.utea.service.ProductService;
import net.codejava.utea.customer.service.VariantService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ProductGuestController {

    private final ProductService productService;
    private final ProductCategoryRepository categoryRepo;
    private final VariantService variantService;
    private final ObjectMapper objectMapper;

    @GetMapping("/guest/products")
    public String listProducts(@RequestParam(value = "categoryId", required = false) Long categoryId,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "8") int size,
                               Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = (categoryId != null)
                ? productService.getProductsByCategoryPaged(categoryId, pageable)
                : productService.getAllAvailableProductsPaged(pageable);

        model.addAttribute("categories", categoryRepo.findAll());
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("categoryId", categoryId);

        return "guest/products";
    }

    @GetMapping("/customer/menu")
    public String menuPage(Model model) {
        model.addAttribute("categories", categoryRepo.findByStatus("ACTIVE"));
        model.addAttribute("bestSellers", productService.getTop6BestSellerFromOrders());
        model.addAttribute("milkTea", productService.getByCategoryName("Trà sữa"));
        model.addAttribute("juice", productService.getByCategoryName("Nước ép"));
        model.addAttribute("cake", productService.getByCategoryName("Bánh"));
        model.addAttribute("useCustomerCSS", true);
        return "customer/menu";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) throws Exception {
        var product = productService.findAvailableById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        var variants = variantService.findActiveByProduct(id);
        if (variants.isEmpty()) {
            model.addAttribute("variants", java.util.List.of());
            model.addAttribute("defaultPrice", product.getPrice());
        } else {
            var def = variantService.cheapest(variants);
            if (def == null) def = variants.get(0);

            var map = new java.util.LinkedHashMap<Long, java.math.BigDecimal>();
            variants.forEach(v -> map.put(v.getId(), v.getPrice()));
            model.addAttribute("variants", variants);
            model.addAttribute("defaultVariantId", def.getId());
            model.addAttribute("defaultPrice", def.getPrice());
            model.addAttribute("priceMapJson", new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(map));
        }

        model.addAttribute("product", product);
        model.addAttribute("useCustomerCSS", true);
        return "customer/product-detail";
    }
}
