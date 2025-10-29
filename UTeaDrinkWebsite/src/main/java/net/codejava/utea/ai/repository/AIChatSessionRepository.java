package net.codejava.utea.ai.repository;

import net.codejava.utea.ai.entity.AIChatSession;
import net.codejava.utea.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AIChatSessionRepository extends JpaRepository<AIChatSession, Long> {

    /**
     * Tìm session active của user
     */
    Optional<AIChatSession> findFirstByUserAndIsActiveTrueOrderByUpdatedAtDesc(User user);

    /**
     * Tìm session active của guest
     */
    Optional<AIChatSession> findFirstByGuestSessionIdAndIsActiveTrueOrderByUpdatedAtDesc(String guestSessionId);

    /**
     * Lấy tất cả sessions của user
     */
    List<AIChatSession> findByUserOrderByUpdatedAtDesc(User user);

    /**
     * Đếm số tin nhắn trong session
     */
    @Query("SELECT COUNT(m) FROM AIChatMessage m WHERE m.session.id = :sessionId")
    long countMessagesBySessionId(@Param("sessionId") Long sessionId);
}

