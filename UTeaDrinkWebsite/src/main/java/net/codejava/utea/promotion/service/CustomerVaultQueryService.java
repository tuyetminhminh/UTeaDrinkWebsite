package net.codejava.utea.promotion.service;

import net.codejava.utea.promotion.view.VoucherCardVM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerVaultQueryService {
    Page<VoucherCardVM> list(Long userId,
                             String q,
                             String sort,
                             boolean savedOnly,
                             Pageable pageable);
}