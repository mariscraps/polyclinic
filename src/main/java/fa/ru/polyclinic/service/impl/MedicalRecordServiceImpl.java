package fa.ru.polyclinic.service.impl;

import fa.ru.polyclinic.dto.MedicalRecordDto;
import fa.ru.polyclinic.model.MedicalRecord;
import fa.ru.polyclinic.model.Patient;
import fa.ru.polyclinic.model.Doctor;
import fa.ru.polyclinic.repository.MedicalRecordRepository;
import fa.ru.polyclinic.repository.PatientRepository;
import fa.ru.polyclinic.repository.DoctorRepository;
import fa.ru.polyclinic.service.MedicalRecordService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MedicalRecordServiceImpl implements MedicalRecordService {
    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public MedicalRecordServiceImpl(MedicalRecordRepository medicalRecordRepository,
                                    PatientRepository patientRepository,
                                    DoctorRepository doctorRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @Override
    public MedicalRecord createRecord(MedicalRecordDto dto) {
        if (dto == null) throw new IllegalArgumentException("MedicalRecordDto is null");
        if (dto.getPatientId() == null) throw new IllegalArgumentException("patientId required");
        if (dto.getDoctorId() == null) throw new IllegalArgumentException("doctorId required");

        Patient patient = patientRepository.findById(dto.getPatientId()).orElseThrow(() -> new RuntimeException("Patient not found"));
        Doctor doctor = doctorRepository.findById(dto.getDoctorId()).orElseThrow(() -> new RuntimeException("Doctor not found"));
        MedicalRecord rec = new MedicalRecord();
        rec.setPatient(patient);
        rec.setDoctor(doctor);
        rec.setComplaints(dto.getComplaints());
        rec.setExamination(dto.getExamination());
        rec.setDiagnosis(dto.getDiagnosis());
        rec.setRecommendations(dto.getRecommendations());
        return medicalRecordRepository.save(rec);
    }

    @Override
    public List<MedicalRecord> getAll() {
        return medicalRecordRepository.findAll();
    }

    @Override
    public List<MedicalRecord> getByPatient(Long patientId) {
        if (patientId == null) return List.of();
        return medicalRecordRepository.findByPatientId(patientId);
    }

    @Override
    public void delete(Long id) {
        if (id == null) throw new IllegalArgumentException("Id is null");
        if (!medicalRecordRepository.existsById(id)) throw new RuntimeException("Medical record not found");
        medicalRecordRepository.deleteById(id);
    }
}
