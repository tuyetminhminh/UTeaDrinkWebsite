package net.codejava.utea.catalog.service.impl;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.entity.Topping;
import net.codejava.utea.catalog.repository.ToppingRepository;
import net.codejava.utea.catalog.service.ToppingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ToppingServiceImpl implements ToppingService {

    private final ToppingRepository toppingRepository;

    @Override
    public List<Topping> getToppingsForShop(Long shopId) {
        return toppingRepository.findByShopId(shopId);
    }

    @Override
    public Topping getToppingById(Long toppingId) {
        return toppingRepository.findById(toppingId).orElse(null);
    }

    @Override
    public List<Topping> getActiveToppingsForShop(Long shopId) {
        return toppingRepository.findByShopIdAndStatus(shopId, "ACTIVE");
    }
}
