package net.codejava.utea.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.codejava.utea.catalog.dto.ToppingForm;
import net.codejava.utea.catalog.dto.ToppingRow;
import net.codejava.utea.catalog.entity.Topping;
import net.codejava.utea.catalog.service.AdminToppingAppService;
import net.codejava.utea.manager.repository.ShopRepository;
import net.codejava.utea.catalog.repository.ToppingRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/toppings")
@RequiredArgsConstructor
public class AdminToppingController {

    private final AdminToppingAppService app;
    private final ShopRepository shopRepo;
    private final ToppingRepository toppingRepository;

    @GetMapping
    public String index(@RequestParam(required = false) Long shopId,
                        @RequestParam(defaultValue = "") String kw,
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model) {

        int p = Math.max(page - 1, 0);
        kw = (kw == null) ? "" : kw.trim();

        Pageable pageable = PageRequest.of(p, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<ToppingRow> data = toppingRepository.searchRows(shopId, kw, pageable);

        model.addAttribute("page", data);
        model.addAttribute("kw", kw);
        model.addAttribute("shopId", shopId);
        model.addAttribute("shops", shopRepo.findAll());
        return "admin/toppings/index";
    }


    @PostMapping
    public String create(@Valid @ModelAttribute("form") ToppingForm form,
                         BindingResult br, Model model, RedirectAttributes ra){
        if (br.hasErrors()){
            model.addAttribute("shops", shopRepo.findAll());
            return "admin/toppings/form";
        }
        try{
            app.create(form);
            ra.addFlashAttribute("success", "Đã tạo topping.");
            return "redirect:/admin/toppings";
        }catch (IllegalArgumentException ex){
            br.reject("err", ex.getMessage());
            model.addAttribute("shops", shopRepo.findAll());
            return "admin/toppings/form";
        }
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        // Tạo một đối tượng ToppingForm rỗng để Thymeleaf binding
        model.addAttribute("form", new ToppingForm());
        
        // Thêm danh sách các cửa hàng để hiển thị trong dropdown
        model.addAttribute("shops", shopRepo.findAll());
        
        // Trả về view chứa form
        return "admin/toppings/form";
    }
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra){
        try{
            var t = app.findOrThrow(id); // trong service dùng repo.findById(...) đã @EntityGraph
            var f = new ToppingForm();
            f.setId(t.getId());
            f.setShopId(t.getShop().getId());
            f.setName(t.getName());
            f.setPrice(t.getPrice());
            f.setStatus(t.getStatus());
            model.addAttribute("form", f);
            model.addAttribute("shops", shopRepo.findAll());
            return "admin/toppings/form";
        }catch (IllegalArgumentException ex){
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/toppings";
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") ToppingForm form,
                         BindingResult br, Model model, RedirectAttributes ra){
        if (br.hasErrors()){
            model.addAttribute("shops", shopRepo.findAll());
            return "admin/toppings/form";
        }
        try{
            app.update(id, form);
            ra.addFlashAttribute("success", "Đã cập nhật topping.");
            return "redirect:/admin/toppings";
        }catch (IllegalArgumentException ex){
            br.reject("err", ex.getMessage());
            model.addAttribute("shops", shopRepo.findAll());
            return "admin/toppings/form";
        }
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes ra){
        app.toggleStatus(id);
        ra.addFlashAttribute("success", "Đã đổi trạng thái topping.");
        return "redirect:/admin/toppings";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra){
        app.delete(id);
        ra.addFlashAttribute("success", "Đã xóa topping.");
        return "redirect:/admin/toppings";
    }
}
