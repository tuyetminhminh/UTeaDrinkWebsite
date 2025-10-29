package net.codejava.utea.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller xử lý các trang lỗi
 */
@Controller
@RequestMapping("/error")
public class ErrorController {

    /**
     * Trang lỗi 403 - Không có quyền truy cập
     */
    @GetMapping("/403")
    public String accessDenied() {
        return "error/403";
    }
}

