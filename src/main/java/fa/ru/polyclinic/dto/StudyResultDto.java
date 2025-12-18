package fa.ru.polyclinic.dto;

public class StudyResultDto {
    private Long patientId;
    private Long doctorId;
    private String testType;
    private String result;
    private String date;
    private String time;
    private String office;

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getTestType() { return testType; }
    public void setTestType(String testType) { this.testType = testType; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getOffice() { return office; }
    public void setOffice(String office) { this.office = office; }
}