package fa.ru.polyclinic.service.impl;

import fa.ru.polyclinic.dto.StudyResultDto;
import fa.ru.polyclinic.model.StudyResult;
import fa.ru.polyclinic.model.Patient;
import fa.ru.polyclinic.model.Doctor;
import fa.ru.polyclinic.repository.StudyResultRepository;
import fa.ru.polyclinic.repository.PatientRepository;
import fa.ru.polyclinic.repository.DoctorRepository;
import fa.ru.polyclinic.service.StudyResultService;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class StudyResultServiceImpl implements StudyResultService {
    private final StudyResultRepository studyResultRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public StudyResultServiceImpl(StudyResultRepository studyResultRepository,
                                  PatientRepository patientRepository,
                                  DoctorRepository doctorRepository) {
        this.studyResultRepository = studyResultRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @Override
    public StudyResult createResult(StudyResultDto dto) {
        if (dto == null) throw new IllegalArgumentException("StudyResultDto is null");
        if (dto.getPatientId() == null) throw new IllegalArgumentException("patientId required");
        if (dto.getDoctorId() == null) throw new IllegalArgumentException("doctorId required");

        Patient patient = patientRepository.findById(dto.getPatientId()).orElseThrow(() -> new RuntimeException("Patient not found"));
        Doctor doctor = doctorRepository.findById(dto.getDoctorId()).orElseThrow(() -> new RuntimeException("Doctor not found"));

        StudyResult sr = new StudyResult();
        sr.setPatient(patient);
        sr.setDoctor(doctor);
        sr.setTestType(dto.getTestType());
        sr.setResult(dto.getResult());
        sr.setOffice(dto.getOffice());

        if (dto.getDate() != null && !dto.getDate().isBlank()) {
            sr.setDate(LocalDate.parse(dto.getDate()));
        } else {
            sr.setDate(LocalDate.now());
        }

        if (dto.getTime() != null && !dto.getTime().isBlank()) {
            sr.setTime(LocalTime.parse(dto.getTime()));
        } else {
            sr.setTime(LocalTime.now());
        }
        return studyResultRepository.save(sr);
    }

    @Override
    public List<StudyResult> getAll() {
        return studyResultRepository.findAll();
    }

    @Override
    public List<StudyResult> getByPatient(Long patientId) {
        if (patientId == null) return List.of();
        return studyResultRepository.findByPatientId(patientId);
    }

    @Override
    public List<StudyResult> searchByType(String type) {
        if (type == null) return List.of();
        return studyResultRepository.findByTestTypeContainingIgnoreCase(type.trim());
    }

    @Override
    public void delete(Long id) {
        if (id == null) throw new IllegalArgumentException("Id is null");
        if (!studyResultRepository.existsById(id)) throw new RuntimeException("Study result not found");
        studyResultRepository.deleteById(id);
    }

    @Override
    public void updateResult(Long id, String result) {
        StudyResult study = studyResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Исследование не найдено"));
        study.setResult(result);
        studyResultRepository.save(study);
    }

    @Override
    public StudyResult getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }
        return studyResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Исследование не найдено"));
    }
}
