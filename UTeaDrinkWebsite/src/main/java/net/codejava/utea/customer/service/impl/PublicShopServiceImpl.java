package net.codejava.utea.customer.service.impl;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.entity.Product;
import net.codejava.utea.catalog.repository.ProductRepository;
import net.codejava.utea.customer.dto.ProductCardDTO;
import net.codejava.utea.customer.dto.ProductImageDTO;
import net.codejava.utea.customer.dto.SectionDTO;
import net.codejava.utea.customer.service.PublicShopService;
import net.codejava.utea.order.entity.enums.OrderStatus;
import net.codejava.utea.order.repository.OrderItemRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicShopServiceImpl implements PublicShopService {

    private final ProductRepository productRepo;
    private final OrderItemRepository orderItemRepo;

    private static final int LIMIT = 8;

    @Override
    public List<SectionDTO> buildSections(Long shopId) {
        // Tính map số lượng bán theo productId từ các đơn DELIVERED (bán thực sự)
        Map<Long, Integer> soldMap = computeSoldCountMap(shopId);

        List<SectionDTO> sections = new ArrayList<>();

        // ===== TOP_SELLING theo soldCount (từ order items) =====
        var topIds = productRepo.findTopSellingIds(shopId, PageRequest.of(0, LIMIT));
        if (!topIds.isEmpty()) {
            var topProducts = orderByIds(productRepo.findByIdsWithImages(topIds), topIds);
            sections.add(new SectionDTO(
                    "TOP_SELLING",
                    "Bán chạy",
                    topProducts.stream().map(p -> toCard(p, soldMap)).toList()
            ));
        }

        // ===== NEW_ARRIVALS (sp mới) =====
        var newIds = productRepo.findNewestIds(shopId, PageRequest.of(0, LIMIT));
        if (!newIds.isEmpty()) {
            var newProducts = orderByIds(productRepo.findByIdsWithImages(newIds), newIds);
            sections.add(new SectionDTO(
                    "NEW_ARRIVALS",
                    "Mới ra mắt",
                    newProducts.stream().map(p -> toCard(p, soldMap)).toList()
            ));
        }

        return sections;
    }

    /** Tổng số lượng bán theo productId từ các đơn ở trạng thái cho phép */
    private Map<Long, Integer> computeSoldCountMap(Long shopId) {
        // Chỉ tính đơn DELIVERED (muốn cộng thêm PAID thì thêm vào EnumSet)
        var statuses = EnumSet.of(OrderStatus.DELIVERED);

        var rows = orderItemRepo.topBestSellersByShop(
                shopId,
                statuses,
                PageRequest.of(0, 500) // đủ lớn để gom toàn bộ
        );

        Map<Long, Integer> map = new HashMap<>();
        for (var r : rows) {
            var p = r.getProduct();
            if (p != null) {
                map.merge(p.getId(), r.getTotal().intValue(), Integer::sum);
            }
        }
        return map;
    }

    /** Sắp xếp theo thứ tự danh sách id đầu vào */
    private List<Product> orderByIds(List<Product> products, List<Long> ids) {
        Map<Long, Integer> pos = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) pos.put(ids.get(i), i);
        return products.stream()
                .sorted(Comparator.comparingInt(p -> pos.getOrDefault(p.getId(), Integer.MAX_VALUE)))
                .collect(Collectors.toList());
    }

    private ProductCardDTO toCard(Product p, Map<Long, Integer> soldMap) {
        String url = (p.getImages() != null && !p.getImages().isEmpty())
                ? p.getImages().get(0).getUrl()
                : "https://via.placeholder.com/300x300/f5f5f5/999999?text=No+Image";
        var images = List.of(new ProductImageDTO(url));

        int sold = soldMap.getOrDefault(p.getId(), 0);

        return new ProductCardDTO(
                p.getId(),
                p.getName(),
                p.getBasePrice(),
                p.getRatingAvg(), // có thể là BigDecimal, FE đã xử lý
                sold,             // >>> dùng sold thực tế từ orders
                images
        );
    }
}
