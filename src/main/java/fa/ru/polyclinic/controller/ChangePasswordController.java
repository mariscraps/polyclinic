package fa.ru.polyclinic.controller;

import fa.ru.polyclinic.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ChangePasswordController {
    private final UserService userService;
    public ChangePasswordController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/change-password")
    public String showChangePasswordPage(Authentication auth, Model model) {
        model.addAttribute("email", auth.getName());
        return "auth/change_password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            Authentication auth,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Пароли не совпадают");
            return "auth/change_password";
        }

        if (newPassword == null || newPassword.length() < 6) {
            model.addAttribute("error", "Пароль должен содержать не менее 6 символов");
            return "auth/change_password";
        }

        String email = auth.getName();
        userService.changePassword(email, newPassword);

        redirectAttributes.addFlashAttribute("passwordChanged", true);
        return "redirect:/patient";
    }
}
