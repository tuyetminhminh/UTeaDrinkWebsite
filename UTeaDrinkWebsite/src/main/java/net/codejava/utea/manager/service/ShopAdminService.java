package net.codejava.utea.manager.service;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.repository.UserRepository;
import net.codejava.utea.manager.dto.ShopDTO;
import net.codejava.utea.manager.entity.Shop;
import net.codejava.utea.manager.entity.ShopManager;
import net.codejava.utea.manager.repository.ShopManagerRepository;
import net.codejava.utea.manager.repository.ShopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopAdminService {

    private final ShopRepository shopRepo;
    private final ShopManagerRepository shopManagerRepo;
    private final UserRepository userRepo;

    // ---- helpers ----
    public void normalize(ShopDTO f) {
        if (f.getName() != null)
            f.setName(f.getName().trim());
        if (f.getAddress() != null)
            f.setAddress(f.getAddress().trim());
        if (f.getPhone() != null)
            f.setPhone(f.getPhone().trim());
        if (!StringUtils.hasText(f.getStatus()))
            f.setStatus("OPEN");
    }

    public void validateUnique(ShopDTO form, Long excludeId, BindingResult br) {
        Long idNot = excludeId == null ? -1L : excludeId;
        if (!StringUtils.hasText(form.getName())) {
            br.rejectValue("name", "required", "Tên cửa hàng là bắt buộc");
        } else if (shopRepo.existsByNameIgnoreCaseAndIdNot(form.getName(), idNot)) {
            br.rejectValue("name", "duplicate", "Tên cửa hàng đã tồn tại");
        }
        if (StringUtils.hasText(form.getPhone()) &&
                shopRepo.existsByPhoneAndIdNot(form.getPhone(), idNot)) {
            br.rejectValue("phone", "duplicate", "Số điện thoại đã tồn tại");
        }
        if (StringUtils.hasText(form.getAddress()) &&
                shopRepo.existsByAddressIgnoreCaseAndIdNot(form.getAddress(), idNot)) {
            br.rejectValue("address", "duplicate", "Địa chỉ đã tồn tại");
        }
    }

    /**
     * Kiểm tra lựa chọn manager:
     * - nếu có managerId: phải tồn tại, có vai trò MANAGER (tuỳ hệ thống), và không
     * đang quản lý shop khác
     * - khi update: cho phép giữ nguyên manager hiện tại
     */
    public void validateManagerChoice(ShopDTO form, Long editingShopId, BindingResult br) {
        if (form.getManagerId() == null)
            return; // cho phép không gán

        Optional<User> ou = userRepo.findById(form.getManagerId());
        if (ou.isEmpty()) {
            br.rejectValue("managerId", "invalid", "Người quản lý không hợp lệ");
            return;
        }
        // (Nếu bạn có field role/authority => check ở đây, ví dụ:)
        // if (!ou.get().hasRole("MANAGER")) { ... }

        Optional<ShopManager> exists = shopManagerRepo.findByManager_Id(form.getManagerId());
        if (exists.isPresent()) {
            Long shopIdOfManager = exists.get().getShop().getId();
            if (editingShopId == null || !shopIdOfManager.equals(editingShopId)) {
                br.rejectValue("managerId", "inuse", "Người này đang quản lý cửa hàng khác");
            }
        }
    }

    // ---- CRUD ----
    @Transactional
    public void create(ShopDTO form) {
        Shop s = Shop.builder()
                .name(form.getName())
                .phone(StringUtils.hasText(form.getPhone()) ? form.getPhone() : null)
                .address(StringUtils.hasText(form.getAddress()) ? form.getAddress() : null)
                .status(form.getStatus())
                .build();
        s = shopRepo.save(s);

        // gán manager nếu có
        if (form.getManagerId() != null) {
            User manager = userRepo.findById(form.getManagerId())
                    .orElseThrow(() -> new IllegalStateException("Manager không tồn tại"));
            ShopManager sm = ShopManager.builder()
                    .shop(s).manager(manager).build();
            shopManagerRepo.save(sm);
        }
    }

    @Transactional
    public void update(Long id, ShopDTO form) {
        Shop s = shopRepo.findById(id).orElseThrow();
        s.setName(form.getName());
        s.setPhone(StringUtils.hasText(form.getPhone()) ? form.getPhone() : null);
        s.setAddress(StringUtils.hasText(form.getAddress()) ? form.getAddress() : null);
        s.setStatus(form.getStatus());
        shopRepo.save(s);

        // cập nhật quan hệ manager
        Optional<ShopManager> cur = shopManagerRepo.findByShop_Id(id);

        if (form.getManagerId() == null) {
            // bỏ gán nếu đang có
            cur.ifPresent(shopManagerRepo::delete);
        } else {
            User target = userRepo.findById(form.getManagerId())
                    .orElseThrow(() -> new IllegalStateException("Manager không tồn tại"));

            if (cur.isPresent()) {
                ShopManager sm = cur.get();
                if (!sm.getManager().getId().equals(form.getManagerId())) {
                    // đổi manager
                    sm.setManager(target);
                    shopManagerRepo.save(sm);
                }
            } else {
                // gán mới
                ShopManager sm = ShopManager.builder()
                        .shop(s).manager(target).build();
                shopManagerRepo.save(sm);
            }
        }
    }

    @Transactional
    public void delete(Long id) {
        // không xoá theo cascade manager; chỉ xoá Shop, bản ghi ShopManager sẽ bị FK xử
        // lý tuỳ cấu hình
        shopRepo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<ShopDTO> findDtoByIdIncludingManager(Long id) {
        return shopRepo.findById(id).map(shop -> {
            Optional<ShopManager> sm = shopManagerRepo.findByShopIdWithManager(id);
            Long managerId = sm.map(x -> x.getManager() != null ? x.getManager().getId() : null).orElse(null);
            String managerName = sm.map(x -> x.getManager() != null ? x.getManager().getFullName() : null).orElse(null);

            return ShopDTO.builder()
                    .id(shop.getId())
                    .name(shop.getName())
                    .address(shop.getAddress())
                    .phone(shop.getPhone())
                    .status(shop.getStatus())
                    .createdAt(shop.getCreatedAt())
                    .updatedAt(shop.getUpdatedAt())
                    .managerId(managerId)
                    .managerName(managerName)
                    .build();
        });
    }

    /**
     * Trả list JSON cho combobox: chỉ liệt kê manager chưa gán shop khác.
     * Nếu đang edit, luôn include manager hiện tại để không “mất” lựa chọn.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> searchAvailableManagers(String keyword, Long currentShopId) {

        String kw = (keyword == null) ? "" : keyword.trim();

        // LẤY USER CÓ ROLE = MANAGER (role_id = 2)
        List<User> all = userRepo.searchByRoleAndKeyword(2L, kw);

        // tập ID manager đang bận (đang quản lý shop khác; vẫn cho phép manager của shop đang edit)
        Set<Long> busyManagerIds = shopManagerRepo.findAll().stream()
                .filter(sm -> currentShopId == null || !Objects.equals(sm.getShop().getId(), currentShopId))
                .map(sm -> sm.getManager().getId())
                .collect(Collectors.toSet());

        return all.stream()
                .filter(u -> !busyManagerIds.contains(u.getId()))
                .limit(20)
                .map(u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", u.getId());
                    m.put("text", StringUtils.hasText(u.getFullName()) ? u.getFullName() : u.getEmail());
                    String desc = (u.getEmail() != null ? u.getEmail() : "");
                    m.put("desc", desc);
                    return m;
                })
                .collect(Collectors.toList());
    }


}
