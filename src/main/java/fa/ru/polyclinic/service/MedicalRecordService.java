package fa.ru.polyclinic.service;

import fa.ru.polyclinic.dto.MedicalRecordDto;
import fa.ru.polyclinic.model.MedicalRecord;
import java.util.List;

public interface MedicalRecordService {
    MedicalRecord createRecord(MedicalRecordDto dto);
    List<MedicalRecord> getAll();
    List<MedicalRecord> getByPatient(Long patientId);
    void delete(Long id);
}


