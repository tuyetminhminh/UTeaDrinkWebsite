package net.codejava.utea.ai.repository;

import net.codejava.utea.ai.entity.AIChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AIChatMessageRepository extends JpaRepository<AIChatMessage, Long> {

    /**
     * Lấy N tin nhắn cuối cùng của session
     */
    @Query("""
        SELECT m FROM AIChatMessage m
        WHERE m.session.id = :sessionId
        ORDER BY m.createdAt DESC
    """)
    List<AIChatMessage> findLatestBySessionId(@Param("sessionId") Long sessionId, org.springframework.data.domain.Pageable pageable);

    /**
     * Lấy tất cả tin nhắn của session
     */
    List<AIChatMessage> findBySession_IdOrderByCreatedAtAsc(Long sessionId);
}

