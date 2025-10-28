package net.codejava.utea.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.admin.dto.AdminDashboardStatsDTO;
import net.codejava.utea.admin.service.AdminDashboardService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequestMapping("/admin/home")
@RequiredArgsConstructor
public class AdminHomeController {

    private final AdminDashboardService dashboardService;

    @GetMapping
    public String index(Model model) {
        AdminDashboardStatsDTO stats = dashboardService.getDashboardStats();
        model.addAttribute("stats", stats);
        return "home/admin-home";
    }
}