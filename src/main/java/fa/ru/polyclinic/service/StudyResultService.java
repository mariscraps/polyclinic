package fa.ru.polyclinic.service;

import fa.ru.polyclinic.dto.StudyResultDto;
import fa.ru.polyclinic.model.StudyResult;
import java.util.List;

public interface StudyResultService {
    StudyResult createResult(StudyResultDto dto);
    List<StudyResult> getAll();
    List<StudyResult> getByPatient(Long patientId);
    List<StudyResult> searchByType(String type);
    void delete(Long id);
    void updateResult(Long id, String result);
    StudyResult getById(Long id); // ← новое
}

