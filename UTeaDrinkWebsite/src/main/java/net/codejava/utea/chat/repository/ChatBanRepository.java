package net.codejava.utea.chat.repository;

import net.codejava.utea.chat.entity.ChatBan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ChatBanRepository extends JpaRepository<ChatBan, Long> {
    // Tìm một lệnh cấm đang hoạt động cho user
    Optional<ChatBan> findByUserIdAndBannedUntilAfter(Long userId, LocalDateTime now);

    Optional<ChatBan> findByUserId(Long userId);
}