package net.codejava.utea.promotion.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.manager.repository.ShopRepository;
import net.codejava.utea.promotion.dto.VoucherForm;
import net.codejava.utea.promotion.entity.Voucher;
import net.codejava.utea.promotion.entity.enums.PromoScope;
import net.codejava.utea.promotion.repository.VoucherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminVoucherAppService {

    private final VoucherRepository voucherRepo;
    private final ShopRepository shopRepo;
    private final ObjectMapper objectMapper;

    public void create(VoucherForm form) {
        // Kiểm tra mã voucher đã tồn tại chưa
        if (voucherRepo.findByCodeActiveNow(form.getCode(), java.time.LocalDateTime.now()).isPresent()) {
            throw new IllegalArgumentException("Mã voucher đã tồn tại: " + form.getCode());
        }

        Voucher voucher = new Voucher();
        mapFormToEntity(form, voucher);
        voucherRepo.save(voucher);
    }

    public void update(Long id, VoucherForm form) {
        Voucher voucher = findOrThrow(id);
        
        // Kiểm tra nếu đổi mã voucher, phải đảm bảo mã mới không trùng
        if (!voucher.getCode().equals(form.getCode())) {
            if (voucherRepo.findByCodeActiveNow(form.getCode(), java.time.LocalDateTime.now()).isPresent()) {
                throw new IllegalArgumentException("Mã voucher đã tồn tại: " + form.getCode());
            }
        }
        
        mapFormToEntity(form, voucher);
        voucherRepo.save(voucher);
    }

    public VoucherForm findForForm(Long id) {
        Voucher voucher = findOrThrow(id);
        return mapEntityToForm(voucher);
    }

    public void delete(Long id) {
        if (!voucherRepo.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy voucher ID: " + id);
        }
        voucherRepo.deleteById(id);
    }

    public Voucher findOrThrow(Long id) {
        return voucherRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher ID: " + id));
    }

    private void mapFormToEntity(VoucherForm form, Voucher voucher) {
        voucher.setCode(form.getCode().toUpperCase()); // Đảm bảo mã luôn là chữ hoa
        voucher.setScope(form.getScope());
        voucher.setActiveFrom(form.getActiveFrom());
        voucher.setActiveTo(form.getActiveTo());
        voucher.setStatus(form.getStatus());
        voucher.setUsageLimit(form.getUsageLimit());
        voucher.setForFirstOrder(form.getForFirstOrder());
        voucher.setForBirthday(form.getForBirthday());

        if (form.getScope() == PromoScope.SHOP) {
            if (form.getShopId() == null) {
                throw new IllegalArgumentException("Vui lòng chọn cửa hàng cho voucher.");
            }
            var shop = shopRepo.findById(form.getShopId())
                    .orElseThrow(() -> new IllegalArgumentException("Cửa hàng không hợp lệ."));
            voucher.setShop(shop);
        } else {
            voucher.setShop(null);
        }

        // Serialize rules to JSON
        Map<String, Object> rules = new HashMap<>();
        addIfNotNull(rules, "minTotal", form.getMinTotal());
        addIfNotNull(rules, "percentOff", form.getPercentOff());
        addIfNotNull(rules, "amountCap", form.getAmountCap());
        addIfNotNull(rules, "amountOff", form.getAmountOff());
        addIfNotNull(rules, "shipDiscountAmount", form.getShipDiscountAmount());
        addIfNotNull(rules, "isFreeShip", form.getIsFreeShip());
        addIfNotNull(rules, "categoryIds", form.getCategoryIds());
        addIfNotNull(rules, "productIds", form.getProductIds());

        try {
            voucher.setRuleJson(objectMapper.writeValueAsString(rules));
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi chuyển đổi quy tắc sang JSON", e);
        }
    }

    private VoucherForm mapEntityToForm(Voucher voucher) {
        VoucherForm form = new VoucherForm();
        form.setId(voucher.getId());
        form.setCode(voucher.getCode());
        form.setScope(voucher.getScope());
        form.setActiveFrom(voucher.getActiveFrom());
        form.setActiveTo(voucher.getActiveTo());
        form.setStatus(voucher.getStatus());
        form.setUsageLimit(voucher.getUsageLimit());
        form.setForFirstOrder(voucher.getForFirstOrder());
        form.setForBirthday(voucher.getForBirthday());

        if (voucher.getShop() != null) {
            form.setShopId(voucher.getShop().getId());
        }

        // Deserialize JSON to rules
        if (voucher.getRuleJson() != null && !voucher.getRuleJson().isBlank()) {
            try {
                TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {};
                Map<String, Object> rules = objectMapper.readValue(voucher.getRuleJson(), typeRef);

                form.setMinTotal(getDecimal(rules, "minTotal"));
                form.setPercentOff((Integer) rules.get("percentOff"));
                form.setAmountCap(getDecimal(rules, "amountCap"));
                form.setAmountOff(getDecimal(rules, "amountOff"));
                form.setShipDiscountAmount(getDecimal(rules, "shipDiscountAmount"));
                form.setIsFreeShip((Boolean) rules.get("isFreeShip"));
                form.setCategoryIds(getLongList(rules, "categoryIds"));
                form.setProductIds(getLongList(rules, "productIds"));

            } catch (Exception e) {
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

    @SuppressWarnings("unchecked")
    private List<Long> getLongList(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        return (List<Long>) val;
    }
}

