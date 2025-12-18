package fa.ru.polyclinic.service.impl;

import fa.ru.polyclinic.model.Doctor;
import fa.ru.polyclinic.model.User;
import fa.ru.polyclinic.repository.DoctorRepository;
import fa.ru.polyclinic.repository.UserRepository;
import fa.ru.polyclinic.service.AppointmentService;
import fa.ru.polyclinic.service.DoctorService;
import fa.ru.polyclinic.service.ScheduleService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/** реализация сервиса врачей */
@Service
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final AppointmentService appointmentService;
    private final ScheduleService scheduleService;

    public DoctorServiceImpl(DoctorRepository doctorRepository, UserRepository userRepository,
                             AppointmentService appointmentService,
                             ScheduleService scheduleService) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.appointmentService = appointmentService;
        this.scheduleService = scheduleService;
    }

    @Override
    public Doctor create(Doctor doctor) {
        if (doctor == null) throw new IllegalArgumentException("Doctor is null");
        if (doctor.getUser() == null) throw new IllegalArgumentException("Doctor.user is required");
        Optional<Doctor> existing = doctorRepository.findByUser(doctor.getUser());
        if (existing.isPresent()) {
            throw new RuntimeException("Doctor profile already exists for this user");
        }
        return doctorRepository.save(doctor);
    }

    @Override
    public Doctor getById(Long id) {
        if (id == null) throw new IllegalArgumentException("Id is null");
        return doctorRepository.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found"));
    }

    @Override
    public Doctor getByUser(User user) {
        if (user == null) return null;
        return doctorRepository.findByUser(user).orElse(null);
    }

    @Override
    public List<Doctor> getAll() {
        return doctorRepository.findAll();
    }

    @Override
    public List<Doctor> searchBySpecialization(String specialization) {
        if (specialization == null) return List.of();
        return doctorRepository.findBySpecializationContainingIgnoreCase(specialization.trim());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) throw new IllegalArgumentException("Id is null");
        Doctor doctor = getById(id);

        appointmentService.deleteByDoctor(id);
        scheduleService.deleteByDoctor(id);
        doctorRepository.deleteById(id);

        if (doctor.getUser() != null) {
            userRepository.deleteById(doctor.getUser().getId());
        }
    }

    @Override
    @Transactional
    public void update(Doctor doctor) {
        if (doctor == null || doctor.getId() == null) {
            throw new IllegalArgumentException("ID required for update");
        }
        Doctor existing = getById(doctor.getId());
        existing.setSpecialization(doctor.getSpecialization());
        existing.setOffice(doctor.getOffice());
        if (doctor.getUser() != null) {
            existing.getUser().setFullName(doctor.getUser().getFullName());
            userRepository.save(existing.getUser());
        }
        doctorRepository.save(existing);
    }

    @Override
    public Doctor getByIdIfExists(Long id) {
        if (id == null) return null;
        return doctorRepository.findById(id).orElse(null);
    }
}
