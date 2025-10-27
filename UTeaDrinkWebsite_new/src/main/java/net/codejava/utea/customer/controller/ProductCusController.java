package net.codejava.utea.customer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.entity.Product;
import net.codejava.utea.catalog.entity.ProductCategory;
import net.codejava.utea.catalog.entity.ProductVariant;
import net.codejava.utea.catalog.repository.ProductCategoryRepository;
import net.codejava.utea.catalog.repository.ProductRepository;
import net.codejava.utea.catalog.repository.ProductVariantRepository;
import net.codejava.utea.review.service.ReviewService;
import net.codejava.utea.review.view.ReviewView;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class ProductCusController {

    private final ProductRepository productRepo;
    private final ProductVariantRepository variantRepo;
    private final ProductCategoryRepository categoryRepo;
    private final ReviewService reviewService;
    private final ObjectMapper om = new ObjectMapper();

    // ------------------- MENU -------------------
    @GetMapping("/menu")
    public String menu(Model model,
                       @RequestParam(value = "page_best", defaultValue = "0") int pageBest,
                       @RequestParam Map<String, String> params) {

        final int PAGE_SIZE = 6;

        // 1) Best seller theo soldCount desc (AVAILABLE)
        Pageable bestPg = PageRequest.of(Math.max(0, pageBest), PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "soldCount"));
        Page<Product> bestSellers = productRepo.findByStatus("AVAILABLE", bestPg);

        // 2) Danh mục ACTIVE
        List<ProductCategory> categories = categoryRepo.findAll().stream()
                .filter(c -> "ACTIVE".equalsIgnoreCase(c.getStatus()))
                .collect(Collectors.toList());

        // 3) Mỗi danh mục phân trang 6 sp/trang
        Map<Long, Page<Product>> catPages = new LinkedHashMap<>();
        Map<Long, Integer> catPageNums = new LinkedHashMap<>();

        for (ProductCategory cat : categories) {
            String key = "page_c_" + cat.getId();
            int pageIdx = 0;
            if (params.containsKey(key)) {
                try { pageIdx = Math.max(0, Integer.parseInt(params.get(key))); }
                catch (NumberFormatException ignore) {}
            }
            Pageable pg = PageRequest.of(pageIdx, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Product> page = productRepo.findByCategory_IdAndStatus(cat.getId(), "AVAILABLE", pg);
            catPages.put(cat.getId(), page);
            catPageNums.put(cat.getId(), pageIdx);
        }

        model.addAttribute("bestSellers", bestSellers);
        model.addAttribute("pageBest", pageBest);
        model.addAttribute("categories", categories);
        model.addAttribute("catPages", catPages);
        model.addAttribute("catPageNums", catPageNums);

        return "customer/menu";
    }

    // ------------------- PRODUCT DETAIL -------------------
    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id,
                                @RequestParam(value = "rating", required = false) Integer rating,  // lọc sao
                                @RequestParam(value = "rp", defaultValue = "0") int reviewPage,    // trang review
                                @RequestParam(value = "back", required = false) String back,       // link quay lại
                                Model model) throws JsonProcessingException {

        Product product = productRepo.findByIdAndStatus(id, "AVAILABLE")
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại hoặc đã ẩn"));

        // Biến thể theo giá tăng dần
        List<ProductVariant> variants = variantRepo.findByProduct_IdOrderByPriceAsc(id);

        BigDecimal defaultPrice;
        Long defaultVariantId = null;
        Map<Long, BigDecimal> priceMap = new LinkedHashMap<>();

        if (variants != null && !variants.isEmpty()) {
            defaultPrice = variants.get(0).getPrice();
            defaultVariantId = variants.get(0).getId();
            for (ProductVariant v : variants) {
                priceMap.put(v.getId(), v.getPrice());
            }
        } else {
            defaultPrice = product.getBasePrice();
        }
        String priceMapJson = om.writeValueAsString(priceMap);

        // Related (tối đa 6)
        List<Product> related = (product.getCategory() != null)
                ? productRepo.findByCategory_IdAndStatus(
                product.getCategory().getId(),
                "AVAILABLE",
                PageRequest.of(0, 6)
        ).getContent()
                : Collections.emptyList();

        // Reviews (đã duyệt) + filter sao + phân trang
        final int PAGE_SIZE = 5;
        Page<ReviewView> reviews = reviewService.listApproved(
                product.getId(),
                rating,
                PageRequest.of(Math.max(0, reviewPage), PAGE_SIZE)
        );
        Double avg = reviewService.avgRating(product.getId());
        Map<Integer, Long> counts = reviewService.countByStars(product.getId()); // 1..5

        // ==== Chuẩn bị biến "an toàn" cho template ====
        String backUrl = (back == null || back.isBlank()) ? "/customer/menu" : back;

        List<Integer> stars = Arrays.asList(5, 4, 3, 2, 1);

        int currRp = reviews.getNumber();
        int totalRp = reviews.getTotalPages();
        int prevRp = (currRp > 0) ? (currRp - 1) : 0;
        int nextRp = (totalRp == 0) ? 0 : Math.min(currRp + 1, totalRp - 1);

        List<Integer> rpPages = new ArrayList<>();
        for (int i = 0; i < totalRp; i++) rpPages.add(i);

        // ==== Gán model ====
        model.addAttribute("product", product);
        model.addAttribute("variants", variants);
        model.addAttribute("defaultPrice", defaultPrice);
        model.addAttribute("defaultVariantId", defaultVariantId);
        model.addAttribute("priceMapJson", priceMapJson);
        model.addAttribute("relatedProducts", related);

        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", (avg == null) ? "0.0" : String.format(Locale.US, "%.1f", avg));
        model.addAttribute("ratingCounts", counts);
        model.addAttribute("selectedRating", rating);

        model.addAttribute("stars", stars);
        model.addAttribute("rp", currRp);
        model.addAttribute("totalRp", totalRp);
        model.addAttribute("prevRp", prevRp);
        model.addAttribute("nextRp", nextRp);
        model.addAttribute("rpPages", rpPages);

        model.addAttribute("backUrl", backUrl);

        return "customer/product";
    }
}
