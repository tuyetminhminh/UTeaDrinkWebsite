package net.codejava.utea.engagement.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.codejava.utea.catalog.entity.Product;
import net.codejava.utea.catalog.repository.ProductRepository;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.repository.UserRepository;
import net.codejava.utea.engagement.entity.ViewedProduct;
import net.codejava.utea.engagement.repository.ViewedProductRepository;
import net.codejava.utea.engagement.service.ViewedProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ViewedProductServiceImpl implements ViewedProductService {

    private final ViewedProductRepository viewedProductRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;

    @Override
    public void trackView(User user, Product product) {
        if (user == null || product == null) {
            return;
        }

        try {
            // Tìm xem đã xem sản phẩm này chưa
            Optional<ViewedProduct> existing = viewedProductRepo.findByUserAndProduct(user, product);

            if (existing.isPresent()) {
                // Đã xem rồi -> Update thời gian
                ViewedProduct vp = existing.get();
                vp.touch(); // Update lastSeenAt
                viewedProductRepo.save(vp);
                log.debug("Updated view time for product {} by user {}", product.getId(), user.getId());
            } else {
                // Chưa xem -> Tạo mới
                ViewedProduct vp = ViewedProduct.builder()
                        .user(user)
                        .product(product)
                        .lastSeenAt(LocalDateTime.now())
                        .build();
                viewedProductRepo.save(vp);
                log.debug("Tracked new view for product {} by user {}", product.getId(), user.getId());
            }
        } catch (Exception e) {
            log.error("Error tracking view for product {} by user {}", 
                product.getId(), user.getId(), e);
            // Không throw exception để không ảnh hưởng đến flow chính
        }
    }

    @Override
    public void trackView(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return;
        }

        try {
            User user = userRepo.findById(userId).orElse(null);
            Product product = productRepo.findById(productId).orElse(null);

            if (user != null && product != null) {
                trackView(user, product);
            }
        } catch (Exception e) {
            log.error("Error tracking view for product {} by user {}", productId, userId, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ViewedProduct> getRecentlyViewed(Long userId, Pageable pageable) {
        return viewedProductRepo.findByUser_IdOrderByLastSeenAtDesc(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getRecentlyViewedProducts(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<ViewedProduct> viewed = viewedProductRepo.findByUser_IdOrderByLastSeenAtDesc(userId, pageable);
        
        // Lọc chỉ lấy sản phẩm AVAILABLE
        return viewed.getContent().stream()
                .map(ViewedProduct::getProduct)
                .filter(p -> p != null && "AVAILABLE".equals(p.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countViewed(Long userId) {
        return viewedProductRepo.countByUser_Id(userId);
    }

    @Override
    public void clearHistory(Long userId) {
        viewedProductRepo.deleteByUser_Id(userId);
        log.info("Cleared view history for user {}", userId);
    }
}

