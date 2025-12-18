package fa.ru.polyclinic.repository;

import fa.ru.polyclinic.model.Doctor;
import fa.ru.polyclinic.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUser(User user);
    List<Doctor> findBySpecializationContainingIgnoreCase(String specialization);
    List<Doctor> findByUserFullNameContainingIgnoreCase(String name);
}

