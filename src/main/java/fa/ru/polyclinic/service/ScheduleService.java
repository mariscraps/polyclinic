package fa.ru.polyclinic.service;

import fa.ru.polyclinic.dto.ScheduleDto;
import fa.ru.polyclinic.model.Schedule;
import java.util.List;

/** сервис для управления расписанием врачей */
public interface ScheduleService {
    Schedule create(Schedule schedule);
    List<Schedule> getAll();
    List<Schedule> getAllAvailable();
    void createFromDto(ScheduleDto dto);
    void delete(Long id);

    List<Schedule> findByDoctorIdAndAvailableTrue(Long doctorId);

    Schedule getById(Long id);
    void updateFromDto(ScheduleDto dto);
    void deleteByDoctor(Long doctorId);
    List<Schedule> getAllWithAppointments();
    List<Schedule> getAvailableByDoctor(Long doctorId);

}
