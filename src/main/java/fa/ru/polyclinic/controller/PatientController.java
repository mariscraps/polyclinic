package fa.ru.polyclinic.controller;

import fa.ru.polyclinic.dto.AppointmentDto;
import fa.ru.polyclinic.model.*;
import fa.ru.polyclinic.service.*;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/patient")
public class PatientController {
    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;
    private final LabResultService labResultService;
    private final UserService userService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final ScheduleService scheduleService;
    private final StudyResultService studyResultService;
    private final ReferralService referralService;

    public PatientController(AppointmentService appointmentService,
                             MedicalRecordService medicalRecordService,
                             LabResultService labResultService,
                             UserService userService,
                             PatientService patientService,
                             DoctorService doctorService,
                             ScheduleService scheduleService,
                             StudyResultService studyResultService,
                             ReferralService referralService) {
        this.appointmentService = appointmentService;
        this.medicalRecordService = medicalRecordService;
        this.labResultService = labResultService;
        this.userService = userService;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.scheduleService = scheduleService;
        this.studyResultService = studyResultService;
        this.referralService = referralService;
    }

    @GetMapping
    public String index(Authentication auth, Model model) {
        User user = userService.findByEmail(auth.getName());
        if (user == null) {
            return "redirect:/login";
        }

        Patient patient = patientService.getByUser(user);
        if (patient == null) {
            patient = new Patient();
            patient.setUser(user);
            model.addAttribute("patient", patient);
            return "patient/profile";
        }

        model.addAttribute("user", user);
        model.addAttribute("patient", patient);

        List<Appointment> appointments = appointmentService.getByPatient(patient.getId());
        appointments = appointments.stream()
                .filter(a -> !a.isCompleted())
                .toList();
        model.addAttribute("appointments", appointments);

        List<Referral> allReferrals = referralService.getCompletedFalseReferralsForPatient(patient.getId());
        List<Referral> analyses = allReferrals.stream()
                .filter(r -> "ANALYSIS".equals(r.getType()))
                .toList();
        List<Referral> studies = allReferrals.stream()
                .filter(r -> "STUDY".equals(r.getType()))
                .toList();

        model.addAttribute("analyses", analyses);
        model.addAttribute("studies", studies);

        return "patient/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        User user = userService.findByEmail(auth.getName());
        if (user == null) return "redirect:/login";
        Patient patient = patientService.getByUser(user);
        if (patient == null) {
            patient = new Patient();
            patient.setUser(user);
        }
        model.addAttribute("patient", patient);
        return "patient/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @Valid @ModelAttribute("patient") Patient patient,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("validationErrors", bindingResult.getAllErrors());
            return "patient/profile";
        }

