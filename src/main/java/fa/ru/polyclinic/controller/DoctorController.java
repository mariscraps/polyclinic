package fa.ru.polyclinic.controller;

import fa.ru.polyclinic.dto.MedicalRecordDto;
import fa.ru.polyclinic.dto.LabResultDto;
import fa.ru.polyclinic.dto.StudyResultDto;
import fa.ru.polyclinic.model.*;
import fa.ru.polyclinic.repository.ReferralRepository;
import fa.ru.polyclinic.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/doctor")
public class DoctorController {
    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;
    private final LabResultService labResultService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final UserService userService;
    private final ReferralService referralService;
    private final StudyResultService studyResultService;
    private final ReferralRepository referralRepository;

    public DoctorController(AppointmentService appointmentService,
                            MedicalRecordService medicalRecordService,
                            LabResultService labResultService,
                            PatientService patientService, DoctorService doctorService, UserService userService,
                            ReferralService referralService, StudyResultService studyResultService,
                            ReferralRepository referralRepository) {
        this.appointmentService = appointmentService;
        this.medicalRecordService = medicalRecordService;
        this.labResultService = labResultService;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.userService = userService;
        this.referralService = referralService;
        this.studyResultService = studyResultService;
        this.referralRepository = referralRepository;
    }

    @GetMapping
    public String index(Authentication auth, Model model) {
        User user = userService.findByEmail(auth.getName());
        Doctor doctor = doctorService.getByUser(user);
        model.addAttribute("doctor", doctor);
        return "doctor/dashboard";
    }

    @GetMapping("/appointments")
    public String appointments(
            Authentication auth,
            @RequestParam(defaultValue = "patient") String sort,
            @RequestParam(defaultValue = "asc") String order,
            Model model) {

        User user = userService.findByEmail(auth.getName());
        if (user == null) {
            model.addAttribute("appointments", List.of());
            model.addAttribute("sort", sort);
            model.addAttribute("order", order);
            return "doctor/appointments";
        }

        Doctor doctor = doctorService.getByUser(user);
        if (doctor == null) {
            model.addAttribute("appointments", List.of());
        } else {
            List<Appointment> appointments = appointmentService.getActiveAppointmentsByDoctor(doctor.getId());

            Comparator<Appointment> comparator = switch (sort) {
                case "patient" ->
                        Comparator.comparing(a -> a.getPatient().getUser().getFullName(), String.CASE_INSENSITIVE_ORDER);
                case "date" ->
                        Comparator.comparing(a -> a.getSchedule().getDate());
                case "time" ->
                        Comparator.comparing(a -> a.getSchedule().getTime());
                default ->
                        Comparator.comparing(a -> a.getPatient().getUser().getFullName(), String.CASE_INSENSITIVE_ORDER);
            };

            if ("desc".equals(order)) {
                comparator = comparator.reversed();
            }

            appointments = appointments.stream().sorted(comparator).toList();
            model.addAttribute("appointments", appointments);
        }

        model.addAttribute("sort", sort);
        model.addAttribute("order", order);
        return "doctor/appointments";
    }

    @PostMapping("/appointments/complete/{id}")
    public String completeAppointment(@PathVariable("id") Long id) {
        appointmentService.completeAppointment(id);
        return "redirect:/doctor/appointments";
    }

