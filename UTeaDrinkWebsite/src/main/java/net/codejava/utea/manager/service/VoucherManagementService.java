package net.codejava.utea.manager.service;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.manager.dto.VoucherManagementDTO;
import net.codejava.utea.manager.entity.Shop;
import net.codejava.utea.manager.entity.ShopManager;
import net.codejava.utea.manager.repository.ShopManagerRepository;
import net.codejava.utea.promotion.entity.Voucher;
import net.codejava.utea.promotion.entity.enums.PromoScope;
import net.codejava.utea.promotion.repository.VoucherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherManagementService {

    private final VoucherRepository voucherRepo;
    private final ShopManagerRepository shopManagerRepo;
    private final VoucherEmailService voucherEmailService;

    // ==================== VOUCHER CRUD ====================

    /**
     * Lấy tất cả voucher của shop
     */
    @Transactional(readOnly = true)
    public List<VoucherManagementDTO> getAllVouchers(Long managerId) {
        Shop shop = getShopByManagerId(managerId);
        
        // Lấy cả voucher của shop và global voucher
        List<Voucher> vouchers = voucherRepo.findAll().stream()
                .filter(v -> v.getScope() == PromoScope.GLOBAL || 
                           (v.getShop() != null && v.getShop().getId().equals(shop.getId())))
                .collect(Collectors.toList());
        
        return vouchers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết voucher
     */
    @Transactional(readOnly = true)
    public VoucherManagementDTO getVoucherById(Long managerId, Long voucherId) {
        Shop shop = getShopByManagerId(managerId);
        
        Voucher voucher = voucherRepo.findById(voucherId)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

        // Kiểm tra voucher có thuộc shop của manager không (hoặc là GLOBAL)
        if (voucher.getScope() != PromoScope.GLOBAL && 
            (voucher.getShop() == null || !voucher.getShop().getId().equals(shop.getId()))) {
            throw new RuntimeException("Không có quyền truy cập voucher này");
        }

        return convertToDTO(voucher);
    }

    /**
     * Tạo voucher mới cho shop
     */
    @Transactional
    public VoucherManagementDTO createVoucher(Long managerId, VoucherManagementDTO voucherDTO) {
        Shop shop = getShopByManagerId(managerId);

        // Kiểm tra code đã tồn tại chưa
        if (voucherRepo.findAll().stream().anyMatch(v -> v.getCode().equalsIgnoreCase(voucherDTO.getCode()))) {
            throw new RuntimeException("Mã voucher '" + voucherDTO.getCode() + "' đã tồn tại");
        }

        Voucher voucher = Voucher.builder()
                .code(voucherDTO.getCode().toUpperCase())
                .scope(PromoScope.SHOP) // Chỉ cho phép tạo voucher ở scope SHOP
                .shop(shop)
                .ruleJson(voucherDTO.getRuleJson())
                .forFirstOrder(voucherDTO.getForFirstOrder() != null ? voucherDTO.getForFirstOrder() : false)
                .forBirthday(voucherDTO.getForBirthday() != null ? voucherDTO.getForBirthday() : false)
                .activeFrom(voucherDTO.getActiveFrom())
                .activeTo(voucherDTO.getActiveTo())
                .status("ACTIVE")
                .usageLimit(voucherDTO.getUsageLimit())
                .usedCount(0)
                .build();

        voucher = voucherRepo.save(voucher);

        return convertToDTO(voucher);
    }

    /**
     * Cập nhật voucher
     */
    @Transactional
    public VoucherManagementDTO updateVoucher(Long managerId, Long voucherId, VoucherManagementDTO voucherDTO) {
        Shop shop = getShopByManagerId(managerId);

        Voucher voucher = voucherRepo.findById(voucherId)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

        // Kiểm tra voucher có thuộc shop của manager không
        if (voucher.getShop() == null || !voucher.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền chỉnh sửa voucher này");
        }

        // Không cho đổi code nếu đã có người dùng
        if (voucher.getUsedCount() > 0 && !voucher.getCode().equals(voucherDTO.getCode())) {
            throw new RuntimeException("Không thể đổi mã voucher đã được sử dụng");
        }

        voucher.setCode(voucherDTO.getCode().toUpperCase());
        voucher.setRuleJson(voucherDTO.getRuleJson());
        voucher.setForFirstOrder(voucherDTO.getForFirstOrder() != null ? voucherDTO.getForFirstOrder() : false);
        voucher.setForBirthday(voucherDTO.getForBirthday() != null ? voucherDTO.getForBirthday() : false);
        voucher.setActiveFrom(voucherDTO.getActiveFrom());
        voucher.setActiveTo(voucherDTO.getActiveTo());
        voucher.setUsageLimit(voucherDTO.getUsageLimit());
        voucher.setStatus(voucherDTO.getStatus());

        // Auto set EXHAUSTED nếu đã hết lượt
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            voucher.setStatus("EXHAUSTED");
        }

        voucher = voucherRepo.save(voucher);

        return convertToDTO(voucher);
    }

    /**
     * Xóa voucher
     */
    @Transactional
    public void deleteVoucher(Long managerId, Long voucherId) {
        Shop shop = getShopByManagerId(managerId);

        Voucher voucher = voucherRepo.findById(voucherId)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

        // Kiểm tra voucher có thuộc shop của manager không
        if (voucher.getShop() == null || !voucher.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền xóa voucher này");
        }

        // Không cho xóa nếu đã có người dùng
        if (voucher.getUsedCount() > 0) {
            throw new RuntimeException("Không thể xóa voucher đã được sử dụng. Hãy vô hiệu hóa thay vì xóa.");
        }

        voucherRepo.delete(voucher);
    }

    /**
     * Kích hoạt/Vô hiệu hóa voucher
     */
    @Transactional
    public VoucherManagementDTO toggleVoucherStatus(Long managerId, Long voucherId) {
        Shop shop = getShopByManagerId(managerId);

        Voucher voucher = voucherRepo.findById(voucherId)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

        // Kiểm tra voucher có thuộc shop của manager không
        if (voucher.getShop() == null || !voucher.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền chỉnh sửa voucher này");
        }

        // Toggle giữa ACTIVE và INACTIVE (không đụng EXHAUSTED)
        if ("ACTIVE".equals(voucher.getStatus())) {
            voucher.setStatus("INACTIVE");
        } else if ("INACTIVE".equals(voucher.getStatus())) {
            voucher.setStatus("ACTIVE");
        }
        
        voucher = voucherRepo.save(voucher);

        return convertToDTO(voucher);
    }

    /**
     * Gửi email voucher cho khách hàng phù hợp điều kiện
     */
    @Transactional
    public int broadcastVoucher(Long managerId, Long voucherId) {
        Shop shop = getShopByManagerId(managerId);

        Voucher voucher = voucherRepo.findById(voucherId)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

        // Kiểm tra voucher có thuộc shop của manager không
        if (voucher.getShop() == null || !voucher.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Không có quyền gửi voucher này");
        }

        // Kiểm tra voucher phải ACTIVE
        if (!"ACTIVE".equals(voucher.getStatus())) {
            throw new RuntimeException("Chỉ có thể gửi voucher đang hoạt động");
        }

        // Gửi email qua VoucherEmailService
        return voucherEmailService.broadcastVoucher(voucher, shop);
    }

    // ==================== HELPER METHODS ====================

    @Transactional(readOnly = true)
    private Shop getShopByManagerId(Long managerId) {
        ShopManager shopManager = shopManagerRepo.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Manager chưa đăng ký shop"));
        return shopManager.getShop();
    }

    private VoucherManagementDTO convertToDTO(Voucher voucher) {
        // Voucher GLOBAL (toàn hệ thống) không cho manager edit
        boolean isEditable = voucher.getScope() == PromoScope.SHOP && voucher.getShop() != null;
        
        return VoucherManagementDTO.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .scope(voucher.getScope().name())
                .shopId(voucher.getShop() != null ? voucher.getShop().getId() : null)
                .shopName(voucher.getShop() != null ? voucher.getShop().getName() : "Toàn hệ thống")
                .ruleJson(voucher.getRuleJson())
                .forFirstOrder(voucher.getForFirstOrder())
                .forBirthday(voucher.getForBirthday())
                .activeFrom(voucher.getActiveFrom())
                .activeTo(voucher.getActiveTo())
                .status(voucher.getStatus())
                .usageLimit(voucher.getUsageLimit())
                .usedCount(voucher.getUsedCount())
                .isEditable(isEditable)
                .build();
    }
}

