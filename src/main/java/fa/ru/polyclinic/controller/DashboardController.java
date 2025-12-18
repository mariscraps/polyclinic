package fa.ru.polyclinic.controller;

import fa.ru.polyclinic.model.Role;
import fa.ru.polyclinic.model.User;
import fa.ru.polyclinic.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** перенаправление на соответствующий кабинет в зависимости от роли */
@Controller
public class DashboardController {
    private final UserService userService;
    public DashboardController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        if (user == null) {
            return "redirect:/login";
        }
        Role role = user.getRole();
        switch (role) {
            case ADMIN:
                return "redirect:/admin";
            case REGISTRAR:
                return "redirect:/registrar";
            case DOCTOR:
                return "redirect:/doctor";
            case PATIENT:
                return "redirect:/patient";
            default:
                return "redirect:/login";
        }
    }
}
