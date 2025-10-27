package net.codejava.utea.customer.repository.projection;

import net.codejava.utea.catalog.entity.Product;

public interface BestSellerRow {
    Product getProduct();
    Long getTotal(); // sum(quantity)
}
