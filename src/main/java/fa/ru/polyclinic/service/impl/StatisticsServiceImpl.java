package fa.ru.polyclinic.service.impl;

import fa.ru.polyclinic.model.Appointment;
import fa.ru.polyclinic.repository.*;
import fa.ru.polyclinic.service.StatisticsService;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final ScheduleRepository scheduleRepository;

    public StatisticsServiceImpl(UserRepository userRepository,
                                 DoctorRepository doctorRepository,
                                 PatientRepository patientRepository,
                                 AppointmentRepository appointmentRepository,
                                 ScheduleRepository scheduleRepository) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.scheduleRepository = scheduleRepository;
    }

    @Override
    public long getTotalUsers() {
        return userRepository.count();
    }

    @Override
    public long getTotalDoctors() {
        return doctorRepository.count();
    }

    @Override
    public long getTotalPatients() {
        return patientRepository.count();
    }

    @Override
    public long getTotalAppointments() {
        return appointmentRepository.count();
    }

    @Override
    public long getCompletedAppointments() {
        return appointmentRepository.findAll().stream()
                .filter(Appointment::isCompleted)
                .count();
    }

    @Override
    public long getActiveScheduleSlots() {
        return scheduleRepository.findByAvailableTrue().size();
    }

    @Override
    public String getAverageWaitingTimeDaysHours() {
        List<Appointment> completed = appointmentRepository.findAll().stream()
                .filter(a -> a.isCompleted() && a.getCreatedAt() != null)
                .collect(Collectors.toList());

        if (completed.isEmpty()) {
            return "0 дней 0 часов";
        }

        long totalMinutes = 0;
        for (Appointment a : completed) {
            LocalDateTime created = a.getCreatedAt();
            LocalDateTime scheduled = a.getSchedule().getDate().atTime(a.getSchedule().getTime());
            Duration duration = Duration.between(created, scheduled);
            totalMinutes += Math.abs(duration.toMinutes());
        }

        long avgMinutes = totalMinutes / completed.size();
        long days = avgMinutes / (24 * 60);
        long hours = (avgMinutes % (24 * 60)) / 60;
        return days + " " + getDayWord(days) + " " + hours + " " + getHourWord(hours);
    }

    private String getDayWord(long days) {
        if (days % 10 == 1 && days % 100 != 11) return "день";
        if (days % 10 >= 2 && days % 10 <= 4 && (days % 100 < 10 || days % 100 >= 20)) return "дня";
        return "дней";
    }

    private String getHourWord(long hours) {
        if (hours % 10 == 1 && hours % 100 != 11) return "час";
        if (hours % 10 >= 2 && hours % 10 <= 4 && (hours % 100 < 10 || hours % 100 >= 20)) return "часа";
        return "часов";
    }

    @Override
    public List<String> getAppointmentLabels() {
        List<Appointment> active = appointmentRepository.findByCompletedFalse();
        return active.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getDoctor().getUser().getFullName() + " (" + a.getDoctor().getSpecialization() + ")",
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(Map.Entry::getKey)
                .toList();
    }

    @Override
    public List<Long> getAppointmentData() {
        List<Appointment> active = appointmentRepository.findByCompletedFalse();
        return active.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getDoctor().getUser().getFullName() + " (" + a.getDoctor().getSpecialization() + ")",
                        Collectors.counting()
                ))
                .values().stream()
                .toList();
    }

    @Override
    public List<String> getSpecializationLabels() {
        List<Appointment> active = appointmentRepository.findByCompletedFalse();
        return active.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getDoctor().getSpecialization(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(Map.Entry::getKey)
                .toList();
    }

    @Override
    public List<Long> getSpecializationData() {
        List<Appointment> active = appointmentRepository.findByCompletedFalse();
        return active.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getDoctor().getSpecialization(),
                        Collectors.counting()
                ))
                .values().stream()
                .toList();
    }
}