        try {
            patientService.createOrUpdate(patient);
            return "redirect:/patient?profileCompleted";
        } catch (Exception ex) {
            model.addAttribute("error", "Ошибка сохранения: " + ex.getMessage());
            return "patient/profile";
        }
    }

    @GetMapping("/schedule")
    public String schedule(
            @RequestParam(required = false) String doctorName,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String time,
            @RequestParam(defaultValue = "doctor") String sort,
            @RequestParam(defaultValue = "asc") String order,
            Model model) {

        List<Schedule> schedules = scheduleService.getAllAvailable();

        if (doctorName != null && !doctorName.isBlank()) {
            schedules = schedules.stream()
                    .filter(s -> s.getDoctor().getUser().getFullName().toLowerCase().contains(doctorName.toLowerCase()))
                    .toList();
        }
        if (specialization != null && !specialization.isBlank()) {
            schedules = schedules.stream()
                    .filter(s -> s.getDoctor().getSpecialization().toLowerCase().contains(specialization.toLowerCase()))
                    .toList();
        }
        if (date != null && !date.isBlank()) {
            schedules = schedules.stream()
                    .filter(s -> s.getDate().toString().equals(date))
                    .toList();
        }
        if (time != null && !time.isBlank()) {
            schedules = schedules.stream()
                    .filter(s -> s.getTime().toString().startsWith(time))
                    .toList();
        }

        Comparator<Schedule> comparator = switch (sort) {
            case "doctor" -> Comparator.comparing(s -> s.getDoctor().getUser().getFullName(), String.CASE_INSENSITIVE_ORDER);
            case "specialization" -> Comparator.comparing(s -> s.getDoctor().getSpecialization(), String.CASE_INSENSITIVE_ORDER);
            case "date" -> Comparator.comparing(Schedule::getDate);
            case "time" -> Comparator.comparing(Schedule::getTime);
            default -> Comparator.comparing(s -> s.getDoctor().getUser().getFullName());
        };

        if ("desc".equals(order)) {
            comparator = comparator.reversed();
        }

        schedules = schedules.stream().sorted(comparator).toList();
        model.addAttribute("schedules", schedules);
        model.addAttribute("doctorName", doctorName);
        model.addAttribute("specialization", specialization);
        model.addAttribute("date", date);
        model.addAttribute("time", time);
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);

        return "patient/schedule";
    }

    @GetMapping("/appointments/book")
    public String bookAppointmentForm(Authentication auth, Model model) {
        User user = userService.findByEmail(auth.getName());
        Patient patient = patientService.getByUser(user);
        if (patient == null) return "redirect:/patient/profile";
        AppointmentDto dto = new AppointmentDto();
        dto.setPatientId(patient.getId());
        model.addAttribute("appointment", dto);
        model.addAttribute("doctors", doctorService.getAll());
        model.addAttribute("schedules", scheduleService.getAllAvailable());
        return "patient/book_appointment";
    }

    @PostMapping("/appointments/book")
    public String bookAppointment(@ModelAttribute("appointment") AppointmentDto dto, Model model) {
        try {
            appointmentService.create(dto);
            return "redirect:/patient/appointments?booked";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("doctors", doctorService.getAll());
            model.addAttribute("schedules", scheduleService.getAllAvailable());
            model.addAttribute("appointment", dto);
            return "patient/book_appointment";
        }
    }

    @GetMapping("/appointments")
    public String myAppointments(
            Authentication auth,
            @RequestParam(required = false) String doctorName,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String office,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String time,
            @RequestParam(defaultValue = "doctor") String sort,
            @RequestParam(defaultValue = "asc") String order,
            Model model) {

        User user = userService.findByEmail(auth.getName());
        List<Appointment> appointments = List.of();

        if (user != null) {
            Patient patient = patientService.getByUser(user);
            if (patient != null) {
                appointments = appointmentService.getActiveAppointmentsByPatient(patient.getId());
            }
        }

        if (doctorName != null && !doctorName.isBlank()) {
            appointments = appointments.stream()
                    .filter(a -> a.getDoctor().getUser().getFullName().toLowerCase().contains(doctorName.toLowerCase()))
                    .toList();
        }
        if (specialization != null && !specialization.isBlank()) {
            appointments = appointments.stream()
                    .filter(a -> a.getDoctor().getSpecialization().toLowerCase().contains(specialization.toLowerCase()))
                    .toList();
        }
        if (office != null && !office.isBlank()) {
            appointments = appointments.stream()
                    .filter(a -> {
                        String o = a.getDoctor().getOffice();
                        return o != null && o.toLowerCase().contains(office.toLowerCase());
                    })
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
            case "doctor" -> Comparator.comparing(a -> a.getDoctor().getUser().getFullName(), String.CASE_INSENSITIVE_ORDER);
            case "specialization" -> Comparator.comparing(a -> a.getDoctor().getSpecialization(), String.CASE_INSENSITIVE_ORDER);
            case "office" -> Comparator.comparing(a -> a.getDoctor().getOffice() != null ? a.getDoctor().getOffice() : "", String.CASE_INSENSITIVE_ORDER);
            case "date" -> Comparator.comparing(a -> a.getSchedule().getDate());
            case "time" -> Comparator.comparing(a -> a.getSchedule().getTime());
            default -> Comparator.comparing(a -> a.getDoctor().getUser().getFullName());
        };

        if ("desc".equals(order)) {
            comparator = comparator.reversed();
        }

        appointments = appointments.stream().sorted(comparator).toList();

        model.addAttribute("appointments", appointments);
        model.addAttribute("doctorName", doctorName);
        model.addAttribute("specialization", specialization);
        model.addAttribute("office", office);
        model.addAttribute("date", date);
        model.addAttribute("time", time);
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);

        return "patient/appointments";
    }

    @GetMapping("/labs")
    public String myLabs(Authentication auth, Model model) {
        User user = userService.findByEmail(auth.getName());
        if (user == null) {
            model.addAttribute("labs", List.of());
        } else {
            Patient patient = patientService.getByUser(user);
            if (patient == null) {
                model.addAttribute("labs", List.of());
            } else {
                model.addAttribute("labs", labResultService.getByPatient(patient.getId()));
            }
        }
        return "patient/labs";
    }

    @GetMapping("/appointments/slots")
    @ResponseBody
    public List<Map<String, Object>> getSlotsForPatient(@RequestParam Long doctorId) {
        List<Schedule> slots = scheduleService.getAvailableByDoctor(doctorId);
        return slots.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("date", s.getDate());
            map.put("time", s.getTime().toString());
            map.put("doctorFullName", s.getDoctor().getUser().getFullName());
            map.put("specialization", s.getDoctor().getSpecialization());
            return map;
        }).toList();
    }

    @GetMapping("/referrals")
    public String referrals(Authentication auth, Model model) {
        User user = userService.findByEmail(auth.getName());
        if (user == null) {
            model.addAttribute("analyses", List.of());
            model.addAttribute("studies", List.of());
        } else {
            Patient patient = patientService.getByUser(user);
            if (patient == null) {
                model.addAttribute("analyses", List.of());
                model.addAttribute("studies", List.of());
            } else {
                List<Referral> referrals = referralService.getActiveReferralsForPatient(patient.getId());
                List<Referral> analyses = referrals.stream()
                        .filter(r -> "ANALYSIS".equals(r.getType()))
                        .toList();
                List<Referral> studies = referrals.stream()
                        .filter(r -> "STUDY".equals(r.getType()))
                        .toList();
                model.addAttribute("analyses", analyses);
                model.addAttribute("studies", studies);
            }
        }
        return "patient/referrals";
    }

    @GetMapping("/studies")
    public String myStudies(Authentication auth, Model model) {
        User user = userService.findByEmail(auth.getName());
        List<StudyResult> studies = List.of();
        if (user != null) {
            Patient patient = patientService.getByUser(user);
            if (patient != null) {
                studies = studyResultService.getByPatient(patient.getId());
            }
        }
        model.addAttribute("studies", studies);
        return "patient/studies";
    }

    @GetMapping("/records")
    public String myRecords(
            Authentication auth,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String doctor,
            @RequestParam(required = false) String specialization,
            @RequestParam(defaultValue = "date") String sort,
            @RequestParam(defaultValue = "asc") String order,
            Model model) {

        User user = userService.findByEmail(auth.getName());
        List<MedicalRecord> records = List.of();

        if (user != null) {
            Patient patient = patientService.getByUser(user);
            if (patient != null) {
                records = medicalRecordService.getByPatient(patient.getId());
            }
        }

        if (date != null && !date.isBlank()) {
            records = records.stream()
                    .filter(r -> r.getCreatedAt().toLocalDate().toString().equals(date))
                    .toList();
        }
        if (doctor != null && !doctor.isBlank()) {
            records = records.stream()
                    .filter(r -> r.getDoctor().getUser().getFullName().toLowerCase().contains(doctor.toLowerCase()))
                    .toList();
        }
        if (specialization != null && !specialization.isBlank()) {
            records = records.stream()
                    .filter(r -> r.getDoctor().getSpecialization().toLowerCase().contains(specialization.toLowerCase()))
                    .toList();
        }

        Comparator<MedicalRecord> comparator = switch (sort) {
            case "date" -> Comparator.comparing(MedicalRecord::getCreatedAt);
            case "doctor" -> Comparator.comparing(r -> r.getDoctor().getUser().getFullName(), String.CASE_INSENSITIVE_ORDER);
            case "specialization" -> Comparator.comparing(r -> r.getDoctor().getSpecialization(), String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparing(MedicalRecord::getCreatedAt);
        };

        if ("desc".equals(order)) {
            comparator = comparator.reversed();
        }

        records = records.stream().sorted(comparator).toList();

        model.addAttribute("records", records);
        model.addAttribute("searchDate", date);
        model.addAttribute("searchDoctor", doctor);
        model.addAttribute("searchSpecialization", specialization);
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);

        return "patient/records";
    }

    @GetMapping("/results")
    public String results(Authentication auth, Model model) {
        User user = userService.findByEmail(auth.getName());
        if (user == null) {
            model.addAttribute("labs", List.of());
            model.addAttribute("studies", List.of());
        } else {
            Patient patient = patientService.getByUser(user);
            if (patient == null) {
                model.addAttribute("labs", List.of());
                model.addAttribute("studies", List.of());
            } else {
                model.addAttribute("labs", labResultService.getByPatient(patient.getId()));
                model.addAttribute("studies", studyResultService.getByPatient(patient.getId()));
            }
        }
        return "patient/results";
    }

    @PostMapping("/appointments/cancel")
    public String cancelAppointment(@RequestParam("id") Long appointmentId,
                                    Authentication auth,
                                    Model model) {
        try {
            User user = userService.findByEmail(auth.getName());
            Patient patient = patientService.getByUser(user);
            Appointment appointment = appointmentService.getById(appointmentId);

            if (!appointment.getPatient().getId().equals(patient.getId())) {
                model.addAttribute("error", "Вы не можете отменить чужую запись");
            } else {
                appointmentService.delete(appointmentId);
            }
            return "redirect:/patient/appointments";
        } catch (Exception ex) {
            model.addAttribute("error", "Ошибка при отмене записи: " + ex.getMessage());
            return "redirect:/patient/appointments";
        }
    }

    @GetMapping("/history")
    public String history(Authentication auth,
                          @RequestParam(required = false) String date,
                          @RequestParam(required = false) String time,
                          @RequestParam(required = false) String office,
                          @RequestParam(required = false) String doctor,
                          @RequestParam(required = false) String specialization,
                          @RequestParam(defaultValue = "date") String sort,
                          @RequestParam(defaultValue = "asc") String order,
                          Model model) {

        User user = userService.findByEmail(auth.getName());
        List<Appointment> appointments = List.of();

        if (user != null) {
            Patient patient = patientService.getByUser(user);
            if (patient != null) {
                appointments = appointmentService.getCompletedAppointmentsByPatient(patient.getId());
            }
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
        if (office != null && !office.isBlank()) {
            appointments = appointments.stream()
                    .filter(a -> {
                        String o = a.getDoctor().getOffice();
                        return o != null && o.toLowerCase().contains(office.toLowerCase());
                    })
                    .toList();
        }
        if (doctor != null && !doctor.isBlank()) {
            appointments = appointments.stream()
                    .filter(a -> a.getDoctor().getUser().getFullName().toLowerCase().contains(doctor.toLowerCase()))
                    .toList();
        }
        if (specialization != null && !specialization.isBlank()) {
            appointments = appointments.stream()
                    .filter(a -> a.getDoctor().getSpecialization().toLowerCase().contains(specialization.toLowerCase()))
                    .toList();
        }

        Comparator<Appointment> comparator = switch (sort) {
            case "date" -> Comparator.comparing(a -> a.getSchedule().getDate());
            case "time" -> Comparator.comparing(a -> a.getSchedule().getTime());
            case "office" -> Comparator.comparing(a -> a.getDoctor().getOffice() != null ? a.getDoctor().getOffice() : "", String.CASE_INSENSITIVE_ORDER);
            case "doctor" -> Comparator.comparing(a -> a.getDoctor().getUser().getFullName(), String.CASE_INSENSITIVE_ORDER);
            case "specialization" -> Comparator.comparing(a -> a.getDoctor().getSpecialization(), String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparing(a -> a.getSchedule().getDate());
        };

        if ("desc".equals(order)) {
            comparator = comparator.reversed();
        }

        appointments = appointments.stream().sorted(comparator).toList();

        model.addAttribute("completedAppointments", appointments);
        model.addAttribute("searchDate", date);
        model.addAttribute("searchTime", time);
        model.addAttribute("searchOffice", office);
        model.addAttribute("searchDoctor", doctor);
        model.addAttribute("searchSpecialization", specialization);
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);
        return "patient/history";
    }
}

