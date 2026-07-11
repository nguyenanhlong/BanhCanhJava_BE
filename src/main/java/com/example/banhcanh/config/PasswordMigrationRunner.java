package com.example.banhcanh.config;

import com.example.banhcanh.model.User;
import com.example.banhcanh.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * One-time startup migration: any account still holding a plain-text password (i.e. not
 * already a BCrypt hash) gets it hashed in place. Safe to run on every startup — accounts
 * already migrated are left untouched (their password already starts with a BCrypt prefix).
 */
@Component
public class PasswordMigrationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PasswordMigrationRunner.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordMigrationRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private static boolean isBcryptHash(String password) {
        return password != null && (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$"));
    }

    @Override
    public void run(String... args) {
        List<User> users = userRepository.findAll();
        int migrated = 0;
        for (User user : users) {
            if (!isBcryptHash(user.getPassword())) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                userRepository.save(user);
                migrated++;
            }
        }
        if (migrated > 0) {
            log.info("Đã mã hoá {} mật khẩu tài khoản còn ở dạng plain-text sang BCrypt.", migrated);
        }
    }
}
