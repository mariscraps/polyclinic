package fa.ru.polyclinic.service;

import fa.ru.polyclinic.model.Doctor;
import fa.ru.polyclinic.model.User;

import java.util.List;

/** сервис для управления врачами */
public interface DoctorService {
    Doctor create(Doctor doctor);
    Doctor getById(Long id);
    Doctor getByUser(User user);
    List<Doctor> getAll();
    List<Doctor> searchBySpecialization(String specialization);
    void delete(Long id);
    void update(Doctor doctor);
    Doctor getByIdIfExists(Long id);
}
