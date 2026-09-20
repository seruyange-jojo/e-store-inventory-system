package com.joseph.ismes.service;

import com.joseph.ismes.entity.Role;
import com.joseph.ismes.entity.User;
import com.joseph.ismes.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class AdminBootstrap implements ApplicationRunner {

    private static final String LEGACY_USERNAME = "admin";
    private static final String LEGACY_PASSWORD = "admin123";
    // Exact V2 seed fingerprint: never use a password match to identify an untouched account.
    private static final String LEGACY_HASH = "$2b$10$MqJnsBdG3MAxvAy6zKHKs.PlGltRJaZbdRjIF7UOHIaovr5XFdeFi";
    private static final String ADMIN_NAME = "System Administrator";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String defaultUsername;
    private final String defaultPassword;

    public AdminBootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          @Value("${app.admin.default-username}") String defaultUsername,
                          @Value("${app.admin.default-password}") String defaultPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.defaultUsername = defaultUsername;
        this.defaultPassword = defaultPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (defaultUsername == null || defaultUsername.isBlank() || defaultUsername.length() > 50
                || defaultPassword == null || defaultPassword.isBlank()) {
            throw new IllegalArgumentException("Admin defaults require a username of 1–50 characters and a nonblank password");
        }

        if (userRepository.count() == 0) {
            userRepository.save(User.builder()
                    .username(defaultUsername)
                    .passwordHash(passwordEncoder.encode(defaultPassword))
                    .fullName(ADMIN_NAME)
                    .role(Role.ADMIN)
                    .active(true)
                    .build());
            log.info("Created initial administrator account");
            return;
        }

        if (LEGACY_USERNAME.equals(defaultUsername) && LEGACY_PASSWORD.equals(defaultPassword)) {
            return;
        }

        User legacyAdmin = userRepository.findByUsername(LEGACY_USERNAME).orElse(null);
        if (!isUntouchedLegacyAdmin(legacyAdmin)) {
            return;
        }

        if (!LEGACY_USERNAME.equals(defaultUsername) && userRepository.existsByUsername(defaultUsername)) {
            log.warn("Admin defaults were not applied: the configured username already exists. Existing accounts were preserved");
            return;
        }

        legacyAdmin.setUsername(defaultUsername);
        legacyAdmin.setPasswordHash(passwordEncoder.encode(defaultPassword));
        userRepository.save(legacyAdmin);
        log.info("Applied configured administrator defaults to the untouched legacy seed account");
    }

    private boolean isUntouchedLegacyAdmin(User user) {
        return user != null
                && LEGACY_USERNAME.equals(user.getUsername())
                && LEGACY_HASH.equals(user.getPasswordHash())
                && ADMIN_NAME.equals(user.getFullName())
                && user.getRole() == Role.ADMIN
                && user.isActive()
                && user.getEmail() == null
                && user.getPhone() == null
                && user.getCreatedAt() != null
                && user.getCreatedAt().equals(user.getUpdatedAt());
    }
}
