package fa.ru.polyclinic.service.impl;

import fa.ru.polyclinic.dto.LabResultDto;
import fa.ru.polyclinic.model.LabResult;
import fa.ru.polyclinic.model.Patient;
import fa.ru.polyclinic.model.Doctor;
import fa.ru.polyclinic.repository.LabResultRepository;
import fa.ru.polyclinic.repository.PatientRepository;
import fa.ru.polyclinic.repository.DoctorRepository;
import fa.ru.polyclinic.service.LabResultService;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** реализация сервиса результатов анализов */
@Service
public class LabResultServiceImpl implements LabResultService {
    private final LabResultRepository labResultRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public LabResultServiceImpl(LabResultRepository labResultRepository,
                                PatientRepository patientRepository,
                                DoctorRepository doctorRepository) {
        this.labResultRepository = labResultRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @Override
    public LabResult createResult(LabResultDto dto) {
        if (dto == null) throw new IllegalArgumentException("LabResultDto is null");
        if (dto.getPatientId() == null) throw new IllegalArgumentException("patientId required");
        if (dto.getDoctorId() == null) throw new IllegalArgumentException("doctorId required");

        Patient patient = patientRepository.findById(dto.getPatientId()).orElseThrow(() -> new RuntimeException("Patient not found"));
        Doctor doctor = doctorRepository.findById(dto.getDoctorId()).orElseThrow(() -> new RuntimeException("Doctor not found"));
        LabResult lr = new LabResult();
        lr.setPatient(patient);
        lr.setDoctor(doctor);
        lr.setTestType(dto.getTestType());
        lr.setResult(dto.getResult());
        lr.setOffice(dto.getOffice());

        if (dto.getDate() != null && !dto.getDate().isBlank()) {
            lr.setDate(LocalDate.parse(dto.getDate()));
        } else {
            lr.setDate(LocalDate.now());
        }

        if (dto.getTime() != null && !dto.getTime().isBlank()) {
            lr.setTime(LocalTime.parse(dto.getTime()));
        } else {
            lr.setTime(LocalTime.now());
        }
        return labResultRepository.save(lr);
    }

    @Override
    public List<LabResult> getAll() {
        return labResultRepository.findAll();
    }

    @Override
    public List<LabResult> getByPatient(Long patientId) {
        if (patientId == null) return List.of();
        return labResultRepository.findByPatientId(patientId);
    }

    @Override
    public List<LabResult> searchByType(String type) {
        if (type == null) return List.of();
        return labResultRepository.findByTestTypeContainingIgnoreCase(type.trim());
    }

    @Override
    public void delete(Long id) {
        if (id == null) throw new IllegalArgumentException("Id is null");
        if (!labResultRepository.existsById(id)) throw new RuntimeException("Lab result not found");
        labResultRepository.deleteById(id);
    }

    @Override
    public LabResult getById(Long id) {
        return labResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Result not found"));
    }

    @Override
    public void update(LabResult lab) {
        if (lab == null || lab.getId() == null) {
            throw new IllegalArgumentException("ID required for update");
        }
        labResultRepository.save(lab);
    }
}
