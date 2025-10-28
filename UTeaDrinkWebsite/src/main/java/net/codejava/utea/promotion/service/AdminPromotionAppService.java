package net.codejava.utea.promotion.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.manager.repository.ShopRepository;
import net.codejava.utea.promotion.dto.PromotionForm;
import net.codejava.utea.promotion.entity.Promotion;
import net.codejava.utea.promotion.entity.enums.PromoScope;
import net.codejava.utea.promotion.repository.PromotionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminPromotionAppService {

    private final PromotionRepository promoRepo;
    private final ShopRepository shopRepo;
    private final ObjectMapper objectMapper; // Spring Boot tự động cung cấp bean này

    public void create(PromotionForm form) {
        Promotion promo = new Promotion();
        mapFormToEntity(form, promo);
        promoRepo.save(promo);
    }

    public void update(Long id, PromotionForm form) {
        Promotion promo = findOrThrow(id);
        mapFormToEntity(form, promo);
        promoRepo.save(promo);
    }

    public PromotionForm findForForm(Long id) {
        Promotion promo = findOrThrow(id);
        return mapEntityToForm(promo);
    }

    public void delete(Long id) {
        if (!promoRepo.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy khuyến mãi ID: " + id);
        }
        promoRepo.deleteById(id);
    }
    
    public Promotion findOrThrow(Long id) {
         return promoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khuyến mãi ID: " + id));
    }


    private void mapFormToEntity(PromotionForm form, Promotion promo) {
        promo.setTitle(form.getTitle());
        promo.setDescription(form.getDescription());
        promo.setScope(form.getScope());
        promo.setType(form.getType());
        promo.setActiveFrom(form.getActiveFrom());
        promo.setActiveTo(form.getActiveTo());
        promo.setStatus(form.getStatus());

        if (form.getScope() == PromoScope.SHOP) {
            if (form.getShopId() == null) {
                throw new IllegalArgumentException("Vui lòng chọn cửa hàng cho khuyến mãi.");
            }
            var shop = shopRepo.findById(form.getShopId())
                    .orElseThrow(() -> new IllegalArgumentException("Cửa hàng không hợp lệ."));
            promo.setShop(shop);
        } else {
            promo.setShop(null);
        }

        // Serialize rules to JSON
        Map<String, Object> rules = new HashMap<>();
        addIfNotNull(rules, "minTotal", form.getMinTotal());
        addIfNotNull(rules, "onlyNewUser", form.getOnlyNewUser());
        addIfNotNull(rules, "percentOff", form.getPercentOff());
        addIfNotNull(rules, "amountCap", form.getAmountCap());
        addIfNotNull(rules, "amountOff", form.getAmountOff());
        addIfNotNull(rules, "shipDiscountAmount", form.getShipDiscountAmount());
        addIfNotNull(rules, "isFreeShip", form.getIsFreeShip());
        addIfNotNull(rules, "categoryIds", form.getCategoryIds());
        addIfNotNull(rules, "productIds", form.getProductIds());
        
        try {
            promo.setRuleJson(objectMapper.writeValueAsString(rules));
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi chuyển đổi quy tắc sang JSON", e);
        }
    }

    private PromotionForm mapEntityToForm(Promotion promo) {
        PromotionForm form = new PromotionForm();
        form.setId(promo.getId());
        form.setTitle(promo.getTitle());
        form.setDescription(promo.getDescription());
        form.setScope(promo.getScope());
        form.setType(promo.getType());
        form.setActiveFrom(promo.getActiveFrom());
        form.setActiveTo(promo.getActiveTo());
        form.setStatus(promo.getStatus());

        if (promo.getShop() != null) {
            form.setShopId(promo.getShop().getId());
        }

        // Deserialize JSON to rules
        if (promo.getRuleJson() != null && !promo.getRuleJson().isBlank()) {
            try {
                TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {};
                Map<String, Object> rules = objectMapper.readValue(promo.getRuleJson(), typeRef);
                
                form.setMinTotal(getDecimal(rules, "minTotal"));
                form.setOnlyNewUser((Boolean) rules.get("onlyNewUser"));
                form.setPercentOff((Integer) rules.get("percentOff"));
                form.setAmountCap(getDecimal(rules, "amountCap"));
                form.setAmountOff(getDecimal(rules, "amountOff"));
                form.setShipDiscountAmount(getDecimal(rules, "shipDiscountAmount"));
                form.setIsFreeShip((Boolean) rules.get("isFreeShip"));
                form.setCategoryIds((List<Long>) rules.get("categoryIds"));
                form.setProductIds((List<Long>) rules.get("productIds"));

            } catch (Exception e) {
                // Log the error, maybe show a warning to the user
                System.err.println("Lỗi khi đọc JSON từ DB: " + e.getMessage());
            }
        }
        return form;
    }
    
    // --- Helper methods ---
    private void addIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            if (value instanceof List && ((List<?>) value).isEmpty()) {
                return; // Không thêm list rỗng
            }
            map.put(key, value);
        }
    }

    private java.math.BigDecimal getDecimal(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        return new java.math.BigDecimal(val.toString());
    }
}