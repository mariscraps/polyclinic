package fa.ru.polyclinic.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotNull(message = "Дата рождения обязательна")
    @Column(name = "birth_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @NotBlank(message = "Паспортные данные обязательны")
    @Column(name = "passport")
    private String passport;

    @NotBlank(message = "Страховой полис обязателен")
    @Column(name = "insurance_policy")
    private String insurancePolicy;

    @NotBlank(message = "Телефон обязателен")
    @Column(name = "phone")
    private String phone;

    public Patient() {}
    public Patient(Long id, User user, LocalDate birthDate,
                   String passport, String insurancePolicy, String phone) {
        this.id = id;
        this.user = user;
        this.birthDate = birthDate;
        this.passport = passport;
        this.insurancePolicy = insurancePolicy;
        this.phone = phone;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public LocalDate getBirthDate() {
        return birthDate;
    }
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
    public String getPassport() {
        return passport;
    }
    public void setPassport(String passport) {
        this.passport = passport;
    }
    public String getInsurancePolicy() {
        return insurancePolicy;
    }
    public void setInsurancePolicy(String insurancePolicy) {
        this.insurancePolicy = insurancePolicy;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
}
