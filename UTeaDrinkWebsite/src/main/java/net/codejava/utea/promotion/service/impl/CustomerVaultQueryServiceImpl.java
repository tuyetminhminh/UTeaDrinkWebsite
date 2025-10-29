package net.codejava.utea.promotion.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.promotion.entity.Voucher;
import net.codejava.utea.promotion.repository.CustomerVoucherRepository;
import net.codejava.utea.promotion.repository.VoucherRepository;
import net.codejava.utea.promotion.service.CustomerVaultQueryService;
import net.codejava.utea.promotion.service.model.PromoRule;
import net.codejava.utea.promotion.view.VoucherCardVM;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerVaultQueryServiceImpl implements CustomerVaultQueryService {

    private final VoucherRepository voucherRepo;
    private final CustomerVoucherRepository cvRepo;
    private final ObjectMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherCardVM> list(Long userId, String q, String sort, boolean savedOnly, Pageable pageable) {
        var now = LocalDateTime.now();
        List<Voucher> all = voucherRepo.findActiveNow(now);
        Set<String> savedCodes = new HashSet<>(cvRepo.findSavedCodesByUserId(userId));

        List<VoucherCardVM> filtered = all.stream()
                .filter(v -> q == null || q.isBlank() || (v.getCode() != null && v.getCode().toLowerCase().contains(q.toLowerCase())))
                .filter(v -> !savedOnly || savedCodes.contains(v.getCode()))
                .map(v -> toVM(v, savedCodes.contains(v.getCode())))
                .collect(Collectors.toList());

        var byFrom = Comparator.comparing(
                VoucherCardVM::getActiveFrom,
                Comparator.nullsLast(Comparator.naturalOrder())
        );
        if ("savedfirst".equalsIgnoreCase(sort)) {
            filtered.sort(Comparator.comparing(VoucherCardVM::isSaved).reversed().thenComparing(byFrom.reversed()));
        } else if ("oldest".equalsIgnoreCase(sort)) {
            filtered.sort(byFrom);
        } else {
            filtered.sort(byFrom.reversed()); // newest
        }

        int from = Math.max(0, pageable.getPageNumber() * pageable.getPageSize());
        int to   = Math.min(filtered.size(), from + pageable.getPageSize());
        var content = (from < to) ? filtered.subList(from, to) : Collections.<VoucherCardVM>emptyList();

        return new PageImpl<>(content, pageable, filtered.size());
    }

    private VoucherCardVM toVM(Voucher v, boolean saved){
        var r = readRule(v.getRuleJson());
        String shopName = (v.getShop()!=null && Hibernate.isInitialized(v.getShop())) ? v.getShop().getName() : null;
        String typeText =
                Boolean.TRUE.equals(r.getFreeShip()) ? "Freeship" :
                        (r.getPercentOff()!=null && r.getPercentOff()>0) ? "Giảm %" :
                                (r.getAmountOff()!=null && r.getAmountOff().signum()>0) ? "Giảm tiền" : "Mã ưu đãi";

        return VoucherCardVM.builder()
                .id(v.getId()).code(v.getCode())
                .scope(v.getScope().name()).shopName(shopName).status(v.getStatus())
                .freeShip(Boolean.TRUE.equals(r.getFreeShip()))
                .percentOff(r.getPercentOff()).amountOff(r.getAmountOff()).amountCap(r.getAmountCap()).minTotal(r.getMinTotal())
                .activeFrom(v.getActiveFrom()).activeTo(v.getActiveTo())
                .usageLimit(v.getUsageLimit()).usedCount(v.getUsedCount())
                .typeText(typeText).saved(saved)
                .build();
    }

    private PromoRule readRule(String json){
        try { return (json==null || json.isBlank()) ? new PromoRule() : mapper.readValue(json, PromoRule.class); }
        catch (Exception e){ return new PromoRule(); }
    }
}