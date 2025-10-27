package net.codejava.utea.manager.service;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.manager.dto.PromotionManagementDTO;
import net.codejava.utea.manager.entity.Shop;
import net.codejava.utea.manager.entity.ShopManager;
import net.codejava.utea.manager.repository.ShopManagerRepository;
import net.codejava.utea.promotion.entity.Promotion;
import net.codejava.utea.promotion.entity.enums.PromoScope;
import net.codejava.utea.promotion.entity.enums.PromoType;
import net.codejava.utea.promotion.repository.PromotionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionManagementService {

    private final PromotionRepository promotionRepo;
    private final ShopManagerRepository shopManagerRepo;

    // ==================== PROMOTION CRUD ====================

    /**
     * Lấy tất cả khuyến mãi: của shop + toàn hệ thống (cả ACTIVE và INACTIVE)
     * Promotion toàn hệ thống (GLOBAL) sẽ có isEditable = false
     */
    @Transactional(readOnly = true)
    public List<PromotionManagementDTO> getAllPromotions(Long managerId) {
        Shop shop = getShopByManagerId(managerId);
        
        // Lấy cả promotion của shop và toàn hệ thống (GLOBAL scope)
        List<Promotion> promotions = promotionRepo.findAll().stream()
                .filter(p -> p.getScope() == PromoScope.GLOBAL || 
                           (p.getShop() != null && p.getShop().getId().equals(shop.getId())))
                .collect(Collectors.toList());
        
        return promotions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết khuyến mãi
     */
    @Transactional(readOnly = true)
    public PromotionManagementDTO getPromotionById(Long managerId, Long promotionId) {
        Shop shop = getShopByManagerId(managerId);
        
        Promotion promotion = promotionRepo.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Khuyến mãi không tồn tại"));

        // Kiểm tra khuyến mãi có thuộc shop của manager không
        if (promotion.getShop() == null || !promotion.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền truy cập khuyến mãi này");
        }

        return convertToDTO(promotion);
    }

    /**
     * Tạo khuyến mãi mới cho shop
     */
    @Transactional
    public PromotionManagementDTO createPromotion(Long managerId, PromotionManagementDTO promotionDTO) {
        Shop shop = getShopByManagerId(managerId);

        Promotion promotion = Promotion.builder()
                .scope(PromoScope.SHOP) // Chỉ cho phép tạo promotion ở scope SHOP
                .shop(shop)
                .type(PromoType.valueOf(promotionDTO.getType()))
                .ruleJson(promotionDTO.getRuleJson())
                .title(promotionDTO.getTitle())
                .description(promotionDTO.getDescription())
                .activeFrom(promotionDTO.getActiveFrom())
                .activeTo(promotionDTO.getActiveTo())
                .status("ACTIVE")
                .build();

        promotion = promotionRepo.save(promotion);

        return convertToDTO(promotion);
    }

    /**
     * Cập nhật khuyến mãi
     */
    @Transactional
    public PromotionManagementDTO updatePromotion(Long managerId, Long promotionId, PromotionManagementDTO promotionDTO) {
        Shop shop = getShopByManagerId(managerId);

        Promotion promotion = promotionRepo.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Khuyến mãi không tồn tại"));

        // Kiểm tra khuyến mãi có thuộc shop của manager không
        if (promotion.getShop() == null || !promotion.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền chỉnh sửa khuyến mãi này");
        }

        promotion.setType(PromoType.valueOf(promotionDTO.getType()));
        promotion.setRuleJson(promotionDTO.getRuleJson());
        promotion.setTitle(promotionDTO.getTitle());
        promotion.setDescription(promotionDTO.getDescription());
        promotion.setActiveFrom(promotionDTO.getActiveFrom());
        promotion.setActiveTo(promotionDTO.getActiveTo());
        promotion.setStatus(promotionDTO.getStatus());

        promotion = promotionRepo.save(promotion);

        return convertToDTO(promotion);
    }

    /**
     * Xóa khuyến mãi
     */
    @Transactional
    public void deletePromotion(Long managerId, Long promotionId) {
        Shop shop = getShopByManagerId(managerId);

        Promotion promotion = promotionRepo.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Khuyến mãi không tồn tại"));

        // Kiểm tra khuyến mãi có thuộc shop của manager không
        if (promotion.getShop() == null || !promotion.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền xóa khuyến mãi này");
        }

        promotionRepo.delete(promotion);
    }

    /**
     * Kích hoạt/Vô hiệu hóa khuyến mãi
     */
    @Transactional
    public PromotionManagementDTO togglePromotionStatus(Long managerId, Long promotionId) {
        Shop shop = getShopByManagerId(managerId);

        Promotion promotion = promotionRepo.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Khuyến mãi không tồn tại"));

        // Kiểm tra khuyến mãi có thuộc shop của manager không
        if (promotion.getShop() == null || !promotion.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền chỉnh sửa khuyến mãi này");
        }

        promotion.setStatus(promotion.getStatus().equals("ACTIVE") ? "INACTIVE" : "ACTIVE");
        promotion = promotionRepo.save(promotion);

        return convertToDTO(promotion);
    }

    // ==================== HELPER METHODS ====================

    @Transactional(readOnly = true)
    private Shop getShopByManagerId(Long managerId) {
        ShopManager shopManager = shopManagerRepo.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Manager chưa đăng ký shop"));
        return shopManager.getShop();
    }

    private PromotionManagementDTO convertToDTO(Promotion promotion) {
        // Promotion GLOBAL (toàn hệ thống) không cho manager edit
        boolean isEditable = promotion.getScope() == PromoScope.SHOP && promotion.getShop() != null;
        
        return PromotionManagementDTO.builder()
                .id(promotion.getId())
                .scope(promotion.getScope().name())
                .shopId(promotion.getShop() != null ? promotion.getShop().getId() : null)
                .shopName(promotion.getShop() != null ? promotion.getShop().getName() : "Toàn hệ thống")
                .type(promotion.getType().name())
                .ruleJson(promotion.getRuleJson())
                .title(promotion.getTitle())
                .description(promotion.getDescription())
                .activeFrom(promotion.getActiveFrom())
                .activeTo(promotion.getActiveTo())
                .status(promotion.getStatus())
                .isEditable(isEditable)
                .build();
    }
}

