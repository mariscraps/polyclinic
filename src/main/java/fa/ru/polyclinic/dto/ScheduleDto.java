package fa.ru.polyclinic.dto;

public class ScheduleDto {
    private Long id;
    private Long doctorId;
    private String date;
    private String time;

    public ScheduleDto() {
    }

    public ScheduleDto(Long id, Long doctorId, String date, String time) {
        this.id = id;
        this.doctorId = doctorId;
        this.date = date;
        this.time = time;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getDoctorId() {
        return doctorId;
    }
    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
}
