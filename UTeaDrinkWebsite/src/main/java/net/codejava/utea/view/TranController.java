package net.codejava.utea.view;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TranController {
    @GetMapping("/home")
    public String roleHome(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/"; // Guest homepage
        }
        if (has(auth, "ROLE_MANAGER") || has(auth, "MANAGER"))   return "redirect:/manager/home";
        if (has(auth, "ROLE_CUSTOMER") || has(auth, "CUSTOMER")) return "redirect:/customer/home";
        if (has(auth, "ROLE_SHIPPER") || has(auth, "SHIPPER")) return "redirect:/shipper/home";
        return "redirect:/"; // fallback
    }

    private boolean has(Authentication auth, String role) {
        for (GrantedAuthority g : auth.getAuthorities()) {
            if (g.getAuthority().equalsIgnoreCase(role)) return true;
        }
        return false;
    }
}
