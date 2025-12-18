package fa.ru.polyclinic.service.impl;

import fa.ru.polyclinic.dto.ScheduleDto;
import fa.ru.polyclinic.model.Appointment;
import fa.ru.polyclinic.model.Doctor;
import fa.ru.polyclinic.model.Schedule;
import fa.ru.polyclinic.repository.AppointmentRepository;
import fa.ru.polyclinic.repository.DoctorRepository;
import fa.ru.polyclinic.repository.ScheduleRepository;
import fa.ru.polyclinic.service.ScheduleService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** реализация сервиса расписаний */
@Service
public class ScheduleServiceImpl implements ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    public ScheduleServiceImpl(ScheduleRepository scheduleRepository, DoctorRepository doctorRepository,
                               AppointmentRepository appointmentRepository) {
        this.scheduleRepository = scheduleRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public Schedule create(Schedule schedule) {
        if (schedule == null) throw new IllegalArgumentException("Schedule is null");
        if (schedule.getDoctor() == null) throw new IllegalArgumentException("Schedule.doctor is required");
        return scheduleRepository.save(schedule);
    }

    @Override
    public List<Schedule> getAll() {
        return scheduleRepository.findAllWithAppointments();
    }

    @Override
    public List<Schedule> getAllAvailable() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        return scheduleRepository.findByAvailableTrue().stream()
                .filter(s -> {
                    if (s.getDate().isAfter(today)) return true;
                    if (s.getDate().isEqual(today)) return s.getTime().isAfter(now);
                    return false;
                })
                .toList();
    }

    @Override
    @Transactional
    public void createFromDto(ScheduleDto dto) {
        if (dto == null) throw new IllegalArgumentException("ScheduleDto is null");
        if (dto.getDoctorId() == null) throw new IllegalArgumentException("doctorId is required");
        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        LocalDate date;
        LocalTime time;
        try {
            date = LocalDate.parse(dto.getDate());
            time = LocalTime.parse(dto.getTime());
        } catch (Exception ex) {
            throw new RuntimeException("Invalid date/time format. Required date yyyy-MM-dd and time HH:mm", ex);
        }

        if (scheduleRepository.existsByDoctorIdAndDateAndTimeAndAvailableTrue(
                doctor.getId(), date, time)) {
            throw new RuntimeException("Слот на " + dto.getDate() + " " + dto.getTime() +
                    " для врача " + doctor.getUser().getFullName() + " уже существует");
        }

        Schedule s = new Schedule();
        s.setDoctor(doctor);
        s.setDate(date);
        s.setTime(time);
        s.setAvailable(true);
        scheduleRepository.save(s);
    }


    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) throw new IllegalArgumentException("Id is null");
        List<Appointment> appointments = appointmentRepository.findByScheduleId(id);
        for (Appointment a : appointments) {
            appointmentRepository.deleteById(a.getId());
        }
        scheduleRepository.deleteById(id);
    }

    @Override
    public List<Schedule> findByDoctorIdAndAvailableTrue(Long doctorId) {
        return scheduleRepository.findByDoctorIdAndAvailableTrue(doctorId);
    }

    @Override
    public Schedule getById(Long id) {
        if (id == null) throw new IllegalArgumentException("ID is null");
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
    }

    @Override
    public void updateFromDto(ScheduleDto dto) {
        if (dto == null || dto.getId() == null) {
            throw new IllegalArgumentException("ID is required for update");
        }
        Schedule existing = getById(dto.getId());
        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        LocalDate date = LocalDate.parse(dto.getDate());
        LocalTime time = LocalTime.parse(dto.getTime());

        existing.setDoctor(doctor);
        existing.setDate(date);
        existing.setTime(time);
        scheduleRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteByDoctor(Long doctorId) {
        scheduleRepository.deleteByDoctorId(doctorId);
    }

    @Override
    public List<Schedule> getAllWithAppointments() {
        return scheduleRepository.findAllWithAppointments();
    }

    @Override
    public List<Schedule> getAvailableByDoctor(Long doctorId) {
        if (doctorId == null) {
            return List.of();
        }
        return scheduleRepository.findByDoctorIdAndAvailableTrue(doctorId);
    }
}
