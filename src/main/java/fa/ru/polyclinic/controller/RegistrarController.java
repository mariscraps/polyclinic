package fa.ru.polyclinic.controller;

import fa.ru.polyclinic.dto.AppointmentDto;
import fa.ru.polyclinic.dto.PatientRegistrationDto;
import fa.ru.polyclinic.dto.ScheduleDto;
import fa.ru.polyclinic.model.*;
import fa.ru.polyclinic.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import fa.ru.polyclinic.repository.UserRepository;
import fa.ru.polyclinic.repository.PatientRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/registrar")
public class RegistrarController {

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final ScheduleService scheduleService;
    private final UserService userService;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AppointmentService appointmentService;
    private final StatisticsService statisticsService;

    public RegistrarController(PatientService patientService,
                               DoctorService doctorService,
                               ScheduleService scheduleService, UserService userService,
                               PatientRepository patientRepository, UserRepository userRepository,
                               PasswordEncoder passwordEncoder, AppointmentService appointmentService,
                               StatisticsService statisticsService) {
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.scheduleService = scheduleService;
        this.userService = userService;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.appointmentService = appointmentService;
        this.statisticsService = statisticsService;
    }

    @GetMapping
    public String index(Authentication auth, Model model) {
        User user = userService.findByEmail(auth.getName());
        model.addAttribute("user", user);
        return "registrar/dashboard";
    }

    @GetMapping("/patients")
    public String patients(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String birthDate,
            @RequestParam(required = false) String passport,
            @RequestParam(required = false) String insurancePolicy,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order,
            Model model) {

        List<Patient> patients;

        if (id != null && id > 0) {
            Patient p = patientService.getByIdIfExists(id);
            patients = p != null ? List.of(p) : List.of();
        } else {
            patients = patientService.search(
                    fullName,
                    birthDate,
                    passport,
                    insurancePolicy,
                    phone
            );

            patients = sortPatients(patients, sort, order);
        }

        model.addAttribute("patients", patients);
        model.addAttribute("searchId", id);
        model.addAttribute("searchFullName", fullName);
        model.addAttribute("searchBirthDate", birthDate);
        model.addAttribute("searchPassport", passport);
        model.addAttribute("searchInsurancePolicy", insurancePolicy);
        model.addAttribute("searchPhone", phone);
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);

