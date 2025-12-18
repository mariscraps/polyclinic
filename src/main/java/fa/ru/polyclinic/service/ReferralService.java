package fa.ru.polyclinic.service;

import fa.ru.polyclinic.model.Referral;
import java.util.List;

public interface ReferralService {
    Referral createReferral(Referral referral);
    List<Referral> getActiveReferralsForPatient(Long patientId);
    List<Referral> getActiveReferralsForDoctor(Long doctorId);
    Referral completeReferral(Long referralId, String result);
    List<Referral> getActiveReferralsForPatientAndDoctor(Long patientId, Long doctorId);
    List<Referral> getAllReferralsForPatient(Long patientId);
    List<Referral> getCompletedFalseReferralsForPatient(Long patientId);
    void deleteByDoctor(Long referralId, Long doctorId);
}