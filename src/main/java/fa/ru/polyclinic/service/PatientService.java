package fa.ru.polyclinic.service;

import fa.ru.polyclinic.model.Patient;
import fa.ru.polyclinic.model.User;
import java.time.LocalDate;
import java.util.List;

public interface PatientService {

    Patient create(Patient patient);
    Patient createOrUpdate(Patient patient);
    Patient getById(Long id);
    Patient getByUser(User user);
    List<Patient> getAll();
    List<Patient> searchByName(String fullName);
    void delete(Long id);

    List<Patient> search(String fullName, String birthDate, String passport,
                         String insurancePolicy, String phone);

    Patient getByIdIfExists(Long id);
    void update(Patient patient);
}