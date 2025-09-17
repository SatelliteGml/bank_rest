package com.example.bankcards;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class UserPasswordTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testAdminPasswordMatchesHash() {
        // Берём пользователя admin из базы
        User admin = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        // Plain password, который мы ожидаем
        String rawPassword = "admin123";

        // Проверяем соответствие пароля хешу из базы
        boolean matches = passwordEncoder.matches(rawPassword, admin.getPassword());

        assertTrue(matches, "Password for admin does not match hash in database");
    }

    @Test
    void testUser1PasswordMatchesHash() {
        User user1 = userRepository.findByUsername("user1")
                .orElseThrow(() -> new RuntimeException("User1 not found"));

        String rawPassword = "user123";

        boolean matches = passwordEncoder.matches(rawPassword, user1.getPassword());

        assertTrue(matches, "Password for user1 does not match hash in database");
    }
}
