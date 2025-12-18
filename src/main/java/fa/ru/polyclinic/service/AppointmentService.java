package fa.ru.polyclinic.service;

import fa.ru.polyclinic.dto.AppointmentDto;
import fa.ru.polyclinic.model.Appointment;

import java.util.List;

/** сервис для работы с записями на прием */
public interface AppointmentService {

    Appointment create(AppointmentDto dto);
    List<Appointment> getAll();
    List<Appointment> getByPatient(Long patientId);
    List<Appointment> getByDoctor(Long doctorId);
    void delete(Long id);
    void deleteByDoctor(Long doctorId);
    Appointment getById(Long id);

    List<Appointment> getActiveAppointmentsByPatient(Long patientId);
    List<Appointment> getCompletedAppointmentsByPatient(Long patientId);

    List<Appointment> getActiveAppointmentsByDoctor(Long doctorId);
    List<Appointment> getCompletedAppointmentsByDoctor(Long doctorId);

    void completeAppointment(Long id);
    List<Appointment> getAllActive();
}
