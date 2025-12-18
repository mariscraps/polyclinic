package fa.ru.polyclinic.service.impl;

import fa.ru.polyclinic.dto.RegistrationDto;
import fa.ru.polyclinic.model.*;
import fa.ru.polyclinic.repository.*;
import fa.ru.polyclinic.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/** реализация UserService*/
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final LabResultRepository labResultRepository;
    private final StudyResultRepository studyResultRepository;
    private final DoctorRepository doctorRepository;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           PatientRepository patientRepository,
                           AppointmentRepository appointmentRepository,
                           ScheduleRepository scheduleRepository,
                           MedicalRecordRepository medicalRecordRepository,
                           LabResultRepository labResultRepository,
                           StudyResultRepository studyResultRepository,
                           DoctorRepository doctorRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.scheduleRepository = scheduleRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.labResultRepository = labResultRepository;
        this.studyResultRepository = studyResultRepository;
        this.doctorRepository = doctorRepository;
    }

    @Override
    public User register(RegistrationDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Registration data is null");
        }
        String email = dto.getEmail();
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }
        User u = new User();
        u.setEmail(email.trim().toLowerCase());
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        u.setPassword(passwordEncoder.encode(dto.getPassword().trim()));
        u.setFullName(dto.getFullName() == null ? "New User" : dto.getFullName().trim());
        Role role = dto.getRole();
        if (role == null) {
            role = Role.PATIENT;
        }
        u.setRole(role);
        return userRepository.save(u);
    }

    @Override
    public User findByEmail(String email) {
        if (email == null) return null;
        Optional<User> opt = userRepository.findByEmail(email.trim().toLowerCase());
        return opt.orElse(null);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void updateUserRole(Long userId, Role role) {
        if (userId == null || role == null) throw new IllegalArgumentException("User id and role are required");
        User u = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        u.setRole(role);
        userRepository.save(u);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (userId == null) throw new IllegalArgumentException("User id is required");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role role = user.getRole();
        if (role == Role.PATIENT) {
            deletePatientData(userId);
        } else if (role == Role.DOCTOR) {
            deleteDoctorData(userId);
        }

        userRepository.deleteById(userId);
    }

    private void deletePatientData(Long userId) {
        Optional<Patient> patientOpt = patientRepository.findByUser(userRepository.getOne(userId));
        if (patientOpt.isEmpty()) return;
        Patient patient = patientOpt.get();
        List<Appointment> appointments = appointmentRepository.findByPatientId(patient.getId());
        for (Appointment a : appointments) {
            Schedule s = a.getSchedule();
            if (s != null) {
                s.setAvailable(true);
                scheduleRepository.save(s);
            }
            appointmentRepository.delete(a);
        }

        medicalRecordRepository.deleteByPatientId(patient.getId());
        labResultRepository.deleteByPatientId(patient.getId());
        studyResultRepository.deleteByPatientId(patient.getId()); // если есть
        patientRepository.deleteById(patient.getId());
    }

    private void deleteDoctorData(Long userId) {
        Optional<Doctor> doctorOpt = doctorRepository.findByUser(userRepository.getOne(userId));
        if (doctorOpt.isEmpty()) return;
        Doctor doctor = doctorOpt.get();
        appointmentRepository.deleteByDoctorId(doctor.getId());
        labResultRepository.deleteByDoctorId(doctor.getId());
        medicalRecordRepository.deleteByDoctorId(doctor.getId());
        scheduleRepository.deleteByDoctorId(doctor.getId());
        doctorRepository.deleteById(doctor.getId());
    }

    @Override
    public void changePassword(String email, String newPassword) {
        User user = findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setFirstLogin(false);
        userRepository.save(user);
    }
}
