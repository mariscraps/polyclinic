package fa.ru.polyclinic.controller;

import fa.ru.polyclinic.dto.LabResultDto;
import fa.ru.polyclinic.model.LabResult;
import fa.ru.polyclinic.service.LabResultService;
import fa.ru.polyclinic.service.PatientService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/lab-results")
public class LabResultController {
    private final LabResultService labResultService;
    private final PatientService patientService;

    public LabResultController(LabResultService labResultService, PatientService patientService) {
        this.labResultService = labResultService;
        this.patientService = patientService;
    }

    @GetMapping
    public String list(Model model) {
        List<LabResult> list = labResultService.getAll();
        model.addAttribute("labs", list);
        return "lab_results/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("lab", new LabResultDto());
        model.addAttribute("patients", patientService.getAll());
        return "lab_results/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("lab") LabResultDto dto, Model model) {
        try {
            labResultService.createResult(dto);
            return "redirect:/lab-results";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("patients", patientService.getAll());
            model.addAttribute("lab", dto);
            return "lab_results/create";
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, Model model) {
        try {
            labResultService.delete(id);
            return "redirect:/lab-results";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("labs", labResultService.getAll());
            return "lab_results/list";
        }
    }
}
