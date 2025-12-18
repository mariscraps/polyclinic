package fa.ru.polyclinic.service.impl;

import fa.ru.polyclinic.model.Patient;
import fa.ru.polyclinic.model.User;
import fa.ru.polyclinic.repository.PatientRepository;
import fa.ru.polyclinic.repository.UserRepository;
import fa.ru.polyclinic.service.PatientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    public PatientServiceImpl(PatientRepository patientRepository, UserRepository userRepository) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Patient create(Patient patient) {
        if (patient == null || patient.getUser() == null) {
            throw new IllegalArgumentException("Patient and user are required");
        }
        if (patientRepository.findByUser(patient.getUser()).isPresent()) {
            throw new RuntimeException("Patient already exists for this user");
        }
        return patientRepository.save(patient);
    }

    @Override
    @Transactional
    public Patient createOrUpdate(Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("Patient cannot be null");
        }
        if (patient.getUser() == null) {
            throw new IllegalArgumentException("Patient.user cannot be null");
        }
        if (patient.getUser().getId() == null) {
            throw new IllegalArgumentException("Patient.user must be an existing user (id required)");
        }

        User existingUser = userRepository.findById(patient.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        existingUser.setFullName(patient.getUser().getFullName());
        userRepository.save(existingUser);

        Optional<Patient> existingProfile = patientRepository.findByUser(existingUser);

        if (patient.getId() == null) {
            if (existingProfile.isPresent()) {
                throw new RuntimeException("Patient profile already exists for this user");
            }
            patient.setUser(existingUser);
            return patientRepository.save(patient);
        } else {
            Patient p = existingProfile
                    .filter(pat -> pat.getId().equals(patient.getId()))
                    .orElseThrow(() -> new RuntimeException("Patient profile not found or does not belong to user"));

            p.setBirthDate(patient.getBirthDate());
            p.setPassport(patient.getPassport());
            p.setInsurancePolicy(patient.getInsurancePolicy());
            p.setPhone(patient.getPhone());
            return patientRepository.save(p);
        }
    }

    @Override
    public Patient getById(Long id) {
        if (id == null) throw new IllegalArgumentException("Id is null");
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    @Override
    public Patient getByUser(User user) {
        if (user == null) return null;
        return patientRepository.findByUser(user).orElse(null);
    }

    @Override
    public List<Patient> getAll() {
        return patientRepository.findAll();
    }

    @Override
    public List<Patient> searchByName(String fullName) {
        if (fullName == null || fullName.isBlank()) return List.of();
        return patientRepository.findByUserFullNameContainingIgnoreCase(fullName.trim());
    }

    @Override
    public void delete(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new RuntimeException("Patient not found");
        }
        patientRepository.deleteById(id);
    }

    @Override
    public List<Patient> search(String fullName, String birthDateStr,
                                String passport, String insurancePolicy, String phone) {
        List<Patient> results = patientRepository.findAll();

        if (fullName != null && !fullName.trim().isEmpty()) {
            String lowerName = fullName.toLowerCase().trim();
            results = results.stream()
                    .filter(p -> p.getUser() != null &&
                            p.getUser().getFullName().toLowerCase().contains(lowerName))
                    .toList();
        }

        if (birthDateStr != null && !birthDateStr.trim().isEmpty()) {
            LocalDate date = LocalDate.parse(birthDateStr);
            results = results.stream()
                    .filter(p -> p.getBirthDate() != null && p.getBirthDate().equals(date))
                    .toList();
        }

        if (passport != null && !passport.trim().isEmpty()) {
            results = results.stream()
                    .filter(p -> passport.equals(p.getPassport()))
                    .toList();
        }

        if (insurancePolicy != null && !insurancePolicy.trim().isEmpty()) {
            results = results.stream()
                    .filter(p -> insurancePolicy.equals(p.getInsurancePolicy()))
                    .toList();
        }

        if (phone != null && !phone.trim().isEmpty()) {
            results = results.stream()
                    .filter(p -> phone.equals(p.getPhone()))
                    .toList();
        }

        return results;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    @Override
    public Patient getByIdIfExists(Long id) {
        if (id == null) return null;
        return patientRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void update(Patient patient) {
        if (patient == null || patient.getId() == null) {
            throw new IllegalArgumentException("ID пациента обязателен для обновления");
        }
        User user = patient.getUser();
        if (user != null) {
            User existingUser = userRepository.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            existingUser.setFullName(user.getFullName());
            userRepository.save(existingUser);
        }
        patientRepository.save(patient);
    }
}