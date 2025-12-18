package fa.ru.polyclinic.repository;

import fa.ru.polyclinic.model.Patient;
import fa.ru.polyclinic.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUser(User user);
    List<Patient> findByUserFullNameContainingIgnoreCase(String name);
    List<Patient> findByPassport(String passport);
    List<Patient> findByInsurancePolicy(String insurancePolicy);
    List<Patient> findByPhone(String phone);
    List<Patient> findByBirthDate(LocalDate birthDate);
}