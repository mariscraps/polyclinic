package fa.ru.polyclinic.service;

import java.util.List;

public interface StatisticsService {
    long getTotalUsers();
    long getTotalDoctors();
    long getTotalPatients();
    long getTotalAppointments();
    long getCompletedAppointments();
    long getActiveScheduleSlots();

    String getAverageWaitingTimeDaysHours();
    List<String> getAppointmentLabels();
    List<Long> getAppointmentData();
    List<String> getSpecializationLabels();
    List<Long> getSpecializationData();
}
