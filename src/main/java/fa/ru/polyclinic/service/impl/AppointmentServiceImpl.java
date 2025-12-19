package fa.ru.polyclinic.service.impl;

import fa.ru.polyclinic.dto.AppointmentDto;
import fa.ru.polyclinic.model.*;
import fa.ru.polyclinic.repository.*;
import fa.ru.polyclinic.service.AppointmentService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/** реализация сервиса записи на прием */
@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ScheduleRepository scheduleRepository;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                  PatientRepository patientRepository,
                                  DoctorRepository doctorRepository,
                                  ScheduleRepository scheduleRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.scheduleRepository = scheduleRepository;
    }

    @Override
    public Appointment create(AppointmentDto dto) {
        if (dto == null) throw new IllegalArgumentException("AppointmentDto is null");
        if (dto.getPatientId() == null) throw new IllegalArgumentException("patientId required");
        if (dto.getDoctorId() == null) throw new IllegalArgumentException("doctorId required");
        if (dto.getScheduleId() == null) throw new IllegalArgumentException("scheduleId required");

        Patient patient = patientRepository.findById(dto.getPatientId()).orElseThrow(() -> new RuntimeException("Patient not found"));
        Doctor doctor = doctorRepository.findById(dto.getDoctorId()).orElseThrow(() -> new RuntimeException("Doctor not found"));
        Schedule schedule = scheduleRepository.findById(dto.getScheduleId()).orElseThrow(() -> new RuntimeException("Schedule slot not found"));

        String newSpecialization = doctor.getSpecialization();
        List<Appointment> activeAppointments = appointmentRepository.findByPatientIdAndCompletedFalse(dto.getPatientId());
        boolean alreadyHasActiveAppointment = activeAppointments.stream()
                .anyMatch(a -> a.getDoctor().getSpecialization().equals(newSpecialization));

        if (alreadyHasActiveAppointment) {
            throw new RuntimeException("Вы уже записаны к врачу специальности '" + newSpecialization +
                    "'. Отмените существующую запись, чтобы записаться к другому врачу этой же специальности.");
        }

        if (!schedule.isAvailable()) {
            throw new RuntimeException("Selected schedule slot is not available");
        }
        if (!schedule.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException("Schedule slot does not belong to selected doctor");
        }

        Appointment a = new Appointment();
        a.setPatient(patient);
        a.setDoctor(doctor);
        a.setSchedule(schedule);
        a.setCreatedAt(LocalDateTime.now());

        Appointment saved = appointmentRepository.save(a);
        schedule.setAvailable(false);
        scheduleRepository.save(schedule);
        return saved;
    }

    @Override
    public List<Appointment> getAll() {
        return appointmentRepository.findAll();
    }

    @Override
    public List<Appointment> getByPatient(Long patientId) {
        if (patientId == null) return List.of();
        return appointmentRepository.findByPatientId(patientId);
    }

    @Override
    public List<Appointment> getByDoctor(Long doctorId) {
        if (doctorId == null) return List.of();
        return appointmentRepository.findByDoctorId(doctorId);
    }

    @Override
    public void delete(Long id) {
        if (id == null) throw new IllegalArgumentException("Id is null");
        Appointment a = appointmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Appointment not found"));
        Schedule s = a.getSchedule();
        if (s != null) {
            s.setAvailable(true);
            scheduleRepository.save(s);
        }
        appointmentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByDoctor(Long doctorId) {
        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctorId);
        for (Appointment a : appointments) {
            if (a.getSchedule() != null) {
                a.getSchedule().setAvailable(true);
                scheduleRepository.save(a.getSchedule());
            }
        }
        appointmentRepository.deleteByDoctorId(doctorId);
    }

    @Override
    public Appointment getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID записи не может быть null");
        }
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Запись не найдена с ID: " + id));
    }

    @Override
    public List<Appointment> getActiveAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findByPatientIdAndCompletedFalse(patientId);
    }

    @Override
    public List<Appointment> getCompletedAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findByPatientIdAndCompletedTrue(patientId);
    }

    @Override
    public List<Appointment> getActiveAppointmentsByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorIdAndCompletedFalse(doctorId);
    }

    @Override
    public List<Appointment> getCompletedAppointmentsByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorIdAndCompletedTrue(doctorId);
    }

    @Override
    @Transactional
    public void completeAppointment(Long id) {
        Appointment a = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Запись не найдена"));

        a.setCompleted(true);
        appointmentRepository.save(a);
    }

    @Override
    public List<Appointment> getAllActive() {
        return appointmentRepository.findByCompletedFalse();
    }
}
