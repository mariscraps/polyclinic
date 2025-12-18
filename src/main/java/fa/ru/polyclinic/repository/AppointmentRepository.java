package fa.ru.polyclinic.repository;

import fa.ru.polyclinic.model.Appointment;
import fa.ru.polyclinic.model.Doctor;
import fa.ru.polyclinic.model.Patient;
import fa.ru.polyclinic.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatient(Patient patient);
    List<Appointment> findByDoctor(Doctor doctor);
    List<Appointment> findBySchedule(Schedule schedule);
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorId(Long doctorId);
    void deleteByDoctorId(Long doctorId);

    List<Appointment> findByPatientIdAndCompletedFalse(Long patientId);
    List<Appointment> findByPatientIdAndCompletedTrue(Long patientId);
    List<Appointment> findByDoctorIdAndCompletedFalse(Long doctorId);
    List<Appointment> findByDoctorIdAndCompletedTrue(Long doctorId);
    List<Appointment> findByCompletedFalse();
    void deleteByPatientId(Long patientId);
    List<Appointment> findByScheduleId(Long scheduleId);

}
