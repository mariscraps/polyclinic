package fa.ru.polyclinic.repository;

import fa.ru.polyclinic.model.LabResult;
import fa.ru.polyclinic.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LabResultRepository extends JpaRepository<LabResult, Long> {
    List<LabResult> findByPatient(Patient patient);
    List<LabResult> findByPatientId(Long patientId);
    List<LabResult> findByTestTypeContainingIgnoreCase(String testType);
    void deleteByPatientId(Long patientId);
    void deleteByDoctorId(Long doctorId);
}
