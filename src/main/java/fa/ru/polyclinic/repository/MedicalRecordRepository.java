package fa.ru.polyclinic.repository;

import fa.ru.polyclinic.model.MedicalRecord;
import fa.ru.polyclinic.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByPatient(Patient patient);
    List<MedicalRecord> findByPatientId(Long patientId);
    void deleteByPatientId(Long patientId);
    void deleteByDoctorId(Long doctorId);
}
