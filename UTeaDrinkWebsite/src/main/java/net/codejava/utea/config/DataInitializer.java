package net.codejava.utea.config;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.Role;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.repository.RoleRepository;
import net.codejava.utea.common.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // 1) Tạo sẵn các ROLE nếu chưa có
        ensureRole("ADMIN",   "Quản trị");
        ensureRole("MANAGER", "Quản lý");
        ensureRole("CUSTOMER","Khách hàng");
        ensureRole("SHIPPER", "Tài xế");

        // 2) Seed user mẫu (đăng nhập bằng email, mật khẩu 123456)
        ensureUserWithRole(
                "admin@utea.local", "admin",
                "Admin Seed", "123456", "ACTIVE",
                "ADMIN"
        );

        ensureUserWithRole(
                "manager@utea.local", "manager",
                "Manager Seed", "123456", "ACTIVE",
                "MANAGER"
        );

        ensureUserWithRole(
                "customer@utea.local", "customer",
                "Customer Seed", "123456", "ACTIVE",
                "CUSTOMER"
        );

        ensureUserWithRole(
                "shipper@utea.local", "shipper",
                "Shipper Seed", "123456", "ACTIVE",
                "SHIPPER"
        );
    }

    private Role ensureRole(String code, String name) {
        return roleRepo.findByCode(code)
                .orElseGet(() -> roleRepo.save(Role.builder()
                        .code(code)
                        .build()));
    }

    private void ensureUserWithRole(String email,
                                    String username,
                                    String fullName,
                                    String rawPassword,
                                    String status,
                                    String roleCode) {
        Role role = ensureRole(roleCode, roleCode);

        userRepo.findByEmail(email).ifPresentOrElse(u -> {
            // Đã có user -> đảm bảo có role
            if (u.getRoles() == null || !u.getRoles().contains(role)) {
                u.getRoles().add(role);
                userRepo.save(u);
            }
        }, () -> {
            // Chưa có -> tạo mới
            User u = User.builder()
                    .email(email)
                    .username(username)
                    .fullName(fullName)
                    .passwordHash(passwordEncoder.encode(rawPassword))
                    .status(status)
                    .roles(Set.of(role))
                    .build();
            userRepo.save(u);
        });
    }
}
