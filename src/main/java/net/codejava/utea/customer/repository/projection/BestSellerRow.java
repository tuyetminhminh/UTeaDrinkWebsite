package net.codejava.utea.customer.repository.projection;

import net.codejava.utea.entity.Product;

public interface BestSellerRow {
    Product getProduct();
    Long getTotal(); // sum(quantity)
}
