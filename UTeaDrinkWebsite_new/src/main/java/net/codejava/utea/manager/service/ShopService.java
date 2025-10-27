package net.codejava.utea.manager.service;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.repository.UserRepository;
import net.codejava.utea.manager.dto.*;
import net.codejava.utea.manager.entity.Shop;
import net.codejava.utea.manager.entity.ShopBanner;
import net.codejava.utea.manager.entity.ShopManager;
import net.codejava.utea.manager.entity.ShopSection;
import net.codejava.utea.manager.repository.ShopBannerRepository;
import net.codejava.utea.manager.repository.ShopManagerRepository;
import net.codejava.utea.manager.repository.ShopRepository;
import net.codejava.utea.manager.repository.ShopSectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepo;
    private final ShopManagerRepository shopManagerRepo;
    private final ShopBannerRepository bannerRepo;
    private final ShopSectionRepository sectionRepo;
    private final UserRepository userRepo;

    // ==================== SHOP CRUD ====================
    
    /**
     * Đăng ký shop mới (1 manager chỉ đăng ký 1 lần)
     */
    @Transactional
    public ShopDTO createShop(Long managerId, ShopDTO shopDTO) {
        // Kiểm tra manager đã có shop chưa
        if (shopManagerRepo.existsByManager_Id(managerId)) {
            throw new RuntimeException("Manager đã đăng ký shop rồi!");
        }

        User manager = userRepo.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager không tồn tại"));

        // Tạo shop mới
        Shop shop = Shop.builder()
                .name(shopDTO.getName())
                .address(shopDTO.getAddress())
                .phone(shopDTO.getPhone())
                .status("OPEN")
                .build();
        shop = shopRepo.save(shop);

        // Gán manager cho shop
        ShopManager shopManager = ShopManager.builder()
                .shop(shop)
                .manager(manager)
                .build();
        shopManagerRepo.save(shopManager);

        return convertToDTO(shop, manager);
    }

    /**
     * Lấy thông tin shop của manager
     */
    @Transactional(readOnly = true)
    public ShopDTO getShopByManagerId(Long managerId) {
        ShopManager shopManager = shopManagerRepo.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Manager chưa đăng ký shop"));

        Shop shop = shopManager.getShop();
        User manager = shopManager.getManager();

        return convertToDTO(shop, manager);
    }

    /**
     * Cập nhật thông tin shop
     */
    @Transactional
    public ShopDTO updateShop(Long managerId, ShopDTO shopDTO) {
        ShopManager shopManager = shopManagerRepo.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Manager chưa đăng ký shop"));

        Shop shop = shopManager.getShop();
        shop.setName(shopDTO.getName());
        shop.setAddress(shopDTO.getAddress());
        shop.setPhone(shopDTO.getPhone());
        shop.setStatus(shopDTO.getStatus());

        shop = shopRepo.save(shop);

        return convertToDTO(shop, shopManager.getManager());
    }

    // ==================== PUBLIC API ====================
    
    /**
     * Lấy tất cả banner ACTIVE của shop (dành cho khách hàng)
     */
    public List<ShopBannerDTO> getActiveBanners(Long shopId) {
        List<ShopBanner> banners = bannerRepo.findByShopIdAndActiveOrderBySortOrderAsc(shopId, true);
        return banners.stream()
                .map(this::convertBannerToDTO)
                .collect(Collectors.toList());
    }

    // ==================== BANNER CRUD ====================

    /**
     * Lấy tất cả banner của shop
     */
    @Transactional(readOnly = true)
    public List<ShopBannerDTO> getAllBanners(Long shopId) {
        return bannerRepo.findByShopIdOrderBySortOrderAsc(shopId).stream()
                .map(this::convertBannerToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Tạo banner mới
     */
    @Transactional
    public ShopBannerDTO createBanner(Long managerId, ShopBannerDTO bannerDTO) {
        ShopManager shopManager = shopManagerRepo.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Manager chưa đăng ký shop"));

        boolean isActive = bannerDTO.isActive();
        
        ShopBanner banner = ShopBanner.builder()
                .shop(shopManager.getShop())
                .title(bannerDTO.getTitle())
                .imageUrl(bannerDTO.getImageUrl())
                .link(bannerDTO.getLink())
                .sortOrder(bannerDTO.getSortOrder() != null ? bannerDTO.getSortOrder() : 0)
                .active(isActive)
                .build();

        banner = bannerRepo.save(banner);

        return convertBannerToDTO(banner);
    }

    /**
     * Cập nhật banner
     */
    @Transactional
    public ShopBannerDTO updateBanner(Long managerId, Long bannerId, ShopBannerDTO bannerDTO) {
        ShopManager shopManager = shopManagerRepo.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Manager chưa đăng ký shop"));

        ShopBanner banner = bannerRepo.findById(bannerId)
                .orElseThrow(() -> new RuntimeException("Banner không tồn tại"));

        // Kiểm tra banner có thuộc shop của manager không
        if (!banner.getShop().getId().equals(shopManager.getShop().getId())) {
            throw new RuntimeException("Không có quyền chỉnh sửa banner này");
        }

        banner.setTitle(bannerDTO.getTitle());
        banner.setImageUrl(bannerDTO.getImageUrl());
        banner.setLink(bannerDTO.getLink());
        banner.setSortOrder(bannerDTO.getSortOrder());
        banner.setActive(bannerDTO.isActive());

        banner = bannerRepo.save(banner);

        return convertBannerToDTO(banner);
    }

    /**
     * Xóa banner
     */
    @Transactional
    public void deleteBanner(Long managerId, Long bannerId) {
        ShopManager shopManager = shopManagerRepo.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Manager chưa đăng ký shop"));

        ShopBanner banner = bannerRepo.findById(bannerId)
                .orElseThrow(() -> new RuntimeException("Banner không tồn tại"));

        // Kiểm tra banner có thuộc shop của manager không
        if (!banner.getShop().getId().equals(shopManager.getShop().getId())) {
            throw new RuntimeException("Không có quyền xóa banner này");
        }

        bannerRepo.delete(banner);
    }

    // ==================== SECTION CRUD ====================

    /**
     * Lấy tất cả section của shop
     */
    @Transactional(readOnly = true)
    public List<ShopSectionDTO> getAllSections(Long shopId) {
        return sectionRepo.findByShopIdOrderBySortOrderAsc(shopId).stream()
                .map(this::convertSectionToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Tạo section mới
     */
    @Transactional
    public ShopSectionDTO createSection(Long managerId, ShopSectionDTO sectionDTO) {
        ShopManager shopManager = shopManagerRepo.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Manager chưa đăng ký shop"));

        ShopSection section = ShopSection.builder()
                .shop(shopManager.getShop())
                .title(sectionDTO.getTitle())
                .sectionType(sectionDTO.getSectionType())
                .contentJson(sectionDTO.getContentJson())
                .sortOrder(sectionDTO.getSortOrder() != null ? sectionDTO.getSortOrder() : 0)
                .isActive(sectionDTO.isActive())
                .build();

        section = sectionRepo.save(section);

        return convertSectionToDTO(section);
    }

    /**
     * Cập nhật section
     */
    @Transactional
    public ShopSectionDTO updateSection(Long managerId, Long sectionId, ShopSectionDTO sectionDTO) {
        ShopManager shopManager = shopManagerRepo.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Manager chưa đăng ký shop"));

        ShopSection section = sectionRepo.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section không tồn tại"));

        // Kiểm tra section có thuộc shop của manager không
        if (!section.getShop().getId().equals(shopManager.getShop().getId())) {
            throw new RuntimeException("Không có quyền chỉnh sửa section này");
        }

        section.setTitle(sectionDTO.getTitle());
        section.setSectionType(sectionDTO.getSectionType());
        section.setContentJson(sectionDTO.getContentJson());
        section.setSortOrder(sectionDTO.getSortOrder());
        section.setActive(sectionDTO.isActive());

        section = sectionRepo.save(section);

        return convertSectionToDTO(section);
    }

    /**
     * Xóa section
     */
    @Transactional
    public void deleteSection(Long managerId, Long sectionId) {
        ShopManager shopManager = shopManagerRepo.findByManager_Id(managerId)
                .orElseThrow(() -> new RuntimeException("Manager chưa đăng ký shop"));

        ShopSection section = sectionRepo.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section không tồn tại"));

        // Kiểm tra section có thuộc shop của manager không
        if (!section.getShop().getId().equals(shopManager.getShop().getId())) {
            throw new RuntimeException("Không có quyền xóa section này");
        }

        sectionRepo.delete(section);
    }

    // ==================== HELPER METHODS ====================

    private ShopDTO convertToDTO(Shop shop, User manager) {
        return ShopDTO.builder()
                .id(shop.getId())
                .name(shop.getName())
                .address(shop.getAddress())
                .phone(shop.getPhone())
                .status(shop.getStatus())
                .createdAt(shop.getCreatedAt())
                .updatedAt(shop.getUpdatedAt())
                .managerId(manager.getId())
                .managerName(manager.getFullName())
                .build();
    }

    private ShopBannerDTO convertBannerToDTO(ShopBanner banner) {
        return ShopBannerDTO.builder()
                .id(banner.getId())
                .shopId(banner.getShop().getId())
                .title(banner.getTitle())
                .imageUrl(banner.getImageUrl())
                .link(banner.getLink())
                .sortOrder(banner.getSortOrder())
                .active(banner.isActive())
                .createdAt(banner.getCreatedAt())
                .build();
    }

    private ShopSectionDTO convertSectionToDTO(ShopSection section) {
        return ShopSectionDTO.builder()
                .id(section.getId())
                .shopId(section.getShop().getId())
                .title(section.getTitle())
                .sectionType(section.getSectionType())
                .contentJson(section.getContentJson())
                .sortOrder(section.getSortOrder())
                .isActive(section.isActive())
                .createdAt(section.getCreatedAt())
                .build();
    }
}