        return "registrar/patients";
    }

    private List<Patient> sortPatients(List<Patient> patients, String sort, String order) {
        return patients.stream()
                .sorted((p1, p2) -> {
                    int cmp = 0;
                    switch (sort) {
                        case "id":
                            cmp = Long.compare(p1.getId(), p2.getId());
                            break;
                        case "fullName":
                            String n1 = p1.getUser() != null ? p1.getUser().getFullName() : "";
                            String n2 = p2.getUser() != null ? p2.getUser().getFullName() : "";
                            cmp = n1.compareToIgnoreCase(n2);
                            break;
                        case "birthDate":
                            LocalDate d1 = p1.getBirthDate();
                            LocalDate d2 = p2.getBirthDate();
                            if (d1 == null && d2 == null) cmp = 0;
                            else if (d1 == null) cmp = -1;
                            else if (d2 == null) cmp = 1;
                            else cmp = d1.compareTo(d2);
                            break;
                        default:
                            cmp = 0;
                    }
                    return "desc".equals(order) ? -cmp : cmp;
                })
                .toList();
    }

    @GetMapping("/patients/add")
    public String addPatientForm(Model model) {
        model.addAttribute("patientDto", new PatientRegistrationDto());
        return "registrar/add_patient";
    }

    @PostMapping("/patients/add")
    public String addPatient(@ModelAttribute("patientDto") PatientRegistrationDto dto, Model model) {
        try {
            String tempPassword = "temp" + System.currentTimeMillis();
            User user = new User();
            user.setEmail(dto.getEmail().trim().toLowerCase());
            user.setFullName(dto.getFullName().trim());
            user.setRole(Role.PATIENT);
            user.setPassword(passwordEncoder.encode(tempPassword));
            user.setFirstLogin(true);
            userRepository.save(user);

            Patient patient = new Patient();
            patient.setUser(user);
            patient.setBirthDate(dto.getBirthDate());
            patient.setPassport(dto.getPassport());
            patient.setInsurancePolicy(dto.getInsurancePolicy());
            patient.setPhone(dto.getPhone());
            patientRepository.save(patient);

            model.addAttribute("generatedPassword", tempPassword);
            model.addAttribute("generatedEmail", user.getEmail());
            return "registrar/patient_created";
        } catch (Exception ex) {
            model.addAttribute("error", "Ошибка: " + ex.getMessage());
            model.addAttribute("patientDto", dto);
            return "registrar/add_patient";
        }
    }

    @GetMapping("/doctors")
    public String doctors(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String office,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order,
            Model model) {

        List<Doctor> doctors = doctorService.getAll();

        if (id != null && id > 0) {
            Doctor d = doctorService.getByIdIfExists(id);
            doctors = d != null ? List.of(d) : List.of();
        } else {
            doctors = doctors.stream().filter(d -> {
                if (fullName != null && !fullName.isBlank()) {
                    String name = d.getUser().getFullName();
                    if (!name.toLowerCase().contains(fullName.toLowerCase())) return false;
                }
                if (specialization != null && !specialization.isBlank()) {
                    if (!d.getSpecialization().toLowerCase().contains(specialization.toLowerCase())) return false;
                }
                if (office != null && !office.isBlank()) {
                    if (d.getOffice() == null || !d.getOffice().toLowerCase().contains(office.toLowerCase())) return false;
                }
                return true;
            }).toList();
        }

        Comparator<Doctor> comparator = switch (sort) {
            case "id" -> Comparator.comparing(Doctor::getId);
            case "fullName" -> Comparator.comparing(d -> d.getUser().getFullName(), String.CASE_INSENSITIVE_ORDER);
            case "email" -> Comparator.comparing(d -> d.getUser().getEmail(), String.CASE_INSENSITIVE_ORDER);
            case "specialization" -> Comparator.comparing(Doctor::getSpecialization, String.CASE_INSENSITIVE_ORDER);
            case "office" -> Comparator.comparing(d -> d.getOffice() != null ? d.getOffice() : "", String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparing(Doctor::getId);
        };

        if ("desc".equals(order)) {
            comparator = comparator.reversed();
        }

        doctors = doctors.stream().sorted(comparator).toList();

        model.addAttribute("doctors", doctors);
        model.addAttribute("searchId", id);
        model.addAttribute("searchFullName", fullName);
        model.addAttribute("searchSpecialization", specialization);
        model.addAttribute("searchOffice", office);
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);
        return "registrar/doctors";
    }

    @GetMapping("/doctors/add")
    public String addDoctorForm(Model model) {
        model.addAttribute("doctor", new Doctor());
        model.addAttribute("users", userService.getAllUsers().stream()
                .filter(u -> u.getRole() == Role.DOCTOR && doctorService.getByUser(u) == null)
                .toList());
        return "registrar/add_doctor";
    }

    @PostMapping("/doctors/add")
    public String addDoctor(@ModelAttribute("doctor") Doctor doctor, Model model) {
        try {
            doctorService.create(doctor);
            return "redirect:/registrar/doctors";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("users", userService.getAllUsers().stream()
                    .filter(u -> u.getRole() == Role.DOCTOR && doctorService.getByUser(u) == null)
                    .toList());
            return "registrar/add_doctor";
        }
    }

    @GetMapping("/schedule")
    public String schedule(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String doctorName,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) Long patientId,
            @RequestParam(defaultValue = "date") String sort,
            @RequestParam(defaultValue = "asc") String order,
            Model model) {

        if (!"asc".equals(order) && !"desc".equals(order)) {
            order = "asc";
        }

        List<Schedule> schedules = scheduleService.getAllWithAppointments();
        schedules = filterSchedules(schedules, id, doctorName, specialization, date, time, patientId);
        schedules = sortSchedules(schedules, sort, order);

        model.addAttribute("schedules", schedules);
        model.addAttribute("doctors", doctorService.getAll());
        model.addAttribute("scheduleDto", new ScheduleDto());
        model.addAttribute("searchId", id);
        model.addAttribute("searchDoctorName", doctorName);
        model.addAttribute("searchSpecialization", specialization);
        model.addAttribute("searchDate", date);
        model.addAttribute("searchTime", time);
        model.addAttribute("searchPatientId", patientId);
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);
        return "registrar/schedule";
    }

    private List<Schedule> filterSchedules(List<Schedule> schedules, Long id, String doctorName,
                                           String specialization, String date, String time, Long patientId) {
        return schedules.stream().filter(s -> {
            if (id != null && !s.getId().equals(id)) return false;
            if (doctorName != null && !doctorName.isBlank()) {
                String fullName = s.getDoctor().getUser().getFullName();
                if (!fullName.toLowerCase().contains(doctorName.toLowerCase())) return false;
            }
            if (specialization != null && !specialization.isBlank()) {
                if (!s.getDoctor().getSpecialization().toLowerCase().contains(specialization.toLowerCase())) return false;
            }
            if (date != null && !date.isBlank()) {
                if (!s.getDate().toString().equals(date)) return false;
            }
            if (time != null && !time.isBlank()) {
                if (!s.getTime().toString().equals(time)) return false;
            }
            if (patientId != null && patientId > 0) {
                if (s.getAppointment() == null) return false;
                if (!s.getAppointment().getPatient().getId().equals(patientId)) return false;
            }
            return true;
        }).toList();
    }

    private List<Schedule> sortSchedules(List<Schedule> schedules, String sort, String order) {
        Comparator<Schedule> comparator = switch (sort) {
            case "id" -> Comparator.comparing(Schedule::getId);
            case "doctor" -> Comparator.comparing(s -> s.getDoctor().getUser().getFullName(), String.CASE_INSENSITIVE_ORDER);
            case "specialization" -> Comparator.comparing(s -> s.getDoctor().getSpecialization(), String.CASE_INSENSITIVE_ORDER);
            case "date" -> Comparator.comparing(Schedule::getDate);
            case "time" -> Comparator.comparing(Schedule::getTime);
            case "status" -> Comparator.comparing(Schedule::isAvailable);
            case "patientId" -> Comparator.comparing(
                    s -> s.getAppointment() != null ? s.getAppointment().getPatient().getId() : 0L
            );
            default -> Comparator.comparing(Schedule::getDate);
        };

        if ("desc".equals(order)) {
            comparator = comparator.reversed();
        }
        return schedules.stream().sorted(comparator).toList();
    }

    @PostMapping("/schedule/create")
    public String createSchedule(@ModelAttribute("scheduleDto") ScheduleDto dto, Model model) {
        try {
            scheduleService.createFromDto(dto);
            return "redirect:/registrar/schedule";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("schedules", scheduleService.getAll());
            model.addAttribute("doctors", doctorService.getAll());
            return "registrar/schedule";
        }
    }

    @GetMapping("/schedule/edit/{id}")
    public String editScheduleForm(@PathVariable("id") Long id, Model model) {
        Schedule schedule = scheduleService.getById(id);
        ScheduleDto dto = new ScheduleDto();
        dto.setId(schedule.getId());
        dto.setDoctorId(schedule.getDoctor().getId());
        dto.setDate(schedule.getDate().toString());
        dto.setTime(schedule.getTime().toString());
        model.addAttribute("scheduleDto", dto);
        model.addAttribute("doctors", doctorService.getAll());
        model.addAttribute("schedules", scheduleService.getAll());
        return "registrar/schedule_edit";
    }

    @PostMapping("/schedule/update")
    public String updateSchedule(@ModelAttribute("scheduleDto") ScheduleDto dto, Model model) {
        try {
            scheduleService.updateFromDto(dto);
            return "redirect:/registrar/schedule";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("doctors", doctorService.getAll());
            model.addAttribute("schedules", scheduleService.getAll());
            return "registrar/schedule_edit";
        }
    }

    @GetMapping("/schedule/delete/{id}")
    public String deleteSchedule(@PathVariable("id") Long id) {
        try {
            scheduleService.delete(id);
        } catch (Exception ex) {
        }
        return "redirect:/registrar/schedule";
    }

    @GetMapping("/appointments/book")
    public String bookAppointmentForm(Model model) {
        model.addAttribute("appointment", new AppointmentDto());
        model.addAttribute("patients", patientService.getAll());
        model.addAttribute("doctors", doctorService.getAll());
        model.addAttribute("schedules", scheduleService.getAllAvailable());
        return "registrar/create_appointment";
    }

    @PostMapping("/appointments/book")
    public String bookAppointment(@ModelAttribute("appointment") AppointmentDto dto, Model model) {
        try {
            appointmentService.create(dto);
            return "redirect:/registrar/appointments";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("patients", patientService.getAll());
            model.addAttribute("doctors", doctorService.getAll());
            model.addAttribute("schedules", scheduleService.getAllAvailable());
            model.addAttribute("appointment", dto);
            return "registrar/create_appointment";
        }
    }

    @GetMapping("/appointments")
    public String appointments(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String time,
            @RequestParam(defaultValue = "datetime") String sort,
            @RequestParam(defaultValue = "asc") String order,
            Model model) {

        List<Appointment> allAppointments = appointmentService.getAllActive();
        List<Appointment> filtered = allAppointments.stream().filter(a -> {
            if (patientId != null && !a.getPatient().getId().equals(patientId)) return false;
            if (patientName != null && !patientName.isBlank()) {
                String name = a.getPatient().getUser().getFullName();
                if (!name.toLowerCase().contains(patientName.toLowerCase())) return false;
            }
            if (phone != null && !phone.isBlank()) {
                String p = a.getPatient().getPhone();
                if (p == null || !p.contains(phone)) return false;
            }
            if (date != null && !date.isBlank()) {
                if (!a.getSchedule().getDate().toString().equals(date)) return false;
            }
            if (time != null && !time.isBlank()) {
                if (!a.getSchedule().getTime().toString().equals(time)) return false;
            }
            return true;
        }).toList();

        List<Appointment> sorted = sortAppointments(filtered, sort, order);
        model.addAttribute("appointments", sorted);
        model.addAttribute("patients", patientService.getAll());
        model.addAttribute("doctors", doctorService.getAll());
        model.addAttribute("schedules", scheduleService.getAllAvailable());
        model.addAttribute("searchPatientId", patientId);
        model.addAttribute("searchPatientName", patientName);
        model.addAttribute("searchPhone", phone);
        model.addAttribute("searchDate", date);
        model.addAttribute("searchTime", time);
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);
        return "registrar/appointments";
    }

    @GetMapping("/appointments/create")
    public String createAppointmentForm(Model model) {
        model.addAttribute("appointment", new AppointmentDto());
        model.addAttribute("patients", patientService.getAll());
        model.addAttribute("doctors", doctorService.getAll());
        model.addAttribute("schedules", scheduleService.getAllAvailable());
        return "registrar/create_appointment";
    }

    @PostMapping("/appointments/create")
    public String createAppointment(@ModelAttribute("appointment") AppointmentDto dto, Model model) {
        try {
            appointmentService.create(dto);
            return "redirect:/registrar/appointments";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("patients", patientService.getAll());
            model.addAttribute("doctors", doctorService.getAll());
            model.addAttribute("schedules", scheduleService.getAllAvailable());
            model.addAttribute("appointment", dto);
            return "registrar/create_appointment";
        }
    }

    @GetMapping("/appointments/delete/{id}")
    public String deleteAppointment(@PathVariable("id") Long id) {
        appointmentService.delete(id);
        return "redirect:/registrar/appointments";
    }

    private List<Appointment> sortAppointments(List<Appointment> appointments, String sort, String order) {
        Comparator<Appointment> comparator = switch (sort) {
            case "id" -> Comparator.comparing(a -> a.getPatient().getId());
            case "patient" -> Comparator.comparing(
                    (Appointment a) -> a.getPatient().getUser().getFullName(),
                    String.CASE_INSENSITIVE_ORDER
            );
            case "phone" -> Comparator.comparing(
                    a -> a.getPatient().getPhone() != null ? a.getPatient().getPhone() : "",
                    String.CASE_INSENSITIVE_ORDER
            );
            case "date" -> Comparator.comparing(a -> a.getSchedule().getDate());
            case "time" -> Comparator.comparing(a -> a.getSchedule().getTime());
            case "doctor" -> Comparator.comparing(
                    a -> a.getDoctor().getUser().getFullName(),
                    String.CASE_INSENSITIVE_ORDER
            );
            case "specialization" -> Comparator.comparing(
                    a -> a.getDoctor().getSpecialization(),
                    String.CASE_INSENSITIVE_ORDER
            );
            default -> Comparator.comparing((Appointment a) -> a.getSchedule().getDate())
                    .thenComparing(a -> a.getSchedule().getTime());
        };

        if ("desc".equals(order)) {
            comparator = comparator.reversed();
        }

        return appointments.stream().sorted(comparator).toList();
    }

    @GetMapping("/appointments/slots")
    @ResponseBody
    public List<Map<String, Object>> getSlotsByDoctor(@RequestParam Long doctorId) {
        List<Schedule> slots = scheduleService.getAvailableByDoctor(doctorId);
        return slots.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("date", s.getDate());
            map.put("time", s.getTime());

            if (s.getDoctor() != null) {
                Doctor d = s.getDoctor();
                Map<String, Object> doctorMap = new HashMap<>();
                doctorMap.put("id", d.getId());
                if (d.getUser() != null) {
                    doctorMap.put("fullName", d.getUser().getFullName());
                } else {
                    doctorMap.put("fullName", "—");
                }
                doctorMap.put("specialization", d.getSpecialization() != null ? d.getSpecialization() : "—");
                map.put("doctor", doctorMap);
            } else {
                map.put("doctor", Map.of("fullName", "—", "specialization", "—"));
            }
            return map;
        }).toList();
    }

    @GetMapping("/doctors/edit/{id}")
    public String editDoctorForm(@PathVariable("id") Long id, Model model) {
        Doctor doctor = doctorService.getById(id);
        model.addAttribute("doctor", doctor);
        model.addAttribute("users", userService.getAllUsers().stream()
                .filter(u -> u.getRole() == Role.DOCTOR)
                .toList());
        return "registrar/edit_doctor";
    }

    @PostMapping("/doctors/edit")
    public String editDoctor(@ModelAttribute("doctor") Doctor doctor, Model model) {
        try {
            doctorService.update(doctor);
            return "redirect:/registrar/doctors";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("users", userService.getAllUsers().stream()
                    .filter(u -> u.getRole() == Role.DOCTOR)
                    .toList());
            return "registrar/edit_doctor";
        }
    }

    @GetMapping("/doctors/delete/{id}")
    public String deleteDoctor(@PathVariable("id") Long id) {
        try {
            doctorService.delete(id);
        } catch (Exception ex) {
        }
        return "redirect:/registrar/doctors";
    }

    @GetMapping("/patients/edit/{id}")
    public String editPatientForm(@PathVariable("id") Long id, Model model) {
        Patient patient = patientService.getById(id);
        model.addAttribute("patient", patient);
        return "registrar/edit_patient";
    }

    @PostMapping("/patients/edit")
    public String editPatient(@ModelAttribute("patient") Patient patient, Model model) {
        try {
            patientService.update(patient);
            return "redirect:/registrar/patients";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            return "registrar/edit_patient";
        }
    }

    @GetMapping("/patients/delete/{id}")
    public String deletePatient(@PathVariable("id") Long id) {
        try {
            patientService.delete(id);
        } catch (Exception ex) {
        }
        return "redirect:/registrar/patients";
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
        return "registrar/statistics";
    }
}
