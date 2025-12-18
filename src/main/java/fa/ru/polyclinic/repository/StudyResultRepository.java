package fa.ru.polyclinic.repository;

import fa.ru.polyclinic.model.StudyResult;
import fa.ru.polyclinic.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudyResultRepository extends JpaRepository<StudyResult, Long> {
    List<StudyResult> findByPatient(Patient patient);
    List<StudyResult> findByPatientId(Long patientId);
    List<StudyResult> findByTestTypeContainingIgnoreCase(String testType);
    void deleteByPatientId(Long patientId);
    void deleteByDoctorId(Long doctorId);
}
