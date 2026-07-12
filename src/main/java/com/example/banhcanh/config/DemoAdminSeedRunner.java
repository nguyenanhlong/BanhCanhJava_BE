package com.example.banhcanh.config;

import com.example.banhcanh.model.User;
import com.example.banhcanh.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Ensures the demo privileged accounts (admin / super_admin) can actually log in through the
 * real backend and receive a JWT.
 *
 * <p>The seeded rows in database.sql ship with PLACEHOLDER password hashes like
 * "$2b$10$example_hash_admin" — these look like BCrypt (so PasswordMigrationRunner leaves them
 * alone) but are not valid hashes of any password, so {@code passwordEncoder.matches(...)} always
 * fails and nobody can log in as admin. Without a working admin JWT, every admin-only endpoint
 * (assign-driver, product CRUD, …) returns 403.
 *
 * <p>This runner repairs those accounts (and creates them if missing) with real BCrypt hashes of
 * their documented demo passwords. It is idempotent and non-destructive: once an account holds a
 * real hash (no longer contains the "example_hash" marker), it is never touched again — so an
 * admin can safely change their password later without it being reset on the next restart.
 */
@Component
@Order(20) // after PasswordMigrationRunner
public class DemoAdminSeedRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoAdminSeedRunner.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoAdminSeedRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private record DemoAccount(String username, String rawPassword, String role, String email, String fullName) {}

    private static boolean isPlaceholderOrMissing(String password) {
        return password == null || password.isBlank() || password.contains("example_hash");
    }

    @Override
    public void run(String... args) {
        DemoAccount[] accounts = {
            new DemoAccount("superadmin", "superadmin", "super_admin", "superadmin@banhcanh.com", "Super Admin"),
            new DemoAccount("admin", "admin", "admin", "admin@banhcanh.com", "Quản lý"),
        };

        for (DemoAccount acc : accounts) {
            User user = userRepository.findByUsername(acc.username()).orElse(null);
            if (user == null) {
                if (userRepository.existsByEmail(acc.email())) {
                    // Email already taken by a different account — skip to avoid a unique-constraint clash.
                    continue;
                }
                user = new User();
                user.setUsername(acc.username());
                user.setEmail(acc.email());
                user.setFullName(acc.fullName());
                user.setRole(acc.role());
                user.setIsActive(true);
                user.setPassword(passwordEncoder.encode(acc.rawPassword()));
                userRepository.save(user);
                log.info("Đã tạo tài khoản demo '{}' (role={}).", acc.username(), acc.role());
            } else if (isPlaceholderOrMissing(user.getPassword())) {
                user.setPassword(passwordEncoder.encode(acc.rawPassword()));
                // Đảm bảo role đúng nếu row seed bị lệch.
                if (user.getRole() == null || !user.getRole().equals(acc.role())) {
                    user.setRole(acc.role());
                }
                user.setIsActive(true);
                userRepository.save(user);
                log.info("Đã sửa mật khẩu placeholder cho tài khoản demo '{}' sang BCrypt.", acc.username());
            }
        }
    }
}