    @GetMapping("/history")
    public String history(Authentication auth,
                          @RequestParam(required = false) String patient,
                          @RequestParam(required = false) String date,
                          @RequestParam(required = false) String time,
                          @RequestParam(defaultValue = "patient") String sort,
                          @RequestParam(defaultValue = "asc") String order,
                          Model model) {

        User user = userService.findByEmail(auth.getName());
        Doctor doctor = doctorService.getByUser(user);
        List<Appointment> appointments = appointmentService.getCompletedAppointmentsByDoctor(doctor.getId());

        if (patient != null && !patient.isBlank()) {
            appointments = appointments.stream()
                    .filter(a -> a.getPatient().getUser().getFullName().toLowerCase().contains(patient.toLowerCase()))
                    .toList();
        }
        if (date != null && !date.isBlank()) {
            appointments = appointments.stream()
                    .filter(a -> a.getSchedule().getDate().toString().equals(date))
                    .toList();
        }
        if (time != null && !time.isBlank()) {
            appointments = appointments.stream()
                    .filter(a -> a.getSchedule().getTime().toString().startsWith(time))
                    .toList();
        }

        Comparator<Appointment> comparator = switch (sort) {
            case "patient" -> Comparator.comparing(a -> a.getPatient().getUser().getFullName(), String.CASE_INSENSITIVE_ORDER);
            case "date" -> Comparator.comparing(a -> a.getSchedule().getDate());
            case "time" -> Comparator.comparing(a -> a.getSchedule().getTime());
            default -> Comparator.comparing(a -> a.getPatient().getUser().getFullName());
        };

        if ("desc".equals(order)) {
            comparator = comparator.reversed();
        }

        appointments = appointments.stream().sorted(comparator).toList();

        model.addAttribute("completedAppointments", appointments);
        model.addAttribute("searchPatient", patient);
        model.addAttribute("searchDate", date);
        model.addAttribute("searchTime", time);
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);
        return "doctor/history";
    }

    @GetMapping("/record/add/{patientId}")
    public String addRecordForm(@PathVariable("patientId") Long patientId,
                                Authentication auth,
                                Model model) {
        MedicalRecordDto dto = new MedicalRecordDto();
        dto.setPatientId(patientId);

        User user = userService.findByEmail(auth.getName());
        Doctor doctor = doctorService.getByUser(user);
        if (doctor == null) {
            throw new RuntimeException("Врач не найден");
        }
        dto.setDoctorId(doctor.getId());

        Patient patient = patientService.getById(patientId);
        model.addAttribute("record", dto);
        model.addAttribute("patient", patient);
        return "doctor/add_record";
    }

    @PostMapping("/record/add")
    public String addRecord(@ModelAttribute("record") MedicalRecordDto dto, Model model) {
        try {
            medicalRecordService.createRecord(dto);
            return "redirect:/doctor/appointments";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("record", dto);
            Patient patient = patientService.getById(dto.getPatientId());
            model.addAttribute("patient", patient);
            model.addAttribute("patients", patientService.getAll());
            return "doctor/add_record";
        }
    }

    @GetMapping("/lab/add/{patientId}")
    public String addLabForm(@PathVariable("patientId") Long patientId,
                             Authentication auth,
                             Model model) {
        LabResultDto dto = new LabResultDto();
        dto.setPatientId(patientId);
        User user = userService.findByEmail(auth.getName());
        Doctor doctor = doctorService.getByUser(user);
        if (doctor == null) {
            throw new RuntimeException("Врач не найден");
        }
        dto.setDoctorId(doctor.getId());

        Patient patient = patientService.getById(patientId);
        model.addAttribute("lab", dto);
        model.addAttribute("patient", patient);
        return "doctor/add_lab";
    }

    @PostMapping("/lab/add")
    public String addLab(@ModelAttribute("lab") LabResultDto dto, Model model) {
        try {
            labResultService.createResult(dto);
            return "redirect:/doctor/appointments";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("lab", dto);
            // Загружаем и передаём пациента
            Patient patient = patientService.getById(dto.getPatientId());
            model.addAttribute("patient", patient);
            return "doctor/add_lab";
        }
    }

    @GetMapping("/patient/{patientId}/history")
    public String patientHistory(@PathVariable("patientId") Long patientId, Model model) {
        Patient patient = patientService.getById(patientId);
        List<MedicalRecord> records = medicalRecordService.getByPatient(patientId);
        List<LabResult> labs = labResultService.getByPatient(patientId);
        List<StudyResult> studies = studyResultService.getByPatient(patientId);

        records.sort((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()));
        labs.sort((l1, l2) -> {
            int cmp = l2.getDate().compareTo(l1.getDate());
            if (cmp != 0) return cmp;
            return l2.getTime().compareTo(l1.getTime());
        });
        studies.sort((s1, s2) -> {
            int cmp = s2.getDate().compareTo(s1.getDate());
            if (cmp != 0) return cmp;
            return s2.getTime().compareTo(s1.getTime());
        });

        model.addAttribute("patient", patient);
        model.addAttribute("records", records);
        model.addAttribute("labs", labs);
        model.addAttribute("studies", studies);
        return "doctor/patient_history";
    }

    @GetMapping("/lab/result/{id}")
    public String editLabResultForm(@PathVariable("id") Long labId, Model model) {
        LabResult lab = labResultService.getById(labId);
        model.addAttribute("lab", lab);
        return "doctor/edit_lab";
    }

    @PostMapping("/lab/result/update")
    public String updateLabResult(@ModelAttribute("lab") LabResult lab, Model model) {
        try {
            labResultService.update(lab);
            return "redirect:/doctor/patient/" + lab.getPatient().getId() + "/history";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("lab", lab);
            return "doctor/edit_lab";
        }
    }

    @GetMapping("/referrals")
    public String referrals(Authentication auth, Model model) {
        User user = userService.findByEmail(auth.getName());
        Doctor doctor = doctorService.getByUser(user);
        List<Referral> referrals = referralService.getActiveReferralsForDoctor(doctor.getId());
        model.addAttribute("referrals", referrals);
        return "doctor/referrals";
    }

    @GetMapping("/referral/{id}/complete")
    public String completeReferralForm(@PathVariable("id") Long id, Model model) {
        Referral referral = referralRepository.findById(id).orElseThrow();
        model.addAttribute("referral", referral);
        return "doctor/complete_referral";
    }

    @PostMapping("/referral/complete")
    public String completeReferral(@RequestParam Long referralId,
                                   @RequestParam String result,
                                   Authentication auth,
                                   Model model) {
        User currentUser = userService.findByEmail(auth.getName());
        Doctor currentDoctor = doctorService.getByUser(currentUser);
        Referral referral = referralRepository.findById(referralId).orElseThrow();
        if (!referral.getDoctor().getId().equals(currentDoctor.getId())) {
            model.addAttribute("error", "Вы не можете заполнить чужое направление");
            model.addAttribute("referral", referral);
            return "doctor/complete_referral";
        }

        referralService.completeReferral(referralId, result);
        return "redirect:/doctor/referrals";
    }

    @GetMapping("/referral/create/analysis/{patientId}")
    public String createAnalysisForm(@PathVariable("patientId") Long patientId, Model model) {
        model.addAttribute("referral", new Referral());
        model.addAttribute("patientId", patientId);
        model.addAttribute("type", "ANALYSIS");

        Patient patient = patientService.getById(patientId);
        model.addAttribute("patient", patient);
        return "doctor/create_referral";
    }

    @GetMapping("/referral/create/study/{patientId}")
    public String createStudyForm(@PathVariable("patientId") Long patientId, Model model) {
        model.addAttribute("referral", new Referral());
        model.addAttribute("patientId", patientId);
        model.addAttribute("type", "STUDY");

        Patient patient = patientService.getById(patientId);
        model.addAttribute("patient", patient);

        return "doctor/create_referral";
    }


    @PostMapping("/referral/create")
    public String createReferral(@ModelAttribute("referral") Referral referral,
                                 @RequestParam Long patientId,
                                 @RequestParam String type,
                                 Authentication auth,
                                 Model model) {
        try {
            User user = userService.findByEmail(auth.getName());
            Doctor doctor = doctorService.getByUser(user);
            Patient patient = patientService.getById(patientId);
            if (referral.getTime() == null) {
                throw new IllegalArgumentException("Время обязательно для назначения");
            }

            referral.setPatient(patient);
            referral.setDoctor(doctor);
            referral.setType(type);
            referral.setCompleted(false);

            referralService.createReferral(referral);
            return "redirect:/doctor/patient/" + patientId + "/history";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("patientId", patientId);
            model.addAttribute("type", type);
            return "doctor/create_referral";
        }
    }

    @GetMapping("/study/add/{patientId}")
    public String addStudyForm(@PathVariable("patientId") Long patientId, Model model) {
        StudyResultDto dto = new StudyResultDto();
        dto.setPatientId(patientId);
        model.addAttribute("study", dto);
        model.addAttribute("patient", patientService.getById(patientId));
        return "doctor/add_study";
    }

    @PostMapping("/study/add")
    public String addStudy(@ModelAttribute("study") StudyResultDto dto, Model model) {
        try {
            studyResultService.createResult(dto);
            return "redirect:/doctor/appointments";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("study", dto);
            model.addAttribute("patient", patientService.getById(dto.getPatientId()));
            return "doctor/add_study";
        }
    }

    @GetMapping("/study/result/{id}")
    public String editStudyResultForm(@PathVariable("id") Long studyId,
                                      Authentication auth,
                                      Model model) {
        User currentUser = userService.findByEmail(auth.getName());
        Doctor currentDoctor = doctorService.getByUser(currentUser);
        StudyResult study = studyResultService.getById(studyId);

        if (!study.getDoctor().getId().equals(currentDoctor.getId())) {
            model.addAttribute("error", "Вы не можете редактировать чужое исследование");
            return "redirect:/doctor/appointments";
        }

        model.addAttribute("study", study);
        return "doctor/edit_study";
    }

    @GetMapping("/patient/{patientId}/referrals")
    public String referralsByPatient(@PathVariable("patientId") Long patientId,
                                     Authentication auth,
                                     Model model) {
        User currentUser = userService.findByEmail(auth.getName());
        Doctor currentDoctor = doctorService.getByUser(currentUser);
        Patient patient = patientService.getById(patientId);

        List<Referral> allReferrals = referralService.getCompletedFalseReferralsForPatient(patientId);
        List<Referral> ownActiveReferrals = referralService.getActiveReferralsForPatientAndDoctor(patientId, currentDoctor.getId());
        List<Referral> analyses = allReferrals.stream()
                .filter(r -> "ANALYSIS".equals(r.getType()))
                .toList();
        List<Referral> studies = allReferrals.stream()
                .filter(r -> "STUDY".equals(r.getType()))
                .toList();

        List<Long> ownActiveReferralIds = ownActiveReferrals != null
                ? ownActiveReferrals.stream().map(Referral::getId).toList()
                : List.of();

        model.addAttribute("patient", patient);
        model.addAttribute("analyses", analyses);
        model.addAttribute("studies", studies);
        model.addAttribute("ownActiveReferralIds", ownActiveReferralIds);
        model.addAttribute("currentDoctorId", currentDoctor.getId());
        return "doctor/referrals_by_patient";
    }

    @PostMapping("/referral/delete/{id}")
    public String deleteReferral(@PathVariable("id") Long id,
                                 Authentication auth,
                                 @RequestParam("patientId") Long patientId,
                                 Model model) {
        try {
            User currentUser = userService.findByEmail(auth.getName());
            Doctor currentDoctor = doctorService.getByUser(currentUser);
            referralService.deleteByDoctor(id, currentDoctor.getId());

            return "redirect:/doctor/patient/" + patientId + "/referrals";
        } catch (Exception ex) {
            model.addAttribute("error", "Ошибка при удалении направления: " + ex.getMessage());
            return referralsByPatient(patientId, auth, model);
        }
    }
}
