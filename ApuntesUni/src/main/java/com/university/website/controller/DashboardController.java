package com.university.website.controller;

import com.university.website.dto.DashboardDto;
import com.university.website.security.CustomUserDetails;
import com.university.website.service.DashboardService;
import com.university.website.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final NotificationService notificationService;

    @GetMapping({"/", "/index.html"})
    public String index(@AuthenticationPrincipal CustomUserDetails currentUser) {
        if (currentUser != null) {
            return "redirect:/dashboard";
        }
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        Long userId = (currentUser != null) ? currentUser.getId() : null;
        DashboardDto dashboardData = dashboardService.getDashboardData(userId);

        long unreadNotifications = 0;
        if (userId != null) {
            unreadNotifications = notificationService.getUnreadCount(userId);
        }

        model.addAttribute("dashboard", dashboardData);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("unreadCount", unreadNotifications);

        return "dashboard";
    }

    @GetMapping("/terminos")
    public String terminos(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        model.addAttribute("currentUser", currentUser);
        return "terminos";
    }

    @GetMapping("/privacidad")
    public String privacidad(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        model.addAttribute("currentUser", currentUser);
        return "privacidad";
    }
}
