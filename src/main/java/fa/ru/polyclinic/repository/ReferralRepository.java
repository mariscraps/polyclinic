package fa.ru.polyclinic.repository;

import fa.ru.polyclinic.model.Referral;
import fa.ru.polyclinic.model.Patient;
import fa.ru.polyclinic.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReferralRepository extends JpaRepository<Referral, Long> {
    List<Referral> findByPatientAndCompletedFalse(Patient patient);
    List<Referral> findByDoctorAndCompletedFalse(Doctor doctor);
    List<Referral> findByPatientIdAndCompletedFalse(Long patientId);
    List<Referral> findByPatientIdAndDoctorIdAndCompletedFalse(Long patientId, Long doctorId);
    List<Referral> findByPatientId(Long patientId);

}
