package fa.ru.polyclinic.dto;

public class MedicalRecordDto {
    private Long patientId;
    private Long doctorId;
    private String complaints;
    private String examination;
    private String diagnosis;
    private String recommendations;

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getComplaints() { return complaints; }
    public void setComplaints(String complaints) { this.complaints = complaints; }

    public String getExamination() { return examination; }
    public void setExamination(String examination) { this.examination = examination; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }
}