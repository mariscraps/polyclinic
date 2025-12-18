package fa.ru.polyclinic.controller;

import fa.ru.polyclinic.model.Role;
import fa.ru.polyclinic.model.User;
import fa.ru.polyclinic.service.StatisticsService;
import fa.ru.polyclinic.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;
    private final StatisticsService statisticsService;
    public AdminController(UserService userService,
                           StatisticsService statisticsService) {
        this.userService = userService;
        this.statisticsService = statisticsService;
    }

    @GetMapping
    public String index(@RequestParam(defaultValue = "id") String sort,
                        @RequestParam(defaultValue = "asc") String order,
                        Model model) {

        if (!"asc".equals(order) && !"desc".equals(order)) {
            order = "asc";
        }

        List<User> users = userService.getAllUsers();
        Comparator<User> comparator = switch (sort) {
            case "id" -> Comparator.comparing(User::getId);
            case "email" -> Comparator.comparing(User::getEmail, String.CASE_INSENSITIVE_ORDER);
            case "fullName" -> Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER);
            case "role" -> Comparator.comparing(User::getRole);
            default -> Comparator.comparing(User::getId);
        };

        if ("desc".equals(order)) {
            comparator = comparator.reversed();
        }

        users = users.stream().sorted(comparator).toList();
        model.addAttribute("users", users);
        model.addAttribute("roles", Role.values());
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);
        return "admin/dashboard";
    }

    @PostMapping("/change-role/{id}")
    public String changeRole(@PathVariable("id") Long id, @RequestParam("role") String role, Model model) {
        try {
            Role r = Role.valueOf(role);
            userService.updateUserRole(id, r);
            return "redirect:/admin";
        } catch (Exception ex) {
            model.addAttribute("error", "Не удалось изменить роль: " + ex.getMessage());
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("roles", Role.values());
            return "admin/dashboard";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, Model model) {
        try {
            userService.deleteUser(id);
            return "redirect:/admin";
        } catch (Exception ex) {
            model.addAttribute("error", "Не удалось удалить пользователя: " + ex.getMessage());
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("roles", Role.values());
            return "admin/dashboard";
        }
    }

    @GetMapping("/statistics")
    public String statistics(Model model) {
        model.addAttribute("totalUsers", statisticsService.getTotalUsers());
        model.addAttribute("totalDoctors", statisticsService.getTotalDoctors());
        model.addAttribute("totalPatients", statisticsService.getTotalPatients());
        model.addAttribute("totalAppointments", statisticsService.getTotalAppointments());
        model.addAttribute("completedAppointments", statisticsService.getCompletedAppointments());
        model.addAttribute("activeSlots", statisticsService.getActiveScheduleSlots());
        model.addAttribute("avgWaitingTime", statisticsService.getAverageWaitingTimeDaysHours());
        model.addAttribute("appointmentLabels", statisticsService.getAppointmentLabels());
        model.addAttribute("appointmentData", statisticsService.getAppointmentData());
        model.addAttribute("specializationLabels", statisticsService.getSpecializationLabels());
        model.addAttribute("specializationData", statisticsService.getSpecializationData());
        return "admin/statistics";
    }
}
