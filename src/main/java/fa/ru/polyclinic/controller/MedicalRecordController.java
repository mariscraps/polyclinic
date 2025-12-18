package fa.ru.polyclinic.controller;

import fa.ru.polyclinic.dto.MedicalRecordDto;
import fa.ru.polyclinic.model.MedicalRecord;
import fa.ru.polyclinic.service.MedicalRecordService;
import fa.ru.polyclinic.service.PatientService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/medical-records")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;
    private final PatientService patientService;

    public MedicalRecordController(MedicalRecordService medicalRecordService, PatientService patientService) {
        this.medicalRecordService = medicalRecordService;
        this.patientService = patientService;
    }

    @GetMapping
    public String list(Model model) {
        List<MedicalRecord> list = medicalRecordService.getAll();
        model.addAttribute("records", list);
        return "medical_records/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("record", new MedicalRecordDto());
        model.addAttribute("patients", patientService.getAll());
        return "medical_records/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("record") MedicalRecordDto dto, Model model) {
        try {
            medicalRecordService.createRecord(dto);
            return "redirect:/medical-records";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("patients", patientService.getAll());
            model.addAttribute("record", dto);
            return "medical_records/create";
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, Model model) {
        try {
            medicalRecordService.delete(id);
            return "redirect:/medical-records";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("records", medicalRecordService.getAll());
            return "medical_records/list";
        }
    }
}
