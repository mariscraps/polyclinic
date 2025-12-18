package fa.ru.polyclinic.repository;

import fa.ru.polyclinic.model.Schedule;
import fa.ru.polyclinic.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByDoctor(Doctor doctor);
    List<Schedule> findByDoctorId(Long doctorId);
    List<Schedule> findByDate(LocalDate date);
    List<Schedule> findByAvailableTrue();
    List<Schedule> findByDoctorIdAndAvailableTrue(Long doctorId);
    void deleteByDoctorId(Long doctorId);

    @Query("SELECT s FROM Schedule s " +
            "LEFT JOIN FETCH s.appointment a " +
            "LEFT JOIN FETCH a.patient " +
            "LEFT JOIN FETCH s.doctor d " +
            "LEFT JOIN FETCH d.user")
    List<Schedule> findAllWithAppointments();

    boolean existsByDoctorIdAndDateAndTimeAndAvailableTrue(Long doctorId, LocalDate date, LocalTime time);


}
