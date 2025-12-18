package fa.ru.polyclinic.service;

import fa.ru.polyclinic.dto.RegistrationDto;
import fa.ru.polyclinic.model.Role;
import fa.ru.polyclinic.model.User;

import java.util.List;

/** сервис для работы с пользователями (регистрация, получение, смена роли, удаление) */
public interface UserService {
    User register(RegistrationDto dto);
    User findByEmail(String email);
    List<User> getAllUsers();
    void updateUserRole(Long userId, Role role);
    void deleteUser(Long userId);
    void changePassword(String email, String newPassword);
}

