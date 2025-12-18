package fa.ru.polyclinic.config;

import fa.ru.polyclinic.model.Role;
import fa.ru.polyclinic.model.User;
import fa.ru.polyclinic.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


/** здесь создается перечень тестовый пользователей при старте приложения */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        createIfNotExists("admin@clinic.com", "admin123", Role.ADMIN);
        createIfNotExists("registrar@clinic.com", "registrar123", Role.REGISTRAR);
        createIfNotExists("doctor@clinic.com", "doctor123", Role.DOCTOR);
            createIfNotExists("patient@clinic.com", "patient123", Role.PATIENT);
    }

    private void createIfNotExists(String email, String rawPassword, Role role) {
        if (email == null || email.isBlank()) return;
        if (userRepository.findByEmail(email).isEmpty()) {
            User u = new User();
            u.setEmail(email.toLowerCase().trim());
            u.setPassword(passwordEncoder.encode(rawPassword));
            u.setRole(role);
            u.setFullName(role.name().substring(0,1) + role.name().substring(1).toLowerCase() + " User");
            userRepository.save(u);
            System.out.println("Created default user: " + email + " with role " + role);
        }
    }
}
