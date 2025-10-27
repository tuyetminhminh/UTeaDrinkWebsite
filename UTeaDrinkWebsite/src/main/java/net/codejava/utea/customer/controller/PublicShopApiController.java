package net.codejava.utea.customer.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.customer.dto.SectionDTO;
import net.codejava.utea.customer.service.PublicShopService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public")
public class PublicShopApiController {

    private final PublicShopService publicShopService;

    @GetMapping("/shops/{shopId}/sections")
    public List<SectionDTO> sections(@PathVariable Long shopId) {
        return publicShopService.buildSections(shopId);
    }
}
