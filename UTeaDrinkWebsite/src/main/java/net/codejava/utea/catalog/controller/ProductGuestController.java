package net.codejava.utea.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.entity.Product;
import net.codejava.utea.catalog.entity.ProductCategory;
import net.codejava.utea.catalog.repository.ProductCategoryRepository;
import net.codejava.utea.catalog.service.ProductService;
import net.codejava.utea.catalog.service.ProductVariantService;

import java.math.BigDecimal;
import java.util.List;

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
	private final ProductVariantService variantService;
	private final ObjectMapper objectMapper;

	@GetMapping({ "/guest/products", "/GuestProducts" }) // alias để khớp link cũ
	public String listProducts(@RequestParam(value = "q", required = false) String q,
			@RequestParam(value = "categoryId", required = false) Long categoryId,
			@RequestParam(value = "min", required = false) BigDecimal min,
			@RequestParam(value = "max", required = false) BigDecimal max,
			@RequestParam(value = "sort", required = false, defaultValue = "new") String sort,
			@RequestParam(value = "page", defaultValue = "0") int page, Model model) {

		int size = 20; // theo yêu cầu thầy
		Pageable pageable = PageRequest.of(page, size);

		Page<Product> productPage = productService.search(q, categoryId, min, max, sort, pageable);

		model.addAttribute("categories", categoryRepo.findAll()); // repo hiện có findAll()
		model.addAttribute("products", productPage.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", productPage.getTotalPages());

		// giữ lại filter để render
		model.addAttribute("q", q);
		model.addAttribute("selectedCategoryId", categoryId);
		model.addAttribute("min", min);
		model.addAttribute("max", max);
		model.addAttribute("sort", sort);

		return "guest/products";
	}

	@GetMapping("/customer/menu")
	public String menuPage(Model model) {
		// categoryRepo chưa có findByStatus => lọc tạm trên bộ nhớ theo field 'status'
		List<ProductCategory> activeCats = categoryRepo.findAll().stream()
				.filter(c -> "ACTIVE".equalsIgnoreCase(c.getStatus())).toList();

		model.addAttribute("categories", activeCats);
		model.addAttribute("bestSellers", productService.getTop6BestSellerFromOrders());
		// LƯU Ý: ProductService.getByCategoryName() đã đổi sang dùng field 'name'
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
			model.addAttribute("defaultPrice", product.getBasePrice()); // entity dùng basePrice
		} else {
			var def = variantService.cheapest(variants);
			if (def == null)
				def = variants.get(0);

			var map = new java.util.LinkedHashMap<Long, java.math.BigDecimal>();
			variants.forEach(v -> map.put(v.getId(), v.getPrice()));

			model.addAttribute("variants", variants);
			model.addAttribute("defaultVariantId", def.getId());
			model.addAttribute("defaultPrice", def.getPrice());
			model.addAttribute("priceMapJson", objectMapper.writeValueAsString(map)); // dùng bean đã inject
		}

		model.addAttribute("product", product);
		model.addAttribute("useCustomerCSS", true);
		return "customer/product-detail";
	}
}
