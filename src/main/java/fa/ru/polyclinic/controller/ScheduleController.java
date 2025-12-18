package fa.ru.polyclinic.controller;

import fa.ru.polyclinic.dto.ScheduleDto;
import fa.ru.polyclinic.model.Schedule;
import fa.ru.polyclinic.service.ScheduleService;
import fa.ru.polyclinic.service.DoctorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** управление расписанием (CRUD для регистратуры) */
@Controller
@RequestMapping("/schedule")
public class ScheduleController {
    private final ScheduleService scheduleService;
    private final DoctorService doctorService;

    public ScheduleController(ScheduleService scheduleService, DoctorService doctorService) {
        this.scheduleService = scheduleService;
        this.doctorService = doctorService;
    }

    @GetMapping
    public String index(Model model) {
        List<Schedule> list = scheduleService.getAll();
        model.addAttribute("schedules", list);
        model.addAttribute("doctors", doctorService.getAll());
        model.addAttribute("scheduleDto", new ScheduleDto());
        return "registrar/schedule";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("scheduleDto") ScheduleDto dto, Model model) {
        try {
            scheduleService.createFromDto(dto);
            return "redirect:/schedule";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("schedules", scheduleService.getAll());
            model.addAttribute("doctors", doctorService.getAll());
            model.addAttribute("scheduleDto", dto);
            return "registrar/schedule";
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, Model model) {
        try {
            scheduleService.delete(id);
            return "redirect:/schedule";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("schedules", scheduleService.getAll());
            model.addAttribute("doctors", doctorService.getAll());
            return "registrar/schedule";
        }
    }
}
