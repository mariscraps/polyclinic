package fa.ru.polyclinic.dto;

public class AppointmentDto {
    private Long patientId;
    private Long doctorId;
    private Long scheduleId;

    public AppointmentDto() {
    }
    public AppointmentDto(Long patientId, Long doctorId, Long scheduleId) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.scheduleId = scheduleId;
    }
    public Long getPatientId() {
        return patientId;
    }
    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }
    public Long getDoctorId() {
        return doctorId;
    }
    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }
    public Long getScheduleId() {
        return scheduleId;
    }
    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }
}
