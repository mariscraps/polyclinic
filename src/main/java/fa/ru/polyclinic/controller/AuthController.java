package fa.ru.polyclinic.controller;

import fa.ru.polyclinic.dto.RegistrationDto;
import fa.ru.polyclinic.model.Role;
import fa.ru.polyclinic.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** регистрация и вход */
@Controller
public class AuthController {
    private final UserService userService;
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getName().equals("anonymousUser")) {
            model.addAttribute("currentUser", authentication.getName());
            return "auth/confirm_logout";
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        RegistrationDto dto = new RegistrationDto();
        dto.setRole(Role.PATIENT);
        model.addAttribute("registrationDto", dto);
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("registrationDto") RegistrationDto dto, Model model) {
        try {
            userService.register(dto);
            return "redirect:/login?registered";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("registrationDto", dto);
            return "auth/register";
        }
    }
}
