package net.codejava.utea.catalog.service;

import net.codejava.utea.catalog.entity.Topping;

import java.util.List;

public interface ToppingService {
    List<Topping> getToppingsForShop(Long shopId);
    Topping getToppingById(Long toppingId);
    List<Topping> getActiveToppingsForShop(Long shopId);
}
