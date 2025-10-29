package net.codejava.utea.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.repository.UserRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service để migrate password từ plaintext sang BCrypt.
 * Chỉ chạy 1 lần khi chuyển từ NoOpPasswordEncoder sang BCryptPasswordEncoder.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordMigrationService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    /**
     * ⚠️ CẢNH BÁO: Phương thức này chỉ dùng để migrate password MỘT LẦN!
     * 
     * Kịch bản sử dụng:
     * 1. Trước đây: PasswordConfig dùng NoOpPasswordEncoder → password lưu plaintext
     * 2. Bây giờ: Chuyển sang BCryptPasswordEncoder
     * 3. Cần: Mã hóa lại tất cả password cũ trong DB
     * 
     * SAU KHI CHẠY XONG, HÃY:
     * - Comment lại annotation @EventListener bên dưới
     * - Hoặc xóa file này
     */
    // @EventListener(ApplicationReadyEvent.class) // ⬅️ COMMENT LẠI SAU KHI CHẠY!
    @Transactional
    public void migratePasswords() {
        log.warn("========================================");
        log.warn("⚠️  PASSWORD MIGRATION STARTING...");
        log.warn("========================================");

        List<User> allUsers = userRepo.findAll();
        int migratedCount = 0;
        int skippedCount = 0;

        for (User user : allUsers) {
            String currentHash = user.getPasswordHash();
            
            // Kiểm tra xem password đã được mã hóa bằng BCrypt chưa
            // BCrypt hash luôn bắt đầu với "$2a$", "$2b$", hoặc "$2y$"
            if (currentHash != null && currentHash.startsWith("$2")) {
                log.debug("User {} đã có BCrypt hash, bỏ qua", user.getEmail());
                skippedCount++;
                continue;
            }

            // Nếu password là plaintext hoặc NoOp, mã hóa lại
            if (currentHash != null && !currentHash.isBlank()) {
                String bcryptHash = passwordEncoder.encode(currentHash);
                user.setPasswordHash(bcryptHash);
                userRepo.save(user);
                
                log.info("✅ Migrated password for user: {} (ID: {})", user.getEmail(), user.getId());
                migratedCount++;
            } else {
                log.warn("⚠️  User {} has null/empty password, skipped", user.getEmail());
                skippedCount++;
            }
        }

        log.warn("========================================");
        log.warn("🎉 PASSWORD MIGRATION COMPLETED!");
        log.warn("📊 Total users: {}", allUsers.size());
        log.warn("✅ Migrated: {}", migratedCount);
        log.warn("⏭️  Skipped: {}", skippedCount);
        log.warn("========================================");
        log.error("🚨 QUAN TRỌNG: Comment lại @EventListener trong PasswordMigrationService.java!");
        log.warn("========================================");
    }

    /**
     * Phương thức kiểm tra xem một password có phải BCrypt hash không
     */
    public boolean isBCryptHash(String password) {
        if (password == null || password.length() < 10) {
            return false;
        }
        return password.startsWith("$2a$") || 
               password.startsWith("$2b$") || 
               password.startsWith("$2y$");
    }
}

