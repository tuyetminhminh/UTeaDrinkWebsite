package net.codejava.utea.promotion.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.promotion.service.model.PromoRule;
import net.codejava.utea.promotion.entity.Promotion;
import net.codejava.utea.promotion.entity.enums.PromoScope;
import net.codejava.utea.promotion.repository.PromotionRepository;
import net.codejava.utea.promotion.service.CustomerPromotionService;
import net.codejava.utea.promotion.view.PromotionCardVM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerPromotionServiceImpl implements CustomerPromotionService {

    private final PromotionRepository promotionRepo;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<PromotionCardVM> getActivePromotions(Long shopId, String keyword, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        
        // Lấy tất cả promotions
        List<Promotion> allPromotions = promotionRepo.findAll();
        
        // Filter: ACTIVE, trong khoảng thời gian, và theo shopId/GLOBAL
        List<Promotion> filtered = allPromotions.stream()
                .filter(p -> "ACTIVE".equals(p.getStatus()))
                .filter(p -> p.getActiveFrom() == null || p.getActiveFrom().isBefore(now) || p.getActiveFrom().isEqual(now))
                .filter(p -> p.getActiveTo() == null || p.getActiveTo().isAfter(now) || p.getActiveTo().isEqual(now))
                .filter(p -> {
                    if (shopId == null) {
                        // Nếu không chỉ định shop, chỉ lấy GLOBAL
                        return p.getScope() == PromoScope.GLOBAL;
                    } else {
                        // Lấy cả GLOBAL và SHOP của shopId này
                        return p.getScope() == PromoScope.GLOBAL || 
                               (p.getScope() == PromoScope.SHOP && p.getShop() != null && p.getShop().getId().equals(shopId));
                    }
                })
                .filter(p -> {
                    // Filter theo keyword
                    if (keyword == null || keyword.isBlank()) {
                        return true;
                    }
                    String lower = keyword.toLowerCase();
                    String title = p.getTitle() != null ? p.getTitle().toLowerCase() : "";
                    String desc = p.getDescription() != null ? p.getDescription().toLowerCase() : "";
                    return title.contains(lower) || desc.contains(lower);
                })
                .sorted((p1, p2) -> {
                    // Sort by createdAt DESC (mới nhất trước) - tạm dùng activeFrom
                    LocalDateTime d1 = p1.getActiveFrom() != null ? p1.getActiveFrom() : LocalDateTime.MIN;
                    LocalDateTime d2 = p2.getActiveFrom() != null ? p2.getActiveFrom() : LocalDateTime.MIN;
                    return d2.compareTo(d1);
                })
                .collect(Collectors.toList());
        
        // Convert to VM
        List<PromotionCardVM> allCards = filtered.stream()
                .map(this::toVM)
                .collect(Collectors.toList());
        
        // Phân trang thủ công
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allCards.size());
        List<PromotionCardVM> pageContent = allCards.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, allCards.size());
    }

    private PromotionCardVM toVM(Promotion p) {
        PromoRule rule = readRule(p.getRuleJson());
        
        String shopName = (p.getShop() != null) ? p.getShop().getName() : null;
        
        String typeText;
        if (Boolean.TRUE.equals(rule.getFreeShip())) {
            typeText = "Miễn phí ship";
        } else if (rule.getPercentOff() != null && rule.getPercentOff() > 0) {
            typeText = "Giảm %";
        } else if (rule.getAmountOff() != null && rule.getAmountOff().signum() > 0) {
            typeText = "Giảm tiền";
        } else {
            typeText = "Ưu đãi";
        }
        
        return PromotionCardVM.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .scope(p.getScope().name())
                .shopName(shopName)
                .status(p.getStatus())
                .type(p.getType().name())
                .freeShip(Boolean.TRUE.equals(rule.getFreeShip()))
                .percentOff(rule.getPercentOff())
                .amountOff(rule.getAmountOff())
                .amountCap(rule.getAmountCap())
                .minTotal(rule.getMinTotal())
                .shipDiscountAmount(null) // PromoRule doesn't have shipDiscountAmount yet
                .activeFrom(p.getActiveFrom())
                .activeTo(p.getActiveTo())
                .typeText(typeText)
                .build();
    }

    private PromoRule readRule(String json) {
        try {
            return (json == null || json.isBlank()) ? new PromoRule() : objectMapper.readValue(json, PromoRule.class);
        } catch (Exception e) {
            return new PromoRule();
        }
    }
}

