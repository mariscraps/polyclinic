package fa.ru.polyclinic.dto;

import java.time.LocalDate;

public class PatientRegistrationDto {
    private String fullName;
    private String email;
    private LocalDate birthDate;
    private String passport;
    private String insurancePolicy;
    private String phone;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getPassport() { return passport; }
    public void setPassport(String passport) { this.passport = passport; }

    public String getInsurancePolicy() { return insurancePolicy; }
    public void setInsurancePolicy(String insurancePolicy) { this.insurancePolicy = insurancePolicy; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}