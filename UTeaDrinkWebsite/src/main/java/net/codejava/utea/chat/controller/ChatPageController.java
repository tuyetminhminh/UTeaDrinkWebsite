// net/codejava/utea/chat/controller/ChatPageController.java
package net.codejava.utea.chat.controller;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatPageController {

    @GetMapping("/customer")
    public String customerChat(@AuthenticationPrincipal CustomUserDetails me, Model model){
        model.addAttribute("me", me.getUser());
        return "chat/customer-chat";
    }

    @GetMapping("/manager/inbox")
    public String managerInbox(@AuthenticationPrincipal CustomUserDetails me, Model model){
        model.addAttribute("me", me.getUser());
        return "chat/manager-inbox";
    }
}
