package fa.ru.polyclinic.service;

import fa.ru.polyclinic.dto.LabResultDto;
import fa.ru.polyclinic.model.LabResult;
import java.util.List;

public interface LabResultService {
    LabResult createResult(LabResultDto dto);
    List<LabResult> getAll();
    List<LabResult> getByPatient(Long patientId);
    List<LabResult> searchByType(String type);
    void delete(Long id);
    LabResult getById(Long id);
    void update(LabResult lab);
}

