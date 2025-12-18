package fa.ru.polyclinic.service.impl;

import fa.ru.polyclinic.model.*;
import fa.ru.polyclinic.repository.*;
import fa.ru.polyclinic.service.ReferralService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReferralServiceImpl implements ReferralService {
    private final ReferralRepository referralRepository;
    private final LabResultRepository labResultRepository;
    private final StudyResultRepository studyResultRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public ReferralServiceImpl(ReferralRepository referralRepository,
                               LabResultRepository labResultRepository,
                               StudyResultRepository studyResultRepository,
                               PatientRepository patientRepository,
                               DoctorRepository doctorRepository) {
        this.referralRepository = referralRepository;
        this.labResultRepository = labResultRepository;
        this.studyResultRepository = studyResultRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @Override
    public Referral createReferral(Referral referral) {
        return referralRepository.save(referral);
    }

    @Override
    public List<Referral> getActiveReferralsForPatient(Long patientId) {
        return referralRepository.findByPatientIdAndCompletedFalse(patientId);
    }

    @Override
    public List<Referral> getActiveReferralsForDoctor(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow();
        return referralRepository.findByDoctorAndCompletedFalse(doctor);
    }

    @Override
    public Referral completeReferral(Long referralId, String result) {
        Referral r = referralRepository.findById(referralId).orElseThrow();
        r.setCompleted(true);
        referralRepository.save(r);

        if ("ANALYSIS".equals(r.getType())) {
            LabResult lr = new LabResult();
            lr.setPatient(r.getPatient());
            lr.setDoctor(r.getDoctor());
            lr.setTestType(r.getTestType());
            lr.setResult(result);
            lr.setDate(r.getDate());
            lr.setTime(r.getTime());
            lr.setOffice(r.getOffice());
            labResultRepository.save(lr);
        } else if ("STUDY".equals(r.getType())) {
            StudyResult sr = new StudyResult();
            sr.setPatient(r.getPatient());
            sr.setDoctor(r.getDoctor());
            sr.setTestType(r.getTestType());
            sr.setResult(result);
            sr.setDate(r.getDate());
            sr.setTime(r.getTime());
            sr.setOffice(r.getOffice());
            studyResultRepository.save(sr);
        }
        return r;
    }

    @Override
    public List<Referral> getActiveReferralsForPatientAndDoctor(Long patientId, Long doctorId) {
        return referralRepository.findByPatientIdAndDoctorIdAndCompletedFalse(patientId, doctorId);
    }

    @Override
    public List<Referral> getAllReferralsForPatient(Long patientId) {
        return referralRepository.findByPatientId(patientId);
    }

    @Override
    public List<Referral> getCompletedFalseReferralsForPatient(Long patientId) {
        return referralRepository.findByPatientIdAndCompletedFalse(patientId);
    }

    @Override
    @Transactional
    public void deleteByDoctor(Long referralId, Long doctorId) {
        Referral r = referralRepository.findById(referralId)
                .orElseThrow(() -> new RuntimeException("Направление не найдено"));

        if (!r.getDoctor().getId().equals(doctorId)) {
            throw new RuntimeException("Нельзя удалить чужое направление");
        }

        referralRepository.deleteById(referralId);
    }
}