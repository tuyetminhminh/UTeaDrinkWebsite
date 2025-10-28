package net.codejava.utea.catalog.service;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.dto.ToppingForm;
import net.codejava.utea.catalog.entity.Topping;
import net.codejava.utea.catalog.repository.ToppingRepository;
import net.codejava.utea.manager.entity.Shop;
import net.codejava.utea.manager.repository.ShopRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminToppingAppService {

    private final ToppingRepository toppingRepo;
    private final ShopRepository shopRepo;

    public Page<Topping> search(Long shopId, String kw, Pageable pageable){
        String k = (kw == null || kw.isBlank()) ? null : kw.trim();
        return toppingRepo.search(shopId, k, pageable);
    }

    public Topping findOrThrow(Long id){
        return toppingRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy topping."));
    }

    @Transactional
    public Topping create(ToppingForm f){
        Shop shop = shopRepo.findById(f.getShopId())
                .orElseThrow(() -> new IllegalArgumentException("Shop không hợp lệ."));
        if (toppingRepo.existsByShopIdAndNameIgnoreCase(f.getShopId(), f.getName()))
            throw new IllegalArgumentException("Tên topping đã tồn tại trong cửa hàng này.");

        var t = Topping.builder()
                .shop(shop)
                .name(f.getName().trim())
                .price(f.getPrice())
                .status(f.getStatus())
                .build();

        try { return toppingRepo.save(t); }
        catch (DataIntegrityViolationException ex){
            throw new IllegalArgumentException("Dữ liệu không hợp lệ hoặc bị trùng.", ex);
        }
    }

    @Transactional
    public Topping update(Long id, ToppingForm f){
        var t = findOrThrow(id);
        if (!shopRepo.existsById(f.getShopId()))
            throw new IllegalArgumentException("Shop không hợp lệ.");
        if (toppingRepo.existsByShopIdAndNameIgnoreCaseAndIdNot(f.getShopId(), f.getName(), id))
            throw new IllegalArgumentException("Tên topping đã tồn tại trong cửa hàng này.");

        // có thể cho đổi shop nếu cần
        t.setShop(shopRepo.getReferenceById(f.getShopId()));
        t.setName(f.getName().trim());
        t.setPrice(f.getPrice());
        t.setStatus(f.getStatus());

        try { return toppingRepo.save(t); }
        catch (DataIntegrityViolationException ex){
            throw new IllegalArgumentException("Dữ liệu không hợp lệ hoặc bị trùng.", ex);
        }
    }

    @Transactional
    public void delete(Long id){
        toppingRepo.deleteById(id);
    }

    @Transactional
    public void toggleStatus(Long id){
        var t = findOrThrow(id);
        t.setStatus("ACTIVE".equalsIgnoreCase(t.getStatus()) ? "INACTIVE" : "ACTIVE");
        toppingRepo.save(t);
    }
}
