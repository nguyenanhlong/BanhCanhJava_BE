package com.example.banhcanh.config;

import com.example.banhcanh.model.Role;
import com.example.banhcanh.model.User;
import com.example.banhcanh.model.UserRole;
import com.example.banhcanh.repository.RoleRepository;
import com.example.banhcanh.repository.UserRepository;
import com.example.banhcanh.repository.UserRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * On every startup, sync each user's {@code User.role} String column with the highest-privilege
 * role found in the {@code user_roles} RBAC table. This fixes the mismatch where the RBAC table
 * has ADMIN/SUPER_ADMIN but the {@code users.role} column still says "customer" (the default).
 *
 * <p>Priority: super_admin > admin > driver > customer.
 */
@Component
@Order(30) // after DemoAdminSeedRunner
public class UserRoleSyncRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(UserRoleSyncRunner.class);

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    public UserRoleSyncRunner(UserRepository userRepository,
                              UserRoleRepository userRoleRepository,
                              RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        List<User> allUsers = userRepository.findAll();
        int synced = 0;

        for (User user : allUsers) {
            List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
            String highestRole = resolveHighestRole(userRoles);

            if (highestRole != null && !highestRole.equals(user.getRole())) {
                String oldRole = user.getRole();
                user.setRole(highestRole);
                user.setUpdatedAt(java.time.LocalDateTime.now());
                userRepository.save(user);
                synced++;
                log.info("Synced role for user '{}': '{}' -> '{}' (from user_roles table)",
                        user.getUsername(), oldRole, highestRole);
            }
        }

        if (synced > 0) {
            log.info("UserRoleSync: updated {} user(s) role from user_roles table.", synced);
        } else {
            log.debug("UserRoleSync: all user roles already in sync.");
        }
    }

    private String resolveHighestRole(List<UserRole> userRoles) {
        String highest = null;
        for (UserRole ur : userRoles) {
            Optional<Role> roleOpt = roleRepository.findById(ur.getRoleId());
            if (roleOpt.isPresent() && Boolean.TRUE.equals(roleOpt.get().getIsActive())) {
                String name = roleOpt.get().getName().toUpperCase();
                if (name.startsWith("ROLE_")) name = name.substring(5);
                if ("SUPER_ADMIN".equals(name)) return "super_admin";
                if ("ADMIN".equals(name) && !"super_admin".equals(highest)) highest = "admin";
                else if ("DRIVER".equals(name) && highest == null) highest = "driver";
            }
        }
        return highest;
    }
}
