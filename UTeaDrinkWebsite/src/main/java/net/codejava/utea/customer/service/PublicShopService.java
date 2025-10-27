package net.codejava.utea.customer.service;

import net.codejava.utea.customer.dto.SectionDTO;

import java.util.List;

public interface PublicShopService {
    List<SectionDTO> buildSections(Long shopId);
}
